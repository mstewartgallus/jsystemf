package com.sstewartgallus.plato.syntax.term;

import com.sstewartgallus.plato.syntax.ext.variables.VarTerm;
import com.sstewartgallus.plato.syntax.ext.variables.VarType;
import com.sstewartgallus.plato.syntax.type.Type;

import java.util.Objects;

public final class NominalTerm<A> implements Term<A> {
    private final Type<A> type;
    private final TermTag<A> tag;

    private NominalTerm(TermTag<A> tag, Type<A> type) {
        this.type = Objects.requireNonNull(type);
        this.tag = Objects.requireNonNull(tag);
    }

    public static <A> NominalTerm<A> ofTag(TermTag<A> tag, Type<A> type) {
        return new NominalTerm<>(tag, type);
    }

    public TermTag<A> tag() {
        return tag;
    }

    @Override
    public final Term<A> visitChildren(Visitor visitor) {
        return ofTag(tag, visitor.type(type));
    }

    @Override
    public final Type<A> type() {
        return type;
    }


    @Override
    public Term<A> resolve(Solution solution) {
        return new NominalTerm<>(tag, type.resolve(solution));
    }

    @Override
    public String toString() {
        return tag.toString();
    }
}
