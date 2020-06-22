package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.Action;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.type.Behaviour;
import com.sstewartgallus.plato.runtime.type.U;

import java.util.Objects;
import java.util.function.Function;

public record FixPointDoes<A>(Variable<U<A>>binder, Does<A>action) implements Does<A> {
    public FixPointDoes {
        Objects.requireNonNull(binder);
        Objects.requireNonNull(action);
    }

    @Override
    public Action<Behaviour> toCps(Function<Action<A>, Action<Behaviour>> k) {
        throw null;
    }

    @Override
    public int contains(Variable<?> variable) {
        // remember to protect against label shadowing
        return binder.equals(variable) ? 0 : action.contains(variable);
    }

    @Override
    public Does<A> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return new FixPointDoes<>(binder, codeVisitor.onCode(action));
    }

    @Override
    public TypeDesc<A> type() {
        return action.type();
    }

    @Override
    public String toString() {
        return "fix " + binder + " ∈ " + binder.type() + ".\n" + action;
    }
}