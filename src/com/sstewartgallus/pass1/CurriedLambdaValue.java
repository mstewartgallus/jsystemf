package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;
import java.util.function.Function;

public record CurriedLambdaValue<A, B>(Type<A>domain,
                                       Function<Term<A>, Body<B>>f) implements LambdaValue<A, B> {
    public CurriedLambdaValue {
        Objects.requireNonNull(domain);
        Objects.requireNonNull(f);
    }

    @Override
    public Type<F<A, B>> type() throws TypeCheckException {
        return domain.to(f.apply(new VarThunk<>(domain, new Id<>(0))).type());
    }

    @Override
    public Term<B> apply(Term<A> x) {
        return f.apply(x).toTerm();
    }

    public interface Body<A> {
        Type<A> type() throws TypeCheckException;

        Term<A> toTerm();
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
            var range = f.apply(new VarThunk<>(domain, new Id<>(0))).type();
            return new FunctionNormal<>(domain, range);
        }

        @Override
        public Term<F<A, B>> toTerm() {
            return new CurriedLambdaValue<>(domain, f);
        }

        @Override
        public String toString() {
            var depth = DEPTH.get();
            DEPTH.set(depth + 1);

            String str;
            try {
                var dummy = new VarThunk<>(domain, new Id<>(depth));
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
        @Override
        public Type<A> type() throws TypeCheckException {
            return body.type();
        }

        @Override
        public Term<A> toTerm() {
            return body;
        }
    }
}
