package com.sstewartgallus;

import com.sstewartgallus.plato.ir.cbpv.*;
import com.sstewartgallus.plato.ir.systemf.Global;
import com.sstewartgallus.plato.ir.systemf.Variable;
import com.sstewartgallus.plato.java.IntType;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.U;

public class ExpandIntrinsics {

    public static <A> Code<A> expand(Code<A> code) {
        if (code instanceof ApplyCode<?, A> apply) {
            return apply(apply);
        }
        if (code instanceof LambdaCode lambda) {
            return (Code) lambda(lambda);
        }
        if (code instanceof LetBeCode<?, A> letBeCode) {
            return letBeCode(letBeCode);
        }
        if (code instanceof ForceCode<A> forceCode) {
            return force(forceCode);
        }
        if (code instanceof ReturnCode forceCode) {
            return returnCode(forceCode);
        }
        if (code instanceof GlobalCode<A> global) {
            return expandIntrinsic(global);
        }
        throw new IllegalArgumentException(code.getClass().toString());
    }

    private static <A> Code<F<A>> returnCode(ReturnCode<A> retCode) {
        return new ReturnCode<>(expand(retCode.literal()));
    }

    private static <A> Code<A> force(ForceCode<A> forceCode) {
        return ForceCode.of(expand(forceCode.thunk()));
    }

    public static <A> Literal<A> expand(Literal<A> literal) {
        if (literal instanceof ThunkLiteral thunkLiteral) {
            return thunk(thunkLiteral);
        }
        return literal;
    }

    private static <A> Code<A> expandIntrinsic(GlobalCode<A> global) {
        var identity = global.global();
        return switch (identity.packageName()) {
            default -> global;
            case "core" -> switch (identity.name()) {
                default -> global;
                case "+" -> (Code) intAddIntrinsic();
                case "-" -> (Code) intSubIntrinsic();
            };
        };
    }

    private static Code<Fn<U<F<Integer>>, Fn<U<F<Integer>>, F<Integer>>>> intSubIntrinsic() {
        throw null;
    }

    private static Code<Fn<U<F<Integer>>, Fn<U<F<Integer>>, F<Integer>>>> intAddIntrinsic() {
        var leftBinder = Variable.newInstance(IntType.INT_TYPE.returns().thunk());
        var rightBinder = Variable.newInstance(IntType.INT_TYPE.returns().thunk());
        var leftForced = Variable.newInstance(IntType.INT_TYPE);
        var rightForced = Variable.newInstance(IntType.INT_TYPE);
        var add = new GlobalCode<>(new Global<>(IntType.INT_TYPE.toFn(IntType.INT_TYPE.toFn(IntType.INT_TYPE.returns())), "core", "+!"));
        var sum =
                new ApplyCode<>(new ApplyCode<>(add, new LocalLiteral<>(leftForced)), new LocalLiteral<>(rightForced));
        return
                new LambdaCode<>(leftBinder,
                        new LambdaCode<>(rightBinder,
                                LetToCode.of(leftForced, ForceCode.of(new LocalLiteral<>(leftBinder)),
                                        LetToCode.of(rightForced, ForceCode.of(new LocalLiteral<>(rightBinder)), sum))));
    }

    private static <A> Literal<U<A>> thunk(ThunkLiteral<A> thunk) {
        return ThunkLiteral.of(expand(thunk.code()));
    }

    private static <A, B> Code<A> letBeCode(LetBeCode<B, A> letBeCode) {
        var value = letBeCode.value();
        var binder = letBeCode.binder();
        var body = letBeCode.body();
        return new LetBeCode<>(binder, expand(value), expand(body));
    }


    private static <A, B> Code<B> apply(ApplyCode<A, B> apply) {
        var f = apply.f();
        var x = apply.x();
        return new ApplyCode<>(expand(f), expand(x));
    }

    private static <A, B> Code<Fn<A, B>> lambda(LambdaCode<A, B> lambda) {
        var binder = lambda.binder();
        var body = lambda.body();

        return new LambdaCode<>(binder, expand(body));
    }
}
