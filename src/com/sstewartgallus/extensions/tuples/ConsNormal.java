package com.sstewartgallus.extensions.tuples;

import com.sstewartgallus.ir.Signature;
import com.sstewartgallus.plato.*;

import java.util.Objects;

public record ConsNormal<H, T extends HList<T>>(Type<H>head, Type<T>tail) implements NormalType<HList.Cons<H, T>> {
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

    @Override
    public <Z> Signature<V<Z, HList.Cons<H, T>>> pointFree(Id<Z> argument, IdGen vars) {
        return new Signature.ConsType<>(head.pointFree(argument, vars), tail.pointFree(argument, vars));
    }

    public String toString() {
        return "(" + head + " Δ " + tail + ")";
    }
}