package com.sstewartgallus.ext.tuples;

public record Cons<H, T extends HList<T>>(H head, T tail) implements HList<Cons<H, T>> {
}
