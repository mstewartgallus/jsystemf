package com.sstewartgallus.plato;

import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.ext.variables.VarValue;

import java.util.Objects;
import java.util.function.Function;

public record LambdaValue<A, B>(Type<A>domain,
                                Function<Term<A>, Term<B>>f) implements ValueTerm<F<A, B>>, CoreTerm<F<A, B>> {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    public LambdaValue {
        Objects.requireNonNull(domain);
        Objects.requireNonNull(f);
    }

    @Override
    public Type<F<A, B>> type() throws TypeCheckException {
        var range = f.apply(new VarValue<>(domain, new Id<>(0))).type();
        return new FunctionType<>(domain, range);
    }

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

    public Term<B> apply(Term<A> x) {
        // fixme... typecheck domain?
        return f.apply(x);
    }

    @Override
    public <X> Term<F<A, B>> substitute(Id<X> v, Type<X> replacement) {
        return domain.substitute(v, replacement).l(x -> f.apply(x).substitute(v, replacement));
    }

    @Override
    public <X> Term<F<A, B>> substitute(Id<X> v, Term<X> replacement) {
        return domain.l(x -> f.apply(x).substitute(v, replacement));
    }
}
