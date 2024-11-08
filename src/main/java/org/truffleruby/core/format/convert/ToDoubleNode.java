/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.convert;

import org.truffleruby.core.cast.ToFNode;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.objects.IsANode;

@NodeChild("value")
public abstract class ToDoubleNode extends FormatNode {

    public abstract double executeToDouble(VirtualFrame frame, Object object);

    public static ToDoubleNode create() {
        return ToDoubleNodeGen.create(null);
    }

    @Specialization
    protected double toDouble(int value) {
        return value;
    }

    @Specialization
    protected double toDouble(long value) {
        return value;
    }

    @Specialization
    protected double toDouble(double value) {
        return value;
    }

    @Specialization
    @TruffleBoundary
    protected double toDouble(RubyBignum bignum) {
        return bignum.value.doubleValue();
    }

    @Specialization(guards = { "!isRubyNumber(object)", "isNumeric(object, isANode)" })
    protected double toDouble(RubyDynamicObject object,
            @Cached IsANode isANode,
            @Cached ToFNode toFNode) {
        return toFNode.executeToDouble(object);
    }

    @Specialization(guards = { "!isRubyNumber(object)", "!isNumeric(object, isANode)" })
    protected double toDouble(Object object,
            @Cached IsANode isANode) {
        throw new RaiseException(
                getContext(),
                coreExceptions().typeErrorCantConvertInto(object, "Float", this));
    }

    protected boolean isNumeric(Object object, IsANode isANode) {
        return isANode.executeIsA(object, coreLibrary().numericClass);
    }
}
