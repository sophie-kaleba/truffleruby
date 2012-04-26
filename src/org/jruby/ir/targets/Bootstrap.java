package org.jruby.ir.targets;

import com.headius.invokebinder.Binder;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyEncoding;
import org.jruby.RubyFixnum;
import org.jruby.RubyModule;
import org.jruby.RubyString;
import org.jruby.RubySymbol;
import org.jruby.internal.runtime.methods.CompiledIRMethod;
import org.jruby.internal.runtime.methods.DynamicMethod;
import org.jruby.ir.operands.UndefinedValue;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.Block;
import org.jruby.runtime.CallType;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.callsite.CacheEntry;
import org.jruby.runtime.opto.GenerationAndSwitchPointInvalidator;
import org.jruby.util.JavaNameMangler;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.invoke.SwitchPoint;

import static java.lang.invoke.MethodHandles.*;
import static org.jruby.runtime.invokedynamic.InvokeDynamicSupport.*;
import static org.jruby.util.CodegenUtils.p;
import static org.jruby.util.CodegenUtils.sig;

public class Bootstrap {
    public static CallSite string(Lookup lookup, String name, MethodType type, String value, int encoding) {
        MethodHandle handle = Binder
                .from(IRubyObject.class, ThreadContext.class)
                .insert(0, new Class[]{String.class, int.class}, value, encoding)
                .invokeStaticQuiet(MethodHandles.lookup(), Bootstrap.class, "string");
        CallSite site = new ConstantCallSite(handle);
        return site;
    }

    public static CallSite array(Lookup lookup, String name, MethodType type) {
        MethodHandle handle = Binder
                .from(type)
                .collect(1, IRubyObject[].class)
                .invokeStaticQuiet(MethodHandles.lookup(), Bootstrap.class, "array");
        CallSite site = new ConstantCallSite(handle);
        return site;
    }

    private static class InvokeSite extends MutableCallSite {
        public InvokeSite(MethodType type, String name) {
            super(type);
            this.name = name;
        }

        public final String name;
    }

    public static CallSite invoke(Lookup lookup, String name, MethodType type) {
        InvokeSite site = new InvokeSite(type, JavaNameMangler.demangleMethodName(name.split(":")[1]));
        MethodHandle handle =
                insertArguments(
                        findStatic(
                                lookup,
                                Bootstrap.class,
                                "invoke",
                                type.insertParameterTypes(0, InvokeSite.class)),
                        0,
                        site);
        site.setTarget(handle);
        return site;
    }

    public static CallSite attrAssign(Lookup lookup, String name, MethodType type) {
        InvokeSite site = new InvokeSite(type, JavaNameMangler.demangleMethodName(name.split(":")[1]));
        MethodHandle handle =
                insertArguments(
                        findStatic(
                                lookup,
                                Bootstrap.class,
                                "attrAssign",
                                type.insertParameterTypes(0, InvokeSite.class)),
                        0,
                        site);
        site.setTarget(handle);
        return site;
    }

    public static CallSite invokeSelf(Lookup lookup, String name, MethodType type) {
        InvokeSite site = new InvokeSite(type, JavaNameMangler.demangleMethodName(name.split(":")[1]));
        MethodHandle handle =
                insertArguments(
                        findStatic(
                                lookup,
                                Bootstrap.class,
                                "invokeSelf",
                                type.insertParameterTypes(0, InvokeSite.class)),
                        0,
                        site);
        site.setTarget(handle);
        return site;
    }

    public static CallSite ivar(Lookup lookup, String name, MethodType type) {
        String[] bits = name.split(":");
        String getSet = bits[0];
        String varName = JavaNameMangler.demangleMethodName(bits[1]);

        MethodHandle handle = Binder
                .from(type)
                .insert(0, varName)
                .invokeStaticQuiet(MethodHandles.lookup(), Bootstrap.class, getSet);
        return new ConstantCallSite(handle);
    }

