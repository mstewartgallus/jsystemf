package com.sstewartgallus.mh;

public interface ArgumentList<A extends Arguments<A>> {
    default <B, C extends Arguments<C>> Val<B> get(Ix<A, Arguments.And<B, C>> ix) {
        throw new UnsupportedOperationException("unimplemented");
    }

    enum None implements ArgumentList<Arguments.None> {
        NONE
    }

    record And<A, B extends Arguments<B>>(Class<A>klass,
                                          ArgumentList<B>tail) implements ArgumentList<Arguments.And<A, B>> {
    }
}

interface Ix<A extends Arguments<A>, B extends Arguments<B>> {
    static <A extends Arguments<A>> Ix<A, A> get0() {
        return new Zero<>();
    }

    static <A extends Arguments<A>, C> Ix<Arguments.And<C, A>, A> get1() {
        return new Succ<>(new Zero<>());
    }

    static <A extends Arguments<A>, C, D> Ix<Arguments.And<C, Arguments.And<D, A>>, A> get2() {
        return new Succ<>(new Succ<>(new Zero<>()));
    }

    record Zero<A extends Arguments<A>>() implements Ix<A, A> {
    }

    record Succ<A extends Arguments<A>, B extends Arguments<B>, C>(Ix<A, Arguments.And<C, B>>ix) implements Ix<A, B> {
    }
}