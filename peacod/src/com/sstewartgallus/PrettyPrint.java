package com.sstewartgallus;

import com.sstewartgallus.plato.runtime.Fun;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.runtime.V;
import com.sstewartgallus.plato.syntax.ext.pretty.PrettyTag;
import com.sstewartgallus.plato.syntax.ext.pretty.PrettyType;
import com.sstewartgallus.plato.syntax.term.*;
import com.sstewartgallus.plato.syntax.type.ForallType;
import com.sstewartgallus.plato.syntax.type.NominalType;
import com.sstewartgallus.plato.syntax.type.Type;
import com.sstewartgallus.plato.syntax.type.TypeApplyType;

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
            var p = NominalType.ofTag(pretty);
            var body = lambda.f().apply(p);
            return "(∀" + pretty + " → " + prettyPrint(body) + ")";
        }
    }

    static <A> String prettyPrint(Term<A> term) {
        if (term instanceof ApplyTerm<?, A> applyThunk) {
            return prettyPrintApply(applyThunk);
        }
        if (term instanceof TypeApplyTerm<?, A> applyThunk) {
            return prettyPrintTypeApply(applyThunk);
        }

        if (term instanceof LambdaTerm<?, ?> lambda) {
            return prettyPrintLambda(lambda);
        }
        if (term instanceof TypeLambdaTerm<?, ?> lambda) {
            return prettyPrintTypeLambda(lambda);
        }
        return term.toString();
    }

    private static <A, B> String prettyPrintTypeLambda(TypeLambdaTerm<A, B> lambda) {
        try (var pretty = PrettyType.<A>generate()) {
            var p = NominalType.ofTag(pretty);
            var body = lambda.apply(p);
            return "(∀" + pretty + " → " + body + ")";
        }
    }

    private static <A> String prettyPrintTypeApply(TypeApplyTerm<?, A> applyThunk) {
        return "(" + noBrackets(applyThunk) + ")";
    }

    private static <A, B> String noBrackets(TypeApplyTerm<A, B> applyThunk) {
        var f = applyThunk.f();
        var x = applyThunk.x();
        if (f instanceof TypeApplyTerm<?, V<A, B>> fApply) {
            return noBrackets(fApply) + " " + prettyPrint(x);
        }
        return prettyPrint(f) + " " + prettyPrint(x);
    }


    private static <A> String prettyPrintApply(ApplyTerm<?, A> applyThunk) {
        return "(" + noBrackets(applyThunk) + ")";
    }

    private static <A, B> String noBrackets(ApplyTerm<A, B> apply) {
        var f = apply.f();
        var x = apply.x();
        if (f instanceof ApplyTerm<?, Fun<U<A>, B>> fApply) {
            return noBrackets(fApply) + " " + prettyPrint(x);
        }
        return prettyPrint(f) + " " + prettyPrint(x);
    }

    private static <A, B> String prettyPrintLambda(LambdaTerm<A, B> lambda) {
        return "(" + noBrackets(lambda) + ")";
    }

    private static <A, B> String noBrackets(LambdaTerm<A, B> lambda) {
        var domain = lambda.domain();
        try (var pretty = PrettyTag.<A>generate()) {
            var body = lambda.apply(NominalTerm.ofTag(pretty, domain));
            if (body instanceof LambdaTerm<?, ?> lambdaTerm) {
                return "λ (" + pretty + " " + domain + ") " + noBrackets(lambdaTerm);
            }
            return "λ (" + pretty + " " + domain + ") " + prettyPrint(body);
        }
    }
}
