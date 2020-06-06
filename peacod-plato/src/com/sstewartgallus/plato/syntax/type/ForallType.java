package com.sstewartgallus.plato.syntax.type;

import com.sstewartgallus.plato.runtime.V;

import java.util.function.Function;

public record ForallType<A, B>(Function<Type<A>, Type<B>>f) implements Type<V<A, B>> {
}
