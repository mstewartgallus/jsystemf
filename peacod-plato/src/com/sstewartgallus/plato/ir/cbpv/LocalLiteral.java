package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.dethunk.LocalThing;
import com.sstewartgallus.plato.ir.dethunk.Thing;
import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.Objects;

public record LocalLiteral<A>(Variable<A>variable) implements Literal<A> {
    public LocalLiteral {
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
    public Literal<A> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return this;
    }

    @Override
    public Thing<A> dethunk() {
        return new LocalThing<>(variable);
    }

    @Override
    public int contains(Variable<?> othervar) {
        return variable.equals(othervar) ? 1 : 0;
    }

}
