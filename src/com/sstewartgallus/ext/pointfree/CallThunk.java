package com.sstewartgallus.ext.pointfree;

import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.*;

// fixme... desugar to forall instead of passing in types...., all extension combinators should be point free.
public record CallThunk<Z, A, B>() implements ThunkTerm<V<Z, V<A, V<B, F<F<Z, F<A, B>>, F<F<Z, A>, F<Z, B>>>>>>> {
    @Override
    public Type<V<Z, V<A, V<B, F<F<Z, F<A, B>>, F<F<Z, A>, F<Z, B>>>>>>> type() throws TypeCheckException {
        return Type.v(z -> Type.v(a -> Type.v(b -> {
            var f = z.to(a.to(b));
            var x = z.to(a);
            return f.to(x.to(z.to(b)));
        })));
    }

    @Override
    public Term<V<Z, V<A, V<B, F<F<Z, F<A, B>>, F<F<Z, A>, F<Z, B>>>>>>> visitChildren(Visitor visitor) {
        return this;
    }

    @Override
    public <X> Term<F<X, V<Z, V<A, V<B, F<F<Z, F<A, B>>, F<F<Z, A>, F<Z, B>>>>>>>> pointFree(VarValue<X> varValue) {
        return Term.constant(varValue.type(), this);
    }

    @Override
    public String toString() {
        return "S";
    }

    @Override
    public Term<V<Z, V<A, V<B, F<F<Z, F<A, B>>, F<F<Z, A>, F<Z, B>>>>>>> stepThunk() {
        return Term.v((Type<Z> z) -> Term.v((Type<A> a) -> Term.v((Type<B> b) -> {
            var fT = z.to(a.to(b));
            var xT = z.to(a);
            return fT.l((Term<F<Z, F<A, B>>> f) -> xT.l((Term<F<Z, A>> x) -> z.l(zVal -> {
                var fResult = Term.apply(f, zVal);
                var xResult = Term.apply(x, zVal);
                return Term.apply(fResult, xResult);
            })));
        })));
    }
}
