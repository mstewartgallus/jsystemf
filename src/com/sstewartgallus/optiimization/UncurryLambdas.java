package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.tuples.*;
import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.*;

import java.util.function.Function;

interface Vars<A extends Tuple<A>, B, C> {
    Type<A> arguments();

    Signature<A, B, C> sig();

    default Term<C> collapse() {
        var head = arguments();
        return new ApplyThunk<>(new CurryValue<>(sig()), arguments().l(args -> collapse(args, new TupleIndex.Zero<>(head))));
    }

    <X extends Tuple<X>> Term<B> collapse(Term<X> source, TupleIndex<X, A> args);

    record NoVars<A>(Term<A>term) implements Vars<N, A, A> {
        @Override
        public Type<N> arguments() {
            return NilTupleType.NIL;
        }

        @Override
        public Signature<N, A, A> sig() {
            return new Signature.Result<>(term.type());
        }

        @Override
        public Term<A> collapse() {
            return term;
        }

        @Override
        public <X extends Tuple<X>> Term<A> collapse(Term<X> source, TupleIndex<X, N> args) {
            return term;
        }
    }

    record AddVar<A, T extends Tuple<T>, B, C>(Type<A>domain,
                                               Function<Term<A>, Vars<T, B, C>>f) implements Vars<P<A, T>, B, F<A, C>> {
        @Override
        public Type<P<A, T>> arguments() {
            return new TuplePairType<>(domain, f.apply(new VarValue<>(domain)).arguments());
        }

        @Override
        public Signature<P<A, T>, B, F<A, C>> sig() {
            return new Signature.AddArg<>(domain, f.apply(new VarValue<>(domain)).sig());
        }

        @Override
        public <X extends Tuple<X>> Term<B> collapse(Term<X> source, TupleIndex<X, P<A, T>> args) {
            var tail = f.apply(new VarValue<>(domain)).arguments();
            var deref = new AtTupleIndexThunk<>(domain, tail, args);
            var body = f.apply(Term.apply(deref, source));
            return body.collapse(source, new TupleIndex.Succ<>(args));
        }
    }

}

public final class UncurryLambdas {
    private UncurryLambdas() {
    }

    public static <A> Term<A> uncurry(Term<A> root) {
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Term<T> term(Term<T> term) {
                if (!(term instanceof SimpleLambdaValue<?, ?> lambdaValue)) {
                    return term.visitChildren(this);
                }
                return (Term) uncurry(lambdaValue);
            }
        });
    }

    public static <A, B> Term<F<A, B>> uncurry(SimpleLambdaValue<A, B> lambdaValue) {
        return uncurryLambda(lambdaValue).collapse();
    }


    private static <A, B> Vars<?, ?, F<A, B>> uncurryLambda(SimpleLambdaValue<A, B> lambda) {
        var domain = lambda.domain();

        return new Vars.AddVar<>(domain, x -> {
            var body = lambda.apply(x);
            if (!(body instanceof SimpleLambdaValue<?, ?> lambdaBody)) {
                return new Vars.NoVars<>(body);
            }
            return (Vars) uncurryLambda(lambdaBody);
        });
    }
}