package com.sstewartgallus.plato.runtime;


import com.sstewartgallus.plato.ir.type.Type;
import com.sstewartgallus.plato.java.IntF;

import static java.lang.invoke.MethodHandles.lookup;

public interface U<A> {
    static <A extends U<A>> U<A> of(A action) {
        return action;
    }

    static <A> A evaluate(U<F<A>> fVal) {
        return Helper.EVAL.eval(fVal);
    }

    static int evaluateInteger(U<IntF> fVal) {
        return Helper.EVAL_INT.eval(fVal);
    }

    static <B, A> U<B> apply(U<Fn<A, B>> fVal, A xVal) {
        return Helper.APPLY_FN.apply(fVal, xVal);
    }

    static <B, A> U<B> apply(U<V<A, B>> fVal, Type<A> xVal) {
        return Helper.APPLY_V.apply(fVal, xVal);
    }

    // fixme... figure out how to make private
    interface GenericApplyFn {
        <A, B> U<B> apply(U<Fn<A, B>> f, A x);
    }

    interface GenericApplyV {
        <A, B> U<B> apply(U<V<A, B>> f, Type<A> x);
    }

    interface GenericEval {
        <A> A eval(U<F<A>> value);
    }

    interface EvalInt {
        int eval(U<IntF> value);
    }
}

class Helper {
    static final U.GenericEval EVAL = ActionInvoker.newInstance(lookup(), U.GenericEval.class);
    static final U.EvalInt EVAL_INT = ActionInvoker.newInstance(lookup(), U.EvalInt.class);

    static final U.GenericApplyFn APPLY_FN = ActionInvoker.newInstance(lookup(), U.GenericApplyFn.class);
    static final U.GenericApplyV APPLY_V = ActionInvoker.newInstance(lookup(), U.GenericApplyV.class);
}
