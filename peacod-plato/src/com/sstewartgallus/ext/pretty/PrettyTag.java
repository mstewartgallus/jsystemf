package com.sstewartgallus.ext.pretty;

import com.sstewartgallus.plato.TermTag;

import java.lang.constant.ConstantDesc;
import java.util.Optional;

public final class PrettyTag<A> implements TermTag<A>, AutoCloseable {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private final int depth;

    private PrettyTag(int depth) {
        this.depth = depth;
    }

    public static <A> PrettyTag<A> generate() {
        var depth = DEPTH.get();
        DEPTH.set(depth + 1);
        return new PrettyTag<>(depth);
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
    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.empty();
    }
}
