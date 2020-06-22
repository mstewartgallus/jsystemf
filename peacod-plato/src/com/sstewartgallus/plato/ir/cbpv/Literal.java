package com.sstewartgallus.plato.ir.cbpv;

import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.dethunk.Thing;
import com.sstewartgallus.plato.ir.type.TypeDesc;

public interface Literal<A> {
    TypeDesc<A> type();

    Literal<A> visitChildren(CodeVisitor codeVisitor,
                             LiteralVisitor literalVisitor);

    Thing<A> dethunk();

    int contains(Variable<?> variable);
}

