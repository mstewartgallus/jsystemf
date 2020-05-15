package com.sstewartgallus.frontend;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

// fixme... abstract out more data about environment entities
// fixme... allow types as entities...
public interface Entity {
    String name();

    record TermEntity(String name, Term<?>term) implements Entity {
    }

    record TypeEntity(String name, Type<?>type) implements Entity {
    }

    record SpecialFormEntity(String name, BiFunction<List<Node>, Environment, Term<?>>f) implements Entity {
    }
}
