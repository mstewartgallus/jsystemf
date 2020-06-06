package com.sstewartgallus.plato.cbpv;

import com.sstewartgallus.plato.runtime.ActionDesc;
import com.sstewartgallus.plato.runtime.Fun;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.runtime.internal.AsmUtils;
import com.sstewartgallus.plato.syntax.ext.pretty.PrettyTag;
import com.sstewartgallus.plato.syntax.type.Type;
import com.sstewartgallus.plato.syntax.type.TypeCheckException;

import java.util.ArrayList;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;


public abstract class LambdaCode<A, B> implements Code<Fun<A, B>> {
    private final Type<A> domain;
    private final Type<B> range;

    public LambdaCode(Type<A> domain, Type<B> range) {
        Objects.requireNonNull(domain);
        this.domain = domain;
        this.range = range;
    }

    public final Type<A> domain() {
        return domain;
    }

    public abstract Code<B> apply(Literal<A> x);

    @Override
    public Fun<A, B> interpret(InterpreterEnvironment environment) {
        var v = new VarLiteral<>(domain);
        var body = apply(v);
        return value -> body.interpret(environment.put(v, value));
    }

    @Override
    public void compile(CompilerEnvironment environment) {
        var type = domain.to(range).describeConstable().get();

        LambdaCode<?, ?> current = this;

        var args = new ArrayList<Class<?>>();

        Class<?> range;
        Code<?> body;
        var argumentVariables = new ArrayList<VarLiteral<?>>();
        for (; ; ) {
            var v = new VarLiteral(current.domain());
            argumentVariables.add(v);
            body = current.apply(v);
            args.add(current.domain().erase());
            range = body.type().erase();

            if (body instanceof LambdaCode<?, ?> lambda) {
                current = lambda;
                continue;
            }
            break;
        }

        var finalBody = body;

        var methodType = methodType(range, args);
        var methodTypeDesc = methodType.describeConstable().get();

        var mh = environment.newMethod(methodTypeDesc, argumentVariables, finalBody::compile);

        var desc = ActionDesc.ofMethod(type, mh);

        environment.method().visitLdcInsn(AsmUtils.toAsm(desc));
    }

    @Override
    public final Type<Fun<A, B>> type() throws TypeCheckException {
        return domain.to(range);
    }

    public final Type<B> range() {
        return range;
    }

    @Override
    public final String toString() {
        // fixme... use pop call by push value syntax...
        try (var pretty = PrettyTag.<A>generate()) {
            var body = apply(NominalLiteral.ofTag(pretty, domain));
            return "(λ (" + pretty + " " + domain + ") " + body + ")";
        }
    }
}
