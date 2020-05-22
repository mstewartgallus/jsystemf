package com.sstewartgallus.runtime;


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * A hack to work around the JVM noninlining/compiling of all bytecode within Throwable subclasses when abusing
 * Throwables for control flow.
 * <p>
 * FIXME probably best to make this a factory sort of thing...
 * FIXME verify this hack actually works!
 */
public final class ValueThrowables {
    private static final MethodHandle CLONE_MH;

    static {
        try {
            CLONE_MH = ValueThrowable.LOOKUP.findVirtual(Object.class, "clone", MethodType.methodType(Object.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private ValueThrowables() {
    }

    public static <T extends ValueThrowable> T clone(T template) {
        try {
            return (T) (Object) CLONE_MH.invokeExact(template);
        } catch (Throwable throwable) {
            return unreachable();
        }
    }

    private static <T extends ValueThrowable> T unreachable() {
        throw new Error("should never happen");
    }
}