    public static CallSite searchConst(Lookup lookup, String name, MethodType type) {
        MutableCallSite site = new MutableCallSite(type);
        String[] bits = name.split(":");
        String constName = bits[1];

        MethodHandle handle = Binder
                .from(type)
                .insert(0, site, constName)
                .invokeStaticQuiet(MethodHandles.lookup(), Bootstrap.class, "searchConst");

        site.setTarget(handle);

        return site;
    }

    public static CallSite inheritanceSearchConst(Lookup lookup, String name, MethodType type) {
        MutableCallSite site = new MutableCallSite(type);
        String[] bits = name.split(":");
        String constName = bits[1];

        MethodHandle handle = Binder
                .from(type)
                .insert(0, site, constName)
                .invokeStaticQuiet(MethodHandles.lookup(), Bootstrap.class, "inheritanceSearchConst");

        site.setTarget(handle);

        return site;
    }

    public static Handle string() {
        return new Handle(Opcodes.H_INVOKESTATIC, p(Bootstrap.class), "string", sig(CallSite.class, Lookup.class, String.class, MethodType.class, String.class, int.class));
    }

    public static Handle array() {
        return new Handle(Opcodes.H_INVOKESTATIC, p(Bootstrap.class), "array", sig(CallSite.class, Lookup.class, String.class, MethodType.class));
    }

    public static Handle invoke() {
        return new Handle(Opcodes.H_INVOKESTATIC, p(Bootstrap.class), "invoke", sig(CallSite.class, Lookup.class, String.class, MethodType.class));
    }

    public static Handle invokeSelf() {
        return new Handle(Opcodes.H_INVOKESTATIC, p(Bootstrap.class), "invokeSelf", sig(CallSite.class, Lookup.class, String.class, MethodType.class));
    }

    public static Handle attrAssign() {
        return new Handle(Opcodes.H_INVOKESTATIC, p(Bootstrap.class), "attrAssign", sig(CallSite.class, Lookup.class, String.class, MethodType.class));
    }

    public static Handle ivar() {
        return new Handle(Opcodes.H_INVOKESTATIC, p(Bootstrap.class), "ivar", sig(CallSite.class, Lookup.class, String.class, MethodType.class));
    }

    public static Handle searchConst() {
        return new Handle(Opcodes.H_INVOKESTATIC, p(Bootstrap.class), "searchConst", sig(CallSite.class, Lookup.class, String.class, MethodType.class));
    }

    public static Handle inheritanceSearchConst() {
        return new Handle(Opcodes.H_INVOKESTATIC, p(Bootstrap.class), "inheritanceSearchConst", sig(CallSite.class, Lookup.class, String.class, MethodType.class));
    }

    public static IRubyObject string(String value, int encoding, ThreadContext context) {
        // obviously wrong: not caching bytelist, not using encoding
        return RubyString.newStringNoCopy(context.runtime, value.getBytes(RubyEncoding.ISO));
    }

    public static IRubyObject array(ThreadContext context, IRubyObject[] elts) {
        return RubyArray.newArrayNoCopy(context.runtime, elts);
    }

    public static IRubyObject invoke(InvokeSite site, ThreadContext context, IRubyObject self) throws Throwable {
        RubyClass selfClass = pollAndGetClass(context, self);
        String methodName = site.name;
        SwitchPoint switchPoint = (SwitchPoint)selfClass.getInvalidator().getData();
        CacheEntry entry = selfClass.searchWithCache(methodName);
        DynamicMethod method = entry.method;

        if (methodMissing(entry, CallType.NORMAL, methodName, self)) {
            return callMethodMissing(entry, CallType.NORMAL, context, self, methodName);
        }

        MethodHandle mh = getHandle(selfClass, switchPoint, site, method, 0);

        site.setTarget(mh);
        return (IRubyObject)mh.invokeWithArguments(context, self);
    }

