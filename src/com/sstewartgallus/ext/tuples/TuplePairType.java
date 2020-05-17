package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record TuplePairType<H, T extends Tuple<T>>(Type<H>head, Type<T>tail) implements Type<P<H, T>> {
    public TuplePairType {
        Objects.requireNonNull(head);
        Objects.requireNonNull(tail);
    }

    @Override
    public <Y> Type<P<H, T>> unify(Type<Y> right) throws TypeCheckException {
        if (right instanceof TuplePairType<?, ?> rightCons) {
            return new TuplePairType<>(head.unify(rightCons.head), tail.unify(rightCons.tail));
        }
        throw new TypeCheckException(this, right);
    }

    public String toString() {
        StringBuilder str = new StringBuilder("(Δ ");
        str.append(head);

        Type<?> next = tail;
        while (next instanceof TuplePairType<?, ?> cons) {
            str.append(" ").append(cons.head);
            next = cons.tail();
        }

        return str + ")";
    }

    public List<Class<?>> flatten() {
        var x = new ArrayList<Class<?>>();
        x.add(head.erase());
        x.addAll(tail.flatten());
        return x;
    }
}