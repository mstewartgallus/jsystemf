package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.U;

import java.util.Objects;

public record ThunkLiteral<A>(Code<A>code) implements Literal<U<A>> {
    public ThunkLiteral {
        Objects.requireNonNull(code);
    }

    public static <A> Literal<U<A>> of(Code<A> code) {
        if (code instanceof ForceCode<A> forceCode) {
            return forceCode.thunk();
        }
        return new ThunkLiteral<>(code);
    }

    @Override
    public TypeDesc<U<A>> type() {
        return code.type().thunk();
    }

    @Override
    public String toString() {
        return "thunk {" + ("\n" + code).replace("\n", "\n\t") + "\n}";
    }
}