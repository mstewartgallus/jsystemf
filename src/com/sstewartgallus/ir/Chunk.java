package com.sstewartgallus.ir;

import java.lang.invoke.MethodHandle;

// fixme... consider multiple intro types...
record Chunk<A>(MethodHandle intro) {
}
