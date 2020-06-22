package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.dethunk.Does;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.type.U;

import java.util.Objects;

public record FixPointCode<A>(Variable<U<A>>binder, Code<A>action) implements Code<A> {
    public FixPointCode {
        Objects.requireNonNull(binder);
        Objects.requireNonNull(action);
    }

    @Override
    public Does<A> dethunk() {
        throw null;
    }

    @Override
    public int contains(Variable<?> variable) {
        // remember to protect against label shadowing
        return binder.equals(variable) ? 0 : action.contains(variable);
    }

    @Override
    public Code<A> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return new FixPointCode<>(binder, codeVisitor.onCode(action));
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