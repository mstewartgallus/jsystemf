package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.*;

import java.util.Objects;

public final class AtTupleIndexValue<B extends Tuple<B>, X extends Tuple<X>, A> extends LambdaValue<X, A> {
    private final Type<A> head;
    private final Type<B> tail;
    private final TupleIndex<X, P<A, B>> index;
    private final int reify;

    public AtTupleIndexValue(Type<A> head, Type<B> tail,
                             TupleIndex<X, P<A, B>> index) {
        super(index.domain());
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
    public Term<A> apply(Term<X> x) {
        return ((TuplePairValue<A, B>) index.index(Interpreter.normalize(x))).head();
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
    public Type<F<X, A>> type() throws TypeCheckException {
        return index.domain().to(head);
    }

    @Override
    public String toString() {
        return "[" + index + "]";
    }

}
