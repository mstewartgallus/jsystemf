package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Label;
import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.StackLabelValue;
import com.sstewartgallus.plato.ir.cps.Value;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.type.Stk;

import java.util.Objects;

public record LabelThing<A>(Label<A>variable) implements Thing<Stk<A>> {
    public LabelThing {
        Objects.requireNonNull(variable);
    }

    @Override
    public String toString() {
        return variable.toString();
    }

    @Override
    public TypeDesc<Stk<A>> type() {
        throw null;
    }

    @Override
    public Thing<Stk<A>> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return this;
    }

    @Override
    public Value<Stk<A>> toCps() {
        return new StackLabelValue<>(variable);
    }

    @Override
    public int contains(Variable<?> othervar) {
        return variable.equals(othervar) ? 1 : 0;
    }

}
