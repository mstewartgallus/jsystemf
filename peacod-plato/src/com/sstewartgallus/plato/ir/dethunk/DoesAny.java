package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.cps.Action;
import com.sstewartgallus.plato.runtime.type.Behaviour;

public interface DoesAny {
    public Action<Behaviour> specialize();
}
