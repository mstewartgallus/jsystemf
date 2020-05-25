package com.sstewartgallus.plato;

import com.sstewartgallus.interpreter.Code;
import com.sstewartgallus.interpreter.ConstantCode;
import com.sstewartgallus.interpreter.Interpreter;
import com.sstewartgallus.interpreter.PureCode;

import java.util.Objects;

public record ForceCode<A>(Code<Term<A>>x) implements Code<Term<A>> {
    public ForceCode {
        Objects.requireNonNull(x);
    }

    private static <A> boolean notNormal(Term<A> x) {
        return x instanceof ApplyTerm;
    }

    private static <A> Term<A> tick(Term<A> x) {
        if (x instanceof ApplyTerm<?, A> applyTerm) {
            return tickApply(applyTerm);
        }
        return x;
    }

    private static <A, B> Term<B> tickApply(ApplyTerm<A, B> applyTerm) {
        var f = applyTerm.f();
        var x = applyTerm.x();

        if (f instanceof LambdaTerm<A, B> fLambda) {
            return fLambda.apply(x);
        }

        var fTick = tick(f);
        return new ApplyTerm<>(fTick, x);
    }

    @Override
    public <X> Interpreter<?, X> execute(Interpreter<Term<A>, X> interpreter) {
        // fixme... looping is tricky...
        return interpreter.loop(x, ForceCode::notNormal, ForceCode::tick);
    }

    @Override
    public String toString() {
        return "!" + x;
    }
}
