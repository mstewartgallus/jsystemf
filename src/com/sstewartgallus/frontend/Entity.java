package com.sstewartgallus.frontend;

import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;

// fixme... abstract out more data about environment entities
// fixme... allow types as entities...
public interface Entity {
    record TermEntity(Term<?>term) implements Entity {
    }

    record TypeEntity(Type<?>type) implements Entity {
    }
}
