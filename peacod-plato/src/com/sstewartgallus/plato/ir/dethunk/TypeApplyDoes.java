package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.Action;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.V;
import com.sstewartgallus.plato.runtime.type.Behaviour;
import com.sstewartgallus.plato.runtime.type.Type;

import java.util.Objects;
import java.util.function.Function;

public record TypeApplyDoes<A, B>(Does<V<A, B>>f, Type<A>x) implements Does<B> {
    public TypeApplyDoes {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public String toString() {
        return x + "\n" + f;
    }

    @Override
    public Action<Behaviour> toCps(Function<Action<B>, Action<Behaviour>> k) {
        throw null;
    }

    @Override
    public int contains(Variable<?> variable) {
        throw null;
    }

    @Override
    public Does<B> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        throw null;
    }

    @Override
    public TypeDesc<B> type() {
        return null;
    }

}
