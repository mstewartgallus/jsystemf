package com.sstewartgallus;

import com.sstewartgallus.plato.ir.cbpv.*;
import com.sstewartgallus.plato.ir.cps.*;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.U;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class CpsTransform {
    private static final AtomicLong IDS = new AtomicLong(0);

    public static <A, C> Instr<C> toCps(Literal<A> code, Function<Value<A>, Instr<C>> k) {
        if (code instanceof GlobalLiteral<A> global) {
            return global(global, k);
        }
        if (code instanceof LocalLiteral<A> local) {
            return local(local, k);
        }
        if (code instanceof ThunkLiteral<?> thunk) {
            return thunk(thunk, (Function) k);
        }
        throw new IllegalArgumentException(code.toString());
    }

    public static <A, C> Instr<C> toCps(Code<A> code, Function<Instr<A>, Instr<C>> k) {
        if (code instanceof LambdaCode<?, ?> lambdaCode) {
            return lambda(lambdaCode, (Function) k);
        }
        if (code instanceof ApplyCode<?, A> apply) {
            return apply(apply, k);
        }
        if (code instanceof ForceCode<A> force) {
            return force(force, k);
        }
        throw new IllegalArgumentException(code.toString());
    }

    private static <B, A, C> Instr<C> apply(ApplyCode<B, A> code, Function<Instr<A>, Instr<C>> k) {
        var x = code.x();
        var f = code.f();

        return toCps(f, fValue ->
                toCps(x, xValue ->
                        k.apply(new PushStack<>(xValue, new NilStack<A>()).apply(fValue))));
    }

    private static <A, B, C> Instr<C> force(ForceCode<A> force, Function<Instr<A>, Instr<C>> k) {
        var thunk = force.thunk();
        return toCps(thunk, value -> k.apply(mkforce(value)));
    }

    private static <A> Instr<A> mkforce(Value<U<A>> value) {
        if (value instanceof ThunkValue<A> thunkValue) {
            return thunkValue.instr();
        }
        return new ForceInstr<>(value);
    }

    private static <A, B, C> Instr<C> lambda(LambdaCode<A, B> code, Function<Instr<Fn<A, B>>, Instr<C>> k) {
        var body = code.body();
        var binder = code.binder();
        //return toCps(body, bodyValue -> new CallStackInstr<B, C>(bodyValue, new EvalStack<A, C, C>(binder.toValue(), k.apply(binder.toValue()), new NilStack<C>()))));
        return toCps(body, bodyValue -> k.apply(new PopInstr<>(binder.toValue(), bodyValue)));
    }

    private static <C, A> Instr<C> global(GlobalLiteral<A> global, Function<Value<A>, Instr<C>> k) {
        return k.apply(new GlobalValue<>(global.type(), global.packageName(), global.name()));
    }

    private static <C, A> Instr<C> local(LocalLiteral<A> local, Function<Value<A>, Instr<C>> k) {
        return k.apply(new LocalValue<>(local.type(), local.name()));
    }

    private static <C, A> Instr<C> thunk(ThunkLiteral<A> thunk, Function<Value<U<A>>, Instr<C>> k) {
        var code = thunk.code();
        var t = code.type();
        return toCps(code, label -> k.apply(mkthunk(label)));
    }

    private static <A> Value<U<A>> mkthunk(Instr<A> label) {
        if (label instanceof ForceInstr<A> call) {
            return call.thunk();
        }
        return new ThunkValue<>(label);
    }
}
