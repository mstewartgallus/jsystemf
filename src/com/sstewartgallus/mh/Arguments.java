package com.sstewartgallus.mh;

interface Arguments<A> {
    record None() implements Arguments<None> {
    }

    record And<A, B extends Arguments<?>>() implements Arguments<And<A, B>> {
    }
}
