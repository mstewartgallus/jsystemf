package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.dethunk.Does;
import com.sstewartgallus.plato.ir.dethunk.LambdaDoes;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Fn;

import java.util.Objects;


public record LambdaCode<A, B>(Variable<A>binder, Code<B>body) implements Code<Fn<A, B>> {
    public LambdaCode {
        Objects.requireNonNull(binder);
        Objects.requireNonNull(body);
    }

    @Override
    public Does<Fn<A, B>> dethunk() {
        return new LambdaDoes<>(binder, body.dethunk());
    }

    @Override
    public int contains(Variable<?> variable) {
        // protect against label shadowing
        return binder.equals(variable) ? 0 : body.contains(variable);
    }

    @Override
    public Code<Fn<A, B>> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return new LambdaCode<>(binder, codeVisitor.onCode(body));
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
