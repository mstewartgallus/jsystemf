package com.sstewartgallus.interpreter;

import java.util.Map;
import java.util.WeakHashMap;

final class Environment {
    private final Map<Id<?>, Object> map;

    Environment() {
        map = new WeakHashMap<>();
    }

    Environment(Environment toCopy) {
        map = new WeakHashMap<>(toCopy.map);
    }

    <A> Id<A> put(A value) {
        var id = new Id<A>();
        map.put(id, value);
        return id;
    }

    <A> A get(Id<A> id) {
        return (A) map.get(id);
    }
}
