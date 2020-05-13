package com.sstewartgallus.plato;

// fixme... consider just a throwable not an exception..
public class TypeCheckException extends RuntimeException {
    public TypeCheckException(Type<?> left, Type<?> right) {
        super("The type " + left + " could not be unified with the type " + right);
    }
}
