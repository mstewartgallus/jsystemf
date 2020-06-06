package com.sstewartgallus.plato.syntax.term;

import com.sstewartgallus.plato.cbpv.Code;
import com.sstewartgallus.plato.cbpv.LambdaCode;
import com.sstewartgallus.plato.cbpv.Literal;
import com.sstewartgallus.plato.runtime.Fun;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.syntax.ext.pretty.PrettyTag;
import com.sstewartgallus.plato.syntax.ext.variables.VarTerm;
import com.sstewartgallus.plato.syntax.ext.variables.VarType;
import com.sstewartgallus.plato.syntax.type.NominalType;
import com.sstewartgallus.plato.syntax.type.Type;
import com.sstewartgallus.plato.syntax.type.TypeCheckException;

import java.util.Objects;
import java.util.function.Function;


public final class LambdaTerm<A, B> implements Term<Fun<U<A>, B>> {
    private final Type<A> domain;
    private final Type<B> range;
    private final Function<Term<A>, Term<B>> f;

    public LambdaTerm(Type<A> domain, Type<B> range, Function<Term<A>, Term<B>> f) {
        this.domain = Objects.requireNonNull(domain);
        this.range = Objects.requireNonNull(range);
        this.f = f;
    }

    public final Type<A> domain() {
        return domain;
    }

    public Term<B> apply(Term<A> x) {
        return f.apply(x);
    }

    @Override
    public final Term<Fun<U<A>, B>> visitChildren(Visitor visitor) {
        var newDomain = visitor.type(domain);
        var v = new VarTerm<>(newDomain);
        var body = visitor.term(apply(v));
        return new LambdaTerm<>(newDomain, visitor.type(range), x -> v.substituteIn(body, x));
    }

    @Override
    public Code<Fun<U<A>, B>> compile(Environment environment) {
        var v = new VarTerm<>(domain);
        var body = apply(v);
        return new LambdaCode<>(domain.thunk(), range) {
            @Override
            public Code<B> apply(Literal<U<A>> x) {
                return body.compile(environment.put(v, x));
            }
        };
    }

    @Override
    public Term<Fun<U<A>, B>> resolve(Solution solution) {
        var newDomain = domain.resolve(solution);
        var v = new VarTerm<>(newDomain);
        var body = apply(v).resolve(solution);
        return new LambdaTerm<>(newDomain, range.resolve(solution), x -> v.substituteIn(body, x));
    }
    @Override
    public final Type<Fun<U<A>, B>> type() {
        return domain.thunk().to(range);
    }

    @Override
    public Constraints findConstraints() throws TypeCheckException {
        // fixme... constraints...
        var v = new VarTerm<>(domain);
        var body = apply(v);
        return body.findConstraints().constrainEqual(body.type(), range);
        // fixme.. not sure this makes sense.
    }

    @Override
    public final String toString() {
        try (var pretty = PrettyTag.<A>generate()) {
            var body = apply(NominalTerm.ofTag(pretty, domain));
            return "(λ (" + pretty + " " + domain + ") " + body + ")";
        }
    }

}
