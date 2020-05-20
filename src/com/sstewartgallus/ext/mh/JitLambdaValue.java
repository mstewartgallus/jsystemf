package com.sstewartgallus.ext.mh;

import com.sstewartgallus.ext.tuples.Signature;
import com.sstewartgallus.ext.tuples.Tuple;
import com.sstewartgallus.plato.*;

import java.lang.invoke.MethodHandle;

import static java.lang.invoke.MethodHandles.lookup;

// fixme... could be an abstract class I suppose or another pass could lower to that...
// fixme... establish an invariant that this must always be a function or a forall.
public final class JitLambdaValue<A extends Tuple<A>, B, X, Y> implements ThunkTerm<F<X, Y>>, JitValue<F<X, Y>> {

    private static final TermInvoker INVOKE_TERM = com.sstewartgallus.runtime.TermInvoker.newInstance(lookup(), TermInvoker.class);
    private final MethodHandle methodHandle;
    private final Signature<A, B, F<X, Y>> sig;

    public JitLambdaValue(Signature<A, B, F<X, Y>> sig,
                          MethodHandle methodHandle) {
        this.sig = sig;
        this.methodHandle = methodHandle;
    }

    MethodHandle methodHandle() {
        return methodHandle;
    }

    @Override
    public Term<F<X, Y>> stepThunk() {
        var d = ((FunctionType<X, Y>) sig.type()).domain();
        var self = this;
        return d.l(x -> INVOKE_TERM.apply(self, x));
    }

    @Override
    public Type<F<X, Y>> type() throws TypeCheckException {
        return sig.type();
    }

    @Override
    public Term<F<X, Y>> visitChildren(Visitor visitor) {
        return this;
    }

    public String toString() {
        return "JIT@" + sig;
    }

    @FunctionalInterface
    public interface TermInvoker {
        <A, B> Term<B> apply(JitLambdaValue<?, ?, A, B> f, Term<A> x);
    }
}