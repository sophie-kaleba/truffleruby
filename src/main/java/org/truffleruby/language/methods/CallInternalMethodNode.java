/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.HostCompilerDirectives.InliningCutoff;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.nodes.EncapsulatingNodeReference;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethodNodeManager;
import org.truffleruby.core.inlined.AlwaysInlinedMethodNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import org.truffleruby.language.RubyCheckArityRootNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.dispatch.LiteralCallNode;

@ReportPolymorphism
@GenerateUncached
@ImportStatic(RubyArguments.class)
public abstract class CallInternalMethodNode extends RubyBaseNode {

    @NeverDefault
    public static CallInternalMethodNode create() {
        return CallInternalMethodNodeGen.create();
    }

//    /** Callers should use {@link RubyArguments#assertFrameArguments} unless they use {@code RubyArguments#pack}.
//     * {@code literalCallNode} is only non-null if this was called splatted with a ruby2_keyword Hash. */
//    public abstract Object execute(Frame frame, InternalMethod method, Object receiver, Object[] rubyArgs,
//            LiteralCallNode literalCallNode);

    public abstract Object execute(Frame frame, InternalMethod method, Object receiver, Object[] rubyArgs,
                                   LiteralCallNode literalCallNode, DispatchNode dispatchNode, RubyClass metaClass, long contextSignature);

    public String getSourceSectionAbbrv(DispatchNode dispatchNode) {
        String result = "NA";

        if (dispatchNode != null) {
            SourceSection source = dispatchNode.getParentSourceSection();
            if (source != null) {
                result = source.getSource().getPath() + ":" + source.getStartLine() + ":"
                        + source.getStartColumn() + ":" + source.getCharLength();
            }
        }

        return result;
    }

    private void logCalls(InternalMethod method, DispatchNode dispatchNode, RubyClass metaclass, CallTarget currentCallTarget) {
        if (getContext().monitorCalls) {
            // "Symbol", "Original.Receiver", "Source.Section", "CT.Address", "Builtin?", "Observed.Receiver"
            getContext().logger.info(getContext().stage + "\t" + method.getName() + "\t" + (metaclass == null ? "NA" : metaclass.getMetaSimpleName()) + "\t" + getSourceSectionAbbrv(dispatchNode) + "\t" + currentCallTarget.getTargetID() + "\t" + method.isBuiltIn() + "\t" + method.getDeclaringModule().getName());
        }
    }

    @Specialization(
            guards = {
                    "isSingleContext()",
                    "method.getCallTarget() == cachedCallTarget",
                    "!cachedMethod.alwaysInlined()" },
            assumptions = "getMethodAssumption(cachedMethod)", // to remove the inline cache entry when the method is redefined or removed
            limit = "getCacheLimit()")
    protected Object callCached(
            InternalMethod method, Object receiver, Object[] rubyArgs, LiteralCallNode literalCallNode, DispatchNode dispatchNode, RubyClass metaclass, long contextSignature,
            @Cached("method.getCallTarget()") RootCallTarget cachedCallTarget,
            @Cached("method") InternalMethod cachedMethod,
            @Cached("createCall(cachedMethod.getName(), cachedCallTarget)") DirectCallNode callNode) {
        if (literalCallNode != null) {
            literalCallNode.copyRuby2KeywordsHash(rubyArgs, cachedMethod.getSharedMethodInfo());
        }

        try {
            cachedCallTarget.setContextSignature(contextSignature);
            return callNode.call(RubyArguments.repackForCall(rubyArgs));
        }
        finally {
            logCalls(method, dispatchNode, metaclass, callNode.getCurrentCallTarget());
        }
    }

    @InliningCutoff
    @Specialization(guards = "!method.alwaysInlined()", replaces = "callCached")
    protected Object callUncached(
            InternalMethod method, Object receiver, Object[] rubyArgs, LiteralCallNode literalCallNode, DispatchNode dispatchNode, RubyClass metaclass, long contextSignature,
            @Cached IndirectCallNode indirectCallNode) {
        if (literalCallNode != null) {
            literalCallNode.copyRuby2KeywordsHash(rubyArgs, method.getSharedMethodInfo());
        }

        try {
            method.getCallTarget().setContextSignature(contextSignature);
            return indirectCallNode.call(method.getCallTarget(), RubyArguments.repackForCall(rubyArgs));
        }
        finally {
            logCalls(method, dispatchNode, metaclass, method.getCallTarget());
        }
    }

