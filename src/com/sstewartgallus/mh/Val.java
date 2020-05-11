package com.sstewartgallus.mh;

public interface Val<B> {
    static <B> Val<B> of(B value) {
        throw null;
    }

    <A extends Arguments<A>> TypedMethodHandle<A, B> toHandle(ArgumentList<A> args, Class<B> range);

    record Pure<A>(Class<A>klass, A value) implements Val<A> {
        @Override
        public <B extends Arguments<B>> TypedMethodHandle<B, A> toHandle(ArgumentList<B> args, Class<A> range) {
            throw new UnsupportedOperationException("unimplemented");
        }
    }

    // fixme... use hlist...
    record Apply<A extends Arguments<A>, B>(TypedMethodHandle<A, B>f, ValList<A> x) implements Val<B> {
        @Override
        public <C extends Arguments<C>> TypedMethodHandle<C, B> toHandle(ArgumentList<C> args, Class<B> range) {
            throw new UnsupportedOperationException("unimplemented");
        }
    }
}
