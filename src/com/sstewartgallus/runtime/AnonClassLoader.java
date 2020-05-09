package com.sstewartgallus.runtime;

// fixme.. look into java modules
final class AnonClassLoader<T> extends ClassLoader {
    static {
        //fixme?
        registerAsParallelCapable();
    }

    private final T value;

    private AnonClassLoader(ClassLoader parent, T value) {
        super(null, parent);
        // fixme... ?
        clearAssertionStatus();
        this.value = value;
    }

    // fixme.. pass classloader as param
    static <T> Class<?> defineClass(T value, ClassLoader parent, String name, byte[] bytes) {
        var inst = new AnonClassLoader<>(parent, value);
        return inst.defineClass(name, bytes, 0, bytes.length);
    }

    // fixme... make private to the class we created....
    public T getValue() {
        return value;
    }
}
