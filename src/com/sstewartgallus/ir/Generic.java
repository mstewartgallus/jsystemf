package com.sstewartgallus.ir;

import com.sstewartgallus.pass1.Index;
import com.sstewartgallus.runtime.*;
import com.sstewartgallus.type.*;
import jdk.dynalink.StandardNamespace;
import jdk.dynalink.StandardOperation;

import java.lang.constant.ConstantDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

/**
 * A category represents Term a -> Term b in a point free way
 * <p>
 * Generic represents Type a -> Term a in a point free way
 * <p>
 * Fixme: Look into a symbolic representation of my target https://www.youtube.com/watch?v=PwL2c6rO6co and then make a dsl for it.
 */
public interface Generic<A, B> {
    static <X, A, B, Z> Generic<Z, F<X, V<A, B>>> curry(Signature<Z, F<X, V<A, B>>> signature,
                                                        Generic<E<Z, A>, F<X, B>> body) {
        return new CurryType<>(signature, body);
    }

    // for Category we use Void the class that has only one inhabitant
    // fixme... I believe what we want for Generic is the T such that Class<T> has only one inhabitant... null!
    static <B> Value<B> compile(MethodHandles.Lookup lookup, Generic<Void, F<HList.Nil, B>> generic) {
        var chunk = generic.compile(lookup, Type.VOID);

        var handle = chunk.intro();

        Object obj;
        try {
            obj = handle.invoke((Object) null);
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        return (Value) obj;
    }

    default Chunk<B> compile(MethodHandles.Lookup lookup, Type<A> klass) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default Signature<A, B> signature() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    record Unit<V, A, B>(Signature<V, F<A, B>>signature,
                         Signature<V, A>domain,
                         Signature<V, B>range,
                         ConstantDesc value) implements Generic<V, F<A, B>> {
        public String toString() {
            return "(K " + value + ")";
        }

        public Chunk<F<A, B>> compile(Lookup lookup, Type<V> klass) {
            var d = domain.apply(klass).flatten();
            var t = range.apply(klass).erase();

            // fixme... do this lazily..
            Object k;
            try {
                k = value.resolveConstantDesc(lookup);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
            var handle = constant(t, k);
            handle = dropArguments(handle, 0, d);
            return new Chunk<>(handle);
        }
    }

    record Identity<V, A>(Signature<V, F<A, A>>signature, Signature<V, A>value) implements Generic<V, F<A, A>> {
        public Chunk<F<A, A>> compile(MethodHandles.Lookup lookup, Type<V> klass) {
            var type = value.apply(klass).erase();
            return new Chunk<>(MethodHandles.identity(type));
        }

        public String toString() {
            return "I";
        }
    }

    record Head<V, X, A, B extends HList<B>>(Signature<V, F<X, A>>signature,
                                             Signature<V, A>first,
                                             Generic<V, F<X, HList.Cons<A, B>>>product) implements Generic<V, F<X, A>> {
        public Chunk<F<X, A>> compile(Lookup lookup, Type<V> klass) {
            var productC = product.compile(lookup, klass).intro();

            var first = this.first.apply(klass).erase();

            System.err.println("head " + first);
            var cs = ValueLinker.link(lookup, StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("head"), methodType(first, ConsValue.class));
            var intro = cs.dynamicInvoker();

            intro = filterReturnValue(productC, intro);

            return new Chunk<>(intro);
        }

        public String toString() {
            return "(head " + product + ")";
        }
    }

    record Tail<V, X, A, B extends HList<B>>(Signature<V, F<X, B>>signature,
                                             Signature<V, B>second,
                                             Generic<V, F<X, HList.Cons<A, B>>>product) implements Generic<V, F<X, B>> {
        public Chunk<F<X, B>> compile(Lookup lookup, Type<V> klass) {
            var productC = product.compile(lookup, klass).intro();

            var second = this.second.apply(klass).erase();

            var cs = ValueLinker.link(lookup, StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("tail"), methodType(second, ConsValue.class));
            var intro = cs.dynamicInvoker();

            intro = filterReturnValue(productC, intro);

            return new Chunk<>(intro);
        }

        public String toString() {
            return "(tail " + product + ")";
        }
    }

    record CurryType<Z, X, A, B, C>(Signature<Z, F<X, V<A, B>>>signature,
                                    Generic<E<Z, A>, F<X, B>>f) implements Generic<Z, F<X, V<A, B>>> {
        public String toString() {
            return "(curry-type " + f + ")";
        }

        public Chunk<F<X, V<A, B>>> compile(MethodHandles.Lookup lookup, Type<Z> klass) {
            // fixme... need to accept a klass arguments at runtime I think...
            // fixme... createa  E<Z,A> from Z ?
            throw new UnsupportedOperationException("unimplemented");
            //  return f.compile(lookup, klass);
        }
    }

    record Curry<V, A, B extends HList<B>, C>(Signature<V, F<B, F<A, C>>>signature,
                                              Signature<V, A>head,
                                              Signature<V, B>tail,
                                              Generic<V, F<HList.Cons<A, B>, C>>f) implements Generic<V, F<B, F<A, C>>> {

        static final MethodHandle PAIR_MH;

        static {
            try {
                PAIR_MH = MethodHandles.lookup().findStatic(ConsValue.class, "of", MethodType.methodType(ConsValue.class, Object.class, ConsValue.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public String toString() {
            return "(curry " + f + ")";
        }

        public Chunk<F<B, F<A, C>>> compile(Lookup lookup, Type<V> klass) {
            var head = this.head.apply(klass).erase();
            var tail = this.tail.apply(klass).erase();

            var fEmit = f.compile(lookup, klass).intro();

            var pair = PAIR_MH.asType(methodType(ConsValue.class, head, tail));
            pair = permuteArguments(pair, methodType(ConsValue.class, tail, head), 1, 0);
            fEmit = filterReturnValue(pair, fEmit);

            // fixme... consider making closures implement... ConsValue?
            // fixme... probably no need to spin a class for each methodhandle....
            // fixme... maybe only specialize for int...
            // fixme.. otoh we might want to inline large environments in the future ....
            // fixme... I could link to the closure above to get it's arguments, or... explicitly track state.
            var intro = Closure.spinFactory(tail, head, fEmit);

            return new Chunk<>(intro);
        }
    }

    record Call<V, Z, A, B>(Signature<V, F<Z, B>>signature,
                            Signature<V, Z>domain,
                            Generic<V, F<Z, F<A, B>>>f,
                            Generic<V, F<Z, A>>x) implements Generic<V, F<Z, B>> {

        public Chunk<F<Z, B>> compile(MethodHandles.Lookup lookup, Type<V> klass) {
            var d = domain.apply(klass);

            var fEmit = f.compile(lookup, klass).intro();
            var xEmit = x.compile(lookup, klass).intro();

            var cs = ValueLinker.link(lookup, StandardOperation.CALL, methodType(Object.class, fEmit.type().returnType(), Void.class, xEmit.type().returnType()));
            var mh = cs.dynamicInvoker();
            mh = insertArguments(mh, 1, (Object) null);
            System.err.println("domain " + mh + " " + fEmit + " " + xEmit);
            mh = filterArguments(mh, 0, fEmit, xEmit);
            var reorder = new int[mh.type().parameterCount()];
            for (var ii = 0; ii < reorder.length; ++ii) {
                reorder[ii] = ii;
            }
            reorder[1] = 0;

            mh = permuteArguments(mh, mh.type().dropParameterTypes(1, 2), reorder);

            return new Chunk<>(mh);
        }

        public String toString() {
            return "(S " + f + " " + x + ")";
        }
    }

    record Get<V, X, A extends HList<A>, B extends HList<B>>(Signature<V, F<A, X>>signature,
                                                             Signature<V, A>value,
                                                             Index<A, HList.Cons<X, B>>ix) implements Generic<V, F<A, X>> {
        public Chunk<F<A, X>> compile(Lookup lookup, Type<V> klass) {
            var domain = value.apply(klass).flatten();
            var num = ix.reify();
            var result = domain.get(num);

            var before = domain.stream().limit(num).collect(Collectors.toUnmodifiableList());
            var after = domain.stream().skip(num + 1).collect(Collectors.toUnmodifiableList());

            var mh = MethodHandles.identity(result);
            mh = dropArguments(mh, 0, before);
            mh = dropArguments(mh, mh.type().parameterCount(), after);

            System.err.println(domain + " " + num + " " + before + " " + after + " " + mh);

            return new Chunk<>(mh);
        }

        public String toString() {
            return "[" + ix + "]";
        }
    }

    record MakeLambda<X, A extends HList<A>, B, Z, R>(
            Signature<X, F<Z, R>>signature,
            Signature<X, Z>domain, Signature<X, R>range,
            Signature<X, A>funDomain, Signature<X, B>funRange,
            Generic<X, F<A, B>>body) implements Generic<X, F<Z, R>> {
        public String toString() {
            return "(λ " + body + ")";
        }

        public Chunk<F<Z, R>> compile(Lookup lookup, Type<X> klass) {
            var toIgnore = domain.apply(klass).flatten();

            var d = funDomain.apply(klass).flatten();
            var r = funRange.apply(klass).erase();

            var bodyEmit = body.compile(lookup, klass).intro();

            var staticK = Static.spin(d, r, bodyEmit);

            var intro = constant(Value.class, staticK);
            intro = dropArguments(intro, 0, toIgnore);

            System.err.println("stati " + intro);
            return new Chunk<>(intro);
        }
    }
}