package com.sstewartgallus.plato.ir.cps;


public record ApplyKont<A>(Action<A>action, Kont<A>next) implements Instr {
    public static <B> Instr of(Action<B> action, GotoKont<B> next) {
        return new ApplyKont<>(action, next);
    }

    @Override
    public String toString() {
        return "(" + action + ", " + next + ")";
    }
}

