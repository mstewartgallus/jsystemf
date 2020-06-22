package com.sstewartgallus.plato.compiler;

import com.sstewartgallus.plato.ir.Global;
import com.sstewartgallus.plato.ir.Variable;
import com.sstewartgallus.plato.ir.cbpv.*;
import com.sstewartgallus.plato.java.IntType;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.type.Stk;

public class ExpandCpbvIntrinsics {
    public static <A> Code<A> expand(Code<A> code) {
        return code.visitChildren(ExpandCpbvIntrinsics::expand, ExpandCpbvIntrinsics::expand);
    }

    public static <A> Literal<A> expand(Literal<A> literal) {
        if (literal instanceof GlobalLiteral<A> global) {
            return expandIntrinsic(global);
        }
        return literal.visitChildren(ExpandCpbvIntrinsics::expand, ExpandCpbvIntrinsics::expand);
    }

    private static <A> Literal<A> expandIntrinsic(GlobalLiteral<A> global) {
        var identity = global.global();
        return switch (identity.packageName()) {
            default -> global;
            case "core" -> switch (identity.name()) {
                default -> global;
                case "+" -> (Literal) intAddIntrinsic();
            };
        };
    }

    private static Literal<Stk<F<Stk<Fn<Stk<F<Stk<F<Integer>>>>, Fn<Stk<F<Stk<F<Integer>>>>, F<Integer>>>>>>> intAddIntrinsic() {
        var leftBinder = Variable.newInstance(IntType.INT_TYPE.returns().thunk());
        var rightBinder = Variable.newInstance(IntType.INT_TYPE.returns().thunk());
        var leftForced = Variable.newInstance(IntType.INT_TYPE);
        var rightForced = Variable.newInstance(IntType.INT_TYPE);
        var add = new GlobalLiteral<>(new Global<>(IntType.INT_TYPE.toFn(IntType.INT_TYPE.toFn(IntType.INT_TYPE.returns())).thunk(), "core", "+!"));
        var sum =
                new ApplyCode<>(new ApplyCode<>(new ForceCode<>(add), new LocalLiteral<>(leftForced)), new LocalLiteral<>(rightForced));
        return new ThunkLiteral<>(
                new LambdaCode<>(leftBinder,
                        new LambdaCode<>(rightBinder,
                                new LetToCode<>(leftForced, new ForceCode<>(new LocalLiteral<>(leftBinder)), new LetToCode<>(rightForced, new ForceCode<>(new LocalLiteral<>(rightBinder)), sum)))));
    }
}
