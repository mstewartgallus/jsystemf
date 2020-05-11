package com.sstewartgallus.mh;

import java.lang.invoke.MethodHandle;
import java.util.function.Function;

public record TypedMethodHandle<A extends Arguments<A>, B>(A domain, Class<B>range, MethodHandle handle) {

    static TypedMethodHandle<Arguments.And<Integer, Arguments.And<Integer, Arguments.None>>, Integer> ADD = null;

    static void test() {
        var foo = TypedMethodHandle.of(int.class, Arguments.none().and(int.class).and(int.class), args -> {
            var x = args.get(Ix.get0());
            var y = args.get(Ix.get1());
            return ADD.apply(ValList.none().and(x).and(y));
        });
    }
    public static <A extends Arguments<A>, B> TypedMethodHandle<A, B> of(Class<B> range, Arguments<A> domain, Function<ArgumentList<A>, Val<B>> function) {
        var args = makeArgs(domain);
        var valResult = function.apply(args);
        return valResult.toHandle(args, range);
    }

    Val<B> apply(ValList<A> x) {
        return new Val.Apply<>(this, x);
    }

    static <A extends Arguments<A>> ArgumentList<A> makeArgs(Arguments<A> domain) {
        if (domain instanceof Arguments.None) {
            return (ArgumentList<A>) ArgumentList.None.NONE;
        }
        var d = (Arguments.And<?, ?>) domain;
        return (ArgumentList<A>) new ArgumentList.And<>(d.clazz(), makeArgs(d.tail()));
    }
}