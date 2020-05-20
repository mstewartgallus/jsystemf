package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.*;

import java.util.Objects;
import java.util.function.Function;

public final class AtTupleIndexThunk<B extends Tuple<B>, X extends Tuple<X>, A> implements ThunkTerm<F<X, A>> {
    private final Type<A> head;
    private final Type<B> tail;
    private final TupleIndex<X, P<A, B>> index;
    private final int reify;

    public AtTupleIndexThunk(Type<A> head, Type<B> tail,
                             TupleIndex<X, P<A, B>> index) {
        this.head = head;
        this.tail = tail;
        this.index = index;
        this.reify = index.reify();
        Objects.requireNonNull(head);
        Objects.requireNonNull(tail);
    }

    public Type<A> head() {
        return head;
    }

    public TupleIndex<X, P<A, B>> index() {
        return index;
    }

    @Override
    public Term<F<X, A>> visitChildren(Visitor visitor) {
        return this;
    }

    @Override
    public <Z> Term<F<Z, F<X, A>>> pointFree(VarValue<Z> varValue) {
        return Term.constant(varValue.type(), this);
    }

    @Override
    public <C> Term<C> stepThunk(Function<ValueTerm<F<X, A>>, Term<C>> k) {
        return k.apply(index.domain().l(x -> ((TuplePairValue<A, B>) index.index(Interpreter.normalize(x))).head()));
    }

    @Override
    public Type<F<X, A>> type() throws TypeCheckException {
        return index.domain().to(head);
    }

    @Override
    public String toString() {
        return "[" + index + "]";
    }
}
