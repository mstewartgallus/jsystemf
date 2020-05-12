package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.Id;

public interface Index<A extends HList<A>, B extends HList<B>> {
    default <V> Index<A, B> substitute(Id<V> argument, TPass0<V> replacement) {
        throw null;
    }

    TPass0<B> range();

    default int reify() {
        Index<?, ?> current = this;
        var ii = 0;
        while (current instanceof Next<?, ?, ?> next) {
            ++ii;
            current = next.f();
        }
        return ii;
    }

    record Zip<A extends HList<A>>(TPass0<A>type) implements Index<A, A> {
        public String toString() {
            return "0";
        }

        @Override
        public TPass0<A> range() {
            return type;
        }
    }

    record Next<X, A extends HList<A>, B extends HList<B>>(Index<A, HList.Cons<X, B>>f) implements Index<A, B> {
        public String toString() {
            return Integer.toString(reify());
        }

        @Override
        public TPass0<B> range() {
            return ((TPass0.ConsType<X, B>) f.range()).tail();
        }
    }
}
