package com.sstewartgallus.type;

public interface HList<H extends HList<H>> {
    record Nil() implements HList<Nil> {
    }

    record Cons<H, T extends HList<T>>(H head, T tail) implements HList<Cons<H, T>> {
    }
}
