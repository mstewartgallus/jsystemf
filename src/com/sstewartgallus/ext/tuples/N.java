package com.sstewartgallus.ext.tuples;

/**
 * A phantom type, the runtime represents it however it wants..
 */
public final class N implements Tuple<N> {
    private N() {
    }
}
