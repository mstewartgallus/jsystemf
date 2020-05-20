package com.sstewartgallus.ext.tuples;

import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.*;
import org.objectweb.asm.MethodVisitor;

import java.util.Objects;
import java.util.function.Function;

import static org.objectweb.asm.Opcodes.*;

public final class AtTupleIndexThunk<B extends Tuple<B>, X extends Tuple<X>, A> implements ThunkTerm<F<X, A>> {
    private final Type<A> head;
    private final Type<B> tail;
    private final TupleIndex<X, P<A, B>> index;
    private final int reify;

    public AtTupleIndexThunk(Type<A> head, Type<B> tail,
                             TupleIndex<X, P<A, B>> index) {
        this.head = head;
        this.tail = tail;
        this.index = index;
        this.reify = index.reify();
        Objects.requireNonNull(head);
        Objects.requireNonNull(tail);
    }

    public Type<A> head() {
        return head;
    }

    public TupleIndex<X, P<A, B>> index() {
        return index;
    }

    @Override
    public Term<F<X, A>> visitChildren(Visitor visitor) {
        return this;
    }

    @Override
    public <Z> Term<F<Z, F<X, A>>> pointFree(VarValue<Z> varValue) {
        return Term.constant(varValue.type(), this);
    }

    @Override
    public <C> Term<C> stepThunk(Function<ValueTerm<F<X, A>>, Term<C>> k) {
        return k.apply(index.domain().l(x -> x.stepThunk(xNorm -> {
            var xPair = (TuplePairValue<A, B>) index.index(xNorm);
            return xPair.head();
        })));
    }

    public void jit(MethodVisitor mw) {
        var h = head.erase();
        var ii = reify;
        if (h.isPrimitive()) {
            switch (h.getName()) {
                case "boolean", "byte", "char", "short", "int" -> {
                    mw.visitVarInsn(ILOAD, ii);
                }
                case "long" -> {
                    mw.visitVarInsn(LLOAD, ii);
                }
                case "float" -> {
                    mw.visitVarInsn(FLOAD, ii);
                }
                case "double" -> {
                    mw.visitVarInsn(DLOAD, ii);
                }
                default -> throw new IllegalStateException(h.getName());
            }
        } else {
            mw.visitVarInsn(ALOAD, ii);
        }
    }

    @Override
    public Type<F<X, A>> type() throws TypeCheckException {
        return index.domain().to(head);
    }

    @Override
    public String toString() {
        return "[" + index + "]";
    }
}
