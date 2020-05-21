package com.sstewartgallus.ext.mh;

import com.sstewartgallus.plato.*;
import com.sstewartgallus.runtime.TermInvoker;

import java.lang.invoke.MethodHandle;

import static java.lang.invoke.MethodHandles.lookup;

// fixme... could be an abstract class I suppose or another pass could lower to that...
// fixme... establish an invariant that this must always be a function or a forall.
public final class JitValue<A> implements ThunkTerm<A>, ValueTerm<A> {

    private static final JitInvoker INVOKE_TERM = TermInvoker.newInstance(lookup(), JitInvoker.class);
    private final MethodHandle methodHandle;
    private final Type<A> type;
    private final String source;

    // fixme...
    public JitValue(String source,
                    Type<A> type,
                    MethodHandle methodHandle) {
        this.source = source;
        this.type = type;
        this.methodHandle = methodHandle;
    }

    MethodHandle methodHandle() {
        return methodHandle;
    }

    @Override
    public <C> Term<C> step(TermCont<A, C> k) {
        var self = this;
        return k.apply(INVOKE_TERM.apply(self));
    }

    @Override
    public Type<A> type() throws TypeCheckException {
        return type;
    }

    @Override
    public Term<A> visitChildren(Visitor visitor) {
        return this;
    }

    public String toString() {
        return source;
    }

    @FunctionalInterface
    public interface JitInvoker {
        <A> ValueTerm<A> apply(JitValue<A> f);
    }
}
