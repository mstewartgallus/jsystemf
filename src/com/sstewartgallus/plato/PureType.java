package com.sstewartgallus.plato;

import com.sstewartgallus.ir.Signature;

// fixme... rename/retype, not clear enough this creates a new type...
public record PureType<A>(Class<A>clazz) implements CoreType<A>, Type<A> {

    public <Y> Type<A> unify(Type<Y> right) throws TypeCheckException {
        if (this != right) {
            throw new TypeCheckException(this, right);
        }
        return this;
    }

    @Override
    public <Z> Type<A> substitute(Id<Z> v, Type<Z> replacement) {
        return new PureType<>(clazz);
    }

    @Override
    public <Z> Signature<V<Z, A>> pointFree(Id<Z> argument, IdGen vars) {
        return new Signature.K<>(new Signature.Pure<>(clazz));
    }

    @Override
    public String toString() {
        return clazz.getCanonicalName();
    }
}
