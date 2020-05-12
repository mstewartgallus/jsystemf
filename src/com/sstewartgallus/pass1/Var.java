package com.sstewartgallus.pass1;

import com.sstewartgallus.ir.PointFree;
import com.sstewartgallus.term.Id;
import com.sstewartgallus.term.VarGen;
import com.sstewartgallus.type.F;
import com.sstewartgallus.type.HList;

import java.util.Set;

public record Var<A>(TPass0<A>type,
                     Id<A>variable) implements Pass0<A>, Pass1<A>, Pass2<A>, Pass3<A>, Comparable<Var<?>> {
    @Override
    public String toString() {
        return variable.toString();
    }

    @Override
    public <X> Pass0<A> substitute(Id<X> argument, Pass0<X> replacement) {
        if (this.variable == argument) {
            return (Pass0) replacement;
        }
        return this;
    }

    @Override
    public <A1> Pass1<A> substitute(Id<A1> argument, Pass1<A1> replacement) {
        if (this.variable == argument) {
            return (Pass1) replacement;
        }
        return this;
    }

    @Override
    public <V> Pass2<A> substitute(Id<V> argument, Pass2<V> replacement) {
        if (this.variable == argument) {
            return (Pass2) replacement;
        }
        return this;
    }

    @Override
    public <V> Pass3<A> substitute(Id<V> argument, Pass3<V> replacement) {
        if (this.variable == argument) {
            return (Pass3) replacement;
        }
        return this;
    }

    @Override
    public Pass1<A> aggregateLambdas(VarGen vars) {
        return this;
    }

    @Override
    public Pass1.Results<A> captureEnv(VarGen vars) {
        return new Pass1.Results<A>(Set.of(this), this);
    }

    @Override
    public Pass3<A> uncurry(VarGen vars) {
        return this;
    }

    @Override
    public <T extends HList<T>> PointFree<F<T, A>> pointFree(Id<T> argument, VarGen vars, TPass0<T> argType) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public int compareTo(Var<?> o) {
        return variable.compareTo(o.variable);
    }
}
