package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.Id;
import com.sstewartgallus.plato.Type;

public interface IndexTuple<A extends HList<A>, B extends HList<B>> {
    default <V> IndexTuple<A, B> substitute(Id<V> argument, Type<V> replacement) {
        throw null;
    }

    Type<B> range();

    default int reify() {
        IndexTuple<?, ?> current = this;
        var ii = 0;
        while (current instanceof Next<?, ?, ?> next) {
            ++ii;
            current = next.f();
        }
        return ii;
    }

    record Zip<A extends HList<A>>(Type<A>type) implements IndexTuple<A, A> {
        public String toString() {
            return "0";
        }

        @Override
        public Type<A> range() {
            return type;
        }
    }

    record Next<X, A extends HList<A>, B extends HList<B>>(
            IndexTuple<A, HList.Cons<X, B>>f) implements IndexTuple<A, B> {
        public String toString() {
            return Integer.toString(reify());
        }

        @Override
        public Type<B> range() {
            return ((ConsNormal<X, B>) f.range()).tail();
        }
    }
}
