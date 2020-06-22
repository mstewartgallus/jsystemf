package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.Action;
import com.sstewartgallus.plato.ir.cps.LetBeAction;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.type.Behaviour;

import java.util.Objects;
import java.util.function.Function;

public record LetBeDoes<A, B>(Variable<A>binder, Thing<A>value, Does<B>body) implements Does<B> {
    public LetBeDoes {
        Objects.requireNonNull(binder);
        Objects.requireNonNull(value);
        Objects.requireNonNull(body);
    }

    @Override
    public Action<Behaviour> toCps(Function<Action<B>, Action<Behaviour>> k) {
        return new LetBeAction<>(binder, value.toCps(), body.toCps(k));
    }

    @Override
    public int contains(Variable<?> variable) {
        // remember to protect against label shadowing.
        return value.contains(variable) + (binder.equals(variable) ? 0 : body.contains(variable));
    }

    @Override
    public Does<B> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return new LetBeDoes<>(binder, literalVisitor.onLiteral(value), codeVisitor.onCode(body));
    }

    @Override
    public TypeDesc<B> type() {
        return body.type();
    }

    @Override
    public String toString() {
        return value + " be " + binder + ".\n" + body;
    }
}