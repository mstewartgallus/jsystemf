package com.sstewartgallus;


import com.sstewartgallus.ast.Node;
import com.sstewartgallus.ir.Category;
import com.sstewartgallus.ir.Generic;
import com.sstewartgallus.ir.VarGen;
import com.sstewartgallus.runtime.Value;
import com.sstewartgallus.runtime.ValueInvoker;
import com.sstewartgallus.term.Prims;
import com.sstewartgallus.term.Term;
import com.sstewartgallus.type.F;
import com.sstewartgallus.type.Type;
import com.sstewartgallus.type.V;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static java.io.StreamTokenizer.TT_EOF;
import static java.io.StreamTokenizer.TT_WORD;
import static java.lang.invoke.MethodHandles.lookup;

public final class Main {
    static final Supplier<Object> TO_EXEC;

    // fixme.. argument check more safely...
    static <A> Term<?> apply(Term<?> f, Term<A> x) {
        if (f.type() instanceof Type.FunType<?, ?> funType) {
            if (!Objects.equals(funType.domain(), x.type())) {
                throw new UnsupportedOperationException("type error");
            }
            // fixme... do this more safely..
            return Term.apply((Term) f, x);
        }
        throw new UnsupportedOperationException("applying nonfunction");
    }

    static void output(String stage, Object results) {
        System.err.print(stage + ("\t" + results).indent(16 - stage.length()));
    }

    private static final TypeApply TP = ValueInvoker.newInstance(lookup(), TypeApply.class);
    private static final Apply AP = ValueInvoker.newInstance(lookup(), Apply.class);
    private static final ApplyInt API = ValueInvoker.newInstance(lookup(), ApplyInt.class);

    static {
        // fixme plan: Source File -> AST -> System F IR -> Category IR -> CPS? -> SSA? -> MethodHandle (or ConstantDesc?)
        // fixme... still need to introduce lazy values and tail recursion..
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

        // hack to work around Java's lack of proper generics
        var expr = Term.apply(Type.INT.l(x -> Type.INT.l(y -> x)), Prims.of(4));

        output("System F", expr + ": " + expr.type());

        var vars = new VarGen();

        var pass1 = expr.aggregateLambdas(vars);
        output("Aggregate", pass1);

        var captures = pass1.captureEnv(vars).value();
        output("Capture Env", captures);

        var ccc = captures.ccc(vars.createArgument(Type.nil()), vars);
        output("Ccc", ccc + ": " + ccc.domain() + " -> " + ccc.range());

        var generic = Category.generic(ccc);
        output("Generic", generic + ": " + generic.signature());

        var main = Generic.compile(lookup(), generic);
        output("Main", main);

        var bar = API.apply(main, 3);
        output("Result", bar);

        TO_EXEC = () -> API.apply(main, 3);
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
        <A, B> int apply(Value<F<A, Integer>> f, int x);
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
        // fixme.. doesn'argument work for special forms..

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
                head = apply(head, terms[ii]);
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

}