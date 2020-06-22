package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.dethunk.Does;
import com.sstewartgallus.plato.ir.type.TypeDesc;

public interface Code<A> {
    Does<A> dethunk();

    int contains(Variable<?> variable);

    Code<A> visitChildren(CodeVisitor codeVisitor,
                          LiteralVisitor literalVisitor);

    TypeDesc<A> type();

}

