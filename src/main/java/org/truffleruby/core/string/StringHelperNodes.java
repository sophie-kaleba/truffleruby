/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.InternalByteArray;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.ErrorHandling;
import com.oracle.truffle.api.strings.TruffleStringIterator;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.core.encoding.EncodingNodes;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.library.RubyStringLibrary;

public abstract class StringHelperNodes {

    @TruffleBoundary
    static Object trTransHelper(EncodingNodes.CheckEncodingNode checkEncodingNode, RubyString self,
            RubyStringLibrary libFromStr, Object fromStr,
            RubyStringLibrary libToStr, Object toStr, boolean sFlag, Node node) {
        final RubyEncoding e1 = checkEncodingNode.executeCheckEncoding(self, fromStr);
        final RubyEncoding e2 = checkEncodingNode.executeCheckEncoding(self, toStr);
        final RubyEncoding enc = e1 == e2 ? e1 : checkEncodingNode.executeCheckEncoding(fromStr, toStr);

        var selfTStringWithEnc = new ATStringWithEncoding(self.tstring, self.getEncodingUncached());
        var fromStrTStringWithEnc = new ATStringWithEncoding(libFromStr, fromStr);
        var toStrTStringWithEnc = new ATStringWithEncoding(libToStr, toStr);
        final TruffleString ret = StringSupport.trTransHelper(selfTStringWithEnc, fromStrTStringWithEnc,
                toStrTStringWithEnc, e1.jcoding, enc, sFlag, node);
        if (ret == null) {
            return Nil.INSTANCE;
        }

        self.setTString(ret, enc);
        return self;
    }

    public abstract static class SingleByteOptimizableNode extends RubyBaseNode {
        public static SingleByteOptimizableNode create() {
            return StringHelperNodesFactory.SingleByteOptimizableNodeGen.create();
        }

        public abstract boolean execute(AbstractTruffleString string, RubyEncoding encoding);

        @Specialization
        protected boolean isSingleByteOptimizable(AbstractTruffleString string, RubyEncoding encoding,
                @Cached ConditionProfile asciiOnlyProfile,
                @Cached TruffleString.GetByteCodeRangeNode getByteCodeRangeNode) {
            if (asciiOnlyProfile.profile(StringGuards.is7Bit(string, encoding, getByteCodeRangeNode))) {
                return true;
            } else {
                return encoding.isSingleByte;
            }
        }
    }

    /** The node to use for inline caches to compare if two TruffleString are equal. It behaves the same as String#==,
     * without coercion. Note that the two encodings do no need to be the same for this node to return true. If you need
     * to ensure the encoding is the same, use {@link EqualSameEncodingNode}.
     *
     * Two strings are considered equal if they are the same byte-by-byte and:
     * <ul>
     * <li>Both strings have the same encoding</li>
     * <li>Both strings are 7-bit (and so both have an ASCII-compatible encoding)</li>
     * <li>Both strings are empty (regardless of their encodings)</li>
     * </ul>
     */
    public abstract static class EqualNode extends RubyBaseNode {

        public final boolean execute(RubyStringLibrary libString, Object rubyString,
                TruffleString cachedString, RubyEncoding cachedEncoding) {
            return execute(libString.getTString(rubyString), libString.getEncoding(rubyString),
                    cachedString, cachedEncoding);
        }

        // cachedString is TruffleString to ensure correctness, caching on a MutableTruffleString is incorrect
        public abstract boolean execute(AbstractTruffleString tstring, RubyEncoding encoding,
                TruffleString cachedString, RubyEncoding cachedEncoding);

        @Specialization
        protected boolean equal(AbstractTruffleString a, RubyEncoding encA, TruffleString b, RubyEncoding encB,
                @Cached EncodingNodes.NegotiateCompatibleStringEncodingNode negotiateCompatibleStringEncodingNode,
                @Cached StringEqualInternalNode stringEqualInternalNode) {
            var compatibleEncoding = negotiateCompatibleStringEncodingNode.execute(a, encA, b, encB);
            return stringEqualInternalNode.executeInternal(a, b, compatibleEncoding);
        }
    }

    @GenerateUncached
    public abstract static class EqualSameEncodingNode extends RubyBaseNode {

        public final boolean execute(RubyStringLibrary libString, Object rubyString,
                TruffleString cachedString, RubyEncoding cachedEncoding) {
            return execute(libString.getTString(rubyString), libString.getEncoding(rubyString),
                    cachedString, cachedEncoding);
        }

        // cachedString is TruffleString to ensure correctness, caching on a MutableTruffleString is incorrect
        public abstract boolean execute(AbstractTruffleString tstring, RubyEncoding encoding,
                TruffleString cachedString, RubyEncoding cachedEncoding);

        @Specialization(guards = "encA == encB")
        protected boolean same(AbstractTruffleString a, RubyEncoding encA, TruffleString b, RubyEncoding encB,
                @Cached StringEqualInternalNode stringEqualInternalNode) {
            return stringEqualInternalNode.executeInternal(a, b, encA);
        }

        @Specialization(guards = "encA != encB")
        protected boolean diff(AbstractTruffleString a, RubyEncoding encA, TruffleString b, RubyEncoding encB) {
            return false;
        }
    }

    @GenerateUncached
    public abstract static class StringEqualInternalNode extends RubyBaseNode {
        // compatibleEncoding is RubyEncoding or null
        public abstract boolean executeInternal(AbstractTruffleString a, AbstractTruffleString b,
                RubyEncoding compatibleEncoding);

        @Specialization(guards = "a.isEmpty() || b.isEmpty()")
        protected boolean empty(AbstractTruffleString a, AbstractTruffleString b, RubyEncoding compatibleEncoding) {
            assert compatibleEncoding != null;
            return a.isEmpty() && b.isEmpty();
        }

        @Specialization(guards = { "compatibleEncoding != null", "!a.isEmpty()", "!b.isEmpty()" })
        protected boolean equalBytes(AbstractTruffleString a, AbstractTruffleString b, RubyEncoding compatibleEncoding,
                @Cached TruffleString.EqualNode equalNode) {
            return equalNode.execute(a, b, compatibleEncoding.tencoding);
        }

        @Specialization(guards = "compatibleEncoding == null")
        protected boolean notComparable(
                AbstractTruffleString a, AbstractTruffleString b, RubyEncoding compatibleEncoding) {
            return false;
        }
    }

    @ImportStatic(StringGuards.class)
    public abstract static class CountRopesNode extends TrTableNode {

        public static CountRopesNode create() {
            return StringHelperNodesFactory.CountRopesNodeFactory.create(null);
        }

        public abstract int executeCount(Object string, TStringWithEncoding[] ropesWithEncs);

        @Specialization(guards = "isEmpty(strings.getTString(string))")
        protected int count(Object string, Object[] args,
                @Cached RubyStringLibrary strings) {
            return 0;
        }

        @Specialization(
                guards = {
                        "cachedArgs.length > 0",
                        "!isEmpty(tstring)",
                        "cachedArgs.length == args.length",
                        "argsMatch(cachedArgs, args)",
                        "encoding == cachedEncoding" })
        protected int countFast(Object string, TStringWithEncoding[] args,
                @Cached(value = "args", dimensions = 1) TStringWithEncoding[] cachedArgs,
                @Cached RubyStringLibrary libString,
                @Bind("libString.getTString(string)") AbstractTruffleString tstring,
                @Bind("libString.getEncoding(string)") RubyEncoding encoding,
                @Cached("libString.getEncoding(string)") RubyEncoding cachedEncoding,
                @Cached(value = "squeeze()", dimensions = 1) boolean[] squeeze,
                @Cached("findEncoding(libString.getTString(string), libString.getEncoding(string), cachedArgs)") RubyEncoding compatEncoding,
                @Cached("makeTables(cachedArgs, squeeze, compatEncoding)") StringSupport.TrTables tables,
                @Cached TruffleString.GetInternalByteArrayNode byteArrayNode,
                @Cached TruffleString.GetByteCodeRangeNode getByteCodeRangeNode) {
            var byteArray = byteArrayNode.execute(tstring, encoding.tencoding);
            var codeRange = getByteCodeRangeNode.execute(tstring, encoding.tencoding);
            return StringSupport.strCount(byteArray, codeRange, squeeze, tables, compatEncoding.jcoding, this);
        }

