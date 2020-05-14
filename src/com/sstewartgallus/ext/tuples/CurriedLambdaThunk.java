package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.ext.pretty.PrettyValue;
import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.plato.*;

import java.util.Objects;
import java.util.function.Function;

/**
 * Fixme... consider lowering to an applicative functor style...
 * <p>
 * pure (\f x y -> f (x, y)) <*> F <*> X <*> Y
 *
 * @param <A>
 */
public record CurriedLambdaThunk<A>(Body<A>body) implements ThunkTerm<A> {
    public CurriedLambdaThunk {
        Objects.requireNonNull(body);
    }

    @Override
    public Type<A> type() throws TypeCheckException {
        return body.type();
    }

    @Override
    public Term<A> stepThunk() {
        return body.toTerm();
    }

    @Override
    public String toString() {
        return "(" + body + ")";
    }

    public interface Body<A> {
        Type<A> type() throws TypeCheckException;

        Term<A> toTerm();

        <X> Body<A> substitute(Id<X> v, Term<X> replacement);
    }

    public static record LambdaBody<A, B>(Type<A>domain,
                                          Function<Term<A>, Body<B>>f) implements Body<F<A, B>> {
        private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

        public LambdaBody {
            Objects.requireNonNull(domain);
            Objects.requireNonNull(f);
        }

        @Override
        public Type<F<A, B>> type() throws TypeCheckException {
            try (var pretty = PrettyValue.generate(domain)) {
                var range = f.apply(pretty).type();
                return new FunctionType<>(domain, range);
            }
        }

        @Override
        public Term<F<A, B>> toTerm() {
            return new LambdaValue<>(domain, x -> new CurriedLambdaThunk<>(f.apply(x)));
        }

        @Override
        public <X> Body<F<A, B>> substitute(Id<X> v, Term<X> replacement) {
            return new LambdaBody<>(domain, x -> f.apply(x).substitute(v, replacement));
        }

        @Override
        public String toString() {
            try (var pretty = PrettyValue.generate(domain)) {
                var body = f.apply(pretty);
                return "{" + pretty + ": " + domain + "} → " + body;
            }
        }
    }

    public record MainBody<A>(Term<A>body) implements Body<A> {
        public MainBody {
            Objects.requireNonNull(body);
        }

        @Override
        public <X> Body<A> substitute(Id<X> v, Term<X> replacement) {
            return new MainBody<>(body.substitute(v, replacement));
        }

        @Override
        public Type<A> type() throws TypeCheckException {
            return body.type();
        }

        @Override
        public Term<A> toTerm() {
            return body;
        }

        @Override
        public String toString() {
            return body.toString();
        }
    }
}