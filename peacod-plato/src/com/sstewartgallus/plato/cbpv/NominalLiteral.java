package com.sstewartgallus.plato.cbpv;

import com.sstewartgallus.plato.syntax.term.TermTag;
import com.sstewartgallus.plato.syntax.type.Type;

import java.util.Objects;

public final class NominalLiteral<A> implements Literal<A> {
    private final Type<A> type;
    private final TermTag<A> tag;

    private NominalLiteral(TermTag<A> tag, Type<A> type) {
        Objects.requireNonNull(tag);
        Objects.requireNonNull(type);
        this.type = type;
        this.tag = tag;
    }

    public static <A> NominalLiteral<A> ofTag(TermTag<A> tag, Type<A> type) {
        return new NominalLiteral<>(tag, type);
    }

    public TermTag<A> tag() {
        return tag;
    }

    @Override
    public final Type<A> type() {
        return type;
    }

    @Override
    public String toString() {
        return tag.toString();
    }
}
