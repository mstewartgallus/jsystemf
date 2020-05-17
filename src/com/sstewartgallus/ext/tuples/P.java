package com.sstewartgallus.ext.tuples;

public final class P<H, T extends Tuple<T>> implements Tuple<P<H, T>> {
    private P() {
    }
}
