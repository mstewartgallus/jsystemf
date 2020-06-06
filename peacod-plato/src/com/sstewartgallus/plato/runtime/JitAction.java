package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.syntax.type.Type;

import java.lang.invoke.MethodHandle;

import static java.lang.invoke.MethodHandles.lookup;

// fixme... could be an abstract class I suppose or another pass could lower to that...
// fixme... establish an invariant that this must always be a function or a forall.
public final class JitAction<A extends U<A>> implements U<A> {
    @Override
    public A action() {
        throw null;
    }

    // fixme... make not public
    public interface FunInvoker {
        Object invoke(JitAction<?> action, Object value);
    }

    private static final FunInvoker FUN_INVOKER = ActionInvoker.newInstance(lookup(), FunInvoker.class);

    private final MethodHandle methodHandle;
    private final String source;
    private final Type<A> type;

    public JitAction(String source,
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
    public String toString() {
        return source;
    }
}
