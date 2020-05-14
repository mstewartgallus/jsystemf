package com.sstewartgallus.optiimization;

import com.sstewartgallus.extensions.tuples.*;
import com.sstewartgallus.ir.PointFree;
import com.sstewartgallus.plato.*;

public final class ConvertPointFree {
    private ConvertPointFree() {
    }

    public static <T extends HList<T>, A> PointFree<F<T, A>> pointFree(Term<A> term, Type<T> argType, Id<T> id, IdGen ids) {
        if (term instanceof UncurryLambdaThunk<?, ?, A> lambda) {
            return uncurryLambda(lambda, ids, argType, id);
        }
        if (term instanceof CurriedApplyThunk<A> apply) {
            return uncurryApply(apply, ids, argType, id);
        }

        if (term instanceof DerefThunk<?, A> get) {
            return pointFreeGet(get, ids, argType, id);
        }

        if (!(term instanceof CoreTerm<A> core)) {
            throw new IllegalArgumentException("Unexpected term " + term);
        }

        if (core instanceof PureValue<A> pure) {
            // fixme..
            return new PointFree.K<>(argType, null);
        }

        if (core instanceof VarValue<A>) {
            if (((VarValue<A>) core).variable().equals(id)) {
                return null;
            }
            throw new Error("todo");
        }

        throw new IllegalArgumentException("Unexpected core list " + term);
    }

    private static <T extends HList<T>, X, A extends HList<A>, B extends HList<B>> PointFree<F<T, X>> pointFreeGet(DerefThunk<B, X> get, IdGen ids, Type<T> argType, Id<T> id) {
        // fixme... be safer..
        var product = (Getter.Get<T, HList.Cons<X, B>>) get.product();
        return getPointFree(product);
    }

    private static <X, A extends HList<A>, B extends HList<B>> PointFree.Get<A, B, X> getPointFree(Getter.Get<A, HList.Cons<X, B>> product) {
        return new PointFree.Get<A, B, X>(product.list().type(), product.index());
    }

    private static <T extends HList<T>, A> PointFree<F<T, A>> uncurryApply(CurriedApplyThunk<A> apply, IdGen ids, Type<T> argType, Id<T> id) {
        throw null;
    }

    private static <T extends HList<T>, A extends HList<A>, B, C> PointFree<F<T, C>> uncurryLambda(UncurryLambdaThunk<A, B, C> lambda, IdGen ids, Type<T> argType, Id<T> id) {
        var sig = lambda.sig();
        var f = lambda.f();

        var v = ids.<A>createId();
        var vType = sig.argType();
        var body = pointFree(f.apply(new VarValue<A>(vType, v)), vType, v, ids);
        return new PointFree.K<>(argType, new PointFree.Lambda<>(sig, body));
    }
}
