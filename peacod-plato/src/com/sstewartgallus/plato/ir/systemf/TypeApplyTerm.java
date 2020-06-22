package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cbpv.Code;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.V;

import java.util.Objects;

public record TypeApplyTerm<A, B>(Term<V<A, B>>f, TypeDesc<A>x) implements Term<B> {
    public TypeApplyTerm {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public String toString() {
        return "(" + f + " " + x + ")";
    }

    @Override
    public int contains(Variable<?> x) {
        throw null;
    }

    @Override
    public Term<B> visitChildren(TermVisitor visitor) {
        throw null;
    }

    @Override
    public Code<B> toCallByPushValue() {
        throw null;
    }

    @Override
    public TypeDesc<B> type() {
        return TypeDesc.ofApply(f.type(), x);
    }

}
