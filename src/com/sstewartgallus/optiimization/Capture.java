package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.tuples.CurriedLambdaThunk;
import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.F;
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
        return root.visit(new CurryVisitor());
    }

    private static <A> Results<A> curryLambda(CurriedLambdaThunk<A> lambda) {
        var results = captureBody(lambda.body());
        var captured = new TreeSet<>(results.captured);

        List<VarValue<?>> free = captured.stream().sorted().collect(Collectors.toUnmodifiableList());

        var chunk = results.value;
        return new Results<>(captured, helper(free, 0, chunk));
    }

    private static <A> BodyResults<A> captureBody(CurriedLambdaThunk.Body<A> body) {
        if (body instanceof CurriedLambdaThunk.MainBody<A> mainBody) {
            var curryVisitor = new CurryVisitor();
            var results = mainBody.body().visit(curryVisitor);
            return new BodyResults<>(curryVisitor.captured, new CurriedLambdaThunk.MainBody<>(results));
        }

        var lambda = (CurriedLambdaThunk.LambdaBody<?, ?>) body;
        // fixme...
        return (BodyResults) captureLambda(lambda);
    }

    private static <A, B> BodyResults<F<A, B>> captureLambda(CurriedLambdaThunk.LambdaBody<A, B> lambda) {
        var domain = lambda.domain();
        var f = lambda.f();

        var v = new VarValue<>(domain);
        var body = f.apply(v);
        var results = captureBody(body);
        Set<VarValue<?>> captures = new TreeSet<>(results.captured);
        captures.remove(v);

        var chunk = results.value;
        return new BodyResults<>(captures, new CurriedLambdaThunk.LambdaBody<>(domain, x -> chunk.substitute(v, x)));
    }

    private static <A> Term<A> helper(List<VarValue<?>> free, int ii, CurriedLambdaThunk.Body<A> body) {
        if (ii >= free.size()) {
            return new CurriedLambdaThunk<>(body);
        }
        return helper(free, ii, free.get(ii), body);
    }

    private static <A, B> Term<A> helper(List<VarValue<?>> free, int ii, VarValue<B> freeVar, CurriedLambdaThunk.Body<A> body) {
        return Term.apply(helper(free, ii + 1, new CurriedLambdaThunk.LambdaBody<>(freeVar.type(), x -> body.substitute(freeVar, x))),
                freeVar);
    }

    static final class CurryVisitor extends Term.Visitor {
        final Set<VarValue<?>> captured = new HashSet<>();

        @Override
        public <T> Term<T> term(Term<T> term) {
            if (term instanceof VarValue<T> v) {
                captured.add(v);
                return v;
            }

            if (!(term instanceof CurriedLambdaThunk<T> thunk)) {
                var child = new CurryVisitor();
                var result = term.visitChildren(child);
                captured.addAll(child.captured);
                return result;
            }
            return curryLambda(thunk).value;
        }
    }

    record Results<L>(Set<VarValue<?>>captured, Term<L>value) {
    }

    record BodyResults<L>(Set<VarValue<?>>captured, CurriedLambdaThunk.Body<L>value) {
    }
}
