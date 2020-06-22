package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.dethunk.Does;
import com.sstewartgallus.plato.ir.dethunk.ReturnDoes;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;

import java.util.Objects;

public record ReturnCode<A>(Literal<A>literal) implements Code<F<A>> {
    public ReturnCode {
        Objects.requireNonNull(literal);
    }

    @Override
    public String toString() {
        return "return " + literal;
    }

    @Override
    public Does<F<A>> dethunk() {
        return new ReturnDoes<>(literal.dethunk());
    }

    @Override
    public int contains(Variable<?> variable) {
        return literal.contains(variable);
    }

    @Override
    public Code<F<A>> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return new ReturnCode<>(literalVisitor.onLiteral(literal));
    }

    @Override
    public TypeDesc<F<A>> type() {
        return literal.type().returns();
    }

}
