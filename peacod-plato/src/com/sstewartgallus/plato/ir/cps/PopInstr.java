package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.FnImpl;
import com.sstewartgallus.plato.runtime.U;

import java.util.Set;
import java.util.TreeSet;

public record PopInstr<A, B>(LocalValue<A>binder, Instr<B>body) implements Instr<Fn<A, B>> {

    @Override
    public FnImpl<A, B> interpret(CpsEnvironment environment) {
        return new FnImpl<A, B>(environment.resolve(binder.type())) {
            @Override
            public U<B> apply(A value) {
                return body.interpret(environment.put(binder, value));
            }
        };
    }

    @Override
    public void compile(CompilerEnvironment environment) {
        body.compile(environment);
    }

    @Override
    public Set<LocalValue<?>> dependencies() {
        var set = new TreeSet<>(body.dependencies());
        set.remove(binder);
        return set;
    }

    @Override
    public Set<LocalValue<?>> arguments() {
        return union(Set.of(binder), body.arguments());
    }

    @Override
    public Set<LocalValue<?>> locals() {
        return body.locals();
    }

    @Override
    public TypeDesc<Fn<A, B>> type() {
        return binder.type().toFn(body.type());
    }

    @Override
    public String toString() {
        return "λ " + binder + " ∈ " + binder.type() + " →\n" + body;
    }
}
