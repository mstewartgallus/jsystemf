package com.sstewartgallus.plato;

import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.runtime.TermDesc;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.*;


public abstract class LambdaTerm<A, B> implements ValueTerm<F<A, B>> {
    private final Type<A> domain;
    private final Type<B> range;

    public LambdaTerm(Type<A> domain, Type<B> range) {
        Objects.requireNonNull(domain);
        this.domain = domain;
        this.range = range;
    }

    public final Type<A> domain() {
        return domain;
    }

    // perhaps use a kontinuation like thunks?
    public abstract Term<B> apply(Term<A> x);

    @Override
    public final Term<F<A, B>> visitChildren(Visitor visitor) {
        var v = new VarValue<>(domain());
        var body = visitor.term(apply(NominalTerm.ofTag(v, domain)));
        return new LambdaTerm<>(visitor.type(domain()), visitor.type(range)) {
            @Override
            public Term<B> apply(Term<A> x) {
                return v.substituteIn(body, x);
            }
        };
    }

    @Override
    public final void jit(ClassDesc thisClass, ClassVisitor classVisitor, MethodVisitor mw, Map<VarValue<?>, VarData> varDataMap) {
        var term = jit(thisClass, classVisitor);
        mw.visitLdcInsn(AsmUtils.toAsm(term));
    }

    private TermDesc<F<A, B>> jit(ClassDesc thisClass, ClassVisitor cv) {

        var td = type().describeConstable().get();

        LambdaTerm<?, ?> current = this;

        var args = new ArrayList<Class<?>>();

        Class<?> range;
        Term<?> body;
        var varDataMap = new HashMap<VarValue<?>, VarData>();
        var ii = 0;
        for (; ; ) {
            var v = new VarValue(current.domain);
            varDataMap.put(v, new VarData(ii, current.domain));
            // fixme.. handle longs/doubles
            ++ii;

            body = current.apply(NominalTerm.ofTag(v, current.domain));
            args.add(current.domain.erase());
            range = body.type().erase();

            if (body instanceof LambdaTerm<?, ?> lambda) {
                current = lambda;
                continue;
            }

            break;
        }

        // fixme... generate unique name...
        var methodName = "apply";

        var methodType = methodType(range, args);
        var methodTypeDesc = methodType.describeConstable().get();

        var mh = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, thisClass, methodName, methodTypeDesc);

        {
            var mw = cv.visitMethod(ACC_PRIVATE | ACC_STATIC, methodName, methodType.descriptorString(), null, null);
            mw.visitCode();

            body.jit(thisClass, cv, mw, varDataMap);

            if (range.isPrimitive()) {
                switch (range.getName()) {
                    case "boolean", "byte", "char", "short", "int" -> {
                        mw.visitInsn(IRETURN);
                    }
                    case "long" -> {
                        mw.visitInsn(LRETURN);
                    }
                    case "float" -> {
                        mw.visitInsn(FRETURN);
                    }
                    case "double" -> {
                        mw.visitInsn(DRETURN);
                    }
                    default -> throw new IllegalStateException(range.getName());
                }
            } else {
                mw.visitInsn(ARETURN);
            }

            mw.visitMaxs(0, 0);
            mw.visitEnd();
        }

        return TermDesc.ofMethod(td, mh);
    }

    @Override
    public final Type<F<A, B>> type() throws TypeCheckException {
        return domain.to(range);
    }

    public Type<B> range() {
        return range;
    }
}
