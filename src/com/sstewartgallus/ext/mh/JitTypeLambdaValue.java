package com.sstewartgallus.ext.mh;

import com.sstewartgallus.ext.tuples.Signature;
import com.sstewartgallus.ext.tuples.Tuple;
import com.sstewartgallus.plato.*;
import com.sstewartgallus.runtime.TermInvoker;

import java.lang.invoke.MethodHandle;

import static java.lang.invoke.MethodHandles.lookup;

public final class JitTypeLambdaValue<A extends Tuple<A>, B, X, Y, C extends V<X, Y>> extends TypeLambdaValue<X, Y> implements JitValue<V<X, Y>> {

    private static final JitInvoker INVOKE_TERM = TermInvoker.newInstance(lookup(), JitInvoker.class);
    private final MethodHandle methodHandle;
    private final Signature<A, B, C> sig;

    public JitTypeLambdaValue(Signature<A, B, C> sig,
                              MethodHandle methodHandle) {
        this.sig = sig;
        this.methodHandle = methodHandle;
    }

    @Override
    public Type<V<X, Y>> type() throws TypeCheckException {
        return (Type) sig.type();
    }

    @Override
    public Term<V<X, Y>> visitChildren(Visitor visitor) {
        return this;
    }

    public String toString() {
        return methodHandle.toString();
    }

    @Override
    public Term<Y> apply(Type<X> x) {
        return INVOKE_TERM.apply(this, x);
    }

    public MethodHandle methodHandle() {
        return methodHandle;
    }

    @FunctionalInterface
    public interface JitInvoker {
        <A, B> Term<B> apply(Term<V<A, B>> f, Type<A> x);
    }
}
