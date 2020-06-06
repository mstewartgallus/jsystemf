package com.sstewartgallus.plato.cbpv;

import com.sstewartgallus.plato.syntax.ext.variables.Id;
import com.sstewartgallus.plato.syntax.type.Type;

import java.util.Objects;

// fixme... should be a nonpure extension to the language ?
public final class VarLiteral<A> implements Literal<A>, Comparable<VarLiteral<?>> {
    private final Type<A> type;
    private final Id<A> variable;

    public VarLiteral(Type<A> type) {
        this(type, new Id<>());
    }

    public VarLiteral(Type<A> type, Id<A> variable) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(variable);
        this.type = type;
        this.variable = variable;
    }

    public Type<A> type() {
        return type;
    }

    @Override
    public String toString() {
        return "v" + variable;
    }

    @Override
    public A interpret(InterpreterEnvironment environment) {
        return environment.get(this);
    }

    @Override
    public void compile(CompilerEnvironment environment) {
        environment.emitVariable(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VarLiteral v && v.variable == variable;
    }

    @Override
    public int compareTo(VarLiteral<?> o) {
        return variable.compareTo(o.variable);
    }

}
