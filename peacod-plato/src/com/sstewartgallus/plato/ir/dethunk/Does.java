package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.*;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.type.Behaviour;
import com.sstewartgallus.plato.runtime.type.Stk;

import java.util.function.Function;

public interface Does<A> {

    static <A> Value<Stk<F<A>>> toContF(Function<Action<F<A>>, Action<Behaviour>> k) {
        Variable<A> label = Variable.newInstance(null);
        return new SimpleLambdaValue<>(label, k.apply(new ReturnAction<>(new LocalValue<>(label))));
    }

    Action<Behaviour> toCps(Function<Action<A>, Action<Behaviour>> k);

    int contains(Variable<?> variable);

    Does<A> visitChildren(CodeVisitor codeVisitor,
                          LiteralVisitor literalVisitor);

    TypeDesc<A> type();

    default Value<Stk<F<Stk<A>>>> toCps() {
        return new ThunkThing<>(this).toCps();
    }
}

