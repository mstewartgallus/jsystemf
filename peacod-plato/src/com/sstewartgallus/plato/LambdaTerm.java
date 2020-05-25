package com.sstewartgallus.plato;

import com.sstewartgallus.ext.pretty.PrettyTag;
import com.sstewartgallus.ext.variables.VarTerm;
import com.sstewartgallus.interpreter.*;

import java.util.Objects;
import java.util.function.Function;


public abstract class LambdaTerm<A, B> implements ValueTerm<F<A, B>> {
    private final Type<A> domain;
    private final Type<B> range;

    public LambdaTerm(Type<A> domain, Type<B> range) {
        Objects.requireNonNull(domain);
        this.domain = domain;
        this.range = range;
    }

    public final Type<A> domain() {
        return domain;
    }

    public abstract Term<B> apply(Term<A> x);

    public final Code<Function<Term<A>, Term<B>>> compileF() {
        var v = new Id<Term<A>>();
        return apply(new IntrinsicTerm<>(new LoadCode<>(v))).compile().pointFree(v);
    }

    @Override
    public Code<Term<F<A, B>>> compile() {
        return new ApplyCode<>(new LamCode<>(domain, range), compileF());
    }

    @Override
    public final Term<F<A, B>> visitChildren(Visitor visitor) {
        var v = new VarTerm<>(domain());
        var body = visitor.term(apply(v));
        return new LambdaTerm<>(visitor.type(domain), visitor.type(range)) {
            @Override
            public Term<B> apply(Term<A> x) {
                return v.substituteIn(body, x);
            }
        };
    }

    @Override
    public final Type<F<A, B>> type() throws TypeCheckException {
        return domain.to(range);
    }

    public final Type<B> range() {
        return range;
    }

    @Override
    public final String toString() {
        try (var pretty = PrettyTag.<A>generate()) {
            var body = apply(NominalTerm.ofTag(pretty, domain));
            return "(λ (" + pretty + " " + domain + ") " + body + ")";
        }
    }
}
