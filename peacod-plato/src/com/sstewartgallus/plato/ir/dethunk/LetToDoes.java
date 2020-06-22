package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.Action;
import com.sstewartgallus.plato.ir.cps.LetToAction;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.type.Behaviour;

import java.util.Objects;
import java.util.function.Function;

public record LetToDoes<A, B>(Variable<A>binder, Does<F<A>>action, Does<B>body) implements Does<B> {
    public LetToDoes {
        Objects.requireNonNull(binder);
        Objects.requireNonNull(action);
        Objects.requireNonNull(body);
    }

    @Override
    public Action<Behaviour> toCps(Function<Action<B>, Action<Behaviour>> k) {
        return action.toCps(act ->
                body.toCps(bodyVal ->
                        new LetToAction<>(binder, act, k.apply(bodyVal))));
    }

    @Override
    public int contains(Variable<?> variable) {
        // remember to protect against label shadowing
        return action.contains(variable) + (binder.equals(variable) ? 0 : body.contains(variable));
    }

    @Override
    public Does<B> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return new LetToDoes<>(binder, codeVisitor.onCode(action), codeVisitor.onCode(body));
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