package com.sstewartgallus.ext.variables;

import com.sstewartgallus.ir.Signature;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.V;

public record VarType<T>(Id<T>variable) implements Type<T> {
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
    public <Z> Signature<V<Z, T>> pointFree(Id<Z> argument, IdGen vars) {
        if (variable == argument) {
            return (Signature) new Signature.Identity<Z>();
        }
        throw new Error("fixme");
    }

    @Override
    public <Y> Type<T> unify(Type<Y> right) {
        throw new UnsupportedOperationException("unimplemented");
    }

}
