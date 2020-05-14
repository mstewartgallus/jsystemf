package com.sstewartgallus;


import com.sstewartgallus.ast.Node;
import com.sstewartgallus.ir.Generic;
import com.sstewartgallus.pass1.*;
import com.sstewartgallus.plato.*;
import com.sstewartgallus.primitives.Prims;
import com.sstewartgallus.runtime.Value;
import com.sstewartgallus.runtime.ValueInvoker;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;

import static java.io.StreamTokenizer.TT_EOF;
import static java.io.StreamTokenizer.TT_WORD;
import static java.lang.invoke.MethodHandles.lookup;

public final class Main {
    public static final int INDENT = 26;
    public static final int INDENT_2 = 50;
    static final Supplier<Object> TO_EXEC;
    private static final TypeApply TP = ValueInvoker.newInstance(lookup(), TypeApply.class);
    private static final Apply AP = ValueInvoker.newInstance(lookup(), Apply.class);
    private static final ApplyInt API = ValueInvoker.newInstance(lookup(), ApplyInt.class);

    static {
        // fixme plan: Source File -> AST -> System F IR -> Category IR -> CPS? -> SSA? -> MethodHandle (or ConstantDesc?)
        // fixme... still need to introduce lazy values and product recursion..
        var source = "λ (x I) λ (y I) x";

        output("Source", source);

        Node.Array ast;
        try {
            ast = parse(new StringReader(source));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        output("AST", ast);

        var vars = new IdGen();

        var environment = new TreeMap<String, Term<?>>();
        environment.put("+", Type.INT.l(x -> Type.INT.l(y -> Prims.add(x, y))));
        environment.put("<", Type.INT.l(x -> Type.INT.l(y -> Prims.lessThan(x, y))));
        var term = toTerm(ast, vars, environment);

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

    // fixme.. arguments check more safely...
    static <A> Term<?> apply(Term<?> f, Term<A> x) throws TypeCheckException {
        var fType = f.type();
        var xType = x.type();

        if (!(fType instanceof FunctionNormal<?, ?> funType)) {
            throw new UnsupportedOperationException("applying nonfunction");
        }

        funType.domain().unify(xType);

        // fixme... do this more safely..
        return Term.apply((Term) f, x);
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

    private static Node.Array parse(Reader reader) throws IOException {
        var tokenizer = new StreamTokenizer(reader);
        tokenizer.resetSyntax();
        tokenizer.wordChars(0, Integer.MAX_VALUE);
        tokenizer.whitespaceChars(' ', ' ');
        tokenizer.ordinaryChar('(');
        tokenizer.ordinaryChar(')');

        List<Node> words = new ArrayList<>();
        var wordsStack = new ArrayList<List<Node>>();
        for (; ; ) {
            switch (tokenizer.nextToken()) {
                case TT_EOF -> {
                    if (!wordsStack.isEmpty()) {
                        throw new IllegalStateException("mid brace");
                    }
                    return Node.of(words);
                }
                case TT_WORD -> words.add(Node.of(tokenizer.sval));
                case '(' -> {
                    wordsStack.add(words);
                    words = new ArrayList<>();
                }
                case ')' -> {
                    var node = Node.of(words);
                    words = wordsStack.remove(0);
                    words.add(node);
                }
                default -> throw new IllegalStateException("other " + (char) tokenizer.ttype);
            }
        }
    }

    private static Type<?> toType(Node node) {
        // fixme.. denormal types are looking more and more plausible...
        if (node instanceof Node.Atom atom) {
            return lookupType(atom.value());
        }
        throw null;
    }

    private static Type<?> lookupType(String str) {
        return switch (str) {
            case "I" -> Type.INT;

            default -> {
                throw new IllegalStateException("Unexpected primHook: " + str);
            }
        };
    }

    private static Term<?> toTerm(Node.Array source, IdGen ids, Map<String, Term<?>> environment) {
        var nodes = source.nodes();
        var nodeZero = nodes.get(0);
        if (nodeZero instanceof Node.Atom atom && atom.value().equals("λ")) {
            var binder = ((Node.Array) nodes.get(1)).nodes();
            var binderName = ((Node.Atom) binder.get(0)).value();
            var binderType = binder.get(1);

            var rest = new Node.Array(nodes.subList(2, nodes.size()));

            return getTerm(binderName, ids, toType(binderType), rest, environment);
        }
        Optional<Term<?>> result = source.nodes().stream().map(node -> {
            if (node instanceof Node.Atom atom) {
                return lookupTerm(atom.value(), environment);
            }
            return toTerm(source, ids, environment);
        }).reduce((f, x) -> {
            var fType = f.type();
            var xType = x.type();

            if (!(fType instanceof FunctionNormal<?, ?> funType)) {
                throw new UnsupportedOperationException("applying nonfunction");
            }

            funType.domain().unify(xType);

            // fixme... do this more safely..
            return (Term<?>) Term.apply((Term) f, x);
        });
        if (result.isEmpty()) {
            throw new Error("todo handle nil " + source);
        }
        return result.get();
    }

    private static <A> Term<?> getTerm(String binder, IdGen ids, Type<A> binderType, Node.Array rest, Map<String, Term<?>> environment) {
        var id = ids.<A>createId();
        var variable = new VarValue<>(binderType, id);

        var newEnv = new TreeMap<>(environment);
        newEnv.put(binder, variable);

        var theTerm = toTerm(rest, ids, newEnv);
        return binderType.l(x -> theTerm.substitute(id, x));
    }

    private static Term<?> lookupTerm(String str, Map<String, Term<?>> environment) {
        isNumber:
        {
            BigInteger number;
            try {
                number = new BigInteger(str);
            } catch (NumberFormatException e) {
                break isNumber;
            }

            return Prims.of(number.intValueExact());
        }
        var term = environment.get(str);
        if (null == term) {
            throw new IllegalStateException("Not a term: " + str);
        }
        return term;
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