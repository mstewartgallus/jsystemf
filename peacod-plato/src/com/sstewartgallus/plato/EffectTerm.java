package com.sstewartgallus.plato;

import com.sstewartgallus.interpreter.Effect;

import java.util.Objects;

public final class EffectTerm<A> implements Term<A> {
    private final Type<A> type;
    private final Effect<Term<A>> effect;

    public EffectTerm(Effect<Term<A>> effect, Type<A> type) {
        Objects.requireNonNull(effect);
        Objects.requireNonNull(type);
        this.type = type;
        this.effect = effect;
    }

    @Override
    public final Term<A> visitChildren(Visitor visitor) {
        return new EffectTerm<>(effect, visitor.type(type));
    }

    @Override
    public Effect<Term<A>> interpret() {
        return effect;
    }

    @Override
    public final Type<A> type() {
        return type;
    }

    @Override
    public String toString() {
        return effect.toString();
    }
}
