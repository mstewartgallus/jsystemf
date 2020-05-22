package com.sstewartgallus.optimizers;

import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.LambdaTerm;
import com.sstewartgallus.plato.NominalTerm;
import com.sstewartgallus.plato.Term;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class Capture {
    private Capture() {
    }

    public static <A> Term<A> capture(Term<A> root) {
        return root.visit(new CaptureVisitor());
    }

    private static <A, B> Results<F<A, B>> captureLambda(LambdaTerm<A, B> lambda) {
        var results = captureLambdaInner(lambda);
        var captured = results.captured;
        List<VarValue<?>> free = captured.stream().sorted().collect(Collectors.toUnmodifiableList());
        return new Results<>(captured, helper(free, 0, results.value));
    }

    private static <A, B> Results<F<A, B>> captureLambdaInner(LambdaTerm<A, B> lambda) {
        var domain = lambda.domain();
        var v = new VarValue<>(domain);

        var body = lambda.apply(NominalTerm.ofTag(v, domain));
        Set<VarValue<?>> captured;
        Term<B> value;
        if (body instanceof LambdaTerm<?, ?> lambdaBody) {
            var results = (Results<B>) captureLambdaInner(lambdaBody);
            captured = results.captured;
            value = results.value;
        } else {
            var visitor = new CaptureVisitor();
            value = body.visit(visitor);
            captured = visitor.captured;
        }
        captured = new TreeSet<>(captured);
        captured.remove(v);

        return new Results<>(captured, domain.l(x -> v.substituteIn(value, x)));
    }

    private static <A> Term<A> helper(List<VarValue<?>> free, int ii, Term<A> body) {
        if (ii >= free.size()) {
            return body;
        }
        return helper(free, ii, free.get(ii), body);
    }

    private static <A, B> Term<A> helper(List<VarValue<?>> free, int ii, VarValue<B> freeVar, Term<A> body) {
        var f = freeVar.type().l(x -> freeVar.substituteIn(body, x));
        return Term.apply(helper(free, ii + 1, f), NominalTerm.ofTag(freeVar, freeVar.type()));
    }

    static final class CaptureVisitor extends Term.Visitor {
        final Set<VarValue<?>> captured = new HashSet<>();

        @Override
        public <T> Term<T> term(Term<T> term) {
            if (term instanceof NominalTerm<T> nominalTerm && nominalTerm.tag() instanceof VarValue<T> v) {
                captured.add(v);
                return nominalTerm;
            }

            if (!(term instanceof LambdaTerm<?, ?> thunk)) {
                var child = new CaptureVisitor();
                var result = term.visitChildren(child);
                captured.addAll(child.captured);
                return result;
            }

            var results = (Results<T>) captureLambda(thunk);
            captured.addAll(results.captured);
            return results.value;
        }
    }

    record Results<L>(Set<VarValue<?>>captured, Term<L>value) {
    }

}