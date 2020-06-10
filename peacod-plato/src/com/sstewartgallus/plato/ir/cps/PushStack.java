package com.sstewartgallus.plato.ir.cps;

import com.sstewartgallus.plato.ir.type.RealType;
import com.sstewartgallus.plato.ir.type.TypeDesc;
import com.sstewartgallus.plato.runtime.ActionDesc;
import com.sstewartgallus.plato.runtime.Fn;
import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.runtime.internal.AsmUtils;
import org.objectweb.asm.Opcodes;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Arrays;

public record PushStack<A, B, C>(Value<A>value, Stack<B, C>k) implements Stack<Fn<A, B>, C> {
    @Override
    public String toString() {
        return value + " :: " + k;
    }

    @Override
    public U<C> interpret(CpsEnvironment environment, U<Fn<A, B>> f) {
        var val = value.interpret(environment);
        var result = U.apply(f, val);
        return k.interpret(environment, result);
    }

    @Override
    public void compile(CompilerEnvironment environment) {
        var arguments = new ArrayList<Value<?>>();
        arguments.add(value);
        Stack<?, C> next = k;
        while (next instanceof PushStack push) {
            arguments.add(push.value);
            next = push.k;
        }

        next.compile(environment);

        environment.method().visitInsn(Opcodes.DUP);

        var argTypes = arguments
                .stream()
                .map(a -> ((RealType<?>) environment.resolve(a.type())).erase().describeConstable().get())
                .toArray(ClassDesc[]::new);
        // fixme... get the right return type erasure...
        var theType = MethodTypeDesc.of(U.class.describeConstable().get(), argTypes);

        {
            var indy = ActionDesc.getApplyLabel(theType);

            var boot = AsmUtils.toHandle(indy.bootstrapMethod());
            environment.method().visitInvokeDynamicInsn(indy.invocationName(), indy.invocationType().descriptorString(), boot,
                    Arrays.stream(indy.bootstrapArgs()).map(AsmUtils::toAsm).toArray(Object[]::new));
        }

        environment.method().visitInsn(Opcodes.SWAP);

        for (var arg : arguments) {
            arg.compile(environment);
        }
        var indy = ActionDesc.callApplyLabel(theType);

        var boot = AsmUtils.toHandle(indy.bootstrapMethod());
        environment.method().visitInvokeDynamicInsn(indy.invocationName(), indy.invocationType().descriptorString(), boot,
                Arrays.stream(indy.bootstrapArgs()).map(AsmUtils::toAsm).toArray(Object[]::new));

    }

    @Override
    public TypeDesc<C> range() {
        return k.range();
    }
}
