package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cbpv.Code;
import com.sstewartgallus.plato.ir.cbpv.FixPointCode;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.type.U;


public record FixPointTerm<A>(Variable<U<A>>binder, Term<A>value) implements Term<A> {
    @Override
    public int contains(Variable<?> x) {
        return binder.equals(x) ? 0 : value.contains(x);
    }

    @Override
    public Term<A> visitChildren(TermVisitor visitor) {
        return new FixPointTerm<>(binder, visitor.onTerm(value));
    }

    @Override
    public Code<A> toCallByPushValue() {
        return new FixPointCode<>(binder, value.toCallByPushValue());
    }

    @Override
    public final TypeDesc<A> type() {
        return value.type();
    }

    @Override
    public final String toString() {
        return "(fix " + binder.name() + " " + value + ")";
    }
}
