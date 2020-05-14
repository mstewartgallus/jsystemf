package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.java.ObjectValue;
import com.sstewartgallus.ext.tuples.CurriedLambdaThunk;
import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.ApplyThunk;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Term;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class Capture {
    private Capture() {
    }

    public static <A> Term<A> capture(Term<A> term) {
        return captureInternal(term).value;
    }

    private static <A> Results<A> captureInternal(Term<A> term) {
        if (term instanceof CurriedLambdaThunk<A> lambda) {
            return curryLambda(lambda);
        }

        if (term instanceof ObjectValue) {
            return new Results<>(Set.of(), term);
        }

        if (term instanceof VarValue<A> v) {
            return new Results<>(Set.of(v), v);
        }

        if (term instanceof ApplyThunk<?, A> apply) {
            return captureApply(apply);
        }

        throw new IllegalArgumentException("Unexpected core list " + term);
    }

    private static <A> Set<A> union(Set<A> left, Set<A> right) {
        var x = new TreeSet<>(left);
        x.addAll(right);
        return x;
    }

    private static <A, B> Results<B> captureApply(ApplyThunk<A, B> apply) {
        var fResults = captureInternal(apply.f());
        var xResults = captureInternal(apply.x());
        var captures = union(fResults.captured, xResults.captured);
        return new Results<>(captures, new ApplyThunk<>(fResults.value, xResults.value));
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
            var results = captureInternal(mainBody.body());
            return new BodyResults<>(results.captured, new CurriedLambdaThunk.MainBody<>(results.value));
        }

        var lambda = (CurriedLambdaThunk.LambdaBody<?, ?>) body;
        // fixme...
        return (BodyResults) captureLambda(lambda);
    }

    private static <A, B> BodyResults<F<A, B>> captureLambda(CurriedLambdaThunk.LambdaBody<A, B> lambda) {
        var domain = lambda.domain();
        var f = lambda.f();

        var v = new Id<A>();
        var load = new VarValue<>(domain, v);
        var body = f.apply(load);
        var results = captureBody(body);
        Set<VarValue<?>> captures = new TreeSet<>(results.captured);
        captures.remove(load);

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
        return new ApplyThunk<>(helper(free, ii + 1, new CurriedLambdaThunk.LambdaBody<>(freeVar.type(), x -> body.substitute(freeVar.variable(), x))),
                freeVar);
    }

    record Results<L>(Set<VarValue<?>>captured, Term<L>value) {
    }

    record BodyResults<L>(Set<VarValue<?>>captured, CurriedLambdaThunk.Body<L>value) {
    }
}
