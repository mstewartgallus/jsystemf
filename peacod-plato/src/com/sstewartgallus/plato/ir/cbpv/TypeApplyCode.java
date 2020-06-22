package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.dethunk.Does;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.V;
import com.sstewartgallus.plato.runtime.type.Type;

import java.util.Objects;

public record TypeApplyCode<A, B>(Code<V<A, B>>f, Type<A>x) implements Code<B> {
    public TypeApplyCode {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public String toString() {
        return x + "\n" + f;
    }

    @Override
    public Does<B> dethunk() {
        throw null;
    }

    @Override
    public int contains(Variable<?> variable) {
        throw null;
    }

    @Override
    public Code<B> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        throw null;
    }

    @Override
    public TypeDesc<B> type() {
        return null;
    }

}
