package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

public interface Getter<A extends HList<A>> extends ThunkTerm<A> {
    record Get<A extends HList<A>, B extends HList<B>>(Term<A>list, IndexTuple<A, B>index) implements Getter<B> {

        @Override
        public Term<B> stepThunk() {
            // fixme... implement later...
            throw null;
        }

        @Override
        public Type<B> type() throws TypeCheckException {
            throw null;
        }

        @Override
        public <X> Term<B> substitute(Id<X> variable, Type<X> replacement) {
            return new Get<>(list.substitute(variable, replacement), index);
        }

        @Override
        public <X> Term<B> substitute(Id<X> variable, Term<X> replacement) {
            return new Get<>(list.substitute(variable, replacement), index);
        }

        @Override
        public String toString() {
            return list + "[" + index + "]";
        }
    }
}
