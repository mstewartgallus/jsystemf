package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;

import java.util.Objects;

public record ReturnCode<A>(Literal<A>literal) implements Code<F<A>> {
    public ReturnCode {
        Objects.requireNonNull(literal);
    }

    @Override
    public String toString() {
        return "return " + literal;
    }

    @Override
    public TypeDesc<F<A>> type() {
        return literal.type().returns();
    }

}
