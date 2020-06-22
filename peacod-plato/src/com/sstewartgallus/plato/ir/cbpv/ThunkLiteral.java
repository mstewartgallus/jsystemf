package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.dethunk.Thing;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.type.Stk;

import java.util.Objects;

public record ThunkLiteral<A>(Code<A>code) implements Literal<Stk<F<Stk<A>>>> {
    public ThunkLiteral {
        Objects.requireNonNull(code);
    }

    @Override
    public TypeDesc<Stk<F<Stk<A>>>> type() {
        return code.type().thunk();
    }

    @Override
    public Literal<Stk<F<Stk<A>>>> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return new ThunkLiteral<>(codeVisitor.onCode(code));
    }

    @Override
    public Thing<Stk<F<Stk<A>>>> dethunk() {
        throw null;
    }

    @Override
    public int contains(Variable<?> variable) {
        return code.contains(variable);
    }

    @Override
    public String toString() {
        return "thunk {" + ("\n" + code).replace("\n", "\n\t") + "\n}";
    }
}