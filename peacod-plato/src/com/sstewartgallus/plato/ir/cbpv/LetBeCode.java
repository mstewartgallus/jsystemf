package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.dethunk.Does;
import com.sstewartgallus.plato.ir.dethunk.LetBeDoes;
import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.Objects;

public record LetBeCode<A, B>(Variable<A>binder, Literal<A>value, Code<B>body) implements Code<B> {
    public LetBeCode {
        Objects.requireNonNull(binder);
        Objects.requireNonNull(value);
        Objects.requireNonNull(body);
    }

    @Override
    public Does<B> dethunk() {
        return new LetBeDoes<>(binder, value.dethunk(), body.dethunk());
    }

    @Override
    public int contains(Variable<?> variable) {
        // remember to protect against label shadowing.
        return value.contains(variable) + (binder.equals(variable) ? 0 : body.contains(variable));
    }

    @Override
    public Code<B> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return new LetBeCode<>(binder, literalVisitor.onLiteral(value), codeVisitor.onCode(body));
    }

    @Override
    public TypeDesc<B> type() {
        return body.type();
    }

    @Override
    public String toString() {
        return value + " be " + binder + " ∈ " + binder.type() + ".\n" + body;
    }
}