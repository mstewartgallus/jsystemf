package com.sstewartgallus.runtime;


import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodHandles.lookup;

/**
 * @see ValueThrowables for all details
 */
public abstract class ValueThrowable extends Throwable implements Cloneable {
    static final MethodHandles.Lookup LOOKUP = lookup();

    protected ValueThrowable() {
        super(null, null, false, false);
    }
}
