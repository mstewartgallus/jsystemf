package com.sstewartgallus.plato;

import java.util.Objects;
import java.util.function.Function;

public record LambdaValue<A, B>(Type<A>domain, Function<Term<A>, Term<B>>f) implements ValueTerm<F<A, B>> {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    public LambdaValue {
        Objects.requireNonNull(domain);
        Objects.requireNonNull(f);
    }

    @Override
    public Type<F<A, B>> type() throws TypeCheckException {
        var range = f.apply(new VarThunk<>(domain, new Id<>(0))).type();
        return new Type.FunType<>(domain, range);
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