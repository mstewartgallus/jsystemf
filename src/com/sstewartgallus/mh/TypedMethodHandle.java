package com.sstewartgallus.mh;

import java.lang.invoke.MethodHandle;

public record TypedMethodHandle<A, B>(Arguments<A>domain, Class<B>range, MethodHandle handle) {
}
