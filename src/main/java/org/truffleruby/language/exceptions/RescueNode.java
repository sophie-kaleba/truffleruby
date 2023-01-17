/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.exceptions;

import com.oracle.truffle.api.interop.InteropLibrary;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.GenerateWrapper;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.profiles.BranchProfile;

@GenerateWrapper
public abstract class RescueNode extends RubyContextSourceNode {

    @Child private RubyNode rescueBody;

    @Child private DispatchNode callTripleEqualsNode;
    @Child private BooleanCastNode booleanCastNode;
    @Child private InteropLibrary interopLibrary;

    private final BranchProfile errorProfile = BranchProfile.create();

    public RescueNode(RubyNode rescueBody) {
        this.rescueBody = rescueBody;
    }

    // Constructor for instrumentation
    protected RescueNode() {
        this.rescueBody = null;
    }

    public abstract boolean canHandle(VirtualFrame frame, Object exceptionObject);

    @Override
    public Object execute(VirtualFrame frame) {
        return rescueBody.execute(frame);
    }

    protected boolean matches(Object exceptionObject, Object handlingClass) {
        if (!(handlingClass instanceof RubyModule)) {
            if (interopLibrary == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                interopLibrary = insert(InteropLibrary.getFactory().createDispatched(getInteropCacheLimit()));
            }

            if (!interopLibrary.isMetaObject(handlingClass)) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().typeErrorRescueInvalidClause(this));
            }
        }

        if (callTripleEqualsNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callTripleEqualsNode = insert(DispatchNode.create());
        }
        if (booleanCastNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            booleanCastNode = insert(BooleanCastNodeGen.create(null));
        }

        final Object matches = callTripleEqualsNode.call(handlingClass, "===", exceptionObject);
        return booleanCastNode.execute(matches);
    }

    @Override
    public WrapperNode createWrapper(ProbeNode probe) {
        return new RescueNodeWrapper(this, probe);
    }

    // Declared abstract here so the instrumentation wrapper delegates it
    @Override
    public abstract Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context);

    protected RubyNode getRescueBody() {
        return rescueBody;
    }
}
