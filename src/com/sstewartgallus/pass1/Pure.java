package com.sstewartgallus.pass1;

import com.sstewartgallus.ir.PointFree;
import com.sstewartgallus.term.Id;
import com.sstewartgallus.term.VarGen;
import com.sstewartgallus.type.F;
import com.sstewartgallus.type.HList;

import java.lang.constant.ConstantDesc;
import java.util.Set;

public record Pure<A>(TPass0<A>type, ConstantDesc value) implements Pass0<A>, Pass1<A>, Pass2<A>, Pass3<A> {
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
    public Pass1<A> aggregateLambdas(VarGen vars) {
        return this;
    }

    @Override
    public Pass1.Results<A> captureEnv(VarGen vars) {
        return new Pass1.Results<A>(Set.of(), this);
    }

    @Override
    public Pass3<A> uncurry(VarGen vars) {
        return this;
    }

    @Override
    public <T extends HList<T>> PointFree<F<T, A>> pointFree(Id<T> argument, VarGen vars, TPass0<T> argType) {
        return new PointFree.K<>(argType, new PointFree.Con<>(this.type, this.value));
    }
}
