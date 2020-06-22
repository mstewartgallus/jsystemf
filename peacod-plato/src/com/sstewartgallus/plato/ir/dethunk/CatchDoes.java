package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Label;
import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.Action;
import com.sstewartgallus.plato.ir.cps.KontAction;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.type.Behaviour;

import java.util.Objects;
import java.util.function.Function;

public record CatchDoes<A>(Label<A>f, Does<A>x) implements Does<A> {
    public CatchDoes {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    //     callCC f = Cont $ \c -> runCont (f (\a -> Cont $ \_ -> c a )) c
    @Override
    public Action<Behaviour> toCps(Function<Action<A>, Action<Behaviour>> k) {
        return k.apply(new KontAction<>(f, x.toCps(k)));
    }

    @Override
    public int contains(Variable<?> variable) {
        return 0;
    }

    @Override
    public Does<A> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        throw null;
    }

    @Override
    public TypeDesc<A> type() {
        throw null;
    }

    @Override
    public String toString() {
        return "catch " + f + ".\n" + x;
    }
}
