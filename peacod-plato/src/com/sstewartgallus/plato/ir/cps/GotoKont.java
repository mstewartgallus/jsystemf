package com.sstewartgallus.plato.ir.cps;

public record GotoKont<A>(Lbl<A>label) implements Kont<A> {
    @Override
    public String toString() {
        return label.toString();
    }

}
