package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.Action;
import com.sstewartgallus.plato.ir.cps.ApplyStackAction;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.type.Behaviour;
import com.sstewartgallus.plato.runtime.type.Stk;

import java.util.Objects;
import java.util.function.Function;

public record ThrowDoes<A, B>(Thing<Stk<A>>f, Does<A>x) implements Does<B> {
    public ThrowDoes {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public String toString() {
        return "throw " + f + ".\n" + x;
    }


    @Override
    public Action<Behaviour> toCps(Function<Action<B>, Action<Behaviour>> k) {
        return x.toCps(xVal -> new ApplyStackAction<>(f.toCps(), xVal));
    }

    @Override
    public int contains(Variable<?> variable) {
        return 0;
    }

    @Override
    public Does<B> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return null;
    }

    @Override
    public TypeDesc<B> type() {
        return null;
    }
}
