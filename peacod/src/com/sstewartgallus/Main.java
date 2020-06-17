package com.sstewartgallus;


import com.sstewartgallus.plato.frontend.Entity;
import com.sstewartgallus.plato.frontend.Environment;
import com.sstewartgallus.plato.frontend.Frontend;
import com.sstewartgallus.plato.frontend.Node;
import com.sstewartgallus.plato.ir.systemf.*;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.java.IntF;
import com.sstewartgallus.plato.java.IntType;
import com.sstewartgallus.plato.runtime.F;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.FreeStack;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.runtime.ValueThrowable;
import com.sstewartgallus.runtime.ValueThrowables;

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

record TypeReference(String canonicalPackage, String canonicalName) {
}

public final class Main {
    static final Supplier<Object> TO_EXEC;
    private static final MyThrowable TEMPLATE = new MyThrowable();

    @PutEnv("int")
    private static final TypeDesc<?> INT_TYPE_TERM = TypeDesc.ofReference("core", "int");

    // fixme.. make globals variables...
    @PutEnv("+")
    private static final Term<?> ADD = new GlobalTerm<>(new Global<>(IntType.INTF_TYPE.thunk().toFn(IntType.INTF_TYPE.thunk().toFn(IntType.INTF_TYPE)),
            "core", "+"));

    @PutEnv("-")
    private static final Term<?> SUBTRACT = new GlobalTerm<>(new Global<>(IntType.INTF_TYPE.thunk().toFn(IntType.INTF_TYPE.thunk().toFn(IntType.INTF_TYPE)),
            "core", "-"));

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

                                if (value instanceof Term<?> term) {
                                    entity = new Entity.ReferenceTermEntity(name, term);
                                } else if (value instanceof TypeDesc<?> type) {
                                    entity = new Entity.ReferenceTypeEntity(name, type);
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
                                return new Entity.SpecialFormEntity(name, f);
                            })
            ).reduce(Environment.empty(), (env, entity) -> env.put(entity.name(), entity), Environment::union);

    static {
        var source = "+ 1 2";

        output("Source", source);

        Node.Array ast;
        try {
            ast = Frontend.reader(new StringReader(source));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        output("AST", ast);

        output("Environment", DEFAULT_ENV);

        var systemf = (Term<F<Integer>>) Frontend.toTerm(ast, DEFAULT_ENV);

        outputT("System F ", systemf, systemf.type());

        systemf = EtaReduction.etaReduction(systemf);
        outputT("Eta Reduction", systemf, systemf.type());
        systemf = InlineTerm.inline(systemf);
        outputT("Inline", systemf, systemf.type());

        var cbpv = TermToCbpv.toCbpv(systemf);

        outputT("Call By Push Value", "", cbpv.type());
        System.err.println("\t" + cbpv.toString().replace("\n", "\n\t"));

        cbpv = ExpandIntrinsics.expand(cbpv);
        outputT("Expand Intrinsics", "", cbpv.type());
        System.err.println("\t" + cbpv.toString().replace("\n", "\n\t"));

        cbpv = InlineCpbv.inline(cbpv);
        outputT("Inline Call By Push Value", "", cbpv.type());
        System.err.println("\t" + cbpv.toString().replace("\n", "\n\t"));

        var cps = CpbvToCps.cps(cbpv);

        outputT("Continuation Passing Style", "", "");
        System.err.println(cps);

        cps = LiftCps.inline(cps);
        outputT("Unwrap CPS", "", "");
        System.err.println(cps);

        cps = InlineCps.inline(cps);
        outputT("Inline CPS", "", "");
        System.err.println(cps);

        // fixme... do closure conversion and track the freeVariables round about this step...

        var decps = DeCps.deCps(cps);
        outputT("De CPS", "", "");
        System.err.println(decps);

        var action = CodeInterpreter.interpret(decps, lookup());

        var results = action.accept(new FreeStack<>());
        output("Results", results);

        var str = new StringWriter();
        var writer = new PrintWriter(str);
        // fixme... pass in a lookup ?
        var compiled = decps.compile(lookup(), writer);
        System.err.print("Jit Code");
        System.err.println(("\n" + str).replace("\n", "\n\t"));
        output("JIT", compiled);

        System.exit(0);
        TO_EXEC = () -> ValueThrowables.clone(TEMPLATE);// API.apply((Value<F<Integer, F<Integer, Integer>>>) main, 3, 3);
    }

    @PutEnv("λ")
    private static Term<?> lambda(List<Node> nodes, Environment environment) {
        var binder = ((Node.Array) nodes.get(1)).nodes();

        var binderName = ((Node.Atom) binder.get(0)).value();
        var binderType = binder.get(1);

        var rest = new Node.Array(nodes.subList(2, nodes.size()));

        var domain = Frontend.toType(binderType, environment);

        var v = new Variable<>(domain.thunk(), binderName);
        var theTerm = Frontend.toTerm(rest, environment.put(binderName, new Entity.ReferenceTermEntity(binderName, new LocalTerm(v))));
        return new LambdaTerm(v, theTerm);
    }

    //@PutEnv("<")
    //private static final Term<?> LESS_THAN = Type.INT.l(binder -> Type.INT.l(y -> Prims.lessThan(binder, y)));

    @PutEnv("∀")
    private static Term<?> forall(List<Node> nodes, Environment environment) {
        var binder = ((Node.Atom) nodes.get(1)).value();
        var rest = new Node.Array(nodes.subList(2, nodes.size()));

        throw null;
        // fixme....
/*        var entity = new Entity.TypeEntity(binder, NominalType.ofTag(variable));
        var newEnv = binder.put(binder, entity);

        var theTerm = Frontend.toTerm(rest, newEnv);
        return new TypeLambdaTerm<>(binder -> variable.substituteIn(theTerm, binder)); */
    }

    static void output(String stage, Object results) {
        outputT(stage, results, "-");
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
        U<IntF> apply(U<Fn<U<IntF>, Fn<U<IntF>, IntF>>> f, int x, int y);
    }

    static class MyThrowable extends ValueThrowable {
    }

}

final class GetValue extends RuntimeException {
    Object value;

    public GetValue(Object value) {
        this.value = value;
    }
}