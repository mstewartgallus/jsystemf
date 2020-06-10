package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.Type;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.ActionBootstraps;
import com.sstewartgallus.plato.runtime.U;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.TreeMap;

public final class CpsEnvironment {
    private static final Object SENTINEL = new Object();
    private final MethodHandles.Lookup lookup;
    private final Map<LocalValue<?>, Object> map;
    private final Map<LabelValue<?>, Instr<?>> labels;

    public CpsEnvironment(MethodHandles.Lookup lookup) {
        this.lookup = lookup;
        this.map = Map.of();
        this.labels = Map.of();
    }

    private CpsEnvironment(MethodHandles.Lookup lookup, Map<LocalValue<?>, Object> map, Map<LabelValue<?>, Instr<?>> labels) {
        this.lookup = lookup;
        this.map = map;
        this.labels = labels;
    }

    public <A> A get(GlobalValue<A> binder) {
        Type type;
        try {
            type = binder.type().resolveConstantDesc(lookup);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
        // fixme... better erasure...
        return (A) ActionBootstraps.ofReference(lookup, binder.name(), U.class, binder.packageName(), type);
    }

    public <A> Type<A> resolve(TypeDesc<A> type) {
        try {
            return type.resolveConstantDesc(lookup);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public <A> Instr<A> get(LabelValue<A> label) {
        var value = labels.get(label);
        if (null == value) {
            throw new IllegalStateException("Label " + label + " not found in environment " + this);
        }
        return (Instr<A>) value;
    }

    public <A> A get(LocalValue<A> binder) {
        var value = map.getOrDefault(binder, SENTINEL);
        if (SENTINEL == value) {
            throw new IllegalStateException("Variable " + binder + " not found in environment " + this);
        }
        return (A) value;
    }


    public <A> CpsEnvironment put(LocalValue<A> binder, A value) {
        var copy = new TreeMap<>(map);
        copy.put(binder, value);
        return new CpsEnvironment(lookup, copy, labels);
    }

    public <A> CpsEnvironment put(LabelValue<A> label, Instr<A> instr) {
        var copy = new TreeMap<>(labels);
        copy.put(label, instr);
        return new CpsEnvironment(lookup, map, copy);
    }
}
