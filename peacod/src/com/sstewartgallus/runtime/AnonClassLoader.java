package com.sstewartgallus.runtime;

public final class AnonClassLoader extends ClassLoader {
    static {
        registerAsParallelCapable();
    }

    private AnonClassLoader(ClassLoader parent) {
        super(null, parent);
        clearAssertionStatus();
    }

    public static <T> Class<?> defineClass(ClassLoader parent, byte[] bytes) {
        var inst = new AnonClassLoader(parent);
        var klass = inst.defineClass(null, bytes, 0, bytes.length);
        // load the class as soon as we define it...
        try {
            Class.forName(klass.getCanonicalName(), true, inst);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return klass;
    }
}
