package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.Global;
import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.dethunk.GlobalThing;
import com.sstewartgallus.plato.ir.dethunk.Thing;
import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.Objects;

public record GlobalLiteral<A>(Global<A>global) implements Literal<A> {
    public GlobalLiteral {
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
    public Literal<A> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return this;
    }

    @Override
    public Thing<A> dethunk() {
        return new GlobalThing<>(global);
    }

    @Override
    public int contains(Variable<?> variable) {
        return 0;
    }

}
