package com.sstewartgallus.plato.frontend;

import com.sstewartgallus.plato.syntax.term.Term;
import com.sstewartgallus.plato.syntax.type.Type;

import java.util.List;
import java.util.function.BiFunction;

// fixme... abstract out more data about environment entities
// fixme... allow types as entities...
public interface Entity {
    String name();

    record TermEntity(String name, Term<?>term) implements Entity {
    }

    record TypeEntity(String name, Type<?>type) implements Entity {
    }

    record PrologTermEntity(String name, org.projog.core.term.Term term) implements Entity {
    }

    record SpecialFormEntity(String name,
                             BiFunction<List<Node>, Environment, org.projog.core.term.Term>prolog,
                             BiFunction<List<Node>, Environment, Term<?>>f) implements Entity {
    }
}
