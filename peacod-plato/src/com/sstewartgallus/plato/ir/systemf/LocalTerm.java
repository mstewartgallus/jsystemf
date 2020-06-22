package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cbpv.Code;
import com.sstewartgallus.plato.ir.cbpv.ForceCode;
import com.sstewartgallus.plato.ir.cbpv.LocalLiteral;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.type.Stk;

public record LocalTerm<A>(Variable<Stk<F<Stk<A>>>>variable) implements Term<A> {

    @Override
    public String toString() {
        return variable.toString();
    }

    @Override
    public int contains(Variable<?> x) {
        return variable.equals(x) ? 1 : 0;
    }

    @Override
    public Term<A> visitChildren(TermVisitor visitor) {
        return this;
    }

    @Override
    public Code<A> toCallByPushValue() {
        return new ForceCode<>(new LocalLiteral<>(variable));
    }

    @Override
    public TypeDesc<A> type() {
        var fType = (TypeDesc.TypeApplicationDesc<A, Stk<F<Stk<A>>>>) variable.type();
        return fType.x();
    }
}
