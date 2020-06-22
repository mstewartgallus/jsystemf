package com.sstewartgallus.plato.ir.systemf;

import com.sstewartgallus.plato.ir.Constant;
import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cbpv.Code;
import com.sstewartgallus.plato.ir.cbpv.ConstantLiteral;
import com.sstewartgallus.plato.ir.cbpv.ReturnCode;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.F;

import java.util.Objects;

public record ConstantTerm<A>(Constant<A>constant) implements Term<F<A>> {
    public ConstantTerm {
        Objects.requireNonNull(constant);
    }

    @Override
    public int contains(Variable<?> x) {
        return 0;
    }

    @Override
    public Term<F<A>> visitChildren(TermVisitor visitor) {
        return this;
    }

    @Override
    public Code<F<A>> toCallByPushValue() {
        return new ReturnCode<>(new ConstantLiteral<>(constant));
    }

    @Override
    public TypeDesc<F<A>> type() {
        return constant.type().returns();
    }

    @Override
    public String toString() {
        return constant.toString();
    }
}
