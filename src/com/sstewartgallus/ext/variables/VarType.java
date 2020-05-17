package com.sstewartgallus.ext.variables;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;

public final class VarType<T> implements Type<T> {
    public VarType() {
    }

    @Override
    public String toString() {
        return "t" + hashCode();
    }

    @Override
    public <Y> Type<T> unify(Type<Y> right) {
        throw new UnsupportedOperationException("unimplemented");
    }

    public <A> Term<A> substituteIn(Term<A> root, Type<T> replacement) {
        var self = this;
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Type<T> type(Type<T> type) {
                if (!(type instanceof VarType<T> varType)) {
                    return type.visitChildren(this);
                }

                if (varType == self) {
                    return (Type) replacement;
                }
                return varType;
            }
        });
    }

    public <A> Type<A> substituteIn(Type<A> root, Type<T> replacement) {
        var self = this;
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Type<T> type(Type<T> type) {
                if (!(type instanceof VarType<T> varType)) {
                    return type.visitChildren(this);
                }

                if (varType == self) {
                    return (Type) replacement;
                }
                return varType;
            }
        });
    }
}