    public static IRubyObject invoke(InvokeSite site, ThreadContext context, IRubyObject self, IRubyObject arg0) throws Throwable {
        RubyClass selfClass = pollAndGetClass(context, self);
        String methodName = site.name;
        SwitchPoint switchPoint = (SwitchPoint)selfClass.getInvalidator().getData();
        CacheEntry entry = selfClass.searchWithCache(methodName);
        DynamicMethod method = entry.method;

        if (methodMissing(entry, CallType.NORMAL, methodName, self)) {
            return callMethodMissing(entry, CallType.NORMAL, context, self, methodName, arg0);
        }

        MethodHandle mh = getHandle(selfClass, switchPoint, site, method, 1);

        site.setTarget(mh);
        return (IRubyObject)mh.invokeWithArguments(context, self, arg0);
    }

    public static IRubyObject invoke(InvokeSite site, ThreadContext context, IRubyObject self, IRubyObject arg0, IRubyObject arg1) throws Throwable {
        RubyClass selfClass = pollAndGetClass(context, self);
        String methodName = site.name;
        SwitchPoint switchPoint = (SwitchPoint)selfClass.getInvalidator().getData();
        CacheEntry entry = selfClass.searchWithCache(methodName);
        DynamicMethod method = entry.method;

        if (methodMissing(entry, CallType.NORMAL, methodName, self)) {
            return callMethodMissing(entry, CallType.NORMAL, context, self, methodName);
        }

        MethodHandle mh = getHandle(selfClass, switchPoint, site, method, 2);

        site.setTarget(mh);
        return (IRubyObject)mh.invokeWithArguments(context, self);
    }

    public static IRubyObject invoke(InvokeSite site, ThreadContext context, IRubyObject self, IRubyObject arg0, IRubyObject arg1, IRubyObject arg2) throws Throwable {
        RubyClass selfClass = pollAndGetClass(context, self);
        String methodName = site.name;
        SwitchPoint switchPoint = (SwitchPoint)selfClass.getInvalidator().getData();
        CacheEntry entry = selfClass.searchWithCache(methodName);
        DynamicMethod method = entry.method;

        if (methodMissing(entry, CallType.NORMAL, methodName, self)) {
            return callMethodMissing(entry, CallType.NORMAL, context, self, methodName);
        }

        MethodHandle mh = getHandle(selfClass, switchPoint, site, method, 3);

        site.setTarget(mh);
        return (IRubyObject)mh.invokeWithArguments(context, self);
    }

    public static IRubyObject invokeSelf(InvokeSite site, ThreadContext context, IRubyObject self) throws Throwable {
        RubyClass selfClass = pollAndGetClass(context, self);
        String methodName = site.name;
        SwitchPoint switchPoint = (SwitchPoint)selfClass.getInvalidator().getData();
        CacheEntry entry = selfClass.searchWithCache(methodName);
        DynamicMethod method = entry.method;

        if (methodMissing(entry, CallType.FUNCTIONAL, methodName, self)) {
            return callMethodMissing(entry, CallType.FUNCTIONAL, context, self, methodName);
        }

        MethodHandle mh = getHandle(selfClass, switchPoint, site, method, 0);

        site.setTarget(mh);
        return (IRubyObject)mh.invokeWithArguments(context, self);
    }

    public static IRubyObject attrAssign(InvokeSite site, ThreadContext context, IRubyObject self, IRubyObject arg0) throws Throwable {
        RubyClass selfClass = pollAndGetClass(context, self);
        String methodName = site.name;
        SwitchPoint switchPoint = (SwitchPoint)selfClass.getInvalidator().getData();
        CacheEntry entry = selfClass.searchWithCache(methodName);
        DynamicMethod method = entry.method;

        if (methodMissing(entry, CallType.NORMAL, methodName, self)) {
            return callMethodMissing(entry, CallType.NORMAL, context, self, methodName, arg0);
        }

        MethodHandle mh = getHandle(selfClass, switchPoint, site, method, 1);

        mh = foldArguments(
                mh,
                Binder.from(site.type())
                        .drop(0, 2)
                        .identity());

        site.setTarget(mh);
        mh.invokeWithArguments(context, self, arg0);
        return arg0;
    }

    private static final int[][] PERMUTES = new int[][] {
            new int[]{1, 0},
            new int[]{1, 0, 2},
            new int[]{1, 0, 2, 3},
            new int[]{1, 0, 2, 3, 4},
    };

