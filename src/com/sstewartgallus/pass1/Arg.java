package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Term;

public interface Arg<A, B> {

    Term<B> apply(Term<A> f);

    record Zero<A>() implements Arg<A, A> {
        public String toString() {
            return "{}";
        }

        @Override
        public Term<A> apply(Term<A> f) {
            return f;
        }
    }

    record Add<H, B, C>(Term<H>argument,
                        Arg<B, C>tail) implements Arg<F<H, B>, C> {
        public String toString() {
            var builder = new StringBuilder();
            builder.append("{");
            builder.append(argument);
            Arg<?, ?> current = tail;
            while (current instanceof Add<?, ?, ?> add) {
                builder.append(",");
                builder.append(add.argument);
                current = add.tail;
            }
            builder.append("}");
            return builder.toString();
        }

        @Override
        public Term<C> apply(Term<F<H, B>> f) {
            return tail.apply(Term.apply(f, argument));
        }
    }
}
