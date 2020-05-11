package com.sstewartgallus.mh;

public interface ValList<A extends Arguments<A>> {
    static ValList<Arguments.None> none() {
        return None.NONE;
    }

    default <B> ValList<Arguments.And<B, A>> and(Val<B> value) {
        return new And<>(value, this);
    }

    enum None implements ValList<Arguments.None> {
        NONE
    }

    record And<A, B extends Arguments<B>>(Val<A>x, ValList<B>tail) implements ValList<Arguments.And<A, B>> {
    }
}
