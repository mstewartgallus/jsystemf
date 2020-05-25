package com.sstewartgallus.ext.mh;

import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;
import com.sstewartgallus.runtime.TermInvoker;

import java.lang.invoke.MethodHandle;

import static java.lang.invoke.MethodHandles.lookup;

// fixme... could be an abstract class I suppose or another pass could lower to that...
// fixme... establish an invariant that this must always be a function or a forall.
public final class JitValue<A, B> implements Term<F<A, B>> {
    private static final JitInvoker INVOKE_TERM = TermInvoker.newInstance(lookup(), JitInvoker.class);
    private final MethodHandle methodHandle;
    private final String source;
    private final Type<B> range;
    private final Type<A> domain;

    // fixme...
    public JitValue(String source,
                    Type<A> domain,
                    Type<B> range,
                    MethodHandle methodHandle) {
        this.source = source;
        this.domain = domain;
        this.range = range;
        this.methodHandle = methodHandle;
    }

    MethodHandle methodHandle() {
        return methodHandle;
    }

    @Override
    public Type<F<A, B>> type() throws TypeCheckException {
        return domain.to(range);
    }

    @Override
    public Term<F<A, B>> visitChildren(Visitor visitor) {
        return this;
    }

    public String toString() {
        return source;
    }

    @FunctionalInterface
    public interface JitInvoker {
        <A, B> Term<B> apply(JitValue<A, B> f, Term<A> x);
    }
}
