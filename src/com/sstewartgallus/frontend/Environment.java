package com.sstewartgallus.frontend;

import com.sstewartgallus.plato.Term;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class Environment {
    private final Map<String, Term<?>> map;

    private Environment(Map<String, Term<?>> map) {
        this.map = map;
    }

    public static Environment empty() {
        return new Environment(new TreeMap<>());
    }

    public Environment put(String str, Term<?> term) {
        var newMap = new TreeMap<>(map);
        newMap.put(str, term);
        return new Environment(newMap);
    }

    public Optional<Term<?>> get(String str) {
        var value = map.get(str);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }
}
