/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.exception;

import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.Nil;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.AllocateHelperNode;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.Shape;

@CoreModule(value = "NoMethodError", isClass = true)
public abstract class NoMethodErrorNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateHelperNode allocateNode = AllocateHelperNode.create();

        @Specialization
        protected RubyNoMethodError allocateNoMethodError(RubyClass rubyClass) {
            final Shape shape = allocateNode.getCachedShape(rubyClass);
            final RubyNoMethodError instance = new RubyNoMethodError(shape, nil, null, nil, null, nil, nil);
            allocateNode.trace(instance, this);
            return instance;
        }

    }

    @CoreMethod(names = "args")
    public abstract static class ArgsNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object args(RubyNoMethodError self) {
            return self.args;
        }

    }

    @Primitive(name = "no_method_error_set_args")
    public abstract static class ArgsSetNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object setArgs(RubyNoMethodError error, Object args) {
            assert args == Nil.INSTANCE || args instanceof RubyArray;
            error.args = args;
            return args;
        }

    }


}
