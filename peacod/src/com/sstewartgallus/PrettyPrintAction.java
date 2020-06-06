package com.sstewartgallus;

import com.sstewartgallus.plato.cbpv.*;
import com.sstewartgallus.plato.syntax.ext.pretty.PrettyTag;

import java.util.Objects;

public class PrettyPrintAction {
    static <A> String prettyPrint(Literal<A> term) {
        Objects.requireNonNull(term);

        if (term instanceof ThunkLiteral<?> thunk) {
            return "delay {\n" + prettyPrint(thunk.code()).replace("\n", "\n\t") + "\n}\n";
        }
        return term.toString();
    }

    static <A> String prettyPrint(Code<A> term) {
        Objects.requireNonNull(term);

        if (term instanceof ApplyCode<?, A> applyThunk) {
            return prettyPrintApply(applyThunk);
        }
        if (term instanceof LambdaCode<?, ?> lambda) {
            return prettyPrintLambda(lambda);
        }
        return term.toString();
    }

    private static <A> String prettyPrintApply(ApplyCode<?, A> apply) {
        var f = apply.f();
        var x = apply.x();
        return prettyPrint(x) + "\n" + prettyPrint(f);
    }

    private static <A, B> String prettyPrintLambda(LambdaCode<A, B> lambda) {
        return noBrackets(lambda);
    }

    private static <A, B> String noBrackets(LambdaCode<A, B> lambda) {
        var domain = lambda.domain();
        try (var pretty = PrettyTag.<A>generate()) {
            var body = lambda.apply(NominalLiteral.ofTag(pretty, domain));
            if (body instanceof LambdaCode<?, ?> lambdaTerm) {
                return "λ (" + pretty + " " + domain + ")\n" + noBrackets(lambdaTerm);
            }
            return "λ (" + pretty + " " + domain + ")\n" + prettyPrint(body);
        }
    }
}