    private static MethodHandle getHandle(RubyClass selfClass, SwitchPoint switchPoint, InvokeSite site, DynamicMethod method, int arity) throws Throwable {
        MethodHandle mh = null;
        if (method.getNativeCall() != null) {
            DynamicMethod.NativeCall nc = method.getNativeCall();
            if (method.getArity().isFixed()) {
                if (method.getArity().getValue() <= 3) {
                    Binder b = Binder.from(site.type());
                    if (!nc.hasContext()) {
                        b.drop(0);
                    }

                    if (nc.hasBlock()) {
                        b.insert(site.type().parameterCount() - 1, Block.NULL_BLOCK);
                    }


                    if (nc.isStatic()) {
                        if (b.type().parameterCount() == nc.getNativeSignature().length) {
                            mh = b
                                    .cast(nc.getNativeReturn(), nc.getNativeSignature())
                                    .invokeStaticQuiet(MethodHandles.lookup(), nc.getNativeTarget(), nc.getNativeName());
//                            System.out.println(mh);
                        }
                    } else {
//                        System.out.println(b.type());
//                        System.out.println(Arrays.toString(nc.getNativeSignature()));
                        if (b.type().parameterCount() == nc.getNativeSignature().length + 1) {
                            // only threadcontext-receivers right now
                            mh = b
                                    .permute(PERMUTES[arity])
                                    .cast(MethodType.methodType(nc.getNativeReturn(), nc.getNativeTarget(), nc.getNativeSignature()))
                                    .invokeVirtualQuiet(MethodHandles.lookup(), nc.getNativeName());
//                            System.out.println(mh);
                        }
                    }
                }
            }
        }

        if (mh == null) {
            // attempt IR direct binding
            if (method instanceof CompiledIRMethod) {
                mh = (MethodHandle)((CompiledIRMethod)method).getHandle();
                mh = MethodHandles.insertArguments(mh, 1, ((CompiledIRMethod)method).getStaticScope());
            }
        }

        MethodHandle fallback = Binder
                .from(site.type())
                .insert(0, site.name)
                .invokeStatic(MethodHandles.lookup(), Bootstrap.class, "invokeSelfSimple");

        if (mh == null) {
            return fallback;
        } else {
            MethodHandle test = Binder
                    .from(site.type().changeReturnType(boolean.class))
                    .insert(0, new Class[]{RubyClass.class}, selfClass)
                    .invokeStatic(MethodHandles.lookup(), Bootstrap.class, "testType");
            mh = MethodHandles.guardWithTest(test, mh, fallback);
            mh = switchPoint.guardWithTest(mh, fallback);
        }

        return mh;
    }

    public static IRubyObject invokeSelfSimple(String name, ThreadContext context, IRubyObject self) {
        return self.getMetaClass().invoke(context, self, name, CallType.FUNCTIONAL);
    }

    public static IRubyObject invokeSelf(InvokeSite site, ThreadContext context, IRubyObject self, IRubyObject arg0) throws Throwable {
        RubyClass selfClass = pollAndGetClass(context, self);
        String methodName = site.name;
        SwitchPoint switchPoint = (SwitchPoint)selfClass.getInvalidator().getData();
        CacheEntry entry = selfClass.searchWithCache(methodName);
        DynamicMethod method = entry.method;

        if (methodMissing(entry, CallType.FUNCTIONAL, methodName, self)) {
            return callMethodMissing(entry, CallType.FUNCTIONAL, context, self, methodName, arg0);
        }

        MethodHandle mh = getHandle(selfClass, switchPoint, site, method, 1);

        site.setTarget(mh);
        return (IRubyObject)mh.invokeWithArguments(context, self, arg0);
    }

    public static IRubyObject invokeSelfSimple(String name, ThreadContext context, IRubyObject self, IRubyObject arg0) {
        return self.getMetaClass().invoke(context, self, name, arg0, CallType.FUNCTIONAL);
    }

