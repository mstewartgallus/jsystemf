package com.sstewartgallus.plato.ir.type;

import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.runtime.V;

import java.lang.constant.*;

public abstract class TypeDesc<A> extends DynamicConstantDesc<Type<A>> {
    private static final String TYPE_PACKAGE = TypeDesc.class.getPackageName();
    public static final ClassDesc CD_Type = ClassDesc.of(TYPE_PACKAGE, "Type");
    public static final ClassDesc CD_TypeBootstraps = ClassDesc.of(TYPE_PACKAGE, "TypeBootstraps");
    private static final DirectMethodHandleDesc APPLY_MH;
    private static final DirectMethodHandleDesc REFERENCE_TYPE_MH;

    static {
        var mt = MethodTypeDesc.of(CD_Type, ConstantDescs.CD_MethodHandles_Lookup, ConstantDescs.CD_String, ConstantDescs.CD_Class, ConstantDescs.CD_String);
        REFERENCE_TYPE_MH = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, CD_TypeBootstraps, "ofReference", mt);
    }

    static {
        var mt = MethodTypeDesc.of(CD_Type, ConstantDescs.CD_MethodHandles_Lookup, ConstantDescs.CD_String, ConstantDescs.CD_Class, CD_Type, CD_Type);
        APPLY_MH = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, CD_TypeBootstraps, "ofApplication", mt);
    }

    private TypeDesc(DirectMethodHandleDesc bootstrapMethod, String constantName, ClassDesc constantType, ConstantDesc... bootstrapArgs) {
        super(bootstrapMethod, constantName, constantType, bootstrapArgs);
    }

    public static <A, B> TypeDesc<B> ofApply(TypeDesc<V<A, B>> f, TypeDesc<A> x) {
        return new TypeApplicationDesc<>(f, x);
    }

    public static <A> TypeDesc<A> ofReference(String packageName, String className) {
        return new TypeReferenceDesc<>(packageName, className);
    }

    public static <A, B> TypeDesc<Fn<U<A>, B>> to(TypeDesc<A> typeDesc, TypeDesc<B> range) {
        return TypeDesc.ofApply(TypeDesc.ofApply(TypeDescs.fun(), typeDesc), range);
    }

    public TypeDesc<U<A>> thunk() {
        return TypeDesc.ofApply(TypeDescs.thunk(), this);
    }

    public <B> TypeDesc<Fn<A, B>> toFn(TypeDesc<B> range) {
        return TypeDesc.ofApply(TypeDesc.ofApply(TypeDescs.fn(), this), range);
    }

    public TypeDesc<F<A>> returns() {
        return TypeDesc.ofApply(TypeDescs.returns(), this);
    }

    public static final class TypeApplicationDesc<A, B> extends TypeDesc<B> {
        private final TypeDesc<V<A, B>> f;
        private final TypeDesc<A> x;

        private TypeApplicationDesc(TypeDesc<V<A, B>> f, TypeDesc<A> x) {
            super(APPLY_MH, ConstantDescs.DEFAULT_NAME, CD_Type, f, x);
            this.f = f;
            this.x = x;
        }

        public TypeDesc<A> x() {
            return x;
        }

        @Override
        public String toString() {
            return "(" + f + " " + x + ")";
        }
    }

    public static final class TypeReferenceDesc<B> extends TypeDesc<B> {
        private final String packageName;
        private final String className;

        private TypeReferenceDesc(String packageName, String className) {
            super(REFERENCE_TYPE_MH, className, CD_Type, packageName);
            this.packageName = packageName;
            this.className = className;
        }

        @Override
        public String toString() {
            return packageName + "/" + className;
        }
    }
}
