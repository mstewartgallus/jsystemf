package com.sstewartgallus.ext.pretty;

import com.sstewartgallus.ext.variables.VarType;
import com.sstewartgallus.ir.Signature;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;
import com.sstewartgallus.plato.V;

public final class PrettyType<A> implements Type<A>, AutoCloseable {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private final int depth;

    private PrettyType(int depth) {
        this.depth = depth;
    }

    public static <A> PrettyType<A> generate() {
        var depth = DEPTH.get();
        DEPTH.set(depth + 1);
        return new PrettyType<>(depth);
    }

    @Override
    public void close() {
        DEPTH.set(depth);
    }

    @Override
    public String toString() {
        return "t" + depth;
    }

    @Override
    public <Y> Type<A> unify(Type<Y> right) throws TypeCheckException {
        throw null;
    }

    @Override
    public <Z> Signature<V<Z, A>> pointFree(VarType<Z> argument) {
        throw null;
    }
}
