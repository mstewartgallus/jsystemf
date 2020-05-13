package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;

// fixme... is there a need for this?
public record UncurryValue<X extends HList<X>, A, B>(Type<A>domain,
                                                     Term<F<HList.Cons<A, X>, B>>f) implements FunctionValue<A, F<X, B>> {
    public UncurryValue {
        Objects.requireNonNull(domain);
        Objects.requireNonNull(f);
    }

    @Override
    public Type<F<A, F<X, B>>> type() throws TypeCheckException {
        throw null;
        //    return domain.to(f.apply(new VarValue<>(domain, new Id<>(0))).type());
    }

    @Override
    public Term<F<X, B>> apply(Term<A> x) {
        return new ClosureValue<>(f, x);
    }

    @Override
    public <Z> Term<F<A, F<X, B>>> substitute(Id<Z> v, Term<Z> replacement) {
        return new UncurryValue<>(domain, f.substitute(v, replacement));
    }

    public String toString() {
        return "(uncurry " + f + ")";
    }

}
