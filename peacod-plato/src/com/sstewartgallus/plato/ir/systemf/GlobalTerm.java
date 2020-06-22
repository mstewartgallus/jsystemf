package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.Global;
import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cbpv.Code;
import com.sstewartgallus.plato.ir.cbpv.ForceCode;
import com.sstewartgallus.plato.ir.cbpv.GlobalLiteral;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.type.Stk;

public record GlobalTerm<A>(Global<Stk<F<Stk<A>>>>global) implements Term<A> {
    @Override
    public String toString() {
        return global.toString();
    }

    @Override
    public int contains(Variable<?> x) {
        return 0;
    }

    @Override
    public Term<A> visitChildren(TermVisitor visitor) {
        return this;
    }

    @Override
    public Code<A> toCallByPushValue() {
        return new ForceCode<>(new GlobalLiteral<>(global));
    }

    @Override
    public TypeDesc<A> type() {
        var fType = (TypeDesc.TypeApplicationDesc<A, Stk<F<Stk<A>>>>) global.type();
        return fType.x();
    }
}
