package com.sstewartgallus.ext.pretty;

import com.sstewartgallus.plato.*;

import java.util.Objects;
import java.util.function.Function;

public final class PrettyThunk<A> implements ThunkTerm<A>, AutoCloseable {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private final Type<A> type;
    private final int depth;

    private PrettyThunk(Type<A> type, int depth) {
        Objects.requireNonNull(type);
        this.type = type;
        this.depth = depth;
    }

    public static <A> PrettyThunk<A> generate(Type<A> type) {
        var depth = DEPTH.get();
        DEPTH.set(depth + 1);
        return new PrettyThunk<>(type, depth);
    }

    @Override
    public void close() {
        DEPTH.set(depth);
    }

    @Override
    public String toString() {
        return "v" + depth;
    }

    @Override
    public <B> Term<B> stepThunk(Function<ValueTerm<A>, Term<B>> k) {
        throw new UnsupportedOperationException("only supports toString");
    }

    @Override
    public Type<A> type() throws TypeCheckException {
        return type;
    }

    @Override
    public Term<A> visitChildren(Visitor visitor) {
        return this;
    }
}
