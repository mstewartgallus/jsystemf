package com.sstewartgallus.pass1;

import com.sstewartgallus.ir.Generic;
import com.sstewartgallus.ir.PointFree;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Id;
import com.sstewartgallus.plato.IdGen;
import com.sstewartgallus.plato.V;

import java.lang.constant.ConstantDesc;
import java.util.Set;

public record Pure<A>(TPass0<A>type,
                      ConstantDesc value) implements Pass0<A>, Pass1<A>, Pass2<A>, Pass3<A>, PointFree<A> {
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public <X> Pass0<A> substitute(Id<X> variable, Pass0<X> replacement) {
        return this;
    }

    @Override
    public <A1> Pass1<A> substitute(Id<A1> argument, Pass1<A1> replacement) {
        return this;
    }

    @Override
    public <V> Pass2<A> substitute(Id<V> argument, Pass2<V> replacement) {
        return this;
    }

    @Override
    public <V> Pass3<A> substitute(Id<V> argument, Pass3<V> replacement) {
        return this;
    }

    @Override
    public <Z> PointFree<A> substitute(Id<Z> argument, TPass0<Z> replacement) {
        return this;
    }

    @Override
    public Pass1<A> aggregateLambdas(IdGen vars) {
        return this;
    }

    @Override
    public Pass1.Results<A> captureEnv(IdGen vars) {
        return new Pass1.Results<A>(Set.of(), this);
    }

    @Override
    public Pass3<A> uncurry(IdGen vars) {
        return this;
    }

    @Override
    public <T extends HList<T>> PointFree<F<T, A>> pointFree(Id<T> argument, IdGen vars, TPass0<T> argType) {
        return new PointFree.K<>(argType, this);
    }

    @Override
    public <Z> Generic<V<Z, A>> generic(Id<Z> argument, IdGen vars) {
        var sig = type().pointFree(argument, vars);
        return new Generic.Pure<>(sig, value);
    }
}