    @Specialization(
            guards = {
                    "isSingleContext()",
                    "method.getCallTarget() == cachedCallTarget",
                    "cachedMethod.alwaysInlined()" },
            assumptions = "getMethodAssumption(cachedMethod)", // to remove the inline cache entry when the method is redefined or removed
            limit = "getCacheLimit()")
    protected Object alwaysInlined(
            Frame frame, InternalMethod method, Object receiver, Object[] rubyArgs, LiteralCallNode literalCallNode, DispatchNode dispatchNode, RubyClass metaclass, long contextSignature,
            @Cached("method.getCallTarget()") RootCallTarget cachedCallTarget,
            @Cached("method") InternalMethod cachedMethod,
            @Cached("createAlwaysInlinedMethodNode(cachedMethod)") AlwaysInlinedMethodNode alwaysInlinedNode,
            @Cached("cachedMethod.getSharedMethodInfo().getArity()") Arity cachedArity,
            @Cached BranchProfile checkArityProfile,
            @Cached BranchProfile exceptionProfile) {
        assert !cachedArity.acceptsKeywords()
                : "AlwaysInlinedMethodNodes are currently assumed to not use keyword arguments, the arity check depends on this";
        assert RubyArguments.getSelf(rubyArgs) == receiver;

        if (literalCallNode != null) {
            literalCallNode.copyRuby2KeywordsHash(rubyArgs, cachedMethod.getSharedMethodInfo());
        }

        try {
            int given = RubyArguments.getPositionalArgumentsCount(rubyArgs, false);
            if (!cachedArity.checkPositionalArguments(given)) {
                checkArityProfile.enter();
                throw RubyCheckArityRootNode.checkArityError(cachedArity, given, alwaysInlinedNode);
            }

            logCalls(method, dispatchNode, metaclass, method.getCallTarget());
            return alwaysInlinedNode.execute(frame, receiver, RubyArguments.repackForCall(rubyArgs), cachedCallTarget);
        } catch (RaiseException e) {
            exceptionProfile.enter();
            return alwaysInlinedException(e, alwaysInlinedNode, cachedCallTarget);
        }
    }

    @InliningCutoff
    private Object alwaysInlinedException(RaiseException e, AlwaysInlinedMethodNode alwaysInlinedNode,
            RootCallTarget cachedCallTarget) {
        final Node location = e.getLocation();
        if (location != null && location.getRootNode() == alwaysInlinedNode.getRootNode()) {
            // if the error originates from the inlined node, rethrow it through the CallTarget to get a proper backtrace
            return RubyContext.indirectCallWithCallNode(this, cachedCallTarget, e);
        } else {
            throw e;
        }
    }

    @Specialization(guards = "method.alwaysInlined()", replaces = "alwaysInlined")
    protected Object alwaysInlinedUncached(
            Frame frame, InternalMethod method, Object receiver, Object[] rubyArgs, LiteralCallNode literalCallNode, DispatchNode dispatchNode, RubyClass metaclass, long contextSignature) {
        return alwaysInlinedBoundary(
                frame == null ? null : frame.materialize(),
                method,
                receiver,
                rubyArgs,
                literalCallNode,
                isAdoptable(),
                dispatchNode,
                metaclass);
    }

    @TruffleBoundary // getUncachedAlwaysInlinedMethodNode(method) and arity are not PE constants
    private Object alwaysInlinedBoundary(
            MaterializedFrame frame, InternalMethod method, Object receiver, Object[] rubyArgs,
            LiteralCallNode literalCallNode, boolean cachedToUncached, DispatchNode dispatchNode, RubyClass metaclass) {
        EncapsulatingNodeReference encapsulating = null;
        Node prev = null;
        if (cachedToUncached) {
            encapsulating = EncapsulatingNodeReference.getCurrent();
            prev = encapsulating.set(this);
        }
        try {
            long contextSignature = -66;
            return alwaysInlined(
                    frame,
                    method,
                    receiver,
                    rubyArgs,
                    literalCallNode,
                    dispatchNode,
                    metaclass,
                    contextSignature,
                    method.getCallTarget(),
                    method,
                    getUncachedAlwaysInlinedMethodNode(method),
                    method.getSharedMethodInfo().getArity(),
                    BranchProfile.getUncached(),
                    BranchProfile.getUncached());
        } finally {
            if (cachedToUncached) {
                encapsulating.set(prev);
            }
        }
    }

    protected AlwaysInlinedMethodNode createAlwaysInlinedMethodNode(InternalMethod method) {
        return (AlwaysInlinedMethodNode) CoreMethodNodeManager
                .createNodeFromFactory(method.alwaysInlinedNodeFactory, RubyNode.EMPTY_ARRAY);
    }

    /** Asserted in {@link CoreMethodNodeManager#createCoreMethodCallTarget} */
    protected AlwaysInlinedMethodNode getUncachedAlwaysInlinedMethodNode(InternalMethod method) {
        return (AlwaysInlinedMethodNode) method.alwaysInlinedNodeFactory.getUncachedInstance();
    }

    protected Assumption getMethodAssumption(InternalMethod method) {
        return isSingleContext()
                ? method.getDeclaringModule().fields.getOrCreateMethodAssumption(method.getName())
                : Assumption.ALWAYS_VALID;
    }

    protected int getCacheLimit() {
        return getLanguage().options.DISPATCH_CACHE;
    }

    protected DirectCallNode createCall(String methodName, RootCallTarget callTarget) {
        final DirectCallNode callNode = DirectCallNode.create(callTarget);
        final DispatchNode dispatch = NodeUtil.findParent(this, DispatchNode.class);
        if (dispatch != null) {
            dispatch.applySplittingInliningStrategy(callTarget, methodName, callNode);
        }
        return callNode;
    }
}
