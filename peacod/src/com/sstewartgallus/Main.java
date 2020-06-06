package com.sstewartgallus;


import com.sstewartgallus.plato.cbpv.Code;
import com.sstewartgallus.plato.cbpv.InterpreterEnvironment;
import com.sstewartgallus.plato.frontend.Entity;
import com.sstewartgallus.plato.frontend.Environment;
import com.sstewartgallus.plato.frontend.Frontend;
import com.sstewartgallus.plato.frontend.Node;
import com.sstewartgallus.plato.runtime.ActionInvoker;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fun;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.syntax.ext.variables.VarType;
import com.sstewartgallus.plato.syntax.term.Term;
import com.sstewartgallus.plato.syntax.type.NominalType;
import com.sstewartgallus.plato.syntax.type.Type;
import com.sstewartgallus.runtime.ValueThrowable;
import com.sstewartgallus.runtime.ValueThrowables;
import org.projog.core.term.Atom;
import org.projog.core.term.Structure;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
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
    private static final ApplyInt API = ActionInvoker.newInstance(lookup(), ApplyInt.class);
    private static final ApplyInt API2 = ActionInvoker.newInstance(lookup(), ApplyInt.class);

    @PutEnv("int")
    private static final org.projog.core.term.Term INT_TYPE_TERM = Structure.createStructure("/",
            new org.projog.core.term.Term[]{new Atom("builtin"), new Atom("int")});

    @PutEnv("+")
    private static final org.projog.core.term.Term ADD = Structure.createStructure("/",
            new org.projog.core.term.Term[]{new Atom("builtin"), new Atom("+")});

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
                                } else if (value instanceof org.projog.core.term.Term) {
                                    entity = new Entity.PrologTermEntity(name, (org.projog.core.term.Term) value);
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

                                // fixme... lol..
                                var f = MethodHandleProxies.asInterfaceInstance(BiFunction.class, methodHandle);
                                return new Entity.SpecialFormEntity(name, f, f);
                            })
            ).reduce(Environment.empty(), (env, entity) -> env.put(entity.name(), entity), Environment::union);

    static {
        var source = "+";

        output("Source", source);

        Node.Array ast;
        try {
            ast = Frontend.reader(new StringReader(source));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        output("AST", ast);

        output("Environment", DEFAULT_ENV);

        var prologterm = Frontend.toPrologTerm(ast, DEFAULT_ENV);
        output("Prolog Term", prologterm);

        var typedterm = Frontend.typecheck(prologterm);
        output("Typed Term", typedterm);

        var procbpv = Frontend.cbpv(prologterm);
        output("CBPV", procbpv);

        var myast = Frontend.oftypeToTerm(typedterm);
        output("Typed Term", myast);

        var term = (Term<Fun<U<F<Integer>>, Fun<U<F<Integer>>, F<Integer>>>>) Frontend.toTerm(ast, DEFAULT_ENV);

        outputT("System F", term);

        var constraints = term.findConstraints();
        output("Constraints", constraints);

        var solution = constraints.solve();
        output("Solution", solution);

        term = term.resolve(solution);
        outputT("Resolved", term);

        var cbpv = term.compile(new com.sstewartgallus.plato.syntax.term.Environment());

        output("Call By Push Value Constraints", cbpv.findConstraints());

        outputT("Call By Push Value", cbpv, cbpv.type());

        var interpreted = cbpv.interpret(new InterpreterEnvironment());
        output("Results", API.apply(interpreted, 4, 3));

        var str = new StringWriter();
        var writer = new PrintWriter(str);
        var compiled = Code.compile(cbpv, writer);
        System.err.println(str.toString());
//        output("JIT", compiled);

        output("Results", API2.apply(compiled, 4, 3));

        System.exit(0);
        TO_EXEC = () -> ValueThrowables.clone(TEMPLATE);// API.apply((Value<F<Integer, F<Integer, Integer>>>) main, 3, 3);
    }


    @PutEnv("λ")
    private static org.projog.core.term.Term lambdaProlog(List<Node> nodes, Environment environment) {
        var binder = ((Node.Array) nodes.get(1)).nodes();

        var binderName = ((Node.Atom) binder.get(0)).value();
        var binderType = binder.get(1);

        var rest = new Node.Array(nodes.subList(2, nodes.size()));

        var domain = Frontend.toPrologTerm(binderType, environment);

        var v = new Atom(binderName);
        var theTerm = Frontend.toPrologTerm(rest, environment.put(binderName, new Entity.PrologTermEntity(binderName,
                Structure.createStructure("/", new org.projog.core.term.Term[]{new Atom("local"), v}))));
        return Structure.createStructure("λ", new org.projog.core.term.Term[]{
                domain,
                v,
                theTerm
        });
    }

    //@PutEnv("<")
    //private static final Term<?> LESS_THAN = Type.INT.l(x -> Type.INT.l(y -> Prims.lessThan(x, y)));

    @PutEnv("∀")
    private static Term<?> forall(List<Node> nodes, Environment environment) {
        var binder = ((Node.Atom) nodes.get(1)).value();
        var rest = new Node.Array(nodes.subList(2, nodes.size()));

        var variable = new VarType<>();
        var entity = new Entity.TypeEntity(binder, NominalType.ofTag(variable));
        var newEnv = environment.put(binder, entity);

        var theTerm = Frontend.toTerm(rest, newEnv);
        return Term.v(x -> variable.substituteIn(theTerm, x));
    }

    static void output(String stage, Object results) {
        outputT(stage, results, "-");
    }

    static void outputT(String stage, Term<Fun<U<F<Integer>>, Fun<U<F<Integer>>, F<Integer>>>> term) {
        outputT(stage, PrettyPrint.prettyPrint(term), PrettyPrint.prettyPrint(term.type()));
        //  var output = API.apply(term, 2, 5);
        //     outputT(" ⇒", output, "-");
    }

    static <A> void outputT(String stage, Code<A> term) {
        outputT(stage, PrettyPrintAction.prettyPrint(term), PrettyPrint.prettyPrint(term.type()));
        outputT(" ⇒", term.interpret(new InterpreterEnvironment()), "-");
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

    @FunctionalInterface
    public interface ApplyInt {
        int apply(U<Fun<U<F<Integer>>, Fun<U<F<Integer>>, F<Integer>>>> f, int x, int y);
    }

    static class MyThrowable extends ValueThrowable {
    }

}