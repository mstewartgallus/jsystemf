package com.sstewartgallus;


import com.sstewartgallus.frontend.Entity;
import com.sstewartgallus.frontend.Environment;
import com.sstewartgallus.frontend.Frontend;
import com.sstewartgallus.frontend.Node;
import com.sstewartgallus.ir.Generic;
import com.sstewartgallus.pass1.*;
import com.sstewartgallus.plato.*;
import com.sstewartgallus.primitives.Prims;
import com.sstewartgallus.runtime.Value;
import com.sstewartgallus.runtime.ValueInvoker;

import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.function.Supplier;

import static java.lang.invoke.MethodHandles.lookup;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface PutEnv {
    String value();
}

public final class Main {
    public static final int INDENT = 26;
    public static final int INDENT_2 = 50;
    static final Supplier<Object> TO_EXEC;
    private static final TypeApply TP = ValueInvoker.newInstance(lookup(), TypeApply.class);
    private static final Apply AP = ValueInvoker.newInstance(lookup(), Apply.class);
    private static final ApplyInt API = ValueInvoker.newInstance(lookup(), ApplyInt.class);

    @PutEnv("+")
    private static final Term<?> ADD = Type.INT.l(x -> Type.INT.l(y -> Prims.add(x, y)));
    @PutEnv("I")
    private static final Type<?> INT_TYPE = Type.INT;

    //@PutEnv("<")
    //private static final Term<?> LESS_THAN = Type.INT.l(x -> Type.INT.l(y -> Prims.lessThan(x, y)));

    private static final Environment DEFAULT_ENV =
            Arrays.
                    stream(Main.class.getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(PutEnv.class))
                    .reduce(Environment.empty(), (env, field) -> {
                        var defaultAnnotation = field.getAnnotation(PutEnv.class);
                        var name = defaultAnnotation.value();

                        Object value;
                        try {
                            value = field.get(null);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }

                        Entity entity;
                        if (value instanceof Term) {
                            entity = new Entity.TermEntity((Term<?>) value);
                        } else if (value instanceof Type) {
                            entity = new Entity.TypeEntity((Type<?>) value);
                        } else {
                            throw new RuntimeException("error " + value);
                        }
                        return env.put(name, entity);
                    }, Environment::union);

    static {
        // fixme plan: Source File -> AST -> System F IR -> Category IR -> CPS? -> SSA? -> MethodHandle (or ConstantDesc?)
        // fixme... still need to introduce lazy values and product recursion..
        var source = "λ (x I) λ (y I) x";

        output("Source", source);

        Node.Array ast;
        try {
            ast = Frontend.parse(new StringReader(source));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        output("AST", ast);

        var vars = new IdGen();

        output("Environment", DEFAULT_ENV);

        var term = Frontend.toTerm(ast, vars, DEFAULT_ENV);

        output("System F", term);

        record Penguin() {
            static <A> Term<V<A, F<A, F<A, A>>>> id() {
                return Term.v(t -> t.l(x -> t.l(y -> x)));
            }
        }

        try {
            var kValue = Term.apply(Term.apply(Term.apply(Penguin.id(), Type.INT), Prims.of(3)), Prims.of(5));
            outputT("System F", Penguin.id(), Penguin.id().type());

            var norm = Interpreter.normalize(kValue);
            outputT("System F", norm, norm.type());
        } catch (TypeCheckException e) {
            throw new RuntimeException(e);
        }

        var expr = Term.apply(Type.INT.l(x -> Type.INT.l(y -> x)), Prims.of(4));
        try {
            outputT("System F", expr, expr.type());

            var interpreterOutput = Interpreter.normalize(expr);
            outputT("Interpreter Output", interpreterOutput, interpreterOutput.type());

            var curry = Curry.curry(expr, vars);
            outputT("Curried", curry, curry.type());

            var captured = Capture.capture(curry, vars);
            outputT("Partial Application", captured, captured.type());

            var applyCurried = CurryApply.curryApply(curry, vars);
            outputT("Apply Curried", applyCurried, applyCurried.type());

            var tuple = Tuple.uncurry(applyCurried, vars);
            outputT("Tuple", tuple, tuple.type());

            var uncurry = Uncurry.uncurry(tuple, vars);
            outputT("Uncurry", uncurry, uncurry.type());

            var captures = Pass2.from(captured, vars);
            outputT("Explicit Environment", captures, captures.type());

            var uncurried = captures.uncurry(vars);
            outputT("Uncurried", uncurried, uncurried.type());

            var pointFree = uncurried.pointFree(vars.createId(), vars, TPass0.NilType.NIL);
            outputT("Point Free", pointFree, pointFree.type());

            var generic = pointFree.<Void>generic(vars.createId(), vars);
            outputT("Generic", generic, generic.signature());

            var main = Generic.compile(lookup(), generic);
            output("Main", main);

            var bar = API.apply(main, 3);
            output("Result", bar);

            TO_EXEC = () -> API.apply(main, 3);
        } catch (TypeCheckException e) {
            throw new RuntimeException(e);
        }
    }

    static void output(String stage, Object results) {
        outputT(stage, results, "-");
    }

    static void outputT(String stage, Object results, Object type) {
        var resultsStr = results.toString();
        var x = 1 + (INDENT - stage.length());
        var y = (INDENT_2 - resultsStr.length());
        if (x < 0) {
            x = INDENT;
        }
        if (y < 0) {
            y = INDENT_2;
        }
        System.err.println(stage + " ".repeat(x) + "\t" + resultsStr + " ".repeat(y) + ":" + "\t" + type);
    }

    public static void main(String... args) {
        var results = new Object[245];
        for (long ii = 0; ii < Long.MAX_VALUE; ++ii) {
            results[(int) (ii % results.length)] = TO_EXEC.get();
        }
        System.out.println(results[0]);
    }

    // fixme... pass in type as well?
    @FunctionalInterface
    public interface TypeApply {
        <A, B> Value<B> apply(Value<V<A, B>> f, Class<A> x);
    }

    @FunctionalInterface
    public interface Apply {
        <A, B> Value<B> apply(Value<F<A, B>> f, int x);
    }

    @FunctionalInterface
    public interface ApplyInt {
        int apply(Value<F<Integer, Integer>> f, int y);
    }

}