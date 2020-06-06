package com.sstewartgallus.plato.cbpv;

import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.syntax.type.Type;

import java.util.function.Function;

public record LetToCode<A, B>(Type<A>domain, Code<F<A>>action, Function<Literal<A>, Code<B>>f) implements Code<B> {
    public static <A, B> Code<B> of(Type<A> domain, Code<F<A>> action, Function<Literal<A>, Code<B>> f) {
        if (action instanceof ReturnCode) {
            var ret = (ReturnCode<A>) action;
            return f.apply(ret.literal());
        }
        return new LetToCode<A, B>(domain, action, f);
    }

    @Override
    public Type<B> type() {
        throw null;
    }

    @Override
    public U<B> interpret(InterpreterEnvironment environment) {
        // fixme... correct types...
        var v = new VarLiteral<A>(domain);
        var body = f.apply(v);

        var effect = action.interpret(environment).action();

        var result = effect.evaluate();
        return body.interpret(environment.put(v, result));
    }

    @Override
    public String toString() {
        var v = new VarLiteral<A>(domain);
        return action + " to " + v + ".\n" + f.apply(v);
    }
}