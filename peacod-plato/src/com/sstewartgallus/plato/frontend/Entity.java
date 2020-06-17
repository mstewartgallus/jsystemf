package com.sstewartgallus.plato.frontend;

import com.sstewartgallus.plato.ir.systemf.Term;
import com.sstewartgallus.plato.ir.type.TypeDesc;

import java.util.List;
import java.util.function.BiFunction;

// fixme... abstract out more data about binder entities
// fixme... allow types as entities...
public interface Entity {
    String name();

    // fixme... add a type...
    record ReferenceTermEntity(String name, Term<?>term) implements Entity {
    }

    // fixme... add a type...
    record ReferenceTypeEntity(String name, TypeDesc<?>type) implements Entity {
    }

    record SpecialFormEntity(String name,
                             BiFunction<List<Node>, Environment, Term<?>>f) implements Entity {
    }
}
