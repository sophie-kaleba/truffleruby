package org.truffleruby.language.contextualdispatch;

import org.truffleruby.language.RubyDynamicObject;

public class ContextSignatureUtils {

    public static final int[] primeForSignature = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97};

    public static long getArgumentHashcode(Object value) {
        if (value != null) {
            if (value instanceof RubyDynamicObject) {
                return ((RubyDynamicObject) value).getClassHashCode();
            }
            return value.getClass().hashCode();
        }
        return 0;
    }

    public static long getContextSignature(Object[] args, Object receiver){
        long contextSignature = 0;

        for (int i = 0; i < args.length; i++) {
            int j = i % ContextSignatureUtils.primeForSignature.length;
            contextSignature += ContextSignatureUtils.getArgumentHashcode(args[i]) * ContextSignatureUtils.primeForSignature[j];
        }

        return contextSignature;
    }
}
