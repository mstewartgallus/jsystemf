package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.Id;
import com.sstewartgallus.plato.NormalType;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;

enum NilNormal implements NormalType<HList.Nil> {
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
}