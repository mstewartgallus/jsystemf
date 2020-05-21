package com.sstewartgallus;

import com.sstewartgallus.ext.pretty.PrettyThunk;
import com.sstewartgallus.ext.pretty.PrettyType;
import com.sstewartgallus.plato.*;

public class PrettyPrint {
    static String prettyPrint(Type<?> type) {
        if (type instanceof ForallType<?, ?> lambda) {
            return prettyPrintForall(lambda);
        }
        if (type instanceof NominalType<?> nominal) {
            return nominal.tag().toString();
        }
        if (type instanceof TypeApplyType<?, ?> apply) {
            return prettyPrintTypeApply(apply);
        }
        return type.toString();
    }

    private static String prettyPrintTypeApply(TypeApplyType<?, ?> apply) {
        return "(" + prettyPrint(apply.f()) + " " + prettyPrint(apply.x()) + ")";
    }

    private static <A, B> String prettyPrintForall(ForallType<A, B> lambda) {
        try (var pretty = PrettyType.<A>generate()) {
            var body = lambda.f().apply(pretty);
            return "(∀" + pretty + " → " + prettyPrint(body) + ")";
        }
    }

    static <A> String prettyPrint(Term<A> term) {
        if (term instanceof LambdaValue<?, ?> lambda) {
            return prettyPrintLambda(lambda);
        }
        if (term instanceof ApplyThunk<?, A> applyThunk) {
            return prettyPrintApply(applyThunk);
        }
        if (term instanceof TypeLambdaValue<?, ?> lambda) {
            return prettyPrintTypeLambda(lambda);
        }
        if (term instanceof TypeApplyThunk<?, A> applyThunk) {
            return prettyPrintTypeApply(applyThunk);
        }
        return term.toString();
    }

    private static <A, B> String prettyPrintTypeLambda(TypeLambdaValue<A, B> lambda) {
        try (var pretty = PrettyType.<A>generate()) {
            var body = lambda.apply(pretty);
            return "(∀" + pretty + " → " + body + ")";
        }
    }

    private static <A> String prettyPrintTypeApply(TypeApplyThunk<?, A> applyThunk) {
        return "(" + noBrackets(applyThunk) + ")";
    }

    private static <A, B> String noBrackets(TypeApplyThunk<A, B> applyThunk) {
        var f = applyThunk.f();
        var x = applyThunk.x();
        if (f instanceof TypeApplyThunk<?, V<A, B>> fApply) {
            return noBrackets(fApply) + " " + prettyPrint(x);
        }
        return prettyPrint(f) + " " + prettyPrint(x);
    }


    private static <A> String prettyPrintApply(ApplyThunk<?, A> applyThunk) {
        return "(" + noBrackets(applyThunk) + ")";
    }

    private static <A, B> String noBrackets(ApplyThunk<A, B> apply) {
        var f = apply.f();
        var x = apply.x();
        if (f instanceof ApplyThunk<?, F<A, B>> fApply) {
            return noBrackets(fApply) + " " + prettyPrint(x);
        }
        return prettyPrint(f) + " " + prettyPrint(x);
    }

    private static <A, B> String prettyPrintLambda(LambdaValue<A, B> lambda) {
        return "(" + noBrackets(lambda) + ")";
    }

    private static <A, B> String noBrackets(LambdaValue<A, B> lambda) {
        var domain = lambda.domain();
        try (var pretty = PrettyThunk.generate(domain)) {
            var body = lambda.apply(pretty);
            if (body instanceof LambdaValue<?, ?> lambdaValue) {
                return "λ (" + pretty + " " + domain + ") " + noBrackets(lambdaValue);
            }
            return "λ (" + pretty + " " + domain + ") " + prettyPrint(body);
        }
    }
}
