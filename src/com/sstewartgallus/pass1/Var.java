package com.sstewartgallus.pass1;

import com.sstewartgallus.ir.PointFree;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Id;
import com.sstewartgallus.plato.IdGen;

public record Var<A>(TPass0<A>type,
                     Id<A>variable) implements Pass2<A>, Pass3<A>, Comparable<Var<?>> {
    @Override
    public String toString() {
        return variable.toString();
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
    public Pass3<A> uncurry(IdGen vars) {
        return this;
    }

    @Override
    public <T extends HList<T>> PointFree<F<T, A>> pointFree(Id<T> argument, IdGen vars, TPass0<T> argType) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public int compareTo(Var<?> o) {
        return variable.compareTo(o.variable);
    }
}
