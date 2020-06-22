package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.LocalValue;
import com.sstewartgallus.plato.ir.cps.Value;
import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.Objects;

public record LocalThing<A>(Variable<A>variable) implements Thing<A> {
    public LocalThing {
        Objects.requireNonNull(variable);
    }

    @Override
    public String toString() {
        return variable.toString();
    }

    @Override
    public TypeDesc<A> type() {
        return variable.type();
    }

    @Override
    public Thing<A> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return this;
    }

    @Override
    public Value<A> toCps() {
        return new LocalValue<>(variable);
    }

    @Override
    public int contains(Variable<?> othervar) {
        return variable.equals(othervar) ? 1 : 0;
    }

}
