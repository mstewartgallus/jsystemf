package com.sstewartgallus.ext.pointfree;

import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.*;

public record ConstantThunk<A, B>(Type<A>left, Type<B>right) implements ThunkTerm<F<A, F<B, A>>> {
    @Override
    public Term<F<A, F<B, A>>> stepThunk() {
        return left.l(x -> right.l(y -> x));
    }

    @Override
    public Type<F<A, F<B, A>>> type() throws TypeCheckException {
        return left.to(right.to(left));
    }

    @Override
    public Term<F<A, F<B, A>>> visitChildren(Visitor visitor) {
        return new ConstantThunk<>(visitor.type(left), visitor.type(right));
    }

    @Override
    public <X> Term<F<X, F<A, F<B, A>>>> pointFree(VarValue<X> varValue) {
        return Term.constant(varValue.type(), this);
    }

    @Override
    public String toString() {
        return "K";
    }
}
