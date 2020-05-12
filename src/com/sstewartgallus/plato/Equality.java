package com.sstewartgallus.plato;

public interface Equality<A, B> {
    B from(A value);

    A to(B value);

    record Identical<A>() implements Equality<A, A> {
        @Override
        public A from(A value) {
            return value;
        }

        @Override
        public A to(A value) {
            return value;
        }
    }
}
