package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;
import java.util.function.Function;

public record CurriedLambdaValue<A>(Body<A>body) implements ThunkTerm<A> {
    public CurriedLambdaValue {
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
        return body.toString();
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
            var range = f.apply(new VarValue<>(domain, new Id<>(0))).type();
            return new FunctionNormal<>(domain, range);
        }

        @Override
        public Term<F<A, B>> toTerm() {
            return new LambdaValue<>(domain, x -> new CurriedLambdaValue<>(f.apply(x)));
        }

        @Override
        public <X> Body<F<A, B>> substitute(Id<X> v, Term<X> replacement) {
            return new LambdaBody<>(domain, x -> f.apply(x).substitute(v, replacement));
        }

        // fixme... have a common id generator for depth...
        @Override
        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var dummy = new VarValue<>(domain, new Id<>(depth));
                var body = f.apply(dummy);
                String bodyStr = body.toString();

                str = "({" + dummy + ": " + domain + "} → " + bodyStr + ")";
            } finally {
                DEPTH.set(depth);
                if (depth == 0) {
                    DEPTH.remove();
                }
            }
            return str;
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
