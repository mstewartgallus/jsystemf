package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.dethunk.Does;
import com.sstewartgallus.plato.ir.dethunk.LetToDoes;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;

import java.util.Objects;

public record LetToCode<A, B>(Variable<A>binder, Code<F<A>>action, Code<B>body) implements Code<B> {
    public LetToCode {
        Objects.requireNonNull(binder);
        Objects.requireNonNull(action);
        Objects.requireNonNull(body);
    }

    @Override
    public Does<B> dethunk() {
        return new LetToDoes<>(binder, action.dethunk(), body.dethunk());
    }

    @Override
    public int contains(Variable<?> variable) {
        // remember to protect against label shadowing
        return action.contains(variable) + (binder.equals(variable) ? 0 : body.contains(variable));
    }

    @Override
    public Code<B> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return new LetToCode<>(binder, codeVisitor.onCode(action), codeVisitor.onCode(body));
    }

    @Override
    public TypeDesc<B> type() {
        return body.type();
    }

    @Override
    public String toString() {
        return action + " to " + binder + " ∈ " + binder.type() + ".\n" + body;
    }
}