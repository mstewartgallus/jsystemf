package com.sstewartgallus.plato.runtime.type;

import com.sstewartgallus.plato.java.IntF;
import com.sstewartgallus.plato.runtime.V;

final class ThunkType<A> extends NamedType<V<A, U<A>>> implements GenericType<A, U<A>> {
    private static final ThunkType THUNK_TYPE = new ThunkType();

    private ThunkType() {
        super("core", "u");
    }

    public static Type<?> thunk() {
        return THUNK_TYPE;
    }

    @Override
    public Type<U<A>> apply(Type<A> x) {
        return new ThunkedType<>(this, x);
    }

    // fixme.. make abstract and ahve a special type for ints and etc...
    private static final class ThunkedType<A> extends TypeApplyType<A, U<A>> implements RealType<U<A>> {
        ThunkedType(Type<V<A, U<A>>> f, Type<A> x) {
            super(f, x);
        }

        @Override
        public Class<?> erase() {
            return U.class;
        }

        @Override
        public U<A> cast(Object value) {
            // fixme... make use the same sources as conversions from guardingtypeconverter
            // fixme.. have a special int Thunked type?
            if (value instanceof Integer intValue) {
                return (U) IntF.of(intValue);
            }
            return (U) value;
        }
    }
}