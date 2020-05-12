package com.sstewartgallus.plato;

public record VarType<T>(Id<T>variable) implements ExtensionDenormal<T> {
    @Override
    public String toString() {
        return "t" + variable;
    }

    @Override
    public <Z> Type<T> substitute(Id<Z> v, Type<Z> replacement) {
        if (v == variable) {
            return (Type<T>) replacement;
        }
        return this;
    }

    @Override
    public <Y> Type<T> unify(Type<Y> right) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public <L> L visit(Visitor<L, T> visitor) {
        return visitor.onLoadType(variable);
    }
}
