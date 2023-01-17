/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.kernel;

import java.io.IOException;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import org.graalvm.collections.Pair;
import org.truffleruby.Layouts;
import org.truffleruby.annotations.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.annotations.CoreModule;
import org.truffleruby.annotations.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.kernel.TruffleKernelNodesFactory.GetSpecialVariableStorageNodeGen;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.string.TStringWithEncoding;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.arguments.ReadCallerVariablesIfAvailableNode;
import org.truffleruby.language.arguments.ReadCallerVariablesNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.globals.ReadSimpleGlobalVariableNode;
import org.truffleruby.language.globals.WriteSimpleGlobalVariableNode;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.loader.FileLoader;
import org.truffleruby.language.locals.FindDeclarationVariableNodes;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.annotations.Split;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreModule("Truffle::KernelOperations")
public abstract class TruffleKernelNodes {

    @CoreMethod(names = "at_exit", onSingleton = true, needsBlock = true, required = 1, split = Split.NEVER)
    public abstract static class AtExitSystemNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object atExit(boolean always, RubyProc block) {
            getContext().getAtExitManager().add(block, always);
            return nil;
        }
    }

    @Primitive(name = "kernel_load")
    public abstract static class LoadNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(file)", limit = "1")
        protected boolean load(Object file, Nil wrapModule,
                @Cached RubyStringLibrary strings,
                @Cached IndirectCallNode callNode) {
            final String feature = RubyGuards.getJavaString(file);
            final Pair<Source, TStringWithEncoding> sourceRopePair = getSourceRopePair(feature);

            final DeclarationContext declarationContext = DeclarationContext.topLevel(getContext());
            final LexicalScope lexicalScope = getContext().getRootLexicalScope();
            final Object self = getContext().getCoreLibrary().mainObject;
            final RootCallTarget callTarget = getContext().getCodeLoader().parseTopLevelWithCache(sourceRopePair, this);

            final CodeLoader.DeferredCall deferredCall = getContext().getCodeLoader().prepareExecute(
                    callTarget,
                    ParserContext.TOP_LEVEL,
                    declarationContext,
                    null,
                    self,
                    lexicalScope);

            deferredCall.call(callNode);

            return true;
        }

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(file)", limit = "1")
        protected boolean load(Object file, RubyModule wrapModule,
                @Cached RubyStringLibrary strings,
                @Cached IndirectCallNode callNode) {
            final String feature = RubyGuards.getJavaString(file);
            final Pair<Source, TStringWithEncoding> sourceRopePair = getSourceRopePair(feature);

            final DeclarationContext declarationContext = DeclarationContext.topLevel(wrapModule);
            final LexicalScope lexicalScope = new LexicalScope(getContext().getRootLexicalScope(), wrapModule);

            // self
            final RubyBasicObject mainObject = getContext().getCoreLibrary().mainObject;
            final Object self = DispatchNode.getUncached().call(mainObject, "clone");
            DispatchNode.getUncached().call(self, "extend", wrapModule);

            // callTarget
            final RubySource rubySource = new RubySource(
                    sourceRopePair.getLeft(),
                    feature,
                    sourceRopePair.getRight());
            final RootCallTarget callTarget = getContext()
                    .getCodeLoader()
                    .parse(rubySource, ParserContext.TOP_LEVEL, null, lexicalScope, this);

            final CodeLoader.DeferredCall deferredCall = getContext().getCodeLoader().prepareExecute(
                    callTarget,
                    ParserContext.TOP_LEVEL,
                    declarationContext,
                    null,
                    self,
                    lexicalScope);

            deferredCall.call(callNode);

            return true;
        }

        private Pair<Source, TStringWithEncoding> getSourceRopePair(String feature) {
            try {
                final FileLoader fileLoader = new FileLoader(getContext(), getLanguage());
                return fileLoader.loadFile(feature);
            } catch (IOException e) {
                throw new RaiseException(getContext(), coreExceptions().loadErrorCannotLoad(feature, this));
            }
        }

    }

    // Only used internally with a constant literal name, does not trigger hooks
    @Primitive(name = "global_variable_set")
    @ImportStatic(Layouts.class)
    public abstract static class WriteGlobalVariableNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "name == cachedName", limit = "1")
        protected Object write(RubySymbol name, Object value,
                @Cached("name") RubySymbol cachedName,
                @Cached("create(cachedName.getString())") WriteSimpleGlobalVariableNode writeNode) {
            return writeNode.execute(value);
        }
    }

    // Only used internally with a constant literal name, does not trigger hooks
    @Primitive(name = "global_variable_get")
    @ImportStatic(Layouts.class)
    public abstract static class ReadGlobalVariableNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "name == cachedName", limit = "1")
        protected Object read(RubySymbol name,
                @Cached("name") RubySymbol cachedName,
                @Cached("create(cachedName.getString())") ReadSimpleGlobalVariableNode readNode) {
            return readNode.execute();
        }
    }

    @CoreMethod(names = "define_hooked_variable_with_is_defined", onSingleton = true, required = 4)
    public abstract static class DefineHookedVariableInnerNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object defineHookedVariableInnerNode(
                RubySymbol name, RubyProc getter, RubyProc setter, RubyProc isDefined) {
            getContext().getCoreLibrary().globalVariables.define(
                    name.getString(),
                    getter,
                    setter,
                    isDefined,
                    this);
            return nil;
        }

    }

    public static int declarationDepth(Frame topFrame) {
        MaterializedFrame frame = topFrame.materialize();
        MaterializedFrame nextFrame;
        int count = 0;

        while ((nextFrame = RubyArguments.getDeclarationFrame(frame)) != null) {
            frame = nextFrame;
            count++;
        }
        return count;
    }

    @ImportStatic(TruffleKernelNodes.class)
    public abstract static class GetSpecialVariableStorage extends RubyBaseNode {

        @NeverDefault
        public static GetSpecialVariableStorage create() {
            return GetSpecialVariableStorageNodeGen.create();
        }

        public abstract SpecialVariableStorage execute(Frame frame);

        @Specialization(guards = "frame.getFrameDescriptor() == descriptor", limit = "1")
        protected SpecialVariableStorage getFromKnownFrameDescriptor(Frame frame,
                @Cached(value = "frame.getFrameDescriptor()", neverDefault = true) FrameDescriptor descriptor,
                @Cached(value = "declarationDepth(frame)", neverDefault = false) int declarationFrameDepth) {
            Object variables;
            if (declarationFrameDepth == 0) {
                variables = SpecialVariableStorage.get(frame);
                if (variables == nil) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    variables = new SpecialVariableStorage();
                    SpecialVariableStorage.set(frame, (SpecialVariableStorage) variables);
                    SpecialVariableStorage.getAssumption(frame.getFrameDescriptor()).invalidate();
                }
            } else {
                Frame storageFrame = RubyArguments.getDeclarationFrame(frame, declarationFrameDepth);

                if (storageFrame == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    int depth = 0;
                    MaterializedFrame currentFrame = RubyArguments.getDeclarationFrame(frame);
                    while (currentFrame != null) {
                        depth += 1;
                        currentFrame = RubyArguments.getDeclarationFrame(currentFrame);
                    }

                    String message = String.format(
                            "Expected %d declaration frames but only found %d frames.",
                            declarationFrameDepth,
                            depth);
                    throw CompilerDirectives.shouldNotReachHere(message);
                }

                variables = SpecialVariableStorage.get(storageFrame);
                if (variables == nil) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    variables = new SpecialVariableStorage();
                    SpecialVariableStorage.set(storageFrame, (SpecialVariableStorage) variables);
                    SpecialVariableStorage.getAssumption(storageFrame.getFrameDescriptor()).invalidate();
                }
            }
            return (SpecialVariableStorage) variables;
        }

        @Specialization(replaces = "getFromKnownFrameDescriptor")
        protected SpecialVariableStorage slowPath(Frame frame) {
            return getSlow(frame.materialize());
        }

        @TruffleBoundary
        public static SpecialVariableStorage getSlow(MaterializedFrame aFrame) {
            MaterializedFrame frame = FindDeclarationVariableNodes.getOuterDeclarationFrame(aFrame);
            Object variables = SpecialVariableStorage.get(frame);
            if (variables == Nil.INSTANCE) {
                variables = new SpecialVariableStorage();
                SpecialVariableStorage.set(frame, (SpecialVariableStorage) variables);
                SpecialVariableStorage.getAssumption(frame.getFrameDescriptor()).invalidate();
            }
            return (SpecialVariableStorage) variables;
        }

    }

    @Primitive(name = "caller_special_variables")
    public abstract static class GetCallerSpecialVariableStorage extends PrimitiveArrayArgumentsNode {

        @Child ReadCallerVariablesNode callerVariablesNode = new ReadCallerVariablesNode();

        @Specialization
        protected Object storage(VirtualFrame frame) {
            return callerVariablesNode.execute(frame);
        }
    }

    @Primitive(name = "caller_special_variables_if_available")
    public abstract static class GetCallerSpecialVariableStorageIfFast extends PrimitiveArrayArgumentsNode {

        @Child ReadCallerVariablesIfAvailableNode callerVariablesNode = new ReadCallerVariablesIfAvailableNode();

        @Specialization
        protected Object storage(VirtualFrame frame,
                @Cached ConditionProfile nullProfile) {
            Object variables = callerVariablesNode.execute(frame);
            if (nullProfile.profile(variables == null)) {
                return nil;
            } else {
                return variables;
            }
        }
    }

    @Primitive(name = "get_original_require")
    @ImportStatic(Layouts.class)
    public abstract static class GetOriginalRequireNode extends PrimitiveArrayArgumentsNode {

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @TruffleBoundary
        @Specialization
        protected Object getOriginalRequire(Object string,
                @Cached RubyStringLibrary strings) {
            final String originalRequire = getContext()
                    .getCoreLibrary()
                    .getOriginalRequires()
                    .get(RubyGuards.getJavaString(string));
            if (originalRequire == null) {
                return Nil.INSTANCE;
            } else {
                return createString(fromJavaStringNode, originalRequire, Encodings.UTF_8);
            }
        }
    }

    @Primitive(name = "proc_special_variables")
    public abstract static class GetProcSpecialVariableStorage extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object variables(RubyProc proc) {
            return proc.declarationVariables;
        }
    }

    @Primitive(name = "share_special_variables")
    @ImportStatic(TruffleKernelNodes.class)
    public abstract static class ShareSpecialVariableStorage extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "frame.getFrameDescriptor() == descriptor", limit = "1")
        protected Object shareSpecialVariable(VirtualFrame frame, SpecialVariableStorage storage,
                @Cached(value = "frame.getFrameDescriptor()", neverDefault = true) FrameDescriptor descriptor,
                @Cached(value = "declarationDepth(frame)", neverDefault = false) int declarationFrameDepth) {
            final Frame storageFrame = RubyArguments.getDeclarationFrame(frame, declarationFrameDepth);
            SpecialVariableStorage.set(storageFrame, storage);
            return nil;
        }

        @Specialization(replaces = "shareSpecialVariable")
        protected Object slowPath(VirtualFrame frame, SpecialVariableStorage storage) {
            return shareSlow(frame.materialize(), storage);
        }

        @TruffleBoundary
        public Object shareSlow(MaterializedFrame aFrame, SpecialVariableStorage storage) {
            MaterializedFrame frame = FindDeclarationVariableNodes.getOuterDeclarationFrame(aFrame);
            SpecialVariableStorage.set(frame, storage);
            // TODO: should invalidate here?
            return nil;
        }

        public static GetSpecialVariableStorage create() {
            return GetSpecialVariableStorageNodeGen.create();
        }
    }

    @Primitive(name = "regexp_last_match_set")
    public abstract static class SetRegexpMatch extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object setRegexpMatch(SpecialVariableStorage variables, Object lastMatch,
                @Cached ConditionProfile unsetProfile,
                @Cached ConditionProfile sameThreadProfile) {
            variables.setLastMatch(lastMatch, getContext(), unsetProfile, sameThreadProfile);
            return lastMatch;
        }
    }

    @Primitive(name = "regexp_last_match_get")
    public abstract static class GetRegexpMatch extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object getRegexpMatch(SpecialVariableStorage variables,
                @Cached ConditionProfile unsetProfile,
                @Cached ConditionProfile sameThreadProfile) {
            return variables.getLastMatch(unsetProfile, sameThreadProfile);
        }
    }

    @Primitive(name = "io_last_line_set")
    public abstract static class SetLastIO extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object setRegexpMatch(SpecialVariableStorage variables, Object lastIO,
                @Cached ConditionProfile unsetProfile,
                @Cached ConditionProfile sameThreadProfile) {
            variables.setLastLine(lastIO, getContext(), unsetProfile, sameThreadProfile);
            return lastIO;
        }
    }

    @Primitive(name = "io_last_line_get")
    public abstract static class GetLastIO extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object getLastIO(SpecialVariableStorage storage,
                @Cached ConditionProfile unsetProfile,
                @Cached ConditionProfile sameThreadProfile) {
            return storage.getLastLine(unsetProfile, sameThreadProfile);
        }
    }
}
