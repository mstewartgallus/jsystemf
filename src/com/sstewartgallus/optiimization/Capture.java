package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.java.ObjectValue;
import com.sstewartgallus.ext.tuples.CurriedLambdaThunk;
import com.sstewartgallus.ext.variables.IdGen;
import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.ApplyThunk;
import com.sstewartgallus.plato.CoreTerm;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Term;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class Capture {
    private Capture() {
    }

    public static <A> Term<A> capture(Term<A> term, IdGen ids) {
        return captureInternal(term, ids).value;
    }

    private static <A> Results<A> captureInternal(Term<A> term, IdGen ids) {
        if (term instanceof CurriedLambdaThunk<A> lambda) {
            return curryLambda(lambda, ids);
        }

        if (!(term instanceof CoreTerm<A> core)) {
            throw new IllegalArgumentException("Unexpected list " + term);
        }

        if (core instanceof ObjectValue) {
            return new Results<>(Set.of(), core);
        }

        if (core instanceof VarValue<A> v) {
            return new Results<>(Set.of(v), core);
        }

        if (core instanceof ApplyThunk<?, A> apply) {
            return captureApply(apply, ids);
        }

        throw new IllegalArgumentException("Unexpected core list " + term);
    }

    private static <A> Set<A> union(Set<A> left, Set<A> right) {
        var x = new TreeSet<>(left);
        x.addAll(right);
        return x;
    }

    private static <A, B> Results<B> captureApply(ApplyThunk<A, B> apply, IdGen ids) {
        var fResults = captureInternal(apply.f(), ids);
        var xResults = captureInternal(apply.x(), ids);
        var captures = union(fResults.captured, xResults.captured);
        return new Results<>(captures, new ApplyThunk<>(fResults.value, xResults.value));
    }

    private static <A> Results<A> curryLambda(CurriedLambdaThunk<A> lambda, IdGen ids) {
        var results = captureBody(lambda.body(), ids);
        var captured = new TreeSet<>(results.captured);

        List<VarValue<?>> free = captured.stream().sorted().collect(Collectors.toUnmodifiableList());

        var chunk = results.value;
        return new Results<>(captured, helper(free, 0, chunk));
    }

    private static <A> BodyResults<A> captureBody(CurriedLambdaThunk.Body<A> body, IdGen ids) {
        if (body instanceof CurriedLambdaThunk.MainBody<A> mainBody) {
            var results = captureInternal(mainBody.body(), ids);
            return new BodyResults<>(results.captured, new CurriedLambdaThunk.MainBody<>(results.value));
        }

        var lambda = (CurriedLambdaThunk.LambdaBody<?, ?>) body;
        // fixme...
        return (BodyResults) captureLambda(lambda, ids);
    }

    private static <A, B> BodyResults<F<A, B>> captureLambda(CurriedLambdaThunk.LambdaBody<A, B> lambda, IdGen ids) {
        var domain = lambda.domain();
        var f = lambda.f();

        var v = ids.<A>createId();
        var load = new VarValue<>(domain, v);
        var body = f.apply(load);
        var results = captureBody(body, ids);
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
