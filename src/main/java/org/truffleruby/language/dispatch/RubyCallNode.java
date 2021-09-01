/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.dispatch;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayAppendOneNode;
import org.truffleruby.core.array.ArrayToObjectArrayNode;
import org.truffleruby.core.array.ArrayToObjectArrayNodeGen;
import org.truffleruby.core.array.AssignableNode;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.core.inlined.LambdaToProcNode;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.literal.NilLiteralNode;
import org.truffleruby.language.methods.BlockDefinitionNode;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

import java.util.Map;

import static org.truffleruby.language.dispatch.DispatchConfiguration.PRIVATE;
import static org.truffleruby.language.dispatch.DispatchConfiguration.PRIVATE_RETURN_MISSING;
import static org.truffleruby.language.dispatch.DispatchConfiguration.PROTECTED;

public class RubyCallNode extends RubyContextSourceNode implements AssignableNode {

    protected final String methodName;

    @Child private RubyNode receiver;
    @Child private RubyNode block;
    private final boolean hasLiteralBlock;
    @Children private final RubyNode[] arguments;

    private final boolean isSplatted;
    protected final DispatchConfiguration dispatchConfig;
    private final boolean isVCall;
    private final boolean isSafeNavigation;
    protected final boolean isAttrAssign;

    @Child private DispatchNode dispatch;
    @Child private ArrayToObjectArrayNode toObjectArrayNode;
    @Child private DefinedNode definedNode;

    private final ConditionProfile nilProfile;

