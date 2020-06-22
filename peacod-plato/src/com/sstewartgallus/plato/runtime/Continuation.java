package com.sstewartgallus.plato.runtime;

import com.sstewartgallus.plato.runtime.type.Stk;

// fixme... make the continuation class..
public final class Continuation<C> {
    // based on impure functional objects + tail call elimination on the jvm
    // fixme... impure functional objects part isn't really implemented yet...
    // fixme... place all or much of the stack on the context object...
    private int tcc;

    private Stk<F> ip;
    private Object env;

    public Continuation(Stk<F<Stk<F<C>>>> stack) {
        class HalterStk implements FreeStk<C> {
            @Override
            public <C1> void enter(Continuation<C1> context, C value) {
                context.ip = null;
                context.env = value;
            }

            @Override
            public String toString() {
                return "halt";
            }
        }
        ;
        ip = (Stk) stack;
        env = new HalterStk();
    }

    private boolean checkStackDepth() {
        return tcc-- <= 0;
    }

    // fixme... consider throwing throwable ?
    private <B> void save(Stk<F<B>> action, B next) {
        ip = (Stk) action;
        env = next;
    }

    // fixme.. use indy to generate these stubs?
    public <B> void saveOrEnter(Stk<F<B>> action, B next) {
        if (checkStackDepth()) {
            save(action, next);
            return;
        }
        ((FreeStk<B>) action).enter(this, next);
    }

    // probably a good default to start with might be 40
    public void step(int steps) {
        tcc = steps;
        ((FreeStk) ip).enter(this, env);
    }

    public boolean isDone() {
        return ip == null;
    }

    @Override
    public String toString() {
        return "Cont[env=" + env + ", ip=" + ip + "]";
    }
}
