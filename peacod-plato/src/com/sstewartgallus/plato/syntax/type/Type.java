package com.sstewartgallus.plato.syntax.type;

import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fun;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.runtime.V;
import com.sstewartgallus.plato.syntax.ext.variables.VarType;
import com.sstewartgallus.plato.syntax.term.LambdaTerm;
import com.sstewartgallus.plato.syntax.term.Solution;
import com.sstewartgallus.plato.syntax.term.Term;
import org.projog.core.term.Variable;

import java.lang.constant.Constable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * This is intended to be pristine source language untainted by compiler stuff.
 * <p>
 * Any processing should happen AFTER this step.
 */
public interface Type<X> extends Constable {

    static <A, B> Type<V<A, B>> v(Function<Type<A>, Type<B>> f) {
        return new ForallType<>(f);
    }

    static <A, B> Type<B> apply(Type<V<A, B>> f, Type<A> x) {
        if (f instanceof ForallType<A, B> forall) {
            return forall.f().apply(x);
        }
        return new TypeApplyType<>(f, x);
    }

    static <A, B> Type<V<A, V<B, Fun<A, B>>>> function() {
        return Helper.FUNCTION;
    }

    static <A> Type<V<A, U<A>>> thunkType() {
        return Helper.THUNK;
    }

    static <A> Type<V<A, F<A>>> returnType() {
        return Helper.BOX;
    }

    // fixme... how to move out...
    default Optional<TypeDesc<X>> describeConstable() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    // fixme... inline...
    default <B> Term<Fun<U<X>, B>> l(Function<Term<X>, Term<B>> f) {
        return new LambdaTerm<>(this, NominalType.ofTag(new VarType<>()), f);
    }

    default <B> Type<Fun<X, B>> to(Type<B> range) {
        return new TypeApplyType<>(new TypeApplyType<>(function(), this), range);
    }

    default Type<U<X>> thunk() {
        return new TypeApplyType<>(thunkType(), this);
    }

    default Type<X> visitChildren(Term.Visitor visitor) {
        throw null;
    }

    // fixme.. how to move out... ?
    default Class<?> erase() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default Type<F<X>> unboxed() {
        return new TypeApplyType<>(returnType(), this);
    }

    default Type<X> resolve(Solution environment) {
        throw new UnsupportedOperationException(this.getClass().toString());
    }

    default org.projog.core.term.Term toTerm(Map<VarType<?>, Variable> holevars) {
        throw new UnsupportedOperationException(this.getClass().toString());
    }

    default void holes(Set<VarType<?>> holes) {
        throw new UnsupportedOperationException(this.getClass().toString());
    }

}

class Helper {
    static final NominalType FUNCTION = NominalType.ofTag(FunctionTag.function());
    static final NominalType THUNK = new NominalType(new ThunkTag());
    static final NominalType BOX = new NominalType(new BoxTag());


}
