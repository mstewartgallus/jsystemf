package com.sstewartgallus.plato;

import com.sstewartgallus.ext.mh.JitLambdaValue;
import com.sstewartgallus.ext.pretty.PrettyThunk;
import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.runtime.AnonClassLoader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

import static java.lang.invoke.MethodHandles.lookup;
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

    public abstract Term<B> apply(Term<A> x);

    @Override
    public final Term<F<A, B>> visitChildren(Visitor visitor) {
        var v = new VarValue<>(domain());
        var body = visitor.term(apply(v));
        return new SimpleLambdaValue<>(visitor.type(domain()), x -> v.substituteIn(body, x));
    }

    // fixme... attach all lambdas in an expression to the same class?
    public final Term<F<A, B>> jit() {
        var v = new VarValue<>(domain());
        var body = apply(v);

        var args = domain().flatten();
        var range = body.type().erase();

        var methodType = methodType(range, args);

        var myname = org.objectweb.asm.Type.getInternalName(LambdaValue.class);
        var newclassname = myname + "Impl";

        // fixme... privatise as much as possible...
        var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        var str = new StringWriter();
        var cv = new TraceClassVisitor(cw, new PrintWriter(str));

        cv.visit(V14, ACC_FINAL | ACC_PUBLIC, newclassname, null, myname, null);

        {
            var mw = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, "apply", methodType.descriptorString(), null, null);
            mw.visitCode();

            body.jit(mw);

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

        cv.visitEnd();

        var bytes = cw.toByteArray();

        var definedClass = AnonClassLoader.defineClass(LambdaValue.class.getClassLoader(), bytes);
        MethodHandle mh;
        try {
            mh = lookup().findStatic(definedClass, "apply", methodType);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return new JitLambdaValue<>(str.toString(), domain().to(body.type()), mh);
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
