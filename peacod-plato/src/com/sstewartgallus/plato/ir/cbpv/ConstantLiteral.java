package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.Constant;
import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.dethunk.ConstantThing;
import com.sstewartgallus.plato.ir.dethunk.Thing;
import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.Objects;

public record ConstantLiteral<A>(Constant<A>constant) implements Literal<A> {
    public ConstantLiteral {
        Objects.requireNonNull(constant);
    }

    @Override
    public String toString() {
        return constant.toString();
    }

    @Override
    public TypeDesc<A> type() {
        return constant.type();
    }

    @Override
    public Literal<A> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return this;
    }

    @Override
    public Thing<A> dethunk() {
        return new ConstantThing<>(constant);
    }

    @Override
    public int contains(Variable<?> variable) {
        return 0;
    }
}
