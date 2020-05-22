package com.sstewartgallus.plato;

record Halt<A>(Term<A>value) implements State<A> {
}
