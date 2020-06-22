package com.sstewartgallus.plato.runtime.type;


import com.sstewartgallus.plato.runtime.V;

import java.lang.invoke.MethodHandles;

@SuppressWarnings("unused")
public final class TypeBootstraps {
    private TypeBootstraps() {
    }

    @SuppressWarnings("unused")
    public static <A, B> Type<B> ofApplication(MethodHandles.Lookup lookup, String name, Class<A> klass, Type<V<A, B>> f, Type<A> x) {
        var generic = (GenericType<A, B>) f;
        return generic.apply(x);
    }

    @SuppressWarnings("unused")
    public static Type<?> ofReference(MethodHandles.Lookup lookup, String name, Class<?> klass, String packageName) {
        // fixme.. global cache
        // fixme... we need a cache of all packages.. but how to allow gc..?
        if ("core".equals(packageName)) {
            switch (name) {
                case "u":
                    return ThunkType.thunk();
                case "fn":
                    return FnType.function();
                case "int":
                    return IntFType.INTF_TYPE;
                case "#int":
                    return IntType.INT_TYPE;
            }
        }
        throw new Error("unimplemented " + packageName + "/" + name);
    }
}
