package com.sstewartgallus.plato.compiler;

import com.sstewartgallus.plato.ir.cbpv.*;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.type.Stk;

public class SimplifyCpbvIdentities {
    public static <A> Code<A> simplify(Code<A> code) {
        if (code instanceof ForceCode<A> forceCode) {
            code = simplifyForce(forceCode);
        } else if (code instanceof LetToCode<?, A> letToCode) {
            code = simplifyLetTo(letToCode);
        }
        return code.visitChildren(SimplifyCpbvIdentities::simplify, SimplifyCpbvIdentities::simplify);
    }

    private static <A> Literal<A> simplify(Literal<A> literal) {
        if (literal instanceof ThunkLiteral thunkLiteral) {
            literal = simplifyThunk(thunkLiteral);
        }
        return literal.visitChildren(SimplifyCpbvIdentities::simplify, SimplifyCpbvIdentities::simplify);
    }

    /**
     * (return V) to X. N => V be X. N
     */
    private static <B, A> Code<A> simplifyLetTo(LetToCode<B, A> letToCode) {
        if (letToCode.action() instanceof ReturnCode returnCode) {
            return new LetBeCode<>(letToCode.binder(), returnCode.literal(), letToCode.body());
        }
        return letToCode;
    }

    /**
     * (force (thunk X)) => X
     */
    private static <A> Code<A> simplifyForce(ForceCode<A> forceCode) {
        if (forceCode.thunk() instanceof ThunkLiteral<A> thunkLiteral) {
            return thunkLiteral.code();
        }
        return forceCode;
    }

    /**
     * (thunk (force X)) => X
     */
    private static <A> Literal<Stk<F<Stk<A>>>> simplifyThunk(ThunkLiteral<A> thunkLiteral) {
        if (thunkLiteral.code() instanceof ForceCode<A> forceCode) {
            return forceCode.thunk();
        }
        return thunkLiteral;
    }
}
