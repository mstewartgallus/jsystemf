package com.sstewartgallus.plato;

import com.sstewartgallus.ext.tuples.AtTupleIndexThunk;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.function.Function;

import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

public record ApplyThunk<A, B>(Term<F<A, B>>f, Term<A>x) implements ThunkTerm<B>, LambdaTerm<B> {

    private static final Handle HANDLE = new Handle(H_INVOKESTATIC, "bar", "gar",
            methodType(CallSite.class, MethodHandles.Lookup.class, String.class, Class.class).descriptorString(),
            false);

    public ApplyThunk {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }


    @Override
    public Term<B> visitChildren(Visitor visitor) {
        return Term.apply(visitor.term(f), visitor.term(x));
    }

    @Override
    public void jit(MethodVisitor mw) {
        if (f instanceof AtTupleIndexThunk tuple) {
            tuple.jit(mw);
            return;
        }

        f.jit(mw);

        mw.visitInsn(Opcodes.ACONST_NULL);

        x.jit(mw);

        var t = ((FunctionType<A, B>) f.type()).range().erase();
        mw.visitInvokeDynamicInsn("CALL", methodType(t, f.type().erase(), Void.class, x.type().erase()).descriptorString(), HANDLE);
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        var fType = f.type();

        var funType = (FunctionType<A, B>) fType;
        var range = funType.range();

        var argType = x.type();

        fType.unify(argType.to(range));

        return funType.range();
    }

    @Override
    public String toString() {
        return "(" + noBrackets() + ")";
    }

    private String noBrackets() {
        if (f instanceof ApplyThunk<?, F<A, B>> fApply) {
            return fApply.noBrackets() + " " + x;
        }
        return f + " " + x;
    }

    @Override
    public <C> Term<C> stepThunk(Function<ValueTerm<B>, Term<C>> k) {
        var theX = x;
        return f.stepThunk(fValue -> {
            var fLambda = (LambdaValue<A, B>) fValue;
            return fLambda.apply(theX).stepThunk(k);
        });
    }
}
