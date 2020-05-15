package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.ext.variables.VarType;
import com.sstewartgallus.ir.Signature;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;
import com.sstewartgallus.plato.V;

public enum NilType implements Type<Nil> {
    NIL;

    @Override
    public <Y> Type<Nil> unify(Type<Y> right) throws TypeCheckException {
        if (right == NIL) {
            return NIL;
        }
        throw new TypeCheckException(this, right);
    }

    @Override
    public <Z> Signature<V<Z, Nil>> pointFree(VarType<Z> argument) {
        return new Signature.K<>(Signature.NilSig.NIL);
    }
}