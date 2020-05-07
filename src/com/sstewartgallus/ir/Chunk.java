package com.sstewartgallus.ir;

import java.lang.invoke.MethodHandle;
import java.util.List;

import static java.lang.invoke.MethodHandles.identity;

// fixme... consider multiple intro types...
record Chunk<A>(MethodHandle intro, List<MethodHandle>eliminators) {
    Chunk(MethodHandle intro) {
        this(intro, List.of(identity(intro.type().returnType())));
    }
}
