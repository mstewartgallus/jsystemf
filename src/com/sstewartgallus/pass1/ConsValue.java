package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;

public record ConsValue<H, T extends HList<T>>(Term<H>head, Term<T>tail) implements ValueTerm<HList.Cons<H, T>> {
    public ConsValue {
        Objects.requireNonNull(head);
        Objects.requireNonNull(tail);
    }

    @Override
    public Type<HList.Cons<H, T>> type() throws TypeCheckException {
        return new ConsNormal<>(head.type(), tail.type());
    }

    @Override
    public <X> Term<HList.Cons<H, T>> substitute(Id<X> v, Term<X> replacement) {
        return new ConsValue<>(head.substitute(v, replacement), tail.substitute(v, replacement));
    }

    public String toString() {
        return "(" + head + " Δ " + tail + ")";
    }
}
