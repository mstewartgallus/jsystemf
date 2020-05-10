package com.sstewartgallus.pass1;

import com.sstewartgallus.type.HList;
import com.sstewartgallus.type.Type;

public interface Index<A extends HList<A>, B extends HList<B>> {
    default <V> Index<A, B> substitute(Type.Var<V> argument, Type<V> replacement) {
        throw null;
    }

    Type<B> range();

    default int reify() {
        Index<?, ?> current = this;
        var ii = 0;
        while (current instanceof Next<?, ?, ?> next) {
            ++ii;
            current = next.f();
        }
        return ii;
    }

    record Zip<A extends HList<A>>(Type<A>type) implements Index<A, A> {
        public String toString() {
            return "0";
        }

        @Override
        public Type<A> range() {
            return type;
        }
    }

    record Next<X, A extends HList<A>, B extends HList<B>>(Index<A, HList.Cons<X, B>>f) implements Index<A, B> {


        public String toString() {
            return Integer.toString(reify());
        }

        @Override
        public Type<B> range() {
            return ((Type.ConsType<X, B>) f.range()).tail();
        }
    }
}
