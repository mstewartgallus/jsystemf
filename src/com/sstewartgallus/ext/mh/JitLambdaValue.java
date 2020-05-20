package com.sstewartgallus.ext.mh;

import com.sstewartgallus.plato.*;
import com.sstewartgallus.runtime.TermInvoker;

import java.lang.invoke.MethodHandle;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.lookup;

// fixme... could be an abstract class I suppose or another pass could lower to that...
// fixme... establish an invariant that this must always be a function or a forall.
public final class JitLambdaValue<A, B> implements ThunkTerm<F<A, B>>, JitValue<F<A, B>> {

    private static final JitInvoker INVOKE_TERM = TermInvoker.newInstance(lookup(), JitInvoker.class);
    private final MethodHandle methodHandle;
    private final Type<F<A, B>> sig;

    public JitLambdaValue(Type<F<A, B>> sig,
                          MethodHandle methodHandle) {
        this.sig = sig;
        this.methodHandle = methodHandle;
    }

    MethodHandle methodHandle() {
        return methodHandle;
    }

    @Override
    public <C> Term<C> stepThunk(Function<ValueTerm<F<A, B>>, Term<C>> k) {
        var d = ((FunctionType<A, B>) sig).domain();
        var self = this;
        return k.apply(d.l(x -> INVOKE_TERM.apply(self, x)));
    }

    @Override
    public Type<F<A, B>> type() throws TypeCheckException {
        return sig;
    }

    @Override
    public Term<F<A, B>> visitChildren(Visitor visitor) {
        return this;
    }

    public String toString() {
        return "JIT@" + sig;
    }

    @FunctionalInterface
    public interface JitInvoker {
        <A, B> Term<B> apply(JitLambdaValue<A, B> f, Term<A> x);
    }
}
