package com.sstewartgallus;

import com.sstewartgallus.plato.ir.cbpv.*;
import com.sstewartgallus.plato.ir.systemf.Variable;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.U;

import java.util.Map;
import java.util.TreeMap;

public class InlineCpbv {
    public static <A> Code<A> inline(Code<A> term) {
        for (; ; ) {
            var opt = inline(new Environment(Map.of()), term);
            if (opt.equals(term)) {
                return term;
            }
            term = opt;
        }
    }

    private static <A> Code<A> inline(Environment env, Code<A> term) {
        if (term instanceof LambdaCode<?, ?> lambdaCode) {
            return (Code) lambda(env, lambdaCode);
        }
        if (term instanceof ApplyCode<?, A> apply) {
            return apply(env, apply);
        }
        if (term instanceof LetToCode<?, A> letBeCode) {
            return letToCode(env, letBeCode);
        }
        if (term instanceof LetBeCode<?, A> letBeCode) {
            return letBeCode(env, letBeCode);
        }
        if (term instanceof ReturnCode returnCode) {
            return returnCode(env, returnCode);
        }
        if (term instanceof ForceCode<A> forceCode) {
            return forceCode(env, forceCode);
        }
        if (term instanceof GlobalCode) {
            return term;
        }
        throw new IllegalArgumentException(term.getClass().toString());
    }

    private static <A, B> Code<Fn<A, B>> lambda(Environment env, LambdaCode<A, B> lambdaCode) {
        var binder = lambdaCode.binder();
        return new LambdaCode<>(binder, inline(env.clear(binder), lambdaCode.body()));
    }

    private static <A> Code<A> forceCode(Environment env, ForceCode<A> forceCode) {
        return ForceCode.of(inline(env, forceCode.thunk()));
    }

    private static <A, B> Code<A> letBeCode(Environment env, LetBeCode<B, A> letBeCode) {
        var binder = letBeCode.binder();
        var x = letBeCode.value();
        var body = letBeCode.body();
        var n = contains(body, binder);

        if (n <= 1) {
            return inline(env.put(binder, x), body);
        }
        return new LetBeCode<>(binder, inline(env, x), inline(env.clear(binder), body));
    }

    private static <A, B> Code<A> letToCode(Environment env, LetToCode<B, A> letBeCode) {
        var binder = letBeCode.binder();
        return LetToCode.of(binder, inline(env, letBeCode.action()), inline(env.clear(binder), letBeCode.body()));
    }

    private static <A> Code<F<A>> returnCode(Environment env, ReturnCode<A> returnCode) {
        return new ReturnCode<>(inline(env, returnCode.literal()));
    }

    private static <A> Literal<U<A>> thunk(Environment env, ThunkLiteral<A> thunk) {
        return ThunkLiteral.of(inline(env, thunk.code()));
    }

    private static <B, A> Code<A> apply(Environment env, ApplyCode<B, A> term) {
        var f = inline(env, term.f());
        var x = inline(env, term.x());
        if (f instanceof LambdaCode<B, A> lambdaCode) {
            var binder = lambdaCode.binder();
            var body = lambdaCode.body();
            return letBeCode(env, new LetBeCode<>(binder, x, body));
        }
        return new ApplyCode<>(f, x);
    }

    private static <A, B> Literal<A> inline(Environment env, Literal<A> term) {
        if (term instanceof LocalLiteral<A> localLit) {
            return inlineLocal(env, localLit);
        }
        if (term instanceof ThunkLiteral thunkLiteral) {
            return inlineThunk(env, thunkLiteral);
        }
        return term;
    }

    private static <A> Literal<A> inlineLocal(Environment env, LocalLiteral<A> localLit) {
        var replacement = env.get(localLit.variable());
        if (replacement == null) {
            return localLit;
        }
        return replacement;
    }

    private static <A> Literal<U<A>> inlineThunk(Environment env, ThunkLiteral<A> thunkLiteral) {
        return ThunkLiteral.of(inline(env, thunkLiteral.code()));
    }

    private static <C> int contains(Literal<C> term, Variable<?> x) {
        if (term instanceof LocalLiteral<?> localCode && localCode.variable().equals(x)) {
            return 1;
        }
        if (term instanceof ThunkLiteral<?> thunkLiteral) {
            return contains(thunkLiteral.code(), x);
        }
        return 0;
    }

    private static <C> int contains(Code<C> term, Variable<?> x) {
        if (term instanceof LambdaCode<?, ?> lambdaCode) {
            // fixme... what about variable scoping ?
            return contains(lambdaCode.body(), x);
        }
        if (term instanceof ApplyCode<?, C> apply) {
            return contains(apply.f(), x) + contains(apply.x(), x);
        }
        if (term instanceof LetToCode<?, C> letToCode) {
            return contains(letToCode.action(), x) + contains(letToCode.body(), x);
        }
        if (term instanceof LetBeCode<?, C> letToCode) {
            return contains(letToCode.value(), x) + contains(letToCode.body(), x);
        }
        if (term instanceof ForceCode<C> letToCode) {
            return contains(letToCode.thunk(), x);
        }
        if (term instanceof ReturnCode<?> returnCode) {
            return contains(returnCode.literal(), x);
        }
        if (term instanceof GlobalCode) {
            return 0;
        }
        throw new IllegalArgumentException(term.getClass().toString());
    }

    private static record Environment(Map<Variable, Literal>variables) {

        public <A> Literal<A> get(Variable<A> variable) {
            return variables.get(variable);
        }

        public <A> Environment put(Variable<A> binder, Literal<A> f) {
            var copy = new TreeMap<>(variables);
            copy.put(binder, f);
            return new Environment(copy);
        }

        public <A> Environment clear(Variable<A> binder) {
            var copy = new TreeMap<>(variables);
            copy.remove(binder);
            return new Environment(copy);
        }
    }
}
