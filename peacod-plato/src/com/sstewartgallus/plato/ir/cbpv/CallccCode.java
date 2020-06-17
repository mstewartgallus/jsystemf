package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.cps.Lbl;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.Jit;

public record CallccCode<A>(Lbl<A>label, Code<A>contents) implements Code<A> {
    @Override
    public String toString() {
        return label + ":" + ("\n" + contents).replace("\n", "\n\t");
    }

    @Override
    public TypeDesc<A> type() {
        throw null;
    }


    @Override
    public void compile(Jit.Environment environment) {
        var frame = environment.frame(label);
        contents.compile(frame);
        frame.local().methodVisitor().visitMaxs(0, 0);
        frame.local().methodVisitor().visitEnd();
    }
}
