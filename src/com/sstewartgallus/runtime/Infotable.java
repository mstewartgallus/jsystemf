package com.sstewartgallus.runtime;

import java.lang.invoke.MethodHandle;
import java.util.List;

// fixme... eventually eliminate ...
record Infotable(
        List<Class<?>>arguments,
        MethodHandle entryPoint) {
}
