/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.parser.IdentifierType;
import org.truffleruby.parser.Identifiers;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;

@ImportStatic(Identifiers.class)
public abstract class CheckIVarNameNode extends RubyBaseNode {

    /** Pass both the j.l.String name and the original name, the original name can be faster to check and the j.l.String
     * name is needed by all callers so it is better for footprint that callers convert to j.l.String */
    public abstract void execute(Object object, String name, Object originalName);

    @Specialization
    protected void checkSymbol(Object object, String name, RubySymbol originalName,
            @Cached @Shared BranchProfile errorProfile) {
        if (originalName.getType() != IdentifierType.INSTANCE) {
            errorProfile.enter();
            throw new RaiseException(getContext(), getContext().getCoreExceptions().nameErrorInstanceNameNotAllowable(
                    name,
                    object,
                    this));
        }
    }

    @Specialization(
            guards = { "name == cachedName", "isValidInstanceVariableName(cachedName)", "!isRubySymbol(originalName)" },
            limit = "getDynamicObjectCacheLimit()")
    protected void cached(Object object, String name, Object originalName,
            @Cached("name") String cachedName) {
    }

    @Specialization(replaces = "cached", guards = "!isRubySymbol(originalName)")
    protected void uncached(Object object, String name, Object originalName,
            @Cached @Shared BranchProfile errorProfile) {
        if (!Identifiers.isValidInstanceVariableName(name)) {
            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().nameErrorInstanceNameNotAllowable(
                    name,
                    object,
                    this));
        }
    }
}
