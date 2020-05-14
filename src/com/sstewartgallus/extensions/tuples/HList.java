package com.sstewartgallus.extensions.tuples;

public interface HList<H extends HList<H>> {
    enum Nil implements HList<Nil> {
        NIL
    }

    record Cons<H, T extends HList<T>>(H head, T tail) implements HList<Cons<H, T>> {
    }
}