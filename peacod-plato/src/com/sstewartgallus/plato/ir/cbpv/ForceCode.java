package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.Label;
import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.dethunk.*;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.type.Stk;

import java.util.Objects;

public record ForceCode<A>(Literal<Stk<F<Stk<A>>>>thunk) implements Code<A> {
    public ForceCode {
        Objects.requireNonNull(thunk);
    }

    @Override
    public Does<A> dethunk() {
        Thing<Stk<F<Stk<A>>>> t = thunk.dethunk();
        Label<A> label = Label.newInstance(null);
        return new CatchDoes<>(label, new ThrowDoes<>(t, new ReturnDoes<>(new LabelThing<>(label))));
    }

    @Override
    public int contains(Variable<?> variable) {
        return thunk.contains(variable);
    }

    @Override
    public Code<A> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return new ForceCode<>(literalVisitor.onLiteral(thunk));
    }

    @Override
    public TypeDesc<A> type() {
        var fType = (TypeDesc.TypeApplicationDesc<A, Stk<F<Stk<A>>>>) thunk.type();
        return fType.x();
    }

    @Override
    public String toString() {
        return "! " + thunk;
    }
}