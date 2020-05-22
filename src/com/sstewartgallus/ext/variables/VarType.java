package com.sstewartgallus.ext.variables;

import com.sstewartgallus.plato.NominalType;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
import com.sstewartgallus.plato.TypeTag;

import java.lang.constant.ConstantDesc;
import java.util.Optional;

public final class VarType<T> implements TypeTag<T> {
    public VarType() {
    }

    @Override
    public String toString() {
        return "t" + hashCode();
    }

    public <A> Term<A> substituteIn(Term<A> root, Type<T> replacement) {
        var self = this;
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Type<T> type(Type<T> type) {
                if (!(type instanceof NominalType<T> nominalType && nominalType.tag() instanceof VarType<T> varType)) {
                    return type.visitChildren(this);
                }

                if (varType == self) {
                    return (Type) replacement;
                }
                return nominalType;
            }
        });
    }

    public <A> Type<A> substituteIn(Type<A> root, Type<T> replacement) {
        var self = this;
        return root.visit(new Term.Visitor() {
            @Override
            public <T> Type<T> type(Type<T> type) {
                if (!(type instanceof NominalType<T> nominalType && nominalType.tag() instanceof VarType<T> varType)) {
                    return type.visitChildren(this);
                }

                if (varType == self) {
                    return (Type) replacement;
                }
                return nominalType;
            }
        });
    }

    @Override
    public Class<?> erase() {
        throw null;
    }

    @Override
    public Optional<? extends ConstantDesc> describeConstable() {
        throw null;
    }
}
