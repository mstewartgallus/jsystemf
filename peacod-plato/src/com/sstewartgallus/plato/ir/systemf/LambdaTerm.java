package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.U;


public record LambdaTerm<A, B>(LocalTerm<A>binder, Term<B>body) implements Term<Fn<U<A>, B>> {
    @Override
    public final TypeDesc<Fn<U<A>, B>> type() {
        return TypeDesc.to(binder.type(), body.type());
    }

    @Override
    public final String toString() {
        return "(λ (" + binder.name() + " " + binder.type() + ") " + body + ")";
    }
}
