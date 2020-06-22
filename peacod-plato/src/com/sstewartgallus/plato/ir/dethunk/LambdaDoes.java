package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.Action;
import com.sstewartgallus.plato.ir.cps.LambdaAction;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.type.Behaviour;

import java.util.Objects;
import java.util.function.Function;


public record LambdaDoes<A, B>(Variable<A>binder, Does<B>body) implements Does<Fn<A, B>> {
    public LambdaDoes {
        Objects.requireNonNull(binder);
        Objects.requireNonNull(body);
    }

    @Override
    public Action<Behaviour> toCps(Function<Action<Fn<A, B>>, Action<Behaviour>> k) {
        return body.toCps(theBody -> k.apply(new LambdaAction<>(binder, theBody)));
    }

    @Override
    public int contains(Variable<?> variable) {
        // protect against label shadowing
        return binder.equals(variable) ? 0 : body.contains(variable);
    }

    @Override
    public Does<Fn<A, B>> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return new LambdaDoes<>(binder, codeVisitor.onCode(body));
    }

    @Override
    public final TypeDesc<Fn<A, B>> type() {
        return binder.type().toFn(body.type());
    }

    @Override
    public final String toString() {
        return "λ " + binder.name() + " ∈ " + binder.type() + " →\n" + body;
    }
}
