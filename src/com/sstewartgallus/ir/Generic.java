package com.sstewartgallus.ir;

import com.sstewartgallus.ext.tuples.Cons;
import com.sstewartgallus.ext.tuples.HList;
import com.sstewartgallus.ext.tuples.Index;
import com.sstewartgallus.ext.tuples.Nil;
import com.sstewartgallus.mh.Arguments;
import com.sstewartgallus.mh.TypedMethodHandle;
import com.sstewartgallus.plato.F;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.V;
import com.sstewartgallus.runtime.Static;
import com.sstewartgallus.runtime.Value;
import com.sstewartgallus.runtime.ValueLinker;
import jdk.dynalink.StandardOperation;

import java.lang.invoke.MethodHandles;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

public interface Generic<A> {
    // fixme.. consider static <A, B> Generic<B> apply(Generic<V<A, B>> generic, Signature<A> clazz) {
    static <A, B> Chunk<B> compile(Lookup lookup, Generic<V<A, B>> generic, Signature<A> klass) {
        return ((GenericV<A, B>) generic).compile(lookup, klass);
    }

    static <A, B> Generic<B> apply(Generic<V<A, B>> generic, Signature<A> klass) {
        return ((GenericV<A, B>) generic).apply(klass);
    }

    // fixme... what really want instead of Void is a T such that Type<T> only has one inhabitant...
    static <B> Value<B> compile(Lookup lookup, Generic<V<Object, F<Nil, B>>> generic) {
        var chunk = Generic.compile(lookup, generic, new Signature.Pure<>(Object.class));

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

    default Chunk<A> compile(Lookup lookup) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default Signature<A> signature() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    record Bundle<C extends Arguments<C>, D, B>(TypedMethodHandle<C, D>handle, Proof<C, D, B>proof) {
    }

    record IntValue<L>(Signature<V<L, Integer>>signature,
                      int value) implements GenericV<L, Integer> {
        public String toString() {
            return String.valueOf(value);
        }

        public Bundle<?, ?, Integer> compileToHandle(Lookup lookup, Type<L> klass) {
            throw null;
        }

        public Chunk<Integer> compile(Lookup lookup, Signature<L> klass) {
            return new Chunk<>(constant(int.class, value));
        }
    }

    record K<L, A, B>(Signature<V<L, F<A, B>>>signature,
                      Signature<V<L, A>>domain,
                      Generic<V<L, B>>value) implements GenericV<L, F<A, B>> {
        public String toString() {
            return "(K " + value + ")";
        }

        public Bundle<?, ?, F<A, B>> compileToHandle(MethodHandles.Lookup lookup, Type<L> klass) {
            throw null;
        }

        public Chunk<F<A, B>> compile(MethodHandles.Lookup lookup, Signature<L> klass) {
            var d = Signature.apply(domain, klass).flatten();
            var handle = Generic.compile(lookup, value, klass).intro();
            handle = dropArguments(handle, 0, d);
            return new Chunk<>(handle);
        }
    }

    record Call<L, Z, A, B>(Signature<V<L, F<Z, B>>>signature,
                            Signature<V<L, Z>>domain,
                            Generic<V<L, F<Z, F<A, B>>>>f,
                            Generic<V<L, F<Z, A>>>x) implements GenericV<L, F<Z, B>> {

        public Chunk<F<Z, B>> compile(MethodHandles.Lookup lookup, Signature<L> klass) {
            var fEmit = Generic.compile(lookup, f, klass).intro();
            var xEmit = Generic.compile(lookup, x, klass).intro();

            var cs = ValueLinker.link(lookup, StandardOperation.CALL, methodType(Object.class, fEmit.type().returnType(), Void.class, xEmit.type().returnType()));
            var mh = cs.dynamicInvoker();
            mh = insertArguments(mh, 1, (Object) null);

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

    record Get<L, X, A extends HList<A>, B extends HList<B>>(Signature<V<L, F<A, X>>>signature,
                                                             Signature<V<L, A>>value,
                                                             Index<A, Cons<X, B>>ix) implements GenericV<L, F<A, X>> {
        public Chunk<F<A, X>> compile(Lookup lookup, Signature<L> klass) {
            var domain = Signature.apply(value, klass).flatten();
            var num = ix.reify();
            var result = domain.get(num);

            var before = domain.stream().limit(num).collect(Collectors.toUnmodifiableList());
            var after = domain.stream().skip(num + 1).collect(Collectors.toUnmodifiableList());

            var mh = MethodHandles.identity(result);
            mh = dropArguments(mh, 0, before);
            mh = dropArguments(mh, mh.type().parameterCount(), after);

            return new Chunk<>(mh);
        }

        public String toString() {
            return "[" + ix + "]";
        }
    }

    record Lambda<X, A extends HList<A>, B, R>(
            Signature<V<X, R>>signature,
            Signature<V<X, A>>funDomain, Signature<V<X, B>>funRange,
            Generic<V<X, F<A, B>>>body) implements GenericV<X, R> {
        public String toString() {
            return "(λ" + funDomain + " " + body + ")";
        }

        public Chunk<R> compile(Lookup lookup, Signature<X> klass) {
            var d = Signature.apply(funDomain, klass).flatten();
            var r = Signature.apply(funRange, klass).erase();

            var bodyEmit = Generic.compile(lookup, body, klass).intro();

            // fixme... attach a name or some other metadata...
            var staticK = Static.spin(d, r, bodyEmit);

            var intro = constant(Value.class, staticK);

            return new Chunk<>(intro);
        }
    }
}
