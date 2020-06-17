package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.systemf.Global;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.ActionDesc;
import com.sstewartgallus.plato.runtime.Jit;

import java.util.Objects;

public record GlobalCode<A>(Global<A>global) implements Code<A> {
    public GlobalCode {
        Objects.requireNonNull(global);
    }

    @Override
    public String toString() {
        return global.toString();
    }

    @Override
    public TypeDesc<A> type() {
        return global.type();
    }

    @Override
    public void compile(Jit.Environment environment) {
        // fixme... know if tail calll...
        var desc = ActionDesc.callGlobal(global.packageName(), global.name(), null);
        environment.local().indy(desc);
    }
}
