package com.sstewartgallus.plato;

import com.sstewartgallus.ext.pretty.PrettyThunk;
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


public abstract class LambdaValue<A, B> implements ValueTerm<F<A, B>>, LambdaTerm<F<A, B>> {
    private final Type<A> domain;

    public LambdaValue(Type<A> domain) {
        Objects.requireNonNull(domain);
        this.domain = domain;
    }

    public final Type<A> domain() {
        return domain;
    }

    // perhaps use a kontinuation like thunks?
    public abstract Term<B> apply(Term<A> x);

    @Override
    public final Term<F<A, B>> visitChildren(Visitor visitor) {
        var v = new VarValue<>(domain());
        var body = visitor.term(apply(v));
        return new SimpleLambdaValue<>(visitor.type(domain()), x -> v.substituteIn(body, x));
    }

    @Override
    public void jit(ClassDesc thisClass, ClassVisitor classVisitor, MethodVisitor mw, Map<VarValue<?>, VarData> varDataMap) {
        var term = jit(thisClass, classVisitor);
        mw.visitLdcInsn(AsmUtils.toAsm(term));
    }

    public final TermDesc<F<A, B>> jit(ClassDesc thisClass, ClassVisitor cv) {

        var td = type().describeConstable().get();


        LambdaValue<?, ?> current = this;

        var args = new ArrayList<Class<?>>();

        Class<?> range;
        Term<?> body;
        var varDataMap = new HashMap<VarValue<?>, VarData>();
        var ii = 0;
        for (; ; ) {
            var v = new VarValue<>(current.domain);
            varDataMap.put(v, new VarData(ii));
            // fixme.. handle longs/doubles
            ++ii;

            body = current.apply((VarValue) v);
            args.add(current.domain.erase());
            range = body.type().erase();

            if (body instanceof LambdaValue<?, ?> lambda) {
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
        try (var pretty = PrettyThunk.generate(domain())) {
            var range = apply(pretty).type();
            return new FunctionType<>(domain(), range);
        }
    }

    @Override
    public final String toString() {
        return "(" + noBrackets() + ")";
    }

    private String noBrackets() {
        try (var pretty = PrettyThunk.generate(domain())) {
            var body = apply(pretty);
            if (body instanceof LambdaValue<?, ?> lambdaValue) {
                return "λ (" + pretty + " " + domain() + ") " + lambdaValue.noBrackets();
            }
            return "λ (" + pretty + " " + domain() + ") " + body;
        }
    }

}
