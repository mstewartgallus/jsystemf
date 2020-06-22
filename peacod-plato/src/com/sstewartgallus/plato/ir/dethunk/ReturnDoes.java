package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.Action;
import com.sstewartgallus.plato.ir.cps.ReturnAction;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.type.Behaviour;

import java.util.Objects;
import java.util.function.Function;

public record ReturnDoes<A>(Thing<A>literal) implements Does<F<A>> {
    public ReturnDoes {
        Objects.requireNonNull(literal);
    }

    @Override
    public String toString() {
        return "return " + literal;
    }

    @Override
    public Action<Behaviour> toCps(Function<Action<F<A>>, Action<Behaviour>> k) {
        return k.apply(new ReturnAction<>(literal.toCps()));
    }

    @Override
    public int contains(Variable<?> variable) {
        return literal.contains(variable);
    }

    @Override
    public Does<F<A>> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return new ReturnDoes<>(literalVisitor.onLiteral(literal));
    }

    @Override
    public TypeDesc<F<A>> type() {
        return literal.type().returns();
    }

}
