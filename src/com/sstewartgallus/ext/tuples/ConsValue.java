package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;
import com.sstewartgallus.plato.ValueTerm;

import java.util.Objects;

public record ConsValue<H, T extends HList<T>>(Term<H>head, Term<T>tail) implements ValueTerm<Cons<H, T>> {
    public ConsValue {
        Objects.requireNonNull(head);
        Objects.requireNonNull(tail);
    }

    @Override
    public Type<Cons<H, T>> type() throws TypeCheckException {
        return new ConsType<>(head.type(), tail.type());
    }

    @Override
    public <X> Term<Cons<H, T>> substitute(Id<X> v, Term<X> replacement) {
        return new ConsValue<>(head.substitute(v, replacement), tail.substitute(v, replacement));
    }

    public String toString() {
        return "(" + head + " Δ " + tail + ")";
    }
}
