package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Global;
import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.GlobalValue;
import com.sstewartgallus.plato.ir.cps.Value;
import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.Objects;

public record GlobalThing<A>(Global<A>global) implements Thing<A> {
    public GlobalThing {
        Objects.requireNonNull(global);
    }

    @Override
    public String toString() {
        return global.toString();
    }

    @Override
    public TypeDesc<A> type() {
        return global.type();
    }

    @Override
    public Thing<A> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return this;
    }

    @Override
    public Value<A> toCps() {
        return new GlobalValue<>(global);
    }

    @Override
    public int contains(Variable<?> variable) {
        return 0;
    }

}
