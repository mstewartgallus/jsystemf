package com.sstewartgallus.interpreter;

import java.util.Map;
import java.util.WeakHashMap;

final class Environment {
    private final Map<Id<?>, Code<?>> map;

    Environment() {
        map = new WeakHashMap<>();
    }

    private Environment(Map<Id<?>, Code<?>> toCopy) {
        map = toCopy;
    }

    <A> Code<A> get(Id<A> id) {
        return (Code<A>) map.get(id);
    }

    public <Z> Environment put(Id<Z> binder, Code<Z> value) {
        var newMap = new WeakHashMap<>(map);
        newMap.put(binder, value);
        return new Environment(newMap);
    }
}
