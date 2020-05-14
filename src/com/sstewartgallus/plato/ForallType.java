package com.sstewartgallus.plato;

import com.sstewartgallus.ext.pretty.PrettyType;
import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.ext.variables.IdGen;
import com.sstewartgallus.ir.Signature;

import java.util.function.Function;

public record ForallType<A, B>(Function<Type<A>, Type<B>>f) implements CoreType<V<A, B>>, Type<V<A, B>> {
    public String toString() {
        try (var pretty = PrettyType.<A>generate()) {
            var body = f.apply(pretty);
            return "(∀" + pretty + " → " + body + ")";
        }
    }

    @Override
    public <Y> Type<V<A, B>> unify(Type<Y> right) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public <T> Type<V<A, B>> substitute(Id<T> v, Type<T> replacement) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public <Z> Signature<V<Z, V<A, B>>> pointFree(Id<Z> argument, IdGen vars) {
        throw null;
    }
}
