package com.sstewartgallus.extensions.tuples;

import com.sstewartgallus.ir.Signature;
import com.sstewartgallus.plato.*;

public enum NilNormal implements Type<HList.Nil> {
    NIL;

    @Override
    public <Y> Type<HList.Nil> unify(Type<Y> right) throws TypeCheckException {
        if (right == NIL) {
            return NIL;
        }
        throw new TypeCheckException(this, right);
    }

    @Override
    public <X> Type<HList.Nil> substitute(Id<X> v, Type<X> replacement) {
        return NIL;
    }

    @Override
    public <Z> Signature<V<Z, HList.Nil>> pointFree(Id<Z> argument, IdGen vars) {
        return new Signature.K<>(Signature.NilSig.NIL);
    }
}