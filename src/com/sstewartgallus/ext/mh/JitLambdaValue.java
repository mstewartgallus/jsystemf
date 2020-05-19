package com.sstewartgallus.ext.mh;

import com.sstewartgallus.ext.tuples.Signature;
import com.sstewartgallus.ext.tuples.Tuple;
import com.sstewartgallus.plato.*;

import java.lang.invoke.MethodHandle;

import static java.lang.invoke.MethodHandles.lookup;

// fixme... could be an abstract class I suppose or another pass could lower to that...
// fixme... establish an invariant that this must always be a function or a forall.
public final class JitLambdaValue<A extends Tuple<A>, B, X, Y, C extends F<X, Y>> extends LambdaValue<X, Y> {

    private static final TermInvoker INVOKE_TERM = com.sstewartgallus.runtime.TermInvoker.newInstance(lookup(), TermInvoker.class);
    private final MethodHandle methodHandle;
    private final Signature<A, B, C> sig;

    public JitLambdaValue(Signature<A, B, C> sig,
                          MethodHandle methodHandle) {
        super(((FunctionType) sig.type()).domain());
        this.sig = sig;
        this.methodHandle = methodHandle;
    }

    @Override
    public Type<F<X, Y>> type() throws TypeCheckException {
        return (Type) sig.type();
    }

    @Override
    public Term<F<X, Y>> visitChildren(Visitor visitor) {
        return this;
    }

    public String toString() {
        return methodHandle.toString();
    }

    @Override
    public Term<Y> apply(Term<X> x) {
        return INVOKE_TERM.apply(this, x);
    }

    public MethodHandle methodHandle() {
        return methodHandle;
    }

    @FunctionalInterface
    public interface TermInvoker {
        <A, B> Term<B> apply(Term<F<A, B>> f, Term<A> x);
    }
}
