package com.sstewartgallus;


import com.sstewartgallus.ast.Node;
import com.sstewartgallus.ir.Generic;
import com.sstewartgallus.pass1.Pass0;
import com.sstewartgallus.pass1.TPass0;
import com.sstewartgallus.plato.*;
import com.sstewartgallus.primitives.Prims;
import com.sstewartgallus.runtime.Value;
import com.sstewartgallus.runtime.ValueInvoker;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
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
        var source = "53";

        output("Source", source);

        Node.Array ast;
        try {
            ast = parse(new StringReader(source));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        output("AST", ast);

        var term = toTerm(ast);

        output("System F", term);

        record Penguin() {
            static <A> Term<V<A, F<A, F<A, A>>>> id() {
                return Term.v(t -> t.l(x -> t.l(y -> x)));
            }
        }

        try {
            var kValue = Term.apply(Term.apply(Term.apply(Penguin.id(), Type.INT), Prims.of(3)), Prims.of(5));
            outputT("System F", Penguin.id(), Penguin.id().type());

            outputT("System F", kValue, kValue.type());

            var norm = Interpreter.normalize(kValue);
            outputT("System F", norm, norm.type());
        } catch (TypeCheckException e) {
            throw new RuntimeException(e);
        }

        // hack to work around Java's lack of proper generics
        var expr = Term.apply(Type.INT.l(x -> Type.INT.l(y -> x)), Prims.of(4));
        try {
            outputT("System F", expr, expr.type());

            var interpreterOutput = Interpreter.normalize(expr);
            outputT("Interpreter Output", interpreterOutput, interpreterOutput.type());
        } catch (TypeCheckException e) {
            throw new RuntimeException(e);
        }

        var vars = new IdGen();

        var pass0 = Pass0.from(expr, vars);
        outputT("Pass 0", pass0, pass0.type());

        var pass1 = pass0.aggregateLambdas(vars);
        outputT("Aggregate Lambdas", pass1, pass1.type());

        var captures = pass1.captureEnv(vars).value();
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
                    return Node.of(words.toArray(Node[]::new));
                }
                case TT_WORD -> words.add(Node.of(tokenizer.sval));
                case '(' -> {
                    wordsStack.add(words);
                    words = new ArrayList<>();
                }
                case ')' -> {
                    var node = Node.of(words.toArray(Node[]::new));
                    words = wordsStack.remove(0);
                    words.add(node);
                }
                default -> throw new IllegalStateException("other " + (char) tokenizer.ttype);
            }
        }
    }

    private static Term<?> toTerm(Node.Array source) {
        // fixme.. doesn'arguments work for special forms..

        return source.parse(str -> switch (str) {
            case "+" -> Type.INT.l(x -> Type.INT.l(y -> Prims.add(x, y)));
            case "<" -> Type.INT.l(x -> Type.INT.l(y -> Prims.lessThan(x, y)));

            default -> {
                isNumber:
                {
                    BigInteger number;
                    try {
                        number = new BigInteger(str);
                    } catch (NumberFormatException e) {
                        break isNumber;
                    }

                    yield Prims.of(number.intValueExact());
                }
                throw new IllegalStateException("Unexpected primHook: " + str);
            }
        }, (Term<?>[] terms) -> {
            Term<?> head = terms[0];
            for (var ii = 1; ii < terms.length; ++ii) {
                try {
                    head = apply(head, terms[ii]);
                } catch (TypeCheckException e) {
                    throw new RuntimeException(e);
                }
            }
            return head;
        }, Term[]::new);
    }

    // FIXME... consider using varhandles for the locals access?
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