        @Specialization(guards = "!isEmpty(libString.getTString(string))")
        protected int count(Object string, TStringWithEncoding[] ropesWithEncs,
                @Cached BranchProfile errorProfile,
                @Cached RubyStringLibrary libString,
                @Cached TruffleString.GetInternalByteArrayNode byteArrayNode,
                @Cached TruffleString.GetByteCodeRangeNode getByteCodeRangeNode) {
            if (ropesWithEncs.length == 0) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentErrorEmptyVarargs(this));
            }

            var tstring = libString.getTString(string);
            var encoding = libString.getEncoding(string);
            var byteArray = byteArrayNode.execute(tstring, encoding.tencoding);
            var codeRange = getByteCodeRangeNode.execute(tstring, encoding.tencoding);

            RubyEncoding enc = findEncoding(tstring, encoding, ropesWithEncs);
            return countSlow(byteArray, codeRange, ropesWithEncs, enc);
        }

        @TruffleBoundary
        private int countSlow(InternalByteArray byteArray, TruffleString.CodeRange codeRange,
                TStringWithEncoding[] ropesWithEncs, RubyEncoding enc) {
            final boolean[] table = squeeze();
            final StringSupport.TrTables tables = makeTables(ropesWithEncs, table, enc);
            return StringSupport.strCount(byteArray, codeRange, table, tables, enc.jcoding, this);
        }
    }

    public abstract static class TrTableNode extends CoreMethodArrayArgumentsNode {
        @Child protected EncodingNodes.CheckStringEncodingNode checkEncodingNode = EncodingNodes.CheckStringEncodingNode
                .create();
        @Child protected TruffleString.EqualNode equalNode = TruffleString.EqualNode.create();

        protected boolean[] squeeze() {
            return new boolean[StringSupport.TRANS_SIZE + 1];
        }

        protected RubyEncoding findEncoding(AbstractTruffleString tstring, RubyEncoding encoding,
                TStringWithEncoding[] ropes) {
            RubyEncoding enc = checkEncodingNode.executeCheckEncoding(tstring, encoding, ropes[0].tstring,
                    ropes[0].encoding);
            for (int i = 1; i < ropes.length; i++) {
                enc = checkEncodingNode.executeCheckEncoding(tstring, encoding, ropes[i].tstring, ropes[i].encoding);
            }
            return enc;
        }

        protected StringSupport.TrTables makeTables(TStringWithEncoding[] ropesWithEncs, boolean[] squeeze,
                RubyEncoding enc) {
            // The trSetupTable method will consume the bytes from the rope one encoded character at a time and
            // build a TrTable from this. Previously we started with the encoding of rope zero, and at each
            // stage found a compatible encoding to build that TrTable with. Although we now calculate a single
            // encoding with which to build the tables it must be compatible with all ropes, so will not
            // affect the consumption of characters from those ropes.
            StringSupport.TrTables tables = StringSupport.trSetupTable(
                    ropesWithEncs[0].tstring,
                    ropesWithEncs[0].encoding,
                    squeeze,
                    null,
                    true,
                    enc.jcoding,
                    this);

            for (int i = 1; i < ropesWithEncs.length; i++) {
                tables = StringSupport
                        .trSetupTable(ropesWithEncs[i].tstring, ropesWithEncs[i].encoding, squeeze, tables, false,
                                enc.jcoding, this);
            }
            return tables;
        }

        @ExplodeLoop
        protected boolean argsMatch(TStringWithEncoding[] cachedRopes, TStringWithEncoding[] ropes) {
            for (int i = 0; i < cachedRopes.length; i++) {
                if (cachedRopes[i].encoding != ropes[i].encoding) {
                    return false;
                }
                if (!equalNode.execute(cachedRopes[i].tstring, ropes[i].tstring, cachedRopes[i].encoding.tencoding)) {
                    return false;
                }
            }
            return true;
        }
    }

    @ImportStatic(StringGuards.class)
    public abstract static class DeleteBangRopesNode extends TrTableNode {

        public static DeleteBangRopesNode create() {
            return StringHelperNodesFactory.DeleteBangRopesNodeFactory.create(null);
        }

        public abstract Object executeDeleteBang(RubyString string, TStringWithEncoding[] ropesWithEncs);

        @Specialization(guards = "isEmpty(string.tstring)")
        protected Object deleteBangEmpty(RubyString string, Object[] args) {
            return nil;
        }

        @Specialization(
                guards = {
                        "cachedArgs.length > 0",
                        "!isEmpty(string.tstring)",
                        "cachedArgs.length == args.length",
                        "argsMatch(cachedArgs, args)",
                        "libString.getEncoding(string) == cachedEncoding" })
        protected Object deleteBangFast(RubyString string, TStringWithEncoding[] args,
                @Cached(value = "args", dimensions = 1) TStringWithEncoding[] cachedArgs,
                @Cached RubyStringLibrary libString,
                @Cached("libString.getEncoding(string)") RubyEncoding cachedEncoding,
                @Cached(value = "squeeze()", dimensions = 1) boolean[] squeeze,
                @Cached("findEncoding(libString.getTString(string), libString.getEncoding(string), cachedArgs)") RubyEncoding compatEncoding,
                @Cached("makeTables(cachedArgs, squeeze, compatEncoding)") StringSupport.TrTables tables,
                @Cached BranchProfile nullProfile) {
            var processedRope = processStr(string, squeeze, compatEncoding, tables);
            if (processedRope == null) {
                nullProfile.enter();
                return nil;
            }

            string.setTString(processedRope);
            return string;
        }

        @Specialization(guards = "!isEmpty(string.tstring)", replaces = "deleteBangFast")
        protected Object deleteBangSlow(RubyString string, TStringWithEncoding[] args,
                @Cached RubyStringLibrary libString,
                @Cached BranchProfile errorProfile) {
            if (args.length == 0) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentErrorEmptyVarargs(this));
            }

            RubyEncoding enc = findEncoding(string.tstring, libString.getEncoding(string), args);

            return deleteBangSlow(string, args, enc);
        }

        @TruffleBoundary
        private Object deleteBangSlow(RubyString string, TStringWithEncoding[] ropesWithEncs, RubyEncoding enc) {
            final boolean[] squeeze = new boolean[StringSupport.TRANS_SIZE + 1];

            final StringSupport.TrTables tables = makeTables(ropesWithEncs, squeeze, enc);

            var processedRope = processStr(string, squeeze, enc, tables);
            if (processedRope == null) {
                return nil;
            }

            string.setTString(processedRope);
            // REVIEW encoding set

            return string;
        }

        @TruffleBoundary
        private TruffleString processStr(RubyString string, boolean[] squeeze, RubyEncoding enc,
                StringSupport.TrTables tables) {
            return StringSupport.delete_bangCommon19(
                    new ATStringWithEncoding(string.tstring, string.getEncodingUncached()), squeeze, tables, enc, this);
        }
    }

    @GenerateUncached
    public abstract static class HashStringNode extends RubyBaseNode {

        protected static final int CLASS_SALT = 54008340; // random number, stops hashes for similar values but different classes being the same, static because we want deterministic hashes

        public static HashStringNode create() {
            return StringHelperNodesFactory.HashStringNodeGen.create();
        }

        public abstract long execute(Object string);

        @Specialization
        protected long hash(Object string,
                @Cached RubyStringLibrary strings,
                @Cached TruffleString.HashCodeNode hashCodeNode) {
            int hashCode = hashCodeNode.execute(strings.getTString(string), strings.getTEncoding(string));
            return getContext().getHashing(this).hash(CLASS_SALT, hashCode);
        }
    }

    public abstract static class StringGetAssociatedNode extends RubyBaseNode {

        public static StringGetAssociatedNode create() {
            return StringHelperNodesFactory.StringGetAssociatedNodeGen.create();
        }

        public abstract Object execute(Object string);

        @Specialization(limit = "getDynamicObjectCacheLimit()")
        protected Object getAssociated(RubyString string,
                @CachedLibrary("string") DynamicObjectLibrary objectLibrary) {
            return objectLibrary.getOrDefault(string, Layouts.ASSOCIATED_IDENTIFIER, null);
        }

        @Specialization
        protected Object getAssociatedImmutable(ImmutableRubyString string) {
            return null;
        }

    }

    public abstract static class CheckIndexNode extends RubyBaseNode {

        public abstract int executeCheck(int index, int length);

        @Specialization
        protected int checkIndex(int index, int length,
                @Cached ConditionProfile negativeIndexProfile,
                @Cached BranchProfile errorProfile) {
            if (index >= length) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().indexErrorOutOfString(index, this));
            }

            if (negativeIndexProfile.profile(index < 0)) {
                index += length;
                if (index < 0) {
                    errorProfile.enter();
                    throw new RaiseException(
                            getContext(),
                            getContext().getCoreExceptions().indexErrorOutOfString(index, this));
                }
            }

            return index;
        }

    }

    public abstract static class NormalizeIndexNode extends RubyBaseNode {

        public abstract int executeNormalize(int index, int length);

        public static NormalizeIndexNode create() {
            return StringHelperNodesFactory.NormalizeIndexNodeGen.create();
        }

        @Specialization
        protected int normalizeIndex(int index, int length,
                @Cached ConditionProfile negativeIndexProfile) {
            if (negativeIndexProfile.profile(index < 0)) {
                return index + length;
            }

            return index;
        }

    }

    public abstract static class InvertAsciiCaseHelperNode extends RubyBaseNode {

        private final boolean lowerToUpper;
        private final boolean upperToLower;

        public static InvertAsciiCaseHelperNode createLowerToUpper() {
            return StringHelperNodesFactory.InvertAsciiCaseHelperNodeGen.create(true, false);
        }

        public static InvertAsciiCaseHelperNode createUpperToLower() {
            return StringHelperNodesFactory.InvertAsciiCaseHelperNodeGen.create(false, true);
        }

        public static InvertAsciiCaseHelperNode createSwapCase() {
            return StringHelperNodesFactory.InvertAsciiCaseHelperNodeGen.create(true, true);
        }

        protected InvertAsciiCaseHelperNode(boolean lowerToUpper, boolean upperToLower) {
            this.lowerToUpper = lowerToUpper;
            this.upperToLower = upperToLower;
        }

        public abstract byte[] executeInvert(RubyString string, TruffleStringIterator iterator, byte[] initialBytes);

        @Specialization
        protected byte[] invert(RubyString string, TruffleStringIterator iterator, byte[] initialBytes,
                @Cached RubyStringLibrary libString,
                @Cached TruffleStringIterator.NextNode nextNode,
                @Cached TruffleString.CopyToByteArrayNode copyToByteArrayNode,
                @Cached BranchProfile caseSwapProfile) {
            var tstring = string.tstring;
            var encoding = libString.getTEncoding(string);

            byte[] modified = initialBytes;

            while (iterator.hasNext()) {
                int p = iterator.getByteIndex();
                int c = nextNode.execute(iterator);

                if ((lowerToUpper && StringSupport.isAsciiLowercase(c)) ||
                        (upperToLower && StringSupport.isAsciiUppercase(c))) {
                    caseSwapProfile.enter();

                    if (modified == null) {
                        modified = copyToByteArrayNode.execute(tstring, encoding);
                    }

                    // Convert lower-case ASCII code point to upper-case or upper-case ASCII code point to lower-case.
                    modified[p] ^= 0x20;
                }
            }

            return modified;
        }
    }

    public abstract static class InvertAsciiCaseNode extends RubyBaseNode {

        @Child private InvertAsciiCaseHelperNode invertNode;

        public static InvertAsciiCaseNode createLowerToUpper() {
            return StringHelperNodesFactory.InvertAsciiCaseNodeGen
                    .create(InvertAsciiCaseHelperNode.createLowerToUpper());
        }

        public static InvertAsciiCaseNode createUpperToLower() {
            return StringHelperNodesFactory.InvertAsciiCaseNodeGen
                    .create(InvertAsciiCaseHelperNode.createUpperToLower());
        }

        public static InvertAsciiCaseNode createSwapCase() {
            return StringHelperNodesFactory.InvertAsciiCaseNodeGen.create(InvertAsciiCaseHelperNode.createSwapCase());
        }

        public InvertAsciiCaseNode(InvertAsciiCaseHelperNode invertNode) {
            this.invertNode = invertNode;
        }

        public abstract Object executeInvert(RubyString string);

        @Specialization
        protected Object invert(RubyString string,
                @Cached RubyStringLibrary libString,
                @Cached TruffleString.CreateCodePointIteratorNode createCodePointIteratorNode,
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode,
                @Cached ConditionProfile noopProfile) {
            var tencoding = libString.getTEncoding(string);
            var iterator = createCodePointIteratorNode.execute(string.tstring, tencoding,
                    ErrorHandling.RETURN_NEGATIVE);
            byte[] modified = invertNode.executeInvert(string, iterator, null);

            if (noopProfile.profile(modified == null)) {
                return nil;
            } else {
                string.setTString(fromByteArrayNode.execute(modified, tencoding, false)); // codeRangeNode.execute(rope), codePointLengthNode.execute(rope)
                return string;
            }
        }

    }

    @ImportStatic(StringGuards.class)
    public abstract static class GetCodePointNode extends RubyBaseNode {

        public static GetCodePointNode create() {
            return StringHelperNodesFactory.GetCodePointNodeGen.create();
        }

        public abstract int executeGetCodePoint(AbstractTruffleString string, RubyEncoding encoding, int byteIndex);

        @Specialization
        protected int getCodePoint(AbstractTruffleString string, RubyEncoding encoding, int byteIndex,
                @Cached TruffleString.CodePointAtByteIndexNode getCodePointNode,
                @Cached BranchProfile badCodePointProfile) {
            int codePoint = getCodePointNode.execute(string, byteIndex, encoding.tencoding,
                    ErrorHandling.RETURN_NEGATIVE);
            if (codePoint == -1) {
                badCodePointProfile.enter();
                throw new RaiseException(getContext(),
                        coreExceptions().argumentErrorInvalidByteSequence(encoding, this));
            }
            return codePoint;
        }

    }

    public abstract static class StringAppendNode extends RubyBaseNode {

        public static StringAppendNode create() {
            return StringHelperNodesFactory.StringAppendNodeGen.create();
        }

        public abstract RubyString executeStringAppend(Object string, Object other);

        @Specialization(guards = "libOther.isRubyString(other)", limit = "1")
        protected RubyString stringAppend(Object string, Object other,
                @Cached RubyStringLibrary libString,
                @Cached RubyStringLibrary libOther,
                @Cached EncodingNodes.CheckStringEncodingNode checkEncodingNode,
                @Cached TruffleString.ConcatNode concatNode) {

            var left = libString.getTString(string);
            var leftEncoding = libString.getEncoding(string);
            var right = libOther.getTString(other);
            var rightEncoding = libOther.getEncoding(other);

            final RubyEncoding compatibleEncoding = checkEncodingNode.executeCheckEncoding(left, leftEncoding,
                    right, rightEncoding);

            var result = concatNode.execute(left, right, compatibleEncoding.tencoding, true);
            return createString(result, compatibleEncoding);
        }
    }

}
