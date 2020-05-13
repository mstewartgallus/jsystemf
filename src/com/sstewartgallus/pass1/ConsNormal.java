package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.Id;
import com.sstewartgallus.plato.NormalType;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;

import java.util.Objects;

record ConsNormal<H, T extends HList<T>>(Type<H>head, Type<T>tail) implements NormalType<HList.Cons<H, T>> {
    public ConsNormal {
        Objects.requireNonNull(head);
        Objects.requireNonNull(tail);
    }

    @Override
    public <Y> Type<HList.Cons<H, T>> unify(Type<Y> right) throws TypeCheckException {
        if (right instanceof ConsNormal<?, ?> rightCons) {
            return new ConsNormal<>(head.unify(rightCons.head), tail.unify(rightCons.tail));
        }
        throw new TypeCheckException(this, right);
    }

    @Override
    public <X> Type<HList.Cons<H, T>> substitute(Id<X> v, Type<X> replacement) {
        return new ConsNormal<>(head.substitute(v, replacement), tail.substitute(v, replacement));
    }

    public String toString() {
        return "(" + head + " Δ " + tail + ")";
    }
}