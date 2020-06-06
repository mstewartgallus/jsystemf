package com.sstewartgallus.plato.syntax.ext.pretty;

import com.sstewartgallus.plato.syntax.type.TypeTag;

import java.lang.constant.ConstantDesc;
import java.util.Optional;

public final class PrettyType<A> implements TypeTag<A>, AutoCloseable {
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
    public boolean equals(Object right) {
        return right instanceof PrettyType<?> pretty && depth == pretty.depth;
    }

    @Override
    public Class<?> erase() {
        throw null;
    }

    @Override
    public Optional<? extends ConstantDesc> describeConstable() {
        throw null;
    }
}
