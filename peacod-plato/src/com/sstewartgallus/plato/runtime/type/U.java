package com.sstewartgallus.plato.runtime.type;


import com.sstewartgallus.plato.runtime.Continuation;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.FreeStk;

// U a = stk R (f (stk R a)) = f (a -> R) -> R ?
public abstract class U<A> implements FreeStk<Stk<A>> {
    public static <A> U<F<A>> box(A value) {
        return new U<>() {
            @Override
            public <C> void enter(Continuation<C> context, Stk<F<A>> stack) {
                context.saveOrEnter(stack, value);
            }

            @Override
            public String toString() {
                return "box " + value;
            }
        };
    }

    @Override
    public abstract <C> void enter(Continuation<C> context, Stk<A> action);
}