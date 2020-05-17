package com.sstewartgallus;


import com.sstewartgallus.ext.variables.VarType;
import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.frontend.Entity;
import com.sstewartgallus.frontend.Environment;
import com.sstewartgallus.frontend.Frontend;
import com.sstewartgallus.frontend.Node;
import com.sstewartgallus.optiimization.*;
import com.sstewartgallus.plato.*;
import com.sstewartgallus.primitives.Prims;
import com.sstewartgallus.runtime.Value;
import com.sstewartgallus.runtime.ValueInvoker;
import com.sstewartgallus.runtime.ValueThrowable;
import com.sstewartgallus.runtime.ValueThrowables;

import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@interface PutEnv {
    String value();
}


public final class Main {
    static final Supplier<Object> TO_EXEC;
    private static final MyThrowable TEMPLATE = new MyThrowable();
    private static final TypeApply TP = ValueInvoker.newInstance(lookup(), TypeApply.class);
    private static final Apply AP = ValueInvoker.newInstance(lookup(), Apply.class);
    private static final ApplyInt API = ValueInvoker.newInstance(lookup(), ApplyInt.class);
    @PutEnv("+")
    private static final Term<?> ADD = Type.INT.l(x -> Type.INT.l(y -> Prims.add(x, y)));
    @PutEnv("int")
    private static final Type<?> INT_TYPE = Type.INT;
    private static final Environment DEFAULT_ENV =
            Stream.concat(
                    Arrays.
                            stream(Main.class.getDeclaredFields())
                            .filter(f -> f.isAnnotationPresent(PutEnv.class))
                            .map(field -> {
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
                                    entity = new Entity.TermEntity(name, (Term<?>) value);
                                } else if (value instanceof Type) {
                                    entity = new Entity.TypeEntity(name, (Type<?>) value);
                                } else {
                                    throw new RuntimeException("error " + value);
                                }
                                return entity;
                            }),
                    Arrays.
                            stream(Main.class.getDeclaredMethods())
                            .filter(f -> f.isAnnotationPresent(PutEnv.class))
                            .map(method -> {
                                var defaultAnnotation = method.getAnnotation(PutEnv.class);
                                var name = defaultAnnotation.value();

                                MethodHandle methodHandle;
                                try {
                                    methodHandle = lookup().unreflect(method);
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }

                                var f = MethodHandleProxies.asInterfaceInstance(BiFunction.class, methodHandle);
                                return new Entity.SpecialFormEntity(name, f);
                            })
            ).reduce(Environment.empty(), (env, entity) -> env.put(entity.name(), entity), Environment::union);

    static {
        // fixme... still need to introduce lazy proper laziness, strictness analysis and tail recursion..
        var source = "(λ (x int) λ (y int) x) 4 2";

        output("Source", source);

        Node.Array ast;
        try {
            ast = Frontend.parse(new StringReader(source));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        output("AST", ast);

        output("Environment", DEFAULT_ENV);

        var term = Frontend.toTerm(ast, DEFAULT_ENV);

        outputT("System F", term);
        // fixme... implement properly before?...
/*        var captured = Capture.capture(curry);
        outputT("Partial Application", captured, captured.type());
*/
        var curryApply = CurryApply.curryApply(term);
        outputT("Curry Apply", curryApply);

        var uncurry = UncurryLambdas.uncurry(curryApply);
        outputT("Uncurry Lambda", uncurry);

        var elim = EliminateUncurryCurry.eliminate(uncurry);
        outputT("Rewrite uncurry ⚬ curry", elim);

        var pf = ConvertPointFree.pointFree(elim);
        outputT("Point Free", pf);

        // fixme... consdier pointfree generics...
/*
        var generic = pointFree.generic(new VarType<>());
        outputT("Generic", generic, generic.signature());
*/
        var jit = Jit.jit(pf);
        outputT("JIT", jit);

        /*
        // fixme.. hack
        var main = Generic.compile(lookup(), (Generic) generic);
        output("Main", main);

        var bar = API.apply((Value<F<Integer, F<Integer, Integer>>>) main, 3, 5);
        output("Result", bar);
*/
        System.exit(0);
        TO_EXEC = () -> ValueThrowables.clone(TEMPLATE);// API.apply((Value<F<Integer, F<Integer, Integer>>>) main, 3, 3);
    }

    @PutEnv("λ")
    private static Term<?> lambda(List<Node> nodes, Environment environment) {
        var binder = ((Node.Array) nodes.get(1)).nodes();

        var binderName = ((Node.Atom) binder.get(0)).value();
        var binderType = binder.get(1);

        var rest = new Node.Array(nodes.subList(2, nodes.size()));

        var type = Frontend.toType(binderType, environment);

        var v = new VarValue<>(type);
        environment = environment.put(binderName, new Entity.TermEntity(binderName, v));

        var theTerm = Frontend.toTerm(rest, environment);

        return getTerm(v, theTerm);
    }

    private static <A, B> Term<F<A, B>> getTerm(VarValue<A> v, Term<B> theTerm) {
        return v.type().l(x -> v.substituteIn(theTerm, x));
    }

    //@PutEnv("<")
    //private static final Term<?> LESS_THAN = Type.INT.l(x -> Type.INT.l(y -> Prims.lessThan(x, y)));

    @PutEnv("∀")
    private static Term<?> forall(List<Node> nodes, Environment environment) {
        var binder = ((Node.Atom) nodes.get(1)).value();
        var rest = new Node.Array(nodes.subList(2, nodes.size()));

        var variable = new VarType<>();
        var entity = new Entity.TypeEntity(binder, variable);
        var newEnv = environment.put(binder, entity);

        var theTerm = Frontend.toTerm(rest, newEnv);
        return Term.v(x -> variable.substituteIn(theTerm, x));
    }

    static void output(String stage, Object results) {
        outputT(stage, results, "-");
    }

    static void outputT(String stage, Term<?> term) {
        outputT(stage, term, term.type());
        var output = Interpreter.normalize(term);
        outputT(" ⇒", output, output.type());
    }

    static void outputT(String stage, Object term, Object type) {
        System.err.format("%-20s\t\t%-50s\t:\t%s%n", stage, term, type);
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

    static class MyThrowable extends ValueThrowable {
    }

}