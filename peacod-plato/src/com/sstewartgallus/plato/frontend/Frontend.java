package com.sstewartgallus.plato.frontend;

import com.sstewartgallus.plato.java.IntTerm;
import com.sstewartgallus.plato.java.IntType;
import com.sstewartgallus.plato.syntax.term.Term;
import com.sstewartgallus.plato.syntax.type.Type;
import com.sstewartgallus.plato.syntax.type.TypeApplyType;
import org.projog.api.Projog;
import org.projog.core.ProjogDefaultProperties;
import org.projog.core.term.Atom;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.ListFactory;
import org.projog.core.term.Structure;

import java.io.IOException;
import java.io.InputStreamReader;
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
            throw new IllegalStateException("No id found for: " + str);
        }
        var entity = maybeEntity.get();
        return ((Entity.TypeEntity) entity).type();
    }

    public static org.projog.core.term.Term toPrologTerm(Node source, Environment environment) {
        if (source instanceof Node.Atom atom) {
            return lookupPrologTerm(atom.value(), environment);
        }
        var nodes = ((Node.Array) source).nodes();
        var nodeZero = nodes.get(0);

        if (nodeZero instanceof Node.Atom atom) {
            var entity = environment.get(atom.value());
            if (entity.isPresent() && entity.get() instanceof Entity.SpecialFormEntity special) {
                return special.prolog().apply(nodes, environment);
            }
        }

        Optional<org.projog.core.term.Term> result = nodes.stream().map(node -> {
            if (node instanceof Node.Atom atom) {
                return lookupPrologTerm(atom.value(), environment);
            }
            return toPrologTerm(node, environment);
        }).reduce((f, x) -> Structure.createStructure("$", new org.projog.core.term.Term[]{f, x}));
        if (result.isEmpty()) {
            throw new Error("todo handle nil " + source);
        }
        return result.get();
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
            // fixme... do this more safely..
            return (Term<?>) Term.apply((Term) f, x);
        });
        if (result.isEmpty()) {
            throw new Error("todo handle nil " + source);
        }
        return result.get();
    }

    private static org.projog.core.term.Term lookupPrologTerm(String str, Environment environment) {
        isNumber:
        {
            BigInteger number;
            try {
                number = new BigInteger(str);
            } catch (NumberFormatException e) {
                break isNumber;
            }

            return new IntegerNumber(number.intValueExact());
        }

        var maybeEntity = environment.get(str);
        if (maybeEntity.isEmpty()) {
            throw new IllegalStateException("No id found for: " + str);
        }
        var entity = maybeEntity.get();
        if (!(entity instanceof Entity.PrologTermEntity termEntity)) {
            throw new RuntimeException("Not a term " + entity);
        }
        return termEntity.term();
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
        if (!(entity instanceof Entity.TermEntity termEntity)) {
            throw new RuntimeException("Not a list " + entity);
        }
        return termEntity.term();
    }

    public static org.projog.core.term.Term typecheck(org.projog.core.term.Term term) {
          var prolog = new Projog(new ProjogDefaultProperties() {
            @Override
            public boolean isRuntimeCompilationEnabled() {
                return false;
            }
            @Override
              public boolean isSpyPointsEnabled() {
                return true;
            }
          });

        try (var source = Frontend.class.getResourceAsStream("typeinference.pl")) {
            prolog.consultReader(new InputStreamReader(source));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var query = prolog.query("typecheck(Expr, Result).");
        var result = query.getResult();

        // fixme... pass in an environemnt..
        result.setTerm("Expr", term);
        var solutions = new ArrayList<org.projog.core.term.Term>();
        while (result.next()) {
            solutions.add(result.getTerm("Result"));
        }
        if (solutions.isEmpty()) {
            throw new Error("no solutions for term " + term);
        }
        if (solutions.size() > 1) {
            throw new Error("ambiguous solution for term " + term + " " + solutions);
        }

        return solutions.get(0);
    }

    public static org.projog.core.term.Term cbpv(org.projog.core.term.Term term) {
        var prolog = new Projog(new ProjogDefaultProperties() {
            @Override
            public boolean isRuntimeCompilationEnabled() {
                return false;
            }
        });

        try (var source = Frontend.class.getResourceAsStream("cbpv.pl")) {
            prolog.consultReader(new InputStreamReader(source));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var query = prolog.query("cbpv(Expr, Result).");
        var result = query.getResult();
        result.setTerm("Expr", term);
        var solutions = new ArrayList<org.projog.core.term.Term>();
        while (result.next()) {
            solutions.add(result.getTerm("Result"));
        }
        if (solutions.isEmpty()) {
            throw new Error("no solutions for term " + term);
        }
        if (solutions.size() > 1) {
            throw new Error("ambiguous solution for term " + term + " " + solutions);
        }

        return solutions.get(0);
    }

    public static Term<?> oftypeToTerm(org.projog.core.term.Term value) {
        if (!value.getType().isStructure()) {
            throw null;
        }
        if (!"-".equals(value.getName())) {
            throw null;
        }

        // fixme...
        var term = toTerm(value.getArgument(0));
        var type = toType(value.getArgument(1));

        return term;
    }

    private static Term<?> toTerm(org.projog.core.term.Term value) {
        if (value.getType().isStructure()) {
            if ("lambda".equals(value.getName())) {
                var domain = value.getArgument(0);
                var binder = value.getArgument(1);
                var body = value.getArgument(2);
                throw null;
            }

            throw null;
        }
        throw null;
    }

    private static Type<?> toType(org.projog.core.term.Term value) {
        if (value.getType().isStructure()) {
            if (!"$".equals(value.getName())) {
                throw null;
            }
            return new TypeApplyType(toType(value.getArgument(0)), toType(value.getArgument(1)));
        }
        return switch (value.getName()) {
            case "(->)" -> Type.function();
            case "F" -> Type.returnType();
            case "U" -> Type.thunkType();
            case "int" -> IntType.INT_TYPE;
            default -> throw null;
        };
    }
}
