/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format;

public class FormatGuards {

    public static boolean isNull(Object object) {
        return object == null;
    }

    public static boolean isBoolean(Object object) {
        return object instanceof Boolean;
    }

    public static boolean isMissingValue(Object object) {
        return object == MissingValue.INSTANCE;
    }

}
