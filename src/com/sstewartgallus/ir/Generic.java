package com.sstewartgallus.ir;

import com.sstewartgallus.runtime.Closure;
import com.sstewartgallus.runtime.Pair;
import com.sstewartgallus.runtime.Value;
import com.sstewartgallus.runtime.ValueLinker;
import com.sstewartgallus.type.*;
import jdk.dynalink.StandardNamespace;
import jdk.dynalink.StandardOperation;

import java.lang.constant.ConstantDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodHandles.filterReturnValue;
import static java.lang.invoke.MethodType.methodType;

/**
 * A category represents Term a -> Term b in a point free way
 * <p>
 * Generic represents Type a -> Term a in a point free way
 */
public interface Generic<A, B> {
    static <X, A, B, Z> Generic<Z, F<X, V<A, B>>> curry(Signature<Z, F<X, V<A, B>>> signature,
                                                        Generic<E<Z, A>, F<X, B>> body) {
        return new CurryType<>(signature, body);
    }

    // for Category we use Void the class that has only one inhabitant
    // fixme... I believe what we want for Generic is the T such that Class<T> has only one inhabitant... null!
    static <B> Value<B> compile(MethodHandles.Lookup lookup, Generic<Void, F<Void, B>> generic) {
        var handle = generic.compile(lookup, Type.VOID);
        Object obj;
        try {
            obj = handle.invoke((Object) null);
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        return Value.class.cast(obj);
    }

    default MethodHandle compile(MethodHandles.Lookup lookup, Type<A> klass) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default Signature<A, B> signature() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    record Unit<V, A>(Signature<V, F<Void, A>>signature, ConstantDesc value) implements Generic<V, F<Void, A>> {
        public String toString() {
            return value.toString();
        }
    }

    record Identity<V, A>(Signature<V, F<A, A>>signature, Signature<V, A>value) implements Generic<V, F<A, A>> {
        public MethodHandle compile(MethodHandles.Lookup lookup, Type<V> klass) {
            var type = value.apply(klass).erase();
            return MethodHandles.identity(type);
        }

        public String toString() {
            return "id";
        }
    }

    record Compose<V, A, B, C>(Signature<V, F<A, C>>signature,
                               Generic<V, F<B, C>>f,
                               Generic<V, F<A, B>>g) implements Generic<V, F<A, C>> {
        public MethodHandle compile(MethodHandles.Lookup lookup, Type<V> klass) {
            return MethodHandles.filterReturnValue(g.compile(lookup, klass), f.compile(lookup, klass));
        }

        public String toString() {
            return f + " ⚬ " + g;
        }
    }

    record Product<V, A, B, C>(Signature<V, F<A, T<B, C>>>signature,
                               Generic<V, F<A, B>>f,
                               Generic<V, F<A, C>>g) implements Generic<V, F<A, T<B, C>>> {
        public String toString() {
            return f + " Δ " + g;
        }
    }

    record First<V, A, B>(Signature<V, F<T<A, B>, A>>signature,
                          Signature<V, A>firstValue) implements Generic<V, F<T<A, B>, A>> {
        public MethodHandle compile(MethodHandles.Lookup lookup, Type<V> klass) {
            var first = firstValue.apply(klass).erase();
            var cs = ValueLinker.link(lookup, StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("first"), methodType(first, Pair.class));
            return cs.dynamicInvoker();
        }

        public String toString() {
            return "exl";
        }
    }

    record Second<V, A, B>(Signature<V, F<T<A, B>, B>>signature,
                           Signature<V, B>secondValue) implements Generic<V, F<T<A, B>, B>> {
        public MethodHandle compile(MethodHandles.Lookup lookup, Type<V> klass) {
            var second = secondValue.apply(klass).erase();
            var cs = ValueLinker.link(lookup, StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("second"), methodType(second, Pair.class));
            return cs.dynamicInvoker();
        }

        public String toString() {
            return "exr";
        }
    }

    record CurryType<Z, X, A, B, C>(Signature<Z, F<X, V<A, B>>>signature,
                                           Generic<E<Z, A>, F<X, B>>f) implements Generic<Z, F<X, V<A, B>>> {
        public String toString() {
            return "(curry-type " + f + ")";
        }

        public MethodHandle compile(MethodHandles.Lookup lookup, Type<Z> klass) {
            // fixme... need to accept a klass argument at runtime I think...
            // fixme... createa  E<Z,A> from Z ?
            throw new UnsupportedOperationException("unimplemented");
            //  return f.compile(lookup, klass);
        }
    }

    record Curry<V, A, B, C>(Signature<V, F<A, F<B, C>>>signature,
                                    Signature<V, A>left,
                                    Signature<V, B>right,
                                    Generic<V, F<T<A, B>, C>>f) implements Generic<V, F<A, F<B, C>>> {
        public String toString() {
            return "(curry " + f + ")";
        }

        public MethodHandle compile(MethodHandles.Lookup lookup, Type<V> klass) {
            // fixme... obviously wrong...
            var domain = left.apply(klass).erase();
            var second = right.apply(klass).erase();

            var fEmit = f.compile(lookup, klass);
            fEmit = filterReturnValue(PAIR_MH.asType(methodType(Pair.class, domain, second)), fEmit);
            // fixme... probably no need to spin a class for each methodhandle....
            // fixme... maybe only specialize for int...
            // fixme.. otoh we might want to inline large environments in the future ....
            return Closure.spinFactory(domain, second, fEmit);
        }

        static final MethodHandle PAIR_MH;

        static {
            try {
                PAIR_MH = MethodHandles.lookup().findStatic(Pair.class, "of", MethodType.methodType(Pair.class, Object.class, Object.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    record Uncurry<V, A, B, C>(Generic<V, F<A, F<B, C>>>f) implements Generic<V, F<T<A, B>, C>> {
        public String toString() {
            return "(uncurry " + f + ")";
        }
    }

    record Initial<V, A, B, C>(Signature<V, A>f) implements Generic<V, F<A, Void>> {
        public String toString() {
            return "it";
        }
    }
}