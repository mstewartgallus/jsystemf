package com.sstewartgallus.pass1;

import com.sstewartgallus.type.F;
import com.sstewartgallus.type.HList;

public interface Args<A extends HList<A>, B, R> {

    record Zero<A>() implements Args<HList.Nil, A, A> {
        public String toString() {
            return "{}";
        }
    }

    record Add<A, B, L extends HList<L>, R>(TPass0<A>argument,
                                            Args<L, B, R>tail) implements Args<HList.Cons<A, L>, B, F<A, R>> {
        public String toString() {
            var builder = new StringBuilder();
            builder.append("{");
            builder.append(argument);
            Args<?, ?, ?> current = tail;
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
