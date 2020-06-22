package com.sstewartgallus.plato.runtime.type;

import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.V;

final class FnType<A, B> extends NamedType<V<A, V<B, Fn<A, B>>>> implements GenericType<A, V<B, Fn<A, B>>> {
    private static final FnType FN_TYPE = new FnType();

    private FnType() {
        super("core", "fn");
    }

    static <A, B> Type<V<A, V<B, Fn<A, B>>>> function() {
        return FN_TYPE;
    }

    @Override
    public Type<V<B, Fn<A, B>>> apply(Type<A> value) {
        return new PartialFunctionType<>(this, value);
    }

    static final class PartialFunctionType<A, B> extends TypeApplyType<A, V<B, Fn<A, B>>> implements GenericType<B, Fn<A, B>> {
        PartialFunctionType(Type<V<A, V<B, Fn<A, B>>>> f, Type<A> x) {
            super(f, x);
        }

        // fixme... cache ?
        @Override
        public Type<Fn<A, B>> apply(Type<B> value) {
            return new FunctionType<>(this, value);
        }
    }

    static final class FunctionType<A, B> extends TypeApplyType<B, Fn<A, B>> {
        FunctionType(Type<V<B, Fn<A, B>>> f, Type<B> x) {
            super(f, x);
        }
    }
}