    public static IRubyObject invokeSelf(InvokeSite site, ThreadContext context, IRubyObject self, IRubyObject arg0, IRubyObject arg1) throws Throwable {
        RubyClass selfClass = pollAndGetClass(context, self);
        String methodName = site.name;
        SwitchPoint switchPoint = (SwitchPoint)selfClass.getInvalidator().getData();
        CacheEntry entry = selfClass.searchWithCache(methodName);
        DynamicMethod method = entry.method;

        if (methodMissing(entry, CallType.FUNCTIONAL, methodName, self)) {
            return callMethodMissing(entry, CallType.FUNCTIONAL, context, self, methodName, arg0, arg1);
        }

        MethodHandle mh = getHandle(selfClass, switchPoint, site, method, 2);

        site.setTarget(mh);
        return (IRubyObject)mh.invokeWithArguments(context, self, arg0, arg1);
    }

    public static IRubyObject invokeSelfSimple(String name, ThreadContext context, IRubyObject self, IRubyObject arg0, IRubyObject arg1) {
        return self.getMetaClass().invoke(context, self, name, arg0, arg1, CallType.FUNCTIONAL);
    }

    public static IRubyObject invokeSelf(InvokeSite site, ThreadContext context, IRubyObject self, IRubyObject arg0, IRubyObject arg1, IRubyObject arg2) throws Throwable {
        RubyClass selfClass = pollAndGetClass(context, self);
        String methodName = site.name;
        SwitchPoint switchPoint = (SwitchPoint)selfClass.getInvalidator().getData();
        CacheEntry entry = selfClass.searchWithCache(methodName);
        DynamicMethod method = entry.method;

        if (methodMissing(entry, CallType.FUNCTIONAL, methodName, self)) {
            return callMethodMissing(entry, CallType.FUNCTIONAL, context, self, methodName, arg0, arg1, arg2);
        }

        MethodHandle mh = getHandle(selfClass, switchPoint, site, method, 3);

        site.setTarget(mh);
        return (IRubyObject)mh.invokeWithArguments(context, self, arg0, arg1, arg2);
    }

    public static IRubyObject invokeSelfSimple(String name, ThreadContext context, IRubyObject self, IRubyObject arg0, IRubyObject arg1, IRubyObject arg2) {
        return self.getMetaClass().invoke(context, self, name, arg0, arg1, arg2, CallType.FUNCTIONAL);
    }

    public static IRubyObject ivarGet(String name, IRubyObject self) {
        return self.getInstanceVariables().getInstanceVariable(name);
    }

    public static void ivarSet(String name, IRubyObject self, IRubyObject value) {
        self.getInstanceVariables().setInstanceVariable(name, value);
    }

    private static MethodHandle findStatic(Class target, String name, MethodType type) {
        return findStatic(lookup(), target, name, type);
    }

    private static MethodHandle findStatic(Lookup lookup, Class target, String name, MethodType type) {
        try {
            return lookup.findStatic(target, name, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean testType(RubyClass original, ThreadContext context, IRubyObject self) {
        // naive test
        return self.getMetaClass() == original;
    }

    public static boolean testType(RubyClass original, ThreadContext context, IRubyObject self, IRubyObject arg0) {
        // naive test
        return self.getMetaClass() == original;
    }

    public static boolean testType(RubyClass original, ThreadContext context, IRubyObject self, IRubyObject arg0, IRubyObject arg1) {
        // naive test
        return self.getMetaClass() == original;
    }

    public static boolean testType(RubyClass original, ThreadContext context, IRubyObject self, IRubyObject arg0, IRubyObject arg1, IRubyObject arg2) {
        // naive test
        return self.getMetaClass() == original;
    }

    public static boolean testType(RubyClass original, ThreadContext context, IRubyObject self, IRubyObject[] args) {
        // naive test
        return self.getMetaClass() == original;
    }

    ///////////////////////////////////////////////////////////////////////////
    // constant lookup

    public static IRubyObject searchConst(MutableCallSite site, String constName, ThreadContext context, StaticScope staticScope) throws Throwable {
        Ruby runtime = context.runtime;
        SwitchPoint switchPoint = (SwitchPoint)runtime.getConstantInvalidator().getData();
        IRubyObject value = staticScope.getConstant(runtime, constName, runtime.getObject());

        if (value == null) {
            return staticScope.getModule().callMethod(context, "const_missing", runtime.fastNewSymbol(constName));
        }

        // bind constant until invalidated
        MethodHandle target = Binder.from(site.type())
                .drop(0, 2)
                .constant(value);
        MethodHandle fallback = Binder.from(site.type())
                .insert(0, site, constName)
                .invokeStatic(MethodHandles.lookup(), Bootstrap.class, "searchConst");

        site.setTarget(switchPoint.guardWithTest(target, fallback));

        return value;
    }

    public static IRubyObject inheritanceSearchConst(MutableCallSite site, String constName, ThreadContext context, IRubyObject cmVal) throws Throwable {
        Ruby runtime = context.runtime;
        RubyModule module;

        if (cmVal instanceof RubyModule) {
            module = (RubyModule) cmVal;
        } else {
            throw runtime.newTypeError(cmVal + " is not a type/class");
        }

        SwitchPoint switchPoint = (SwitchPoint)runtime.getConstantInvalidator().getData();

        IRubyObject value = module.getConstantFromNoConstMissing(constName, false);
        if (value == null) {
            return (IRubyObject)UndefinedValue.UNDEFINED;
        }

        // bind constant until invalidated
        MethodHandle target = Binder.from(site.type())
                .drop(0, 2)
                .constant(value);
        MethodHandle fallback = Binder.from(site.type())
                .insert(0, site, constName)
                .invokeStatic(MethodHandles.lookup(), Bootstrap.class, "inheritanceSearchConst");

        site.setTarget(switchPoint.guardWithTest(target, fallback));

        return value;
    }

    ///////////////////////////////////////////////////////////////////////////
    // COMPLETED WORK BELOW

    ///////////////////////////////////////////////////////////////////////////
    // Symbol binding

    public static Handle symbol() {
        return new Handle(Opcodes.H_INVOKESTATIC, p(Bootstrap.class), "symbol", sig(CallSite.class, Lookup.class, String.class, MethodType.class, String.class));
    }

    public static CallSite symbol(Lookup lookup, String name, MethodType type, String sym) {
        MutableCallSite site = new MutableCallSite(type);
        MethodHandle handle = Binder
                .from(IRubyObject.class, ThreadContext.class)
                .insert(0, site, sym)
                .invokeStaticQuiet(MethodHandles.lookup(), Bootstrap.class, "symbol");
        site.setTarget(handle);
        return site;
    }

    public static IRubyObject symbol(MutableCallSite site, String name, ThreadContext context) {
        RubySymbol symbol = RubySymbol.newSymbol(context.runtime, name);
        site.setTarget(Binder
                .from(IRubyObject.class, ThreadContext.class)
                .drop(0)
                .constant(symbol)
        );
        return symbol;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fixnum binding

    public static Handle fixnum() {
        return new Handle(Opcodes.H_INVOKESTATIC, p(Bootstrap.class), "fixnum", sig(CallSite.class, Lookup.class, String.class, MethodType.class, long.class));
    }

    public static CallSite fixnum(Lookup lookup, String name, MethodType type, long value) {
        MutableCallSite site = new MutableCallSite(type);
        MethodHandle handle = Binder
                .from(IRubyObject.class, ThreadContext.class)
                .insert(0, site, value)
                .cast(IRubyObject.class, MutableCallSite.class, long.class, ThreadContext.class)
                .invokeStaticQuiet(MethodHandles.lookup(), Bootstrap.class, "fixnum");
        site.setTarget(handle);
        return site;
    }

    public static IRubyObject fixnum(MutableCallSite site, long value, ThreadContext context) {
        RubyFixnum fixnum = RubyFixnum.newFixnum(context.runtime, value);
        site.setTarget(Binder
                .from(IRubyObject.class, ThreadContext.class)
                .drop(0)
                .constant(fixnum)
        );
        return fixnum;
    }
}
