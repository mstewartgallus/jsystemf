package com.sstewartgallus.plato.ir.dethunk;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cps.Value;
import com.sstewartgallus.plato.ir.type.TypeDesc;

public interface Thing<A> {
    TypeDesc<A> type();

    Thing<A> visitChildren(CodeVisitor codeVisitor,
                           LiteralVisitor literalVisitor);

    Value<A> toCps();

    int contains(Variable<?> variable);
}

