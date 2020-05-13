package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;

// fixme... is there a need for this?
public record UncurryValue<X extends HList<X>, A, B>(Type<A>domain,
                                                     Term<F<HList.Cons<A, X>, B>>f) implements ThunkTerm<F<A, F<X, B>>> {
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
    public <Z> Term<F<A, F<X, B>>> substitute(Id<Z> v, Term<Z> replacement) {
        return new UncurryValue<>(domain, f.substitute(v, replacement));
    }

    public String toString() {
        return "(uncurry " + f + ")";
    }

    @Override
    public Term<F<A, F<X, B>>> stepThunk() {
        throw null;
    }
}
