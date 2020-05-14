package com.sstewartgallus.ext.variables;

import com.sstewartgallus.ir.Signature;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.V;

public final class VarType<T> implements Type<T> {
    private final Id<T> variable;

    public VarType() {
        variable = new Id<>();
    }

    @Override
    public String toString() {
        return "t" + variable;
    }

    @Override
    public <Z> Signature<V<Z, T>> pointFree(Id<Z> argument) {
        if (variable == argument) {
            return (Signature) new Signature.Identity<Z>();
        }
        throw new Error("fixme");
    }

    @Override
    public <Y> Type<T> unify(Type<Y> right) {
        throw new UnsupportedOperationException("unimplemented");
    }

    public <A> Term<A> substituteIn(Term<A> root, Type<T> replacement) {
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Type<T> type(Type<T> type) {
                if (!(type instanceof VarType<T> varType)) {
                    return type.visitChildren(this);
                }

                if (varType.variable == variable) {
                    return (Type) replacement;
                }
                return varType;
            }
        });
    }

    public <A> Type<A> substituteIn(Type<A> root, Type<T> replacement) {
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Type<T> type(Type<T> type) {
                if (!(type instanceof VarType<T> varType)) {
                    return type.visitChildren(this);
                }

                if (varType.variable == variable) {
                    return (Type) replacement;
                }
                return varType;
            }
        });
    }
}
