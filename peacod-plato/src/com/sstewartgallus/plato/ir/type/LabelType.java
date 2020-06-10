package com.sstewartgallus.plato.ir.type;

import com.sstewartgallus.plato.runtime.Label;
import com.sstewartgallus.plato.runtime.V;

final class LabelType<A> extends NamedType<V<A, Label<A>>> implements GenericType<A, Label<A>> {
    private static final LabelType THUNK_TYPE = new LabelType();

    private LabelType() {
        super("core", "label");
    }

    public static Type<?> label() {
        return THUNK_TYPE;
    }

    @Override
    public Type<Label<A>> apply(Type<A> x) {
        return new LabeledType<>(this, x);
    }

    private static final class LabeledType<A> extends TypeApplyType<A, Label<A>> implements RealType<Label<A>> {
        LabeledType(Type<V<A, Label<A>>> f, Type<A> x) {
            super(f, x);
        }

        @Override
        public Class<?> erase() {
            return Label.class;
        }

        @Override
        public Label<A> cast(Object value) {
            return (Label) value;
        }
    }
}