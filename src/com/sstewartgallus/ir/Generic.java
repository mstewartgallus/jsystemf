package com.sstewartgallus.ir;

import com.sstewartgallus.mh.Arguments;
import com.sstewartgallus.mh.TypedMethodHandle;
import com.sstewartgallus.pass1.Index;
import com.sstewartgallus.pass1.TPass0;
import com.sstewartgallus.runtime.LdcStub;
import com.sstewartgallus.runtime.Static;
import com.sstewartgallus.runtime.Value;
import com.sstewartgallus.runtime.ValueLinker;
import com.sstewartgallus.type.*;
import jdk.dynalink.StandardOperation;

import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
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

    // fixme... what really want instead of Void is a T such that Type<T> only has one inhabitant...
    static <B> Value<B> compile(MethodHandles.Lookup lookup, Generic<Void, F<HList.Nil, B>> generic) {
        var chunk = generic.compile(lookup, new TPass0.PureType<>(Void.class));

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

    default Bundle<?, ?, B> compileToHandle(MethodHandles.Lookup lookup, Type<A> klass) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default Chunk<B> compile(Lookup lookup, TPass0<A> klass) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default Signature<A, B> signature() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    record Bundle<C extends Arguments<C>, D, B>(TypedMethodHandle<C, D>handle, Proof<C, D, B>proof) {
    }

    record Con<V, A, B>(Signature<V, B>signature,
                        ConstantDesc value) implements Generic<V, B> {
        public String toString() {
            return value.toString();
        }

        public Bundle<?, ?, B> compileToHandle(Lookup lookup, Type<V> klass) {
            throw null;
        }

        public Chunk<B> compile(Lookup lookup, TPass0<V> klass) {
            var t = signature.apply(klass).erase();

            MethodHandle handle;
            if (value instanceof String || value instanceof Float || value instanceof Double || value instanceof Integer || value instanceof Long) {
                handle = constant(t, value);
            } else if (value instanceof DynamicConstantDesc<?> dyn) {
                // fixme... use proper lookup scope..
                handle = LdcStub.spin(lookup, t, dyn);
            } else {
                try {
                    handle = constant(t, value.resolveConstantDesc(lookup));
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }

            return new Chunk<>(handle);
        }
    }

    record K<V, A, B>(Signature<V, F<A, B>>signature,
                      Signature<V, A>domain,
                      Generic<V, B>value) implements Generic<V, F<A, B>> {
        public String toString() {
            return "(K " + value + ")";
        }

        public Bundle<?, ?, F<A, B>> compileToHandle(MethodHandles.Lookup lookup, Type<V> klass) {
            throw null;
        }

        public Chunk<F<A, B>> compile(Lookup lookup, TPass0<V> klass) {
            var d = domain.apply(klass).flatten();
            var handle = value.compile(lookup, klass).intro();
            handle = dropArguments(handle, 0, d);
            return new Chunk<>(handle);
        }
    }

    record CurryType<Z, X, A, B, C>(Signature<Z, F<X, V<A, B>>>signature,
                                    Generic<E<Z, A>, F<X, B>>f) implements Generic<Z, F<X, V<A, B>>> {
        public String toString() {
            return "(curry-type " + f + ")";
        }

        public Chunk<F<X, V<A, B>>> compile(Lookup lookup, TPass0<Z> klass) {
            // fixme... need to accept a klass arguments at runtime I think...
            // fixme... createa  E<Z,A> from Z ?
            throw new UnsupportedOperationException("unimplemented");
            //  return f.compile(lookup, klass);
        }
    }

    record Call<V, Z, A, B>(Signature<V, F<Z, B>>signature,
                            Signature<V, Z>domain,
                            Generic<V, F<Z, F<A, B>>>f,
                            Generic<V, F<Z, A>>x) implements Generic<V, F<Z, B>> {

        public Chunk<F<Z, B>> compile(Lookup lookup, TPass0<V> klass) {
            var fEmit = f.compile(lookup, klass).intro();
            var xEmit = x.compile(lookup, klass).intro();

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

    record Get<V, X, A extends HList<A>, B extends HList<B>>(Signature<V, F<A, X>>signature,
                                                             Signature<V, A>value,
                                                             Index<A, HList.Cons<X, B>>ix) implements Generic<V, F<A, X>> {
        public Chunk<F<A, X>> compile(Lookup lookup, TPass0<V> klass) {
            var domain = value.apply(klass).flatten();
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
            Signature<X, R>signature,
            Signature<X, A>funDomain, Signature<X, B>funRange,
            Generic<X, F<A, B>>body) implements Generic<X, R> {
        public String toString() {
            return "(λ" + funDomain + " " + body + ")";
        }

        public Chunk<R> compile(Lookup lookup, TPass0<X> klass) {
            var d = funDomain.apply(klass).flatten();
            var r = funRange.apply(klass).erase();

            var bodyEmit = body.compile(lookup, klass).intro();

            // fixme... attach a name or some other metadata...
            var staticK = Static.spin(d, r, bodyEmit);

            var intro = constant(Value.class, staticK);

            return new Chunk<>(intro);
        }
    }
}