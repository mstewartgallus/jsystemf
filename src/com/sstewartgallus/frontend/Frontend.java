package com.sstewartgallus.frontend;

import com.sstewartgallus.ext.variables.Id;
import com.sstewartgallus.ext.variables.VarType;
import com.sstewartgallus.ext.variables.VarValue;
import com.sstewartgallus.plato.FunctionType;
import com.sstewartgallus.plato.Term;
import com.sstewartgallus.plato.Type;
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

    private static Type<?> toType(Node node, Environment env) {
        // fixme.. denormal types are looking more and more plausible...
        if (node instanceof Node.Atom atom) {
            return lookupType(atom.value(), env);
        }
        throw null;
    }

    private static Type<?> lookupType(String str, Environment environment) {
        var maybeEntity = environment.get(str);
        if (maybeEntity.isEmpty()) {
            throw new IllegalStateException("No binder found for: " + str);
        }
        var entity = maybeEntity.get();
        return ((Entity.TypeEntity) entity).type();
    }

    public static Term<?> toTerm(Node.Array source, Environment environment) {
        var nodes = source.nodes();
        var nodeZero = nodes.get(0);
        // fixme... put special forms in the environment as well...
        if (nodeZero instanceof Node.Atom atom) {
            switch (atom.value()) {
                case "λ" -> {
                    var binder = ((Node.Array) nodes.get(1)).nodes();
                    var binderName = ((Node.Atom) binder.get(0)).value();
                    var binderType = binder.get(1);

                    var rest = new Node.Array(nodes.subList(2, nodes.size()));

                    return getTerm(binderName, toType(binderType, environment), rest, environment);
                }
                case "∀" -> {
                    var binder = ((Node.Atom) nodes.get(1)).value();
                    var rest = new Node.Array(nodes.subList(2, nodes.size()));

                    var id = new Id<Object>();
                    var variable = new VarType<>(id);
                    var entity = new Entity.TypeEntity(variable);
                    var newEnv = environment.put(binder, entity);

                    var theTerm = toTerm(rest, newEnv);
                    return Term.v(x -> theTerm.substitute(id, x));
                }
            }
        }

        Optional<Term<?>> result = source.nodes().stream().map(node -> {
            if (node instanceof Node.Atom atom) {
                return lookupTerm(atom.value(), environment);
            }
            return toTerm(source, environment);
        }).reduce((f, x) -> {
            var fType = f.type();
            var xType = x.type();

            if (!(fType instanceof FunctionType<?, ?> funType)) {
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

    private static <A> Term<?> getTerm(String binder, Type<A> binderType, Node.Array rest, Environment environment) {
        var id = new Id<A>();
        var variable = new VarValue<>(binderType, id);
        var entity = new Entity.TermEntity(variable);
        var newEnv = environment.put(binder, entity);

        var theTerm = toTerm(rest, newEnv);
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
        if (!(entity instanceof Entity.TermEntity termEntity)) {
            throw new RuntimeException("Not a list " + entity);
        }
        return termEntity.term();
    }
}
