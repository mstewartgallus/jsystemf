package com.sstewartgallus.plato.frontend;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class Environment {
    private final Map<String, Entity> map;

    private Environment(Map<String, Entity> map) {
        this.map = map;
    }

    public static Environment empty() {
        return new Environment(new TreeMap<>());
    }

    // fixme... error on collisions or something....
    public static Environment union(Environment left, Environment right) {
        var newMap = new TreeMap<>(left.map);
        newMap.putAll(right.map);
        return new Environment(newMap);
    }

    public Environment put(String str, Entity entity) {
        var newMap = new TreeMap<>(map);
        newMap.put(str, entity);
        return new Environment(newMap);
    }

    public Optional<Entity> get(String str) {
        var value = map.get(str);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    public String toString() {
        return map.toString();
    }
}
