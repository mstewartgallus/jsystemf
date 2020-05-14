package com.sstewartgallus.ext.pretty;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.ThunkTerm;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;

import java.util.Objects;

// fixme... should be a nonpure extension to the list language...
// fixme... is it a thunk or a value?

/**
 * NOT a core list of the language...
 *
 * @param <A>
 */
public final class PrettyValue<A> implements ThunkTerm<A>, AutoCloseable {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private final Type<A> type;
    private final int depth;

    private PrettyValue(Type<A> type, int depth) {
        Objects.requireNonNull(type);
        this.type = type;
        this.depth = depth;
    }

    public static <A> PrettyValue<A> generate(Type<A> type) {
        var depth = DEPTH.get();
        DEPTH.set(depth + 1);
        return new PrettyValue<>(type, depth);
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
    public Type<A> type() throws TypeCheckException {
        return type;
    }

    @Override
    public Term<A> visitChildren(Visitor visitor) {
        throw new UnsupportedOperationException("only supports toString");
    }

    @Override
    public Term<A> stepThunk() {
        throw new UnsupportedOperationException("only supports toString");
    }
}
