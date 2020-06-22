package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.ApplyStackAction;
import com.sstewartgallus.plato.ir.cps.LetToAction;
import com.sstewartgallus.plato.ir.cps.LocalValue;
import com.sstewartgallus.plato.ir.cps.Value;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.type.Stk;

import java.util.Objects;

public record ThunkThing<A>(Does<A>code) implements Thing<Stk<F<Stk<A>>>> {
    public ThunkThing {
        Objects.requireNonNull(code);
    }

    @Override
    public TypeDesc<Stk<F<Stk<A>>>> type() {
        return code.type().thunk();
    }

    @Override
    public Thing<Stk<F<Stk<A>>>> visitChildren(CodeVisitor codeVisitor, LiteralVisitor literalVisitor) {
        return new ThunkThing<>(codeVisitor.onCode(code));
    }

    @Override
    public Value<Stk<F<Stk<A>>>> toCps() {
        Variable<Stk<A>> value = Variable.newInstance(null);
        return Does.toContF(stack ->
                code.toCps(x ->
                        new LetToAction<>(value, stack,
                                new ApplyStackAction<>(new LocalValue<>(value), x))));
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