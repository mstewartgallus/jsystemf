package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.plato.Interpreter;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.ValueTerm;

public interface TupleIndex<A extends Tuple<A>, B extends Tuple<B>> {
    Type<A> domain();

    Type<B> range();

    default int reify() {
        TupleIndex<?, ?> current = this;
        var ii = 0;
        while (current instanceof Succ<?, ?, ?> next) {
            ++ii;
            current = next.f();
        }
        return ii;
    }

    ValueTerm<B> stepThunk(Term<A> term);

    record Zero<A extends Tuple<A>>(Type<A>range) implements TupleIndex<A, A> {
        @Override
        public String toString() {
            return "0";
        }

        @Override
        public Type<A> domain() {
            return range;
        }

        @Override
        public ValueTerm<A> stepThunk(Term<A> term) {
            return Interpreter.normalize(term);
        }
    }

    record Succ<X, A extends Tuple<A>, B extends Tuple<B>>(
            TupleIndex<A, P<X, B>>f) implements TupleIndex<A, B> {
        @Override
        public Type<A> domain() {
            return f.domain();
        }

        @Override
        public Type<B> range() {
            return ((TuplePairType<X, B>) f.range()).tail();
        }

        @Override
        public ValueTerm<B> stepThunk(Term<A> term) {
            var norm = Interpreter.normalize(f.stepThunk(term));
            return ((TuplePairValue<X, B>) norm).tail();
        }

        @Override
        public String toString() {
            return "" + reify();
        }
    }
}
