package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cbpv.ApplyCode;
import com.sstewartgallus.plato.ir.cbpv.Code;
import com.sstewartgallus.plato.ir.cbpv.ThunkLiteral;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.type.Stk;

import java.util.Objects;

public record ApplyTerm<A, B>(Term<Fn<Stk<F<Stk<A>>>, B>>f,
                              Term<A>x) implements Term<B> {
    public ApplyTerm {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public int contains(Variable<?> x) {
        return 0;
    }

    @Override
    public Term<B> visitChildren(TermVisitor visitor) {
        return new ApplyTerm<>(visitor.onTerm(f), visitor.onTerm(x));
    }

    @Override
    public Code<B> toCallByPushValue() {
        var fCbpv = f.toCallByPushValue();
        var xCbpv = x.toCallByPushValue();
        return new ApplyCode<>(fCbpv, new ThunkLiteral<>(xCbpv));
    }

    @Override
    public TypeDesc<B> type() {
        var fType = (TypeDesc.TypeApplicationDesc<B, Fn<Stk<F<Stk<A>>>, B>>) f.type();
        return fType.x();
    }

    @Override
    public String toString() {
        return "(" + f + " " + x + ")";
    }
}
