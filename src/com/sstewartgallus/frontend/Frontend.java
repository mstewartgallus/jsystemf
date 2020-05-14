package com.sstewartgallus.frontend;

import com.sstewartgallus.plato.*;
import com.sstewartgallus.primitives.Prims;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.io.StreamTokenizer.TT_EOF;
import static java.io.StreamTokenizer.TT_WORD;

public class Frontend {
    public static Node.Array parse(Reader reader) throws IOException {
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

    public static Term<?> toTerm(Node.Array source, IdGen ids, Environment environment) {
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

    private static <A> Term<?> getTerm(String binder, IdGen ids, Type<A> binderType, Node.Array rest, Environment environment) {
        var id = ids.<A>createId();
        var variable = new VarValue<>(binderType, id);
        var entity = new Entity(variable);
        var newEnv = environment.put(binder, entity);

        var theTerm = toTerm(rest, ids, newEnv);
        return binderType.l(x -> theTerm.substitute(id, x));
    }

    private static Term<?> lookupTerm(String str, Environment environment) {
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

        var maybeEntity = environment.get(str);
        if (maybeEntity.isEmpty()) {
            throw new IllegalStateException("No binder found for: " + str);
        }
        var entity = maybeEntity.get();
        return entity.term();
    }
}