package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.ThunkTerm;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeCheckException;

public interface Getter<A extends HList<A>> extends ThunkTerm<A> {
    record Get<A extends HList<A>, B extends HList<B>>(Term<A>list, Index<A, B>index) implements Getter<B> {

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