    public RubyCallNode(RubyCallNodeParameters parameters) {
        this.methodName = parameters.getMethodName();
        this.receiver = parameters.getReceiver();
        this.arguments = parameters.getArguments();

        final RubyNode block = parameters.getBlock();
        this.block = parameters.getBlock();
        this.hasLiteralBlock = block instanceof BlockDefinitionNode || block instanceof LambdaToProcNode;

        this.isSplatted = parameters.isSplatted();
        this.dispatchConfig = parameters.isIgnoreVisibility() ? PRIVATE : PROTECTED;
        this.isVCall = parameters.isVCall();
        this.isSafeNavigation = parameters.isSafeNavigation();
        this.isAttrAssign = parameters.isAttrAssign();

        if (parameters.isSafeNavigation()) {
            nilProfile = ConditionProfile.createCountingProfile();
        } else {
            nilProfile = null;
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object receiverObject = receiver.execute(frame);
        if (isSafeNavigation && nilProfile.profile(receiverObject == nil)) {
            return nil;
        }

        final Object[] executedArguments = executeArguments(frame);

        final Object blockObject = executeBlock(frame);

        // The expansion of the splat is done after executing the block, for m(*args, &args.pop)
        final Object[] argumentsObjects = isSplatted ? splat(executedArguments) : executedArguments;

        return executeWithArgumentsEvaluated(frame, receiverObject, blockObject, argumentsObjects);
    }

    @Override
    public void assign(VirtualFrame frame, Object value) {
        assert (getLastArgumentNode() instanceof NilLiteralNode &&
                ((NilLiteralNode) getLastArgumentNode()).isImplicit()) : getLastArgumentNode();

        final Object receiverObject = receiver.execute(frame);
        if (isSafeNavigation && nilProfile.profile(receiverObject == nil)) {
            return;
        }

        final Object[] executedArguments = executeArguments(frame);

        final Object blockObject = executeBlock(frame);

        final Object[] argumentsObjects;
        if (isSplatted) {
            // The expansion of the splat is done after executing the block, for m(*args, &args.pop)
            argumentsObjects = splat(executedArguments);
            assert argumentsObjects[argumentsObjects.length - 1] == nil;
            argumentsObjects[argumentsObjects.length - 1] = value;
        } else {
            assert executedArguments[arguments.length - 1] == nil;
            executedArguments[arguments.length - 1] = value;
            argumentsObjects = executedArguments;
        }

        executeWithArgumentsEvaluated(frame, receiverObject, blockObject, argumentsObjects);
    }

    public Object executeWithArgumentsEvaluated(VirtualFrame frame, Object receiverObject, Object blockObject,
            Object[] argumentsObjects) {

        if (dispatch == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            dispatch = insert(DispatchNode.create(dispatchConfig));
        }
        Object returnValue = dispatch.dispatch(frame, receiverObject, methodName, blockObject, argumentsObjects);

        if (isAttrAssign) {
            assert argumentsObjects[argumentsObjects.length - 1] != null;
            return argumentsObjects[argumentsObjects.length - 1];
        } else {
            assert returnValue != null;
            return returnValue;
        }
    }

    private Object executeBlock(VirtualFrame frame) {
        if (block != null) {
            return block.execute(frame);
        } else {
            return nil;
        }
    }

    @ExplodeLoop
    private Object[] executeArguments(VirtualFrame frame) {
        final Object[] argumentsObjects = new Object[arguments.length];

        for (int i = 0; i < arguments.length; i++) {
            argumentsObjects[i] = arguments[i].execute(frame);
        }

        return argumentsObjects;
    }

    private Object[] splat(Object[] arguments) {
        if (toObjectArrayNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toObjectArrayNode = insert(ArrayToObjectArrayNodeGen.create());
        }
        // TODO(CS): what happens if it isn't an Array?
        return toObjectArrayNode.unsplat(arguments);
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        if (definedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            definedNode = insert(new DefinedNode());
        }

        return definedNode.isDefined(frame, context);
    }

    public String getName() {
        return methodName;
    }

    public boolean isVCall() {
        return isVCall;
    }

    public boolean hasLiteralBlock() {
        return hasLiteralBlock;
    }

    private RubyNode getLastArgumentNode() {
        final RubyNode lastArg = arguments[arguments.length - 1];
        if (isSplatted && lastArg instanceof ArrayAppendOneNode) {
            return ((ArrayAppendOneNode) lastArg).getValueNode();
        }
        return lastArg;
    }

    @Override
    public AssignableNode toAssignableNode() {
        return this;
    }

    @Override
    public Map<String, Object> getDebugProperties() {
        final Map<String, Object> map = super.getDebugProperties();
        map.put("methodName", methodName);
        return map;
    }

    private class DefinedNode extends RubyBaseNode {

        private final RubySymbol methodNameSymbol = getSymbol(methodName);

        @Child private DispatchNode respondToMissing = DispatchNode.create(PRIVATE_RETURN_MISSING);
        @Child private BooleanCastNode respondToMissingCast = BooleanCastNodeGen.create(null);


        @Child private LookupMethodOnSelfNode lookupMethodNode = LookupMethodOnSelfNode.create();

        private final ConditionProfile receiverDefinedProfile = ConditionProfile.create();
        private final BranchProfile argumentNotDefinedProfile = BranchProfile.create();
        private final BranchProfile allArgumentsDefinedProfile = BranchProfile.create();
        private final BranchProfile receiverExceptionProfile = BranchProfile.create();
        private final ConditionProfile methodNotFoundProfile = ConditionProfile.create();

        @ExplodeLoop
        public Object isDefined(VirtualFrame frame, RubyContext context) {
            if (receiverDefinedProfile.profile(receiver.isDefined(frame, getLanguage(), context) == nil)) {
                return nil;
            }

            for (RubyNode argument : arguments) {
                if (argument.isDefined(frame, getLanguage(), context) == nil) {
                    argumentNotDefinedProfile.enter();
                    return nil;
                }
            }

            allArgumentsDefinedProfile.enter();

            final Object receiverObject;

            try {
                receiverObject = receiver.execute(frame);
            } catch (Exception e) {
                receiverExceptionProfile.enter();
                return nil;
            }

            final InternalMethod method = lookupMethodNode.execute(frame, receiverObject, methodName, dispatchConfig);

            if (methodNotFoundProfile.profile(method == null)) {
                final Object r = respondToMissing.call(receiverObject, "respond_to_missing?", methodNameSymbol, false);

                if (r != DispatchNode.MISSING && !respondToMissingCast.executeToBoolean(r)) {
                    return nil;
                }
            }

            return coreStrings().METHOD.createInstance(context);
        }
    }

    public static final class RubyPhaseCallNode extends RubyCallNode {

        @Child private DispatchNode dispatch0;
        @Child private DispatchNode dispatch1;

        public RubyPhaseCallNode(RubyCallNodeParameters parameters) {
            super(parameters);
        }

        @Override
        public Object executeWithArgumentsEvaluated(VirtualFrame frame, Object receiverObject, Object blockObject,
                                                    Object[] argumentsObjects) {
            Object returnValue = null;
            String ss = (this.getSourceSection() != null) ? this.getSourceSection().toString() : "NULL";
            if (dispatch0 == null && getContext().phaseID == 0) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                dispatch0 = insert(DispatchNode.create(dispatchConfig));
                getContext().logger.info("[Phase 0] Initializing dispatch node for "+methodName+". @: "+ss);
            }
            else if (dispatch1 == null && getContext().phaseID == 1) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                dispatch1 = insert(DispatchNode.create(dispatchConfig));
            }

            if (getContext().phaseID == 0) {
                returnValue = dispatch0.dispatch(frame, receiverObject, methodName, blockObject, argumentsObjects);
            }
            else if (getContext().phaseID == 1) {
                returnValue = dispatch1.dispatch(frame, receiverObject, methodName, blockObject, argumentsObjects);
            }
            if (isAttrAssign) {
                assert argumentsObjects[argumentsObjects.length - 1] != null;
                return argumentsObjects[argumentsObjects.length - 1];
            } else {
                assert returnValue != null;
                return returnValue;
            }
        }
    }
}
