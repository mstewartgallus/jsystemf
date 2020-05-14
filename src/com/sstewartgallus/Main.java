package com.sstewartgallus;


import com.sstewartgallus.ext.tuples.HList;
import com.sstewartgallus.ext.tuples.NilNormal;
import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.ext.variables.IdGen;
import com.sstewartgallus.frontend.Entity;
import com.sstewartgallus.frontend.Environment;
import com.sstewartgallus.frontend.Frontend;
import com.sstewartgallus.frontend.Node;
import com.sstewartgallus.ir.Generic;
import com.sstewartgallus.optiimization.*;
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

        var interpreterOutput = Interpreter.normalize(term);
        outputT("Interpreter Output", interpreterOutput, interpreterOutput.type());

        var curry = Curry.curry(term);
        outputT("Curried", curry, curry.type());

        var captured = Capture.capture(curry, vars);
        outputT("Partial Application", captured, captured.type());

        var applyCurried = CurryApply.curryApply(curry);
        outputT("Apply Curried", applyCurried, applyCurried.type());

        var tuple = Tuple.uncurry(applyCurried);
        outputT("Tuple", tuple, tuple.type());

        var uncurry = Uncurry.uncurry(tuple);
        outputT("Uncurry", uncurry, uncurry.type());

        var pointFree = ConvertPointFree.pointFree(uncurry, NilNormal.NIL, new Id<HList.Nil>(), vars);
        outputT("Point Free", pointFree, pointFree.type());

        var generic = pointFree.generic(new Id<Object>());
        outputT("Generic", generic, generic.signature());

        // fixme.. hack
        var main = Generic.compile(lookup(), (Generic) generic);
        output("Main", main);

        var bar = API.apply((Value<F<Integer, F<Integer, Integer>>>) main, 3, 5);
        output("Result", bar);

        TO_EXEC = () -> API.apply((Value<F<Integer, F<Integer, Integer>>>) main, 3, 3);
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
        int apply(Value<F<Integer, F<Integer, Integer>>> f, int x, int y);
    }

}