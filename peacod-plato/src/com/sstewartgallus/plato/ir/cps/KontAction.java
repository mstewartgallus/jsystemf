package com.sstewartgallus.plato.ir.cps;


public record KontAction<B>(Lbl<B>label, Instr body) implements Action<B> {
    public static <B> Action<B> of(Lbl<B> binder, Instr body) {
        return new KontAction<>(binder, body);
    }

    @Override
    public String toString() {
        return "κ " + label + " →\n" + body;
    }
}

