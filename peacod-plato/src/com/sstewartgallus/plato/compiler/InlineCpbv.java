package com.sstewartgallus.plato.compiler;

import com.sstewartgallus.plato.ir.cbpv.*;
import com.sstewartgallus.plato.runtime.Fn;

public class InlineCpbv {
    public static <A> Code<A> inline(Code<A> term) {
        return inline(new LiteralMap(), term);
    }

    private static <A> Code<A> inline(LiteralMap env, Code<A> term) {
        if (term instanceof LambdaCode<?, ?> lambdaCode) {
            return (Code) lambda(env, lambdaCode);
        }
        if (term instanceof ApplyCode<?, A> apply) {
            return apply(env, apply);
        }
        if (term instanceof LetToCode<?, A> letBeCode) {
            return letToCode(env, letBeCode);
        }
        if (term instanceof LetBeCode<?, A> letBeCode) {
            return letBeCode(env, letBeCode);
        }
        return term.visitChildren(new CodeVisitor() {
            @Override
            public <C> Code<C> onCode(Code<C> code) {
                return inline(env, code);
            }
        }, new LiteralVisitor() {
            @Override
            public <C> Literal<C> onLiteral(Literal<C> literal) {
                return inline(env, literal);
            }
        });
    }

    private static <A, B> Code<Fn<A, B>> lambda(LiteralMap env, LambdaCode<A, B> lambdaCode) {
        var binder = lambdaCode.binder();
        return new LambdaCode<>(binder, inline(env.clear(binder), lambdaCode.body()));
    }

    private static <A, B> Code<A> letBeCode(LiteralMap env, LetBeCode<B, A> letBeCode) {
        var binder = letBeCode.binder();
        var x = letBeCode.value();
        var body = letBeCode.body();
        var n = body.contains(binder);

        if (n <= 1) {
            return inline(env.put(binder, x), body);
        }
        return new LetBeCode<>(binder, inline(env, x), inline(env.clear(binder), body));
    }

    private static <A, B> Code<A> letToCode(LiteralMap env, LetToCode<B, A> letBeCode) {
        var binder = letBeCode.binder();
        return new LetToCode<>(binder, inline(env, letBeCode.action()), inline(env.clear(binder), letBeCode.body()));
    }

    private static <B, A> Code<A> apply(LiteralMap env, ApplyCode<B, A> term) {
        var f = inline(env, term.f());
        var x = inline(env, term.x());
        if (f instanceof LambdaCode<B, A> lambdaCode) {
            var binder = lambdaCode.binder();
            var body = lambdaCode.body();
            return letBeCode(env, new LetBeCode<>(binder, x, body));
        }
        return new ApplyCode<>(f, x);
    }

    private static <A, B> Literal<A> inline(LiteralMap env, Literal<A> term) {
        if (term instanceof LocalLiteral<A> localLit) {
            return inlineLocal(env, localLit);
        }
        return term.visitChildren(new CodeVisitor() {
            @Override
            public <C> Code<C> onCode(Code<C> code) {
                return inline(env, code);
            }
        }, new LiteralVisitor() {
            @Override
            public <C> Literal<C> onLiteral(Literal<C> literal) {
                return inline(env, literal);
            }
        });
    }

    private static <A> Literal<A> inlineLocal(LiteralMap env, LocalLiteral<A> localLit) {
        var replacement = env.get(localLit.variable());
        if (replacement == null) {
            return localLit;
        }
        return replacement;
    }
}

