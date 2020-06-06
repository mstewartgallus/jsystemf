package com.sstewartgallus.plato.syntax.ext.variables;

import com.sstewartgallus.plato.syntax.term.Term;
import com.sstewartgallus.plato.syntax.type.NominalType;
import com.sstewartgallus.plato.syntax.type.Type;
import com.sstewartgallus.plato.syntax.type.TypeTag;

import java.lang.constant.ConstantDesc;
import java.util.Optional;

// fixme.. not sure should be nominal type...
public final class VarType<T> implements TypeTag<T> {
    @Override
    public String toString() {
        return "V" + hashCode();
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

    @Override
    public Class<?> erase() {
        throw null;
    }

    @Override
    public Optional<? extends ConstantDesc> describeConstable() {
        throw null;
    }
}
