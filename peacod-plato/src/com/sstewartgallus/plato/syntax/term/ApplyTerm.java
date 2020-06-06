package com.sstewartgallus.plato.syntax.term;

import com.sstewartgallus.plato.cbpv.ApplyCode;
import com.sstewartgallus.plato.cbpv.Code;
import com.sstewartgallus.plato.cbpv.ThunkLiteral;
import com.sstewartgallus.plato.runtime.Fun;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.syntax.ext.variables.VarType;
import com.sstewartgallus.plato.syntax.type.NominalType;
import com.sstewartgallus.plato.syntax.type.Type;

import java.util.Objects;
import java.util.Set;

public record ApplyTerm<A, B>(Type<B> type, Term<Fun<U<A>, B>>f, Term<A>x) implements Term<B> {
    public ApplyTerm {
        Objects.requireNonNull(type);
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Term<B> visitChildren(Visitor visitor) {
        return new ApplyTerm<>(visitor.type(type), visitor.term(f), visitor.term(x));
    }

    @Override
    public Code<B> compile(Environment environment) {
        return new ApplyCode<>(f.compile(environment), ThunkLiteral.of(x.compile(environment)));
    }

    @Override
    public Constraints findConstraints() {
        var fType = f.type();
        var xType = x.type();

        return Constraints.unify(new Constraints().constrainEqual(fType,
                xType.thunk().to(type)),
                x.findConstraints(),
                f.findConstraints());
    }

    @Override
    public Term<B> resolve(Solution solution) {
        return new ApplyTerm<>(type.resolve(solution), f.resolve(solution), x.resolve(solution));
    }

    public String toString() {
        return "(" + f + " " + x + ")";
    }

}
