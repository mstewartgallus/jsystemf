package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Term;

public interface Arg<A extends HList<A>, B, R> {

    record Zero<A>() implements Arg<HList.Nil, A, A> {
        public String toString() {
            return "{}";
        }
    }

    record Add<A, B, L extends HList<L>, R>(Term<A>argument,
                                            Arg<L, B, R>tail) implements Arg<HList.Cons<A, L>, B, F<A, R>> {
        public String toString() {
            var builder = new StringBuilder();
            builder.append("{");
            builder.append(argument);
            Arg<?, ?, ?> current = tail;
            while (current instanceof Add<?, ?, ?, ?> add) {
                builder.append(",");
                builder.append(add.argument);
                current = add.tail;
            }
            builder.append("}");
            return builder.toString();
        }
    }
}
