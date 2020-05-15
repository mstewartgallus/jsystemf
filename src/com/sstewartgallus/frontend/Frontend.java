package com.sstewartgallus.frontend;

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
        loop:
        for (; ; ) {
            Node newNode;
            switch (tokenizer.nextToken()) {
                case TT_EOF -> {
                    break loop;
                }
                case TT_WORD -> {
                    newNode = Node.of(tokenizer.sval);
                }
                case '(' -> {
                    wordsStack.add(words);
                    words = new ArrayList<>();
                    continue;
                }
                case ')' -> {
                    newNode = Node.of(words);
                    words = wordsStack.remove(wordsStack.size() - 1);
                }
                default -> throw new IllegalStateException("other " + (char) tokenizer.ttype);
            }
            words.add(newNode);
        }

        if (!wordsStack.isEmpty()) {
            throw new IllegalStateException("mid brace");
        }
        return Node.of(words);
    }

    public static Type<?> toType(Node node, Environment env) {
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

        if (nodeZero instanceof Node.Atom atom) {
            var entity = environment.get(atom.value());
            if (entity.isPresent() && entity.get() instanceof Entity.SpecialFormEntity special) {
                return special.f().apply(nodes, environment);
            }
        }

        Optional<Term<?>> result = source.nodes().stream().map(node -> {
            if (node instanceof Node.Atom atom) {
                return lookupTerm(atom.value(), environment);
            }
            return toTerm((Node.Array) node, environment);
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

    public static <A> Term<?> getTerm(String binder, Type<A> binderType, Node.Array rest, Environment environment) {
        var variable = new VarValue<>(binderType);
        var entity = new Entity.TermEntity(binder, variable);
        var newEnv = environment.put(binder, entity);

        var theTerm = toTerm(rest, newEnv);
        return binderType.l(x -> variable.substituteIn(theTerm, x));
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
