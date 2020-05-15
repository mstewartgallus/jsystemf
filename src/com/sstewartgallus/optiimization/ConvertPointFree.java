package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.java.IntValue;
import com.sstewartgallus.ext.java.ObjectValue;
import com.sstewartgallus.ext.tuples.*;
import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.ir.PointFree;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Term;

public final class ConvertPointFree {
    private ConvertPointFree() {
    }

    public static <T extends HList<T>, A> PointFree<F<T, A>> pointFree(Term<A> term, VarValue<T> id) {
        if (term instanceof UncurryLambdaThunk<?, ?, A> lambda) {
            return uncurryLambda(lambda, id);
        }
        if (term instanceof CurriedApplyThunk<A> apply) {
            return uncurryApply(apply, id);
        }

        if (term instanceof DerefThunk<?, A> get) {
            return pointFreeGet(get, id);
        }

        if (term instanceof IntValue pure) {
            // fixme..
            return (PointFree) new PointFree.K<>(id.type(), new PointFree.IntValue(pure.value()));
        }

        if (term instanceof ObjectValue<A> pure) {
            // fixme..
            return new PointFree.K<>(id.type(), null);
        }

        if (term instanceof VarValue<A> varValue) {
            if (varValue.variable().equals(id.variable())) {
                return null;
            }
            throw new Error("todo");
        }

        throw new IllegalArgumentException("Unexpected core list " + term);
    }

    private static <T extends HList<T>, X, A extends HList<A>, B extends HList<B>> PointFree<F<T, X>> pointFreeGet(DerefThunk<B, X> get, VarValue<T> id) {
        // fixme... be safer..
        var product = (Getter.Get<T, Cons<X, B>>) get.product();
        return getPointFree(product);
    }

    private static <X, A extends HList<A>, B extends HList<B>> PointFree.Get<A, B, X> getPointFree(Getter.Get<A, Cons<X, B>> product) {
        return new PointFree.Get<A, B, X>(product.list().type(), product.index());
    }

    private static <T extends HList<T>, A> PointFree<F<T, A>> uncurryApply(CurriedApplyThunk<A> apply, VarValue<T> id) {
        return uncurry(apply.body(), id);
    }

    private static <A, T extends HList<T>> PointFree<F<T, A>> uncurry(CurriedApplyThunk.Body<A> body, VarValue<T> id) {
        if (body instanceof CurriedApplyThunk.MonoBody<A> monoBody) {
            return pointFree(monoBody.body(), id);
        }
        var apply = (CurriedApplyThunk.ApplyBody<?, A>) body;
        return toCall(id, apply);
    }

    private static <A, C, T extends HList<T>> PointFree<F<T, A>> toCall(VarValue<T> id, CurriedApplyThunk.ApplyBody<C, A> apply) {
        return new PointFree.Call<>(uncurry(apply.f(), id), pointFree(apply.x(), id));
    }

    private static <T extends HList<T>, A extends HList<A>, B, C> PointFree<F<T, C>> uncurryLambda(UncurryLambdaThunk<A, B, C> lambda, VarValue<T> id) {
        var sig = lambda.sig();
        var f = lambda.f();

        var vType = sig.argType();
        var v = new VarValue<A>(vType);
        var body = pointFree(f.apply(v), v);
        return new PointFree.K<>(id.type(), new PointFree.Lambda<>(sig, body));
    }
}
