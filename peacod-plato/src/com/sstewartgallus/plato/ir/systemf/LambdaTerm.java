package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cbpv.Code;
import com.sstewartgallus.plato.ir.cbpv.LambdaCode;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.type.Stk;


public record LambdaTerm<A, B>(Variable<Stk<F<Stk<A>>>>binder, Term<B>body) implements Term<Fn<Stk<F<Stk<A>>>, B>> {
    @Override
    public int contains(Variable<?> x) {
        // if there is label shadowing we do not contain the label
        return binder.equals(x) ? 0 : body.contains(x);
    }

    @Override
    public Term<Fn<Stk<F<Stk<A>>>, B>> visitChildren(TermVisitor visitor) {
        return new LambdaTerm<>(binder, visitor.onTerm(body));
    }

    @Override
    public Code<Fn<Stk<F<Stk<A>>>, B>> toCallByPushValue() {
        return new LambdaCode<>(binder, body.toCallByPushValue());
    }

    @Override
    public final TypeDesc<Fn<Stk<F<Stk<A>>>, B>> type() {
        return (binder.type()).toFn(body.type());
    }

    @Override
    public final String toString() {
        return "(λ (" + binder.name() + " " + binder.type() + ") " + body + ")";
    }
}
