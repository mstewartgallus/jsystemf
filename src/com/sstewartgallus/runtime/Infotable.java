package com.sstewartgallus.runtime;

import java.lang.invoke.MethodHandle;
import java.util.List;

record Infotable(List<Class<?>>environment,
                 List<Class<?>>arguments,
                 MethodHandle entryPoint) {
}
