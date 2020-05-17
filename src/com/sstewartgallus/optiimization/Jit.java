package com.sstewartgallus.optiimization;

import com.sstewartgallus.ext.mh.MethodHandleThunk;
import com.sstewartgallus.ext.pointfree.CallThunk;
import com.sstewartgallus.ext.tuples.*;
import com.sstewartgallus.plato.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.identity;

/**
 * Converting to point free form is like taking the derivative of a function.
 * <p>
 * PointFree[ K (x, y) ]_x  = K (1, y)
 * <p>
 * d xy / dx = 1 * y
 */
public final class Jit {
    private Jit() {
    }

    // fixme... how to typecheck
    public static <A> Term<A> jit(Term<A> root) {
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Term<T> term(Term<T> term) {
                if (term instanceof ApplyThunk<?, T> apply) {
                    return jitApply(apply);
                }

                if (false && term instanceof CallThunk<?, ?, ?> k) {
                    return (Term) jitCallThunk(k);
                }

                if (term instanceof AtTupleIndexThunk<?, ?, ?> derefThunk) {
                    return (Term) jitDeref(derefThunk);
                }

                if (term instanceof LambdaValue<?, ?> lambdaValue) {
                    return (Term) jitLambda(lambdaValue);
                }

                if (term instanceof TuplePairValue<?, ?> consValue) {
                    return (Term) jitConsValue(consValue);
                }

                return term.visitChildren(this);
            }
        });
    }

    private static <A, B extends Tuple<B>> Term<P<A, B>> jitConsValue(TuplePairValue<A, B> consValue) {
        List<Term<?>> terms = new ArrayList<>();
        terms.add(jit(consValue.head()));
        Term<?> current = consValue.tail();
        while (current instanceof TuplePairValue<?, ?> k) {
            terms.add(jit(k.head()));
            current = k.tail();
        }
        // fixme... create unboxed tuples....
        // fixme... flattening tuples should probably be a different phase....
        return new FlatValue<>(consValue.type(), Collections.unmodifiableList(terms));
    }

    private static <A, B> Term<F<A, B>> jitLambda(LambdaValue<A, B> lambdaValue) {
        // fixme.. jit to an invokable thingie ... ?

        throw null;
    }

    private static <A extends Tuple<A>, B extends Tuple<B>, C> Term<F<B, C>> jitDeref(AtTupleIndexThunk<A, B, C> derefThunk) {
        var index = derefThunk.index();
        var head = derefThunk.head();
        var indexInt = index.reify();
        var domain = index.domain();

        var domainFlatten = domain.flatten();
        var before = domainFlatten.stream().limit(indexInt).collect(Collectors.toUnmodifiableList());
        var after = domainFlatten.stream().skip(indexInt + 1).collect(Collectors.toUnmodifiableList());

        var handle = identity(head.erase());
        handle = dropArguments(handle, 0, before);
        handle = dropArguments(handle, handle.type().parameterCount(), after);

        var sig = new Signature.AddArg<>(index.domain(), new Signature.Result<>(head));
        return new MethodHandleThunk<>(sig, handle);
    }

    private static <A, B> Term<B> jitApply(ApplyThunk<A, B> apply) {
        var f = apply.f();
        var x = apply.x();

        f = jit(f);
        x = jit(x);

        // fixme... comeup with something better for method handles/well known combinators

        return new ApplyThunk<>(f, x);
    }

    private static <Z, A, B> Term<F<F<Z, F<A, B>>, F<F<Z, A>, F<Z, B>>>> jitCallThunk(CallThunk<Z, A, B> s) {

// f.to(x.to(z.to(b)));
//        var call = ValueLinker.link(lookup(), StandardOperation.CALL, MethodType.methodType(b.erase(), f.erase(), x.erase()));
        //       var mh = identity(left.erase());
        //      mh = dropArguments(mh, 1, right.erase());
        //    return new MethodHandleThunk<>(new Sig.Cons<>(f, new Sig.Cons<>(x, new Sig.Cons<>(z, new Sig.Zero<>(b)))), mh);
        throw null;
    }

    private static <A> Term<F<A, A>> jitIdentity(Type<A> type) {
        var mh = identity(type.erase());
        return new MethodHandleThunk<>(new Signature.AddArg<>(type, new Signature.Result<>(type)), mh);
    }

}
