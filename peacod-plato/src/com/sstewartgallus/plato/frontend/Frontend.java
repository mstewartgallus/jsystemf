package com.sstewartgallus.plato.frontend;

import com.sstewartgallus.plato.ir.systemf.ApplyTerm;
import com.sstewartgallus.plato.ir.systemf.Term;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.java.IntTerm;

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
    public static Node.Array reader(Reader reader) throws IOException {
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

    public static Term<?> toTerm(Node source, Environment environment) {
        if (source instanceof Node.Atom atom) {
            return lookupTerm(atom.value(), environment);
        }
        var nodes = ((Node.Array) source).nodes();
        var nodeZero = nodes.get(0);

        if (nodeZero instanceof Node.Atom atom) {
            var entity = environment.get(atom.value());
            if (entity.isPresent() && entity.get() instanceof Entity.SpecialFormEntity special) {
                return special.f().apply(nodes, environment);
            }
        }

        Optional<Term<?>> result = nodes.stream().map(node -> {
            if (node instanceof Node.Atom atom) {
                return lookupTerm(atom.value(), environment);
            }
            return toTerm(node, environment);
            // fixme...
        }).reduce((f, x) -> {
            return new ApplyTerm(f, x);
        });
        if (result.isEmpty()) {
            throw new Error("todo handle nil " + source);
        }
        return result.get();
    }

    public static TypeDesc<?> toType(Node source, Environment environment) {
        if (source instanceof Node.Atom atom) {
            return lookupType(atom.value(), environment);
        }
        var nodes = ((Node.Array) source).nodes();
        var nodeZero = nodes.get(0);

        /*
        if (nodeZero instanceof Node.Atom atom) {
            var entity = environment.get(atom.value());
            if (entity.isPresent() && entity.get() instanceof Entity.SpecialFormEntity special) {
                return special.f().apply(nodes, environment);
            }
        } */

        Optional<TypeDesc<?>> result = nodes.stream().map(node -> {
            if (node instanceof Node.Atom atom) {
                return lookupType(atom.value(), environment);
            }
            return toType(node, environment);
            // fixme...
        }).reduce((f, x) -> TypeDesc.ofApply((TypeDesc) f, x));
        if (result.isEmpty()) {
            throw new Error("todo handle nil " + source);
        }
        return result.get();
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

            return new IntTerm(number.intValueExact());
        }

        var maybeEntity = environment.get(str);
        if (maybeEntity.isEmpty()) {
            throw new IllegalStateException("No id found for: " + str);
        }
        var entity = maybeEntity.get();
        if (!(entity instanceof Entity.ReferenceTermEntity termEntity)) {
            throw new RuntimeException("Not a term " + entity);
        }
        return termEntity.term();
    }

    private static TypeDesc<?> lookupType(String str, Environment environment) {
        var maybeEntity = environment.get(str);
        if (maybeEntity.isEmpty()) {
            throw new IllegalStateException("No id found for: " + str);
        }
        var entity = maybeEntity.get();
        if (!(entity instanceof Entity.ReferenceTypeEntity termEntity)) {
            throw new RuntimeException("Not a term " + entity);
        }
        return termEntity.type();
    }
}
