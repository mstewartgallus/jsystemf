package com.sstewartgallus.plato;

import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.runtime.TermDesc;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * The high level syntax for the core System F terms in my little language.
 * <p>
 * This is intended to be pristine source language untainted by compiler stuff.
 * <p>
 * Any processing should happen AFTER this step.
 * <p>
 * See https://gitlab.haskell.org/ghc/ghc/-/wikis/commentary/compiler/core-syn-type
 * and https://github.com/DanBurton/Blog/blob/master/Literate%20Haskell/SystemF.lhs
 * for inspiration.
 */
public interface Term<A> extends Constable {
    static <A, B> Term<B> apply(Term<V<A, B>> f, Type<A> x) {
        return new TypeApplyTerm<>(f, x);
    }

    static <A, B> Term<B> apply(Term<F<A, B>> f, Term<A> x) {
        return new ApplyTerm<>(f, x);
    }

    static <A, B> ValueTerm<V<A, B>> v(Function<Type<A>, Term<B>> f) {
        return new SimpleTypeLambdaTerm<>(f);
    }

    default Optional<TermDesc<A>> describeConstable() {
        throw null;
    }

    Type<A> type() throws TypeCheckException;

    default Term<A> visit(Visitor visitor) {
        return visitor.term(this);
    }

    Term<A> visitChildren(Visitor visitor);

    default void jit(ClassDesc thisClass, ClassVisitor classVisitor, MethodVisitor methodVisitor, Map<VarValue<?>, VarData> varDataMap) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    abstract class Visitor {
        public <T> Type<T> type(Type<T> type) {
            return type;
        }

        public <T> Term<T> term(Term<T> term) {
            return term;
        }
    }

    record VarData(int argument, Type<?>type) {
    }
}