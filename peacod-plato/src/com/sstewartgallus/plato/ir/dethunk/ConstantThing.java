package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Constant;
import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.ConstantValue;
import com.sstewartgallus.plato.ir.cps.Value;
import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.Objects;

public record ConstantThing<A>(Constant<A>constant) implements Thing<A> {
    public ConstantThing {
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
    public Thing<A> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return this;
    }

    @Override
    public Value<A> toCps() {
        return new ConstantValue<>(constant);
    }

    @Override
    public int contains(Variable<?> variable) {
        return 0;
    }
}
