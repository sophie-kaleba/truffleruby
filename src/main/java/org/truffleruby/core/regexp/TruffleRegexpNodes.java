/*
 * Copyright (c) 2017, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.IntValueProfile;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.AsTruffleStringNode;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.annotations.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.annotations.CoreModule;
import org.truffleruby.annotations.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.collections.ConcurrentOperations;
import org.truffleruby.core.array.ArrayBuilderNode;
import org.truffleruby.core.array.ArrayBuilderNode.BuilderState;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.library.HashStoreLibrary;
import org.truffleruby.core.kernel.KernelNodes.SameOrEqualNode;
import org.truffleruby.core.regexp.RegexpNodes.ToSNode;
import org.truffleruby.core.regexp.TruffleRegexpNodesFactory.MatchNodeGen;
import org.truffleruby.core.string.ATStringWithEncoding;
import org.truffleruby.core.string.TStringBuilder;
import org.truffleruby.core.string.TStringWithEncoding;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes.StringAppendPrimitiveNode;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.interop.TranslateInteropExceptionNode;
import org.truffleruby.interop.TranslateInteropExceptionNodeGen;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.WarnNode;
import org.truffleruby.language.control.DeferredRaiseException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.parser.RubyDeferredWarnings;

import static com.oracle.truffle.api.strings.TruffleString.CodeRange.ASCII;
import static com.oracle.truffle.api.strings.TruffleString.CodeRange.BROKEN;
import static com.oracle.truffle.api.strings.TruffleString.CodeRange.VALID;

@CoreModule("Truffle::RegexpOperations")
public class TruffleRegexpNodes {

    @TruffleBoundary
    private static void instrumentMatch(ConcurrentHashMap<MatchInfo, AtomicInteger> metricsMap, RubyRegexp regexp,
            Object string, boolean fromStart, boolean collectDetailedStats) {
        TruffleRegexpNodes.MatchInfo matchInfo = new TruffleRegexpNodes.MatchInfo(regexp, fromStart);
        ConcurrentOperations.getOrCompute(metricsMap, matchInfo, x -> new AtomicInteger()).incrementAndGet();

        if (collectDetailedStats) {
            final MatchInfoStats stats = ConcurrentOperations
                    .getOrCompute(MATCHED_REGEXP_STATS, matchInfo, x -> new MatchInfoStats());
            stats.record(new ATStringWithEncoding(RubyStringLibrary.getUncached(), string));
        }
    }

    // MRI: rb_reg_prepare_enc
    public abstract static class PrepareRegexpEncodingNode extends PrimitiveArrayArgumentsNode {

        @Child WarnNode warnNode;

        public static PrepareRegexpEncodingNode create() {
            return TruffleRegexpNodesFactory.PrepareRegexpEncodingNodeFactory.create(null);
        }

        public abstract RubyEncoding executePrepare(RubyRegexp regexp, Object matchString);

        @Specialization(guards = "stringLibrary.isRubyString(matchString)", limit = "1")
        protected RubyEncoding regexpPrepareEncoding(RubyRegexp regexp, Object matchString,
                @Cached RubyStringLibrary stringLibrary,
                @Cached TruffleString.GetByteCodeRangeNode codeRangeNode,
                @Cached BranchProfile asciiOnlyProfile,
                @Cached BranchProfile asciiIncompatibleFixedRegexpEncodingProfile,
                @Cached BranchProfile asciiIncompatibleMatchStringEncodingProfile,
                @Cached BranchProfile brokenMatchStringProfile,
                @Cached BranchProfile defaultRegexEncodingProfile,
                @Cached BranchProfile fallbackProcessingProfile,
                @Cached BranchProfile fixedRegexpEncodingProfile,
                @Cached BranchProfile returnMatchStringEncodingProfile,
                @Cached BranchProfile sameEncodingProfile,
                @Cached BranchProfile validBinaryMatchStringProfile,
                @Cached BranchProfile validUtf8MatchStringProfile) {
            final RubyEncoding regexpEncoding = regexp.encoding;
            final RubyEncoding matchStringEncoding = stringLibrary.getEncoding(matchString);
            var tstring = stringLibrary.getTString(matchString);
            final TruffleString.CodeRange matchStringCodeRange = codeRangeNode.execute(tstring,
                    matchStringEncoding.tencoding);

            if (matchStringCodeRange == BROKEN) {
                brokenMatchStringProfile.enter();

                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentErrorInvalidByteSequence(matchStringEncoding, this));
            }

            // This set of checks optimizes for the very common case where the regexp uses the default encoding and
            // the match string is valid and uses an ASCII-compatible encoding. This extra set of checks is not present
            // in MRI as of 3.0.2. While they permit an early return of the method, they do not conflict with any of
            // the branches in the fallback strategy.
            if (regexpEncoding == Encodings.US_ASCII && regexp.options.canAdaptEncoding()) {
                defaultRegexEncodingProfile.enter();

                // The default String encodings are UTF-8 for internal strings and ASCII-8BIT for external strings.
                // Both encodings are ASCII-compatible and as such can either be CR_7BIT or CR_VALID at this point
                // depending on the contents. CR_BROKEN strings are handled as a failure case earlier.

                if (matchStringCodeRange == ASCII) {
                    asciiOnlyProfile.enter();

                    return Encodings.US_ASCII;
                } else if (matchStringEncoding == Encodings.UTF_8) {
                    validUtf8MatchStringProfile.enter();
                    assert matchStringCodeRange == VALID;

                    return Encodings.UTF_8;
                } else if (matchStringEncoding == Encodings.BINARY) {
                    validBinaryMatchStringProfile.enter();
                    assert matchStringCodeRange == VALID;

                    return Encodings.BINARY;
                }
            }

            // The following set of checks follow the logic in MRI's `rb_reg_prepare_enc`. The branch order is
            // generally significant, although some branches could be reordered because their conditions do not
            // conflict with those in other branches.
            fallbackProcessingProfile.enter();

            if (regexpEncoding == matchStringEncoding) {
                sameEncodingProfile.enter();

                return regexpEncoding;
            } else if (matchStringCodeRange == ASCII && regexpEncoding == Encodings.US_ASCII) {
                asciiOnlyProfile.enter();

                return Encodings.US_ASCII;
            } else if (!matchStringEncoding.isAsciiCompatible) {
                asciiIncompatibleMatchStringEncodingProfile.enter();

                return raiseEncodingCompatibilityError(regexp, matchStringEncoding);
            } else if (regexp.options.isFixed()) {
                fixedRegexpEncodingProfile.enter();

                if (!regexpEncoding.isAsciiCompatible || matchStringCodeRange != ASCII) {
                    asciiIncompatibleFixedRegexpEncodingProfile.enter();

                    return raiseEncodingCompatibilityError(regexp, matchStringEncoding);
                }

                return regexpEncoding;
            } else {
                returnMatchStringEncodingProfile.enter();

                if (regexp.options.isEncodingNone() && matchStringEncoding != Encodings.BINARY &&
                        matchStringCodeRange != ASCII) {
                    // profiled by lazy node
                    warnHistoricalBinaryRegexpMatch(matchStringEncoding);
                }

                return matchStringEncoding;
            }
        }

        // MRI: reg_enc_error
        private RubyEncoding raiseEncodingCompatibilityError(RubyRegexp regexp, RubyEncoding matchStringEncoding) {
            throw new RaiseException(getContext(), coreExceptions()
                    .encodingCompatibilityErrorRegexpIncompatible(regexp.encoding, matchStringEncoding, this));
        }

        private void warnHistoricalBinaryRegexpMatch(RubyEncoding matchStringEncoding) {
            if (warnNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                warnNode = insert(new WarnNode());
            }

            if (warnNode.shouldWarn()) {
                warnNode.warningMessage(
                        getContext().getCallStack().getTopMostUserSourceSection(),
                        StringUtils.format(
                                "historical binary regexp match /.../n against %s string",
                                getEncodingName(matchStringEncoding)));
            }
        }

        @TruffleBoundary
        private String getEncodingName(RubyEncoding matchStringEncoding) {
            return RubyGuards.getJavaString(matchStringEncoding.name);
        }
    }

    @TruffleBoundary
    private static Matcher getMatcher(Regex regex, byte[] stringBytes, int start, int end) {
        return regex.matcher(stringBytes, start, end);
    }

    @TruffleBoundary
    private static Matcher getMatcherNoRegion(Regex regex, byte[] stringBytes, int start, int end) {
        return regex.matcherNoRegion(stringBytes, start, end);
    }

    @TruffleBoundary
    private static Regex makeRegexpForEncoding(RubyContext context, RubyRegexp regexp, RubyEncoding enc,
            Node currentNode) {
        final RubyEncoding[] fixedEnc = new RubyEncoding[]{ null };
        var source = regexp.source;
        var sourceInOtherEncoding = source.forceEncodingUncached(regexp.encoding.tencoding, enc.tencoding);
        try {
            final TStringBuilder preprocessed = ClassicRegexp
                    .preprocess(
                            new TStringWithEncoding(sourceInOtherEncoding, enc),
                            enc,
                            fixedEnc,
                            RegexpSupport.ErrorMode.RAISE);
            final RegexpOptions options = regexp.options;
            return ClassicRegexp.makeRegexp(
                    null,
                    preprocessed,
                    options,
                    enc,
                    source,
                    currentNode);
        } catch (DeferredRaiseException dre) {
            throw dre.getException(context);
        }
    }

    @CoreMethod(names = "union", onSingleton = true, required = 2, rest = true)
    public abstract static class RegexpUnionNode extends CoreMethodArrayArgumentsNode {

        @Child StringAppendPrimitiveNode appendNode = StringAppendPrimitiveNode.create();
        @Child AsTruffleStringNode asTruffleStringNode = AsTruffleStringNode.create();
        @Child ToSNode toSNode = ToSNode.create();
        @Child DispatchNode copyNode = DispatchNode.create();
        @Child private SameOrEqualNode sameOrEqualNode = SameOrEqualNode.create();
        private final RubyStringLibrary rubyStringLibrary = RubyStringLibrary.create();
        private final RubyStringLibrary regexpStringLibrary = RubyStringLibrary.create();

        @Specialization(
                guards = "argsMatch(frame, cachedArgs, args)",
                limit = "getDefaultCacheLimit()")
        protected Object fastUnion(VirtualFrame frame, RubyString str, Object sep, Object[] args,
                @Cached(value = "args", dimensions = 1) Object[] cachedArgs,
                @Cached BranchProfile errorProfile,
                @Cached("buildUnion(str, sep, args, errorProfile)") RubyRegexp union) {
            return copyNode.call(union, "clone");
        }

        @Specialization(replaces = "fastUnion")
        protected Object slowUnion(RubyString str, Object sep, Object[] args,
                @Cached BranchProfile errorProfile) {
            return buildUnion(str, sep, args, errorProfile);
        }

        public RubyRegexp buildUnion(RubyString str, Object sep, Object[] args, BranchProfile errorProfile) {
            assert args.length > 0;
            RubyString regexpString = null;
            for (Object arg : args) {
                if (regexpString == null) {
                    regexpString = appendNode.executeStringAppend(str, string(arg));
                } else {
                    regexpString = appendNode.executeStringAppend(regexpString, sep);
                    regexpString = appendNode.executeStringAppend(regexpString, string(arg));
                }
            }
            var encoding = regexpStringLibrary.getEncoding(regexpString);
            var truffleString = asTruffleStringNode.execute(regexpString.tstring, encoding.tencoding);
            try {
                return createRegexp(truffleString, encoding);
            } catch (DeferredRaiseException dre) {
                errorProfile.enter();
                throw dre.getException(getContext());
            }
        }

        public Object string(Object obj) {
            if (rubyStringLibrary.isRubyString(obj)) {
                final TStringWithEncoding quotedRopeResult = ClassicRegexp
                        .quote19(new ATStringWithEncoding(rubyStringLibrary, obj));
                return createString(quotedRopeResult);
            } else {
                return toSNode.execute((RubyRegexp) obj);
            }
        }

        @ExplodeLoop
        protected boolean argsMatch(VirtualFrame frame, Object[] cachedArgs, Object[] args) {
            if (cachedArgs.length != args.length) {
                return false;
            } else {
                for (int i = 0; i < cachedArgs.length; i++) {
                    if (!sameOrEqualNode.executeSameOrEqual(cachedArgs[i], args[i])) {
                        return false;
                    }
                }
                return true;
            }
        }

        @TruffleBoundary
        public RubyRegexp createRegexp(TruffleString pattern, RubyEncoding encoding)
                throws DeferredRaiseException {
            return RubyRegexp.create(getLanguage(), pattern, encoding, RegexpOptions.fromEmbeddedOptions(0), this);
        }
    }

    @ImportStatic(Encodings.class)
    public abstract static class TRegexCompileNode extends RubyBaseNode {

        public abstract Object executeTRegexCompile(RubyRegexp regexp, boolean atStart, RubyEncoding encoding);

        @Child DispatchNode warnOnFallbackNode;

        @Specialization(guards = "encoding == US_ASCII")
        protected Object usASCII(RubyRegexp regexp, boolean atStart, RubyEncoding encoding) {
            final Object tregex = regexp.tregexCache.getUSASCIIRegex(atStart);
            if (tregex != null) {
                return tregex;
            } else {
                return regexp.tregexCache.compile(getContext(), regexp, atStart, encoding, this);
            }
        }

        @Specialization(guards = "encoding == ISO_8859_1")
        protected Object latin1(RubyRegexp regexp, boolean atStart, RubyEncoding encoding) {
            final Object tregex = regexp.tregexCache.getLatin1Regex(atStart);
            if (tregex != null) {
                return tregex;
            } else {
                return regexp.tregexCache.compile(getContext(), regexp, atStart, encoding, this);
            }
        }

        @Specialization(guards = "encoding == UTF_8")
        protected Object utf8(RubyRegexp regexp, boolean atStart, RubyEncoding encoding) {
            final Object tregex = regexp.tregexCache.getUTF8Regex(atStart);
            if (tregex != null) {
                return tregex;
            } else {
                return regexp.tregexCache.compile(getContext(), regexp, atStart, encoding, this);
            }
        }

        @Specialization(guards = "encoding == BINARY")
        protected Object binary(RubyRegexp regexp, boolean atStart, RubyEncoding encoding) {
            final Object tregex = regexp.tregexCache.getBinaryRegex(atStart);
            if (tregex != null) {
                return tregex;
            } else {
                return regexp.tregexCache.compile(getContext(), regexp, atStart, encoding, this);
            }
        }

        @Fallback
        protected Object other(RubyRegexp regexp, boolean atStart, RubyEncoding encoding) {
            return nil;
        }

        DispatchNode getWarnOnFallbackNode() {
            if (warnOnFallbackNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                warnOnFallbackNode = insert(DispatchNode.create());
            }
            return warnOnFallbackNode;
        }
    }

    public abstract static class RegexpStatsNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        protected <T> RubyArray fillinInstrumentData(Map<T, AtomicInteger> map, ArrayBuilderNode arrayBuilderNode,
                RubyContext context) {
            final int arraySize = (LITERAL_REGEXPS.size() + DYNAMIC_REGEXPS.size()) * 2;
            BuilderState state = arrayBuilderNode.start(arraySize);
            int n = 0;
            for (Entry<T, AtomicInteger> e : map.entrySet()) {
                arrayBuilderNode
                        .appendValue(state, n++,
                                StringOperations.createUTF8String(context, getLanguage(), e.getKey().toString()));
                arrayBuilderNode.appendValue(state, n++, e.getValue().get());
            }
            return createArray(arrayBuilderNode.finish(state, n), n);
        }

        @TruffleBoundary
        protected <T> RubyArray fillinInstrumentData(Set<T> map, ArrayBuilderNode arrayBuilderNode,
                RubyContext context) {
            final int arraySize = (LITERAL_REGEXPS.size() + DYNAMIC_REGEXPS.size()) * 2;
            BuilderState state = arrayBuilderNode.start(arraySize);
            int n = 0;
            for (T e : map) {
                arrayBuilderNode
                        .appendValue(state, n++, e);
                arrayBuilderNode.appendValue(state, n++, 1);
            }
            return createArray(arrayBuilderNode.finish(state, n), n);
        }

        @TruffleBoundary
        protected static Set<RubyRegexp> allCompiledRegexps() {
            final Set<RubyRegexp> ret = new HashSet<>();

            ret.addAll(DYNAMIC_REGEXPS);
            ret.addAll(LITERAL_REGEXPS);

            return ret;
        }

        @TruffleBoundary
        protected static Set<RubyRegexp> allMatchedRegexps() {
            final Set<RubyRegexp> ret = new HashSet<>();

            ret.addAll(
                    MATCHED_REGEXPS_JONI
                            .keySet()
                            .stream()
                            .map(matchInfo -> matchInfo.regex)
                            .collect(Collectors.toSet()));
            ret.addAll(
                    MATCHED_REGEXPS_TREGEX
                            .keySet()
                            .stream()
                            .map(matchInfo -> matchInfo.regex)
                            .collect(Collectors.toSet()));

            return ret;
        }
    }

    @CoreMethod(names = "regexp_compilation_stats_array", onSingleton = true, required = 1)
    public abstract static class RegexpCompilationStatsArrayNode extends RegexpStatsNode {

        @Specialization
        protected Object buildStatsArray(boolean literalRegexps,
                @Cached ArrayBuilderNode arrayBuilderNode) {
            return fillinInstrumentData(
                    literalRegexps ? LITERAL_REGEXPS : DYNAMIC_REGEXPS,
                    arrayBuilderNode,
                    getContext());
        }
    }

    @CoreMethod(names = "match_stats_array", onSingleton = true, required = 1)
    public abstract static class MatchStatsArrayNode extends RegexpStatsNode {

        @Specialization
        protected Object buildStatsArray(boolean joniMatches,
                @Cached ArrayBuilderNode arrayBuilderNode) {
            return fillinInstrumentData(
                    joniMatches ? MATCHED_REGEXPS_JONI : MATCHED_REGEXPS_TREGEX,
                    arrayBuilderNode,
                    getContext());
        }
    }

    @CoreMethod(names = "unused_regexps_array", onSingleton = true, required = 0)
    public abstract static class UnusedRegexpsArray extends RegexpStatsNode {

        @TruffleBoundary
        @Specialization
        protected Object buildUnusedRegexpsArray(
                @Cached ArrayBuilderNode arrayBuilderNode) {
            final Set<RubyRegexp> compiledRegexps = allCompiledRegexps();
            final Set<RubyRegexp> matchedRegexps = allMatchedRegexps();

            final Set<RubyRegexp> unusedRegexps = new HashSet<>(compiledRegexps);
            unusedRegexps.removeAll(matchedRegexps);

            final BuilderState state = arrayBuilderNode.start(unusedRegexps.size());
            int n = 0;
            for (RubyRegexp entry : unusedRegexps) {
                arrayBuilderNode
                        .appendValue(state, n++,
                                StringOperations.createUTF8String(getContext(), getLanguage(), entry.toString()));
            }

            return createArray(arrayBuilderNode.finish(state, n), n);
        }
    }

    @CoreMethod(names = "compiled_regexp_hash_array", onSingleton = true, required = 0)
    public abstract static class CompiledRegexpHashArray extends RegexpStatsNode {

        @TruffleBoundary
        @Specialization
        protected Object buildInfoArray(
                @Cached ArrayBuilderNode arrayBuilderNode,
                @CachedLibrary(limit = "1") HashStoreLibrary hashStoreLibrary) {
            final Set<RubyRegexp> matchedRegexps = allMatchedRegexps();

            final int arraySize = LITERAL_REGEXPS.size() + DYNAMIC_REGEXPS.size();
            final BuilderState state = arrayBuilderNode.start(arraySize);

            processGroup(LITERAL_REGEXPS, matchedRegexps, true, hashStoreLibrary, arrayBuilderNode, state, 0);
            processGroup(
                    DYNAMIC_REGEXPS,
                    matchedRegexps,
                    false,
                    hashStoreLibrary,
                    arrayBuilderNode,
                    state,
                    LITERAL_REGEXPS.size());

            return createArray(arrayBuilderNode.finish(state, arraySize), arraySize);
        }

        private void processGroup(ConcurrentSkipListSet<RubyRegexp> group,
                Set<RubyRegexp> matchedRegexps,
                boolean isRegexpLiteral,
                HashStoreLibrary hashStoreLibrary,
                ArrayBuilderNode arrayBuilderNode, BuilderState state, int offset) {
            int n = 0;
            for (RubyRegexp entry : group) {
                arrayBuilderNode
                        .appendValue(
                                state,
                                offset + n,
                                buildRegexInfoHash(
                                        getContext(),
                                        getLanguage(),
                                        hashStoreLibrary,
                                        entry,
                                        matchedRegexps.contains(entry),
                                        Optional.of(isRegexpLiteral),
                                        Optional.of(1)));
                n++;
            }
        }

        protected static RubyHash buildRegexInfoHash(RubyContext context, RubyLanguage language,
                HashStoreLibrary hashStoreLibrary, RubyRegexp regexpInfo, boolean isUsed,
                Optional<Boolean> isRegexpLiteral,
                Optional<Integer> count) {
            final RubyHash hash = HashOperations.newEmptyHash(context, language);

            hashStoreLibrary.set(
                    hash.store,
                    hash,
                    language.getSymbol("value"),
                    StringOperations.createUTF8String(context, language, regexpInfo.source),
                    true);

            if (count.isPresent()) {
                hashStoreLibrary.set(hash.store, hash, language.getSymbol("count"), count.get(), true);
            }

            if (isRegexpLiteral.isPresent()) {
                hashStoreLibrary.set(hash.store, hash, language.getSymbol("isLiteral"), isRegexpLiteral.get(), true);
            }

            if (context.getOptions().REGEXP_INSTRUMENT_MATCH) {
                hashStoreLibrary.set(hash.store, hash, language.getSymbol("isUsed"), isUsed, true);
            }

            hashStoreLibrary.set(hash.store, hash, language.getSymbol("encoding"), regexpInfo.encoding, true);
            hashStoreLibrary.set(
                    hash.store,
                    hash,
                    language.getSymbol("options"),
                    RegexpOptions.fromJoniOptions(regexpInfo.options.toJoniOptions()).toOptions(),
                    true);

            assert hashStoreLibrary.verify(hash.store, hash);

            return hash;
        }
    }

    @CoreMethod(names = "matched_regexp_hash_array", onSingleton = true, required = 0)
    public abstract static class MatchedRegexpHashArray extends RegexpStatsNode {

        @TruffleBoundary
        @Specialization
        protected Object buildInfoArray(
                @Cached ArrayBuilderNode arrayBuilderNode,
                @CachedLibrary(limit = "3") HashStoreLibrary hashStoreLibrary) {
            final int arraySize = (MATCHED_REGEXPS_JONI.size() + MATCHED_REGEXPS_TREGEX.size());

            final BuilderState state = arrayBuilderNode.start(arraySize);

            processGroup(MATCHED_REGEXPS_JONI, false, hashStoreLibrary, arrayBuilderNode, state, 0);
            processGroup(
                    MATCHED_REGEXPS_TREGEX,
                    true,
                    hashStoreLibrary,
                    arrayBuilderNode,
                    state,
                    MATCHED_REGEXPS_JONI.size());

            return createArray(arrayBuilderNode.finish(state, arraySize), arraySize);
        }

        private void processGroup(ConcurrentHashMap<MatchInfo, AtomicInteger> group,
                boolean isTRegexMatch,
                HashStoreLibrary hashStoreLibrary,
                ArrayBuilderNode arrayBuilderNode, BuilderState state, int offset) {
            int n = 0;
            for (Entry<MatchInfo, AtomicInteger> entry : group.entrySet()) {
                arrayBuilderNode.appendValue(state, offset + n,
                        buildHash(hashStoreLibrary, isTRegexMatch, entry.getKey(), entry.getValue()));
                n++;
            }
        }

        private RubyHash buildHash(HashStoreLibrary hashStoreLibrary,
                boolean isTRegexMatch, MatchInfo matchInfo,
                AtomicInteger count) {
            final RubyHash regexpInfoHash = CompiledRegexpHashArray.buildRegexInfoHash(
                    getContext(),
                    getLanguage(),
                    hashStoreLibrary,
                    matchInfo.regex,
                    true,
                    Optional.empty(),
                    Optional.empty());
            final RubyHash matchInfoHash = HashOperations.newEmptyHash(getContext(), getLanguage());

            hashStoreLibrary
                    .set(matchInfoHash.store, matchInfoHash, getLanguage().getSymbol("regexp"), regexpInfoHash, true);
            hashStoreLibrary
                    .set(matchInfoHash.store, matchInfoHash, getLanguage().getSymbol("count"), count.get(), true);
            hashStoreLibrary
                    .set(matchInfoHash.store, matchInfoHash, getLanguage().getSymbol("isTRegex"), isTRegexMatch, true);
            hashStoreLibrary.set(
                    matchInfoHash.store,
                    matchInfoHash,
                    getLanguage().getSymbol("fromStart"),
                    matchInfo.matchStart,
                    true);

            if (getContext().getOptions().REGEXP_INSTRUMENT_MATCH_DETAILED) {
                hashStoreLibrary.set(
                        matchInfoHash.store,
                        matchInfoHash,
                        getLanguage().getSymbol("match_stats"),
                        buildMatchInfoStatsHash(hashStoreLibrary, matchInfo),
                        true);
            }

            assert hashStoreLibrary.verify(matchInfoHash.store, matchInfoHash);

            return matchInfoHash;
        }

        private RubyHash buildMatchInfoStatsHash(HashStoreLibrary hashStoreLibrary, MatchInfo matchInfo) {
            final MatchInfoStats stats = MATCHED_REGEXP_STATS.get(matchInfo);
            final RubyHash ret = HashOperations.newEmptyHash(getContext(), getLanguage());

            buildAndSetDistributionHash(
                    hashStoreLibrary,
                    ret,
                    "byte_lengths",
                    stats.byteLengthFrequencies,
                    Optional.empty(),
                    Optional.of(count -> count.get()));

            buildAndSetDistributionHash(
                    hashStoreLibrary,
                    ret,
                    "character_lengths",
                    stats.characterLengthFrequencies,
                    Optional.empty(),
                    Optional.of(count -> count.get()));

            buildAndSetDistributionHash(
                    hashStoreLibrary,
                    ret,
                    "code_ranges",
                    stats.codeRangeFrequencies,
                    Optional.of(codeRange -> getLanguage().getSymbol(codeRange.toString())),
                    Optional.of(count -> count.get()));

            buildAndSetDistributionHash(
                    hashStoreLibrary,
                    ret,
                    "encodings",
                    stats.encodingFrequencies,
                    Optional.empty(),
                    Optional.of(count -> count.get()));

            buildAndSetDistributionHash(
                    hashStoreLibrary,
                    ret,
                    "rope_types",
                    stats.ropeClassFrequencies,
                    Optional.of(className -> StringOperations.createUTF8String(getContext(), getLanguage(), className)),
                    Optional.of(count -> count.get()));

            return ret;
        }

        private <K, V> void buildAndSetDistributionHash(HashStoreLibrary hashStoreLibrary, RubyHash hash,
                String keyName, ConcurrentHashMap<K, V> distribution, Optional<Function<K, Object>> keyMapper,
                Optional<Function<V, Object>> valueMapper) {
            final RubyHash distributionHash = HashOperations.toRubyHash(
                    getContext(),
                    getLanguage(),
                    hashStoreLibrary,
                    distribution,
                    keyMapper,
                    valueMapper,
                    true);

            hashStoreLibrary.set(
                    hash.store,
                    hash,
                    getLanguage().getSymbol(keyName),
                    distributionHash,
                    true);
        }
    }

    @Primitive(name = "regexp_match_in_region", lowerFixnum = { 2, 3, 5 })
    public abstract static class MatchInRegionNode extends PrimitiveArrayArgumentsNode {

        public static MatchInRegionNode create() {
            return TruffleRegexpNodesFactory.MatchInRegionNodeFactory.create(null);
        }

        public abstract Object executeMatchInRegion(RubyRegexp regexp, Object string, int fromPos, int toPos,
                boolean atStart, int startPos, boolean createMatchData);

        /** Matches a regular expression against a string over the specified range of characters.
         *
         * @param regexp The regexp to match
         *
         * @param string The string to match against
         *
         * @param fromPos The position to search from
         *
         * @param toPos The position to search to (if less than from pos then this means search backwards)
         *
         * @param atStart Whether to only match at the beginning of the string, if false then the regexp can have any
         *            amount of prematch.
         *
         * @param startPos The position within the string which the matcher should consider the start. Setting this to
         *            the from position allows scanners to match starting part-way through a string while still setting
         *            atStart and thus forcing the match to be at the specific starting position.
         *
         * @param createMatchData Whether to create a Ruby `MatchData` object with the results of the match or return a
         *            simple Boolean value indicating a successful match (true: match; false: mismatch). */
        @Specialization(guards = "libString.isRubyString(string)", limit = "1")
        protected Object matchInRegion(
                RubyRegexp regexp,
                Object string,
                int fromPos,
                int toPos,
                boolean atStart,
                int startPos,
                boolean createMatchData,
                @Cached ConditionProfile createMatchDataProfile,
                @Cached ConditionProfile encodingMismatchProfile,
                @Cached PrepareRegexpEncodingNode prepareRegexpEncodingNode,
                @Cached TruffleString.GetInternalByteArrayNode getInternalByteArrayNode,
                @Cached ConditionProfile zeroOffsetProfile,
                @Cached MatchNode matchNode,
                @Cached RubyStringLibrary libString) {
            Regex regex = regexp.regex;
            final RubyEncoding negotiatedEncoding = prepareRegexpEncodingNode.executePrepare(regexp, string);

            if (encodingMismatchProfile.profile(regexp.encoding != negotiatedEncoding)) {
                final EncodingCache encodingCache = regexp.cachedEncodings;
                regex = encodingCache
                        .getOrCreate(negotiatedEncoding, e -> makeRegexpForEncoding(getContext(), regexp, e, this));
            }

            var tstring = libString.getTString(string);
            var byteArray = getInternalByteArrayNode.execute(tstring, libString.getTEncoding(string));

            final int offset;
            if (zeroOffsetProfile.profile(byteArray.getOffset() == 0)) {
                offset = 0;
            } else {
                offset = byteArray.getOffset();
            }

            final Matcher matcher;
            if (createMatchDataProfile.profile(createMatchData)) {
                matcher = getMatcher(regex, byteArray.getArray(), offset + startPos, byteArray.getEnd());
            } else {
                matcher = getMatcherNoRegion(regex, byteArray.getArray(), offset + startPos, byteArray.getEnd());
            }

            return matchNode.execute(regexp, string, matcher, offset + fromPos, offset + toPos, atStart,
                    createMatchData);
        }
    }

    @Primitive(name = "regexp_match_in_region_tregex", lowerFixnum = { 2, 3, 5 })
    public abstract static class MatchInRegionTRegexNode extends PrimitiveArrayArgumentsNode {

        @Child MatchInRegionNode fallbackMatchInRegionNode;
        @Child DispatchNode warnOnFallbackNode;

        @Child DispatchNode stringDupNode;
        @Child TranslateInteropExceptionNode translateInteropExceptionNode;

        @Child TruffleString.SubstringByteIndexNode substringByteIndexNode;

        @Specialization(guards = "libString.isRubyString(string)", limit = "1")
        protected Object matchInRegionTRegex(
                RubyRegexp regexp,
                Object string,
                int fromPos,
                int toPos,
                boolean atStart,
                int startPos,
                boolean createMatchData,
                @Cached TruffleString.SwitchEncodingNode switchEncodingNode,
                @Cached ConditionProfile createMatchDataProfile,
                @Cached ConditionProfile matchFoundProfile,
                @Cached ConditionProfile tRegexCouldNotCompileProfile,
                @Cached ConditionProfile tRegexIncompatibleProfile,
                @Cached ConditionProfile startPosNotZeroProfile,
                @Cached LoopConditionProfile loopProfile,
                @CachedLibrary(limit = "getInteropCacheLimit()") InteropLibrary regexInterop,
                @CachedLibrary(limit = "getInteropCacheLimit()") InteropLibrary resultInterop,
                @Cached PrepareRegexpEncodingNode prepareRegexpEncodingNode,
                @Cached TRegexCompileNode tRegexCompileNode,
                @Cached RubyStringLibrary libString,
                @Cached IntValueProfile groupCountProfile) {
            final Object tRegex;
            final RubyEncoding negotiatedEncoding = prepareRegexpEncodingNode.executePrepare(regexp, string);
            var tstring = switchEncodingNode.execute(libString.getTString(string), negotiatedEncoding.tencoding);
            final int byteLength = tstring.byteLength(negotiatedEncoding.tencoding);

            if (tRegexIncompatibleProfile
                    .profile(toPos < fromPos || toPos != byteLength || fromPos < 0) ||
                    tRegexCouldNotCompileProfile.profile((tRegex = tRegexCompileNode.executeTRegexCompile(
                            regexp,
                            atStart,
                            negotiatedEncoding)) == nil)) {
                return fallbackToJoni(
                        regexp,
                        string,
                        negotiatedEncoding,
                        fromPos,
                        toPos,
                        atStart,
                        startPos,
                        createMatchData);
            }

            if (getContext().getOptions().REGEXP_INSTRUMENT_MATCH) {
                TruffleRegexpNodes.instrumentMatch(
                        MATCHED_REGEXPS_TREGEX,
                        regexp,
                        string,
                        atStart,
                        getContext().getOptions().REGEXP_INSTRUMENT_MATCH_DETAILED);
            }

            int fromIndex = fromPos;
            final TruffleString tstringToMatch;
            final String execMethod;

            if (createMatchDataProfile.profile(createMatchData)) {
                if (startPosNotZeroProfile.profile(startPos > 0)) {
                    // If startPos != 0, then fromPos == startPos.
                    assert fromPos == startPos;
                    fromIndex = 0;

                    if (substringByteIndexNode == null) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        substringByteIndexNode = insert(TruffleString.SubstringByteIndexNode.create());
                    }
                    tstringToMatch = substringByteIndexNode.execute(tstring, startPos, toPos - startPos,
                            negotiatedEncoding.tencoding, true);
                } else {
                    tstringToMatch = tstring;
                }
                execMethod = "exec";
            } else {
                // Only strscan ever passes a non-zero startPos and that never uses `match?`.
                assert startPos == 0 : "Simple Boolean match not supported with non-zero startPos";

                tstringToMatch = tstring;
                execMethod = "execBoolean";
            }

            final Object result = invoke(regexInterop, tRegex, execMethod, tstringToMatch, fromIndex);

            if (createMatchDataProfile.profile(createMatchData)) {
                final boolean isMatch = (boolean) readMember(resultInterop, result, "isMatch");

                if (matchFoundProfile.profile(isMatch)) {
                    final int groupCount = groupCountProfile
                            .profile((int) readMember(regexInterop, tRegex, "groupCount"));
                    final Region region = new Region(groupCount);

                    try {
                        for (int group = 0; loopProfile.inject(group < groupCount); group++) {
                            region.beg[group] = RubyMatchData.LAZY;
                            region.end[group] = RubyMatchData.LAZY;
                            TruffleSafepoint.poll(this);
                        }
                    } finally {
                        profileAndReportLoopCount(loopProfile, groupCount);
                    }

                    return createMatchData(regexp, dupString(string), region, result);
                } else {
                    return nil;
                }
            } else {
                return result;
            }
        }

        private Object fallbackToJoni(RubyRegexp regexp, Object string, RubyEncoding encoding, int fromPos, int toPos,
                boolean atStart, int startPos, boolean createMatchData) {
            if (getContext().getOptions().WARN_TRUFFLE_REGEX_MATCH_FALLBACK) {
                if (warnOnFallbackNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    warnOnFallbackNode = insert(DispatchNode.create());
                }

                warnOnFallbackNode.call(
                        getContext().getCoreLibrary().truffleRegexpOperationsModule,
                        "warn_fallback",
                        new Object[]{
                                regexp,
                                string,
                                encoding,
                                fromPos,
                                toPos,
                                atStart,
                                startPos });
            }

            if (fallbackMatchInRegionNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                fallbackMatchInRegionNode = insert(MatchInRegionNode.create());
            }

            return fallbackMatchInRegionNode
                    .executeMatchInRegion(regexp, string, fromPos, toPos, atStart, startPos, createMatchData);
        }

        private Object createMatchData(RubyRegexp regexp, Object string, Region region, Object tRegexResult) {
            final RubyMatchData matchData = new RubyMatchData(
                    coreLibrary().matchDataClass,
                    getLanguage().matchDataShape,
                    regexp,
                    string,
                    region);
            matchData.tRegexResult = tRegexResult;
            AllocationTracing.trace(matchData, this);
            return matchData;
        }

        private Object readMember(InteropLibrary interop, Object receiver, String name) {
            try {
                return interop.readMember(receiver, name);
            } catch (InteropException e) {
                if (translateInteropExceptionNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    translateInteropExceptionNode = insert(TranslateInteropExceptionNodeGen.create());
                }
                throw translateInteropExceptionNode.execute(e);
            }
        }

        private Object invoke(InteropLibrary interop, Object receiver, String member, Object... args) {
            try {
                return interop.invokeMember(receiver, member, args);
            } catch (InteropException e) {
                if (translateInteropExceptionNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    translateInteropExceptionNode = insert(TranslateInteropExceptionNodeGen.create());
                }
                throw translateInteropExceptionNode.executeInInvokeMember(e, receiver, args);
            }
        }

        private Object dupString(Object string) {
            if (stringDupNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                stringDupNode = insert(DispatchNode.create());
            }

            return stringDupNode.call(string, "dup");
        }
    }

    public abstract static class MatchNode extends RubyBaseNode {

        @Child private DispatchNode dupNode = DispatchNode.create();

        public static MatchNode create() {
            return MatchNodeGen.create();
        }

        public abstract Object execute(RubyRegexp regexp, Object string, Matcher matcher,
                int startPos, int range, boolean onlyMatchAtStart, boolean createMatchData);

        // Creating a MatchData will store a copy of the source string. It's tempting to use a rope here, but a bit
        // inconvenient because we can't work with ropes directly in Ruby and some MatchData methods are nicely
        // implemented using the source string data. Likewise, we need to taint objects based on the source string's
        // taint state. We mustn't allow the source string's contents to change, however, so we must ensure that we have
        // a private copy of that string. Since the source string would otherwise be a reference to string held outside
        // the MatchData object, it would be possible for the source string to be modified externally.
        //
        // Ex. x = "abc"; x =~ /(.*)/; x.upcase!
        //
        // Without a private copy, the MatchData's source could be modified to be upcased when it should remain the
        // same as when the MatchData was created.
        @Specialization
        protected Object match(
                RubyRegexp regexp,
                Object string,
                Matcher matcher,
                int startPos,
                int range,
                boolean onlyMatchAtStart,
                boolean createMatchData,
                @Cached ConditionProfile createMatchDataProfile,
                @Cached ConditionProfile mismatchProfile) {
            if (getContext().getOptions().REGEXP_INSTRUMENT_MATCH) {
                TruffleRegexpNodes.instrumentMatch(
                        MATCHED_REGEXPS_JONI,
                        regexp,
                        string,
                        onlyMatchAtStart,
                        getContext().getOptions().REGEXP_INSTRUMENT_MATCH_DETAILED);
            }

            int match = runMatch(matcher, startPos, range, onlyMatchAtStart);

            if (createMatchDataProfile.profile(createMatchData)) {
                if (mismatchProfile.profile(match == Matcher.FAILED)) {
                    return nil;
                }

                assert match >= 0;

                final Region region = matcher.getEagerRegion();
                assert assertValidRegion(region);
                final RubyString dupedString = (RubyString) dupNode.call(string, "dup");
                RubyMatchData result = new RubyMatchData(
                        coreLibrary().matchDataClass,
                        getLanguage().matchDataShape,
                        regexp,
                        dupedString,
                        region);
                AllocationTracing.trace(result, this);
                return result;
            } else {
                return match != Matcher.FAILED;
            }
        }

        @TruffleBoundary
        private int runMatch(Matcher matcher, int startPos, int range, boolean onlyMatchAtStart) {
            // Keep status as RUN because MRI has an uninterruptible Regexp engine
            int[] result = new int[1];
            if (onlyMatchAtStart) {
                getContext().getThreadManager().runUntilResultKeepStatus(
                        this,
                        r -> r[0] = matcher.matchInterruptible(startPos, range, Option.DEFAULT),
                        result);
            } else {
                getContext().getThreadManager().runUntilResultKeepStatus(
                        this,
                        r -> r[0] = matcher.searchInterruptible(startPos, range, Option.DEFAULT),
                        result);
            }
            return result[0];
        }

        private boolean assertValidRegion(Region region) {
            for (int i = 0; i < region.numRegs; i++) {
                assert region.beg[i] >= 0 || region.beg[i] == RubyMatchData.MISSING;
                assert region.end[i] >= 0 || region.end[i] == RubyMatchData.MISSING;
            }
            return true;
        }
    }

    static final class MatchInfo {

        private final RubyRegexp regex;
        private final boolean matchStart;

        MatchInfo(RubyRegexp regex, boolean matchStart) {
            assert regex != null;
            this.regex = regex;
            this.matchStart = matchStart;
        }

        @Override
        public int hashCode() {
            return Objects.hash(regex, matchStart);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof MatchInfo)) {
                return false;
            }

            MatchInfo other = (MatchInfo) obj;
            return matchStart == other.matchStart &&
                    regex.equals(other.regex);
        }

        @Override
        public String toString() {
            return String.format(
                    "Match (%s, fromStart = %s)",
                    regex,
                    matchStart);
        }
    }

    static final class MatchInfoStats {

        private final ConcurrentHashMap<Integer, AtomicLong> byteLengthFrequencies = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Integer, AtomicLong> characterLengthFrequencies = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<TruffleString.CodeRange, AtomicLong> codeRangeFrequencies = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<RubyEncoding, AtomicLong> encodingFrequencies = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, AtomicLong> ropeClassFrequencies = new ConcurrentHashMap<>();

        private void record(ATStringWithEncoding string) {
            ConcurrentOperations
                    .getOrCompute(byteLengthFrequencies, string.byteLength(), x -> new AtomicLong())
                    .incrementAndGet();
            ConcurrentOperations
                    .getOrCompute(characterLengthFrequencies, string.characterLength(), x -> new AtomicLong())
                    .incrementAndGet();
            ConcurrentOperations
                    .getOrCompute(codeRangeFrequencies, string.getCodeRange(), x -> new AtomicLong())
                    .incrementAndGet();
            ConcurrentOperations.getOrCompute(encodingFrequencies, string.encoding, x -> new AtomicLong())
                    .incrementAndGet();
            ConcurrentOperations
                    .getOrCompute(ropeClassFrequencies, string.getClass().getSimpleName(), x -> new AtomicLong())
                    .incrementAndGet();
        }

    }

    static ConcurrentSkipListSet<RubyRegexp> DYNAMIC_REGEXPS = new ConcurrentSkipListSet<>();
    static ConcurrentSkipListSet<RubyRegexp> LITERAL_REGEXPS = new ConcurrentSkipListSet<>();
    private static ConcurrentHashMap<MatchInfo, AtomicInteger> MATCHED_REGEXPS_JONI = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<MatchInfo, AtomicInteger> MATCHED_REGEXPS_TREGEX = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<MatchInfo, MatchInfoStats> MATCHED_REGEXP_STATS = new ConcurrentHashMap<>();

    /** WARNING: computeRegexpEncoding() mutates options, so the caller should make sure it's a copy */
    @TruffleBoundary
    public static Regex compile(RubyDeferredWarnings rubyDeferredWarnings, TStringWithEncoding bytes,
            RegexpOptions[] optionsArray, Node currentNode) throws DeferredRaiseException {
        RubyEncoding enc = bytes.getEncoding();
        RubyEncoding[] fixedEnc = new RubyEncoding[]{ null };
        TStringBuilder unescaped = ClassicRegexp.preprocess(bytes, enc, fixedEnc, RegexpSupport.ErrorMode.RAISE);
        enc = ClassicRegexp.computeRegexpEncoding(optionsArray, enc, fixedEnc);

        Regex regexp = ClassicRegexp
                .makeRegexp(rubyDeferredWarnings, unescaped, optionsArray[0], enc, bytes.tstring, currentNode);
        regexp.setUserObject(bytes.forceEncoding(enc));

        return regexp;
    }

}
