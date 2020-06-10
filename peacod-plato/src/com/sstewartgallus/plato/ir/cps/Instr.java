package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Jit;
import com.sstewartgallus.plato.runtime.U;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.TreeSet;

// fixme... just explicitly represent as a list! + a jump at the end
public interface Instr<A> {
    default U<A> interpret(CpsEnvironment environment) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default void compile(CompilerEnvironment environment) {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default Set<LocalValue<?>> dependencies() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default Set<LocalValue<?>> arguments() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default Set<LocalValue<?>> locals() {
        throw new UnsupportedOperationException(getClass().toString());
    }

    default <A extends Comparable> Set<A> union(Set<A>... values) {
        var set = new TreeSet<A>();
        for (var v : values) {
            set.addAll(v);
        }
        return set;
    }

    default U<A> compile(MethodHandles.Lookup lookup, PrintWriter writer) {
        return Jit.jit(this::compile, lookup, writer);
    }

    TypeDesc<A> type();
}
