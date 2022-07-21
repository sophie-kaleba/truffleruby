/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.yield;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.RubyContext;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.arguments.ArgumentsDescriptor;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.LiteralCallNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.methods.DeclarationContext;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import org.truffleruby.language.methods.InternalMethod;

@ReportPolymorphism
@GenerateUncached
public abstract class CallBlockNode extends RubyBaseNode {

    public static CallBlockNode create() {
        return CallBlockNodeGen.create();
    }

    public static CallBlockNode getUncached() {
        return CallBlockNodeGen.getUncached();
    }

    public final Object yield(RubyProc block, ArgumentsDescriptor descriptor, Object[] args,
            LiteralCallNode literalCallNode) {
        return executeCallBlock(block.declarationContext, block, ProcOperations.getSelf(block), nil, descriptor, args,
                literalCallNode);
    }

    public final Object yield(RubyProc block, Object... args) {
        return executeCallBlock(block.declarationContext, block, ProcOperations.getSelf(block), nil,
                EmptyArgumentsDescriptor.INSTANCE, args, null);
    }

    public String getSourceSectionAbbrv(SourceSection source) {
        String result = "NA";

        result = source.getSource().getPath() + ":" + source.getStartLine() + ":"
                        + source.getStartColumn() + ":" + source.getCharLength();

        return result;
    }

    private void logCalls(String method, RubyProc block, CallTarget currentCallTarget) {
        if (RubyContext.monitorCalls) {
            int ctaddress = block.callTarget.getTargetID();
            String receiver = block.callTarget.toString()+"@"+ctaddress;
            // "Symbol", "Original.Receiver", "Source.Section", "CT.Address", "Builtin?", "Observed.Receiver"
            getContext().logger.info(method+"yield|call|[]" + "\t" + receiver + "\t" + this.hashCode() + "\t" + currentCallTarget.getTargetID() + "\t" + block.type + "\t" + receiver);
        }
    }

    /** {@code literalCallNode} is only non-null if this was called splatted with a ruby2_keyword Hash. */
    public abstract Object executeCallBlock(DeclarationContext declarationContext, RubyProc block, Object self,
            Object blockArgument, ArgumentsDescriptor descriptor, Object[] arguments, LiteralCallNode literalCallNode);

    @Specialization(guards = "block.callTarget == cachedCallTarget", limit = "getCacheLimit()")
    protected Object callBlockCached(
            DeclarationContext declarationContext,
            RubyProc block,
            Object self,
            Object blockArgument,
            ArgumentsDescriptor descriptor,
            Object[] arguments,
            LiteralCallNode literalCallNode,
            @Cached("block.callTarget") RootCallTarget cachedCallTarget,
            @Cached("createBlockCallNode(cachedCallTarget)") DirectCallNode callNode) {
        if (literalCallNode != null) {
            literalCallNode.copyRuby2KeywordsHash(arguments, RubyRootNode.of(cachedCallTarget).getSharedMethodInfo());
        }

        final Object[] frameArguments = packArguments(declarationContext, block, self, blockArgument, descriptor,
                arguments);
        try {
            return callNode.call(frameArguments);
        }
        finally {
            logCalls("CACHED", block, callNode.getCurrentCallTarget());
        }
    }

    @Specialization(replaces = "callBlockCached")
    protected Object callBlockUncached(
            DeclarationContext declarationContext,
            RubyProc block,
            Object self,
            Object blockArgument,
            ArgumentsDescriptor descriptor,
            Object[] arguments,
            LiteralCallNode literalCallNode,
            @Cached IndirectCallNode callNode) {
        if (literalCallNode != null) {
            literalCallNode.copyRuby2KeywordsHash(arguments, block.getSharedMethodInfo());
        }

        final Object[] frameArguments = packArguments(declarationContext, block, self, blockArgument, descriptor,
                arguments);
        try {
            return callNode.call(block.callTarget, frameArguments);
        }
        finally {
            logCalls("UNCACHED", block, block.callTarget);
        }
    }

    private Object[] packArguments(DeclarationContext declarationContext, RubyProc block, Object self,
            Object blockArgument, ArgumentsDescriptor descriptor, Object[] arguments) {
        return RubyArguments.pack(
                block.declarationFrame,
                null,
                block.method,
                declarationContext,
                block.frameOnStackMarker,
                self,
                blockArgument,
                descriptor,
                arguments);
    }

    protected DirectCallNode createBlockCallNode(RootCallTarget callTarget) {
        final DirectCallNode callNode = Truffle.getRuntime().createDirectCallNode(callTarget);

        final boolean clone = RubyRootNode.of(callTarget).shouldAlwaysClone() ||
                getContext().getOptions().YIELD_ALWAYS_CLONE;
        if (clone && callNode.isCallTargetCloningAllowed()) {
            callNode.cloneCallTarget();
        }

        if (getContext().getOptions().YIELD_ALWAYS_INLINE && callNode.isInlinable()) {
            callNode.forceInlining();
        }

        return callNode;
    }

    protected int getCacheLimit() {
        return getLanguage().options.YIELD_CACHE;
    }

}
