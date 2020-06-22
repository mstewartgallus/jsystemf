package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.Action;
import com.sstewartgallus.plato.ir.cps.ApplyAction;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.type.Behaviour;

import java.util.Objects;
import java.util.function.Function;

public record ApplyDoes<A, B>(Does<Fn<A, B>>f, Thing<A>x) implements Does<B> {
    public ApplyDoes {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Action<Behaviour> toCps(Function<Action<B>, Action<Behaviour>> k) {
        return f.toCps(fValue -> k.apply(new ApplyAction<>(fValue, x.toCps())));
    }

    @Override
    public int contains(Variable<?> variable) {
        return f.contains(variable) + x.contains(variable);
    }

    @Override
    public Does<B> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return new ApplyDoes<>(codeVisitor.onCode(f), literalVisitor.onLiteral(x));
    }

    @Override
    public TypeDesc<B> type() {
        var fType = (TypeDesc.TypeApplicationDesc<B, Fn<A, B>>) f.type();
        return fType.x();
    }

    @Override
    public String toString() {
        return x + "\n" + f;
    }
}
