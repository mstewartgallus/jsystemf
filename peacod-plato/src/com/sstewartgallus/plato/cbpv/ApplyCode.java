package com.sstewartgallus.plato.cbpv;

import com.sstewartgallus.plato.runtime.U;
import com.sstewartgallus.plato.runtime.internal.AsmUtils;
import com.sstewartgallus.plato.runtime.Fun;
import com.sstewartgallus.plato.syntax.type.Type;
import com.sstewartgallus.plato.syntax.type.TypeCheckException;
import org.objectweb.asm.Opcodes;

import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;

public record ApplyCode<A, B>(Code<Fun<A, B>>f, Literal<A>x) implements Code<B> {
    public ApplyCode {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public U<B> interpret(InterpreterEnvironment environment) {
        var fVal = f.interpret(environment).action();
        var xVal = x.interpret(environment);
        return fVal.apply(xVal);
    }

    @Override
    public void compile(CompilerEnvironment environment) {
        f.compile(environment);

        environment.method().visitInsn(Opcodes.ACONST_NULL);

        x.compile(environment);

        // fixme....
        var range = ((LambdaCode<A, B>) f).range();
        var t = range.erase();
        var methodTypeDesc = methodType(t, f.type().erase(), Void.class, x.type().erase()).describeConstable().get();

        var mt = MethodTypeDesc.ofDescriptor(methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class).descriptorString());
        var bsm = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, AsmUtils.CD_TermBootstraps, "invoke", mt);
        var indy = DynamicCallSiteDesc.of(bsm, "CALL", methodTypeDesc);

        var boot = AsmUtils.toHandle(indy.bootstrapMethod());
        environment.method().visitInvokeDynamicInsn(indy.invocationName(), indy.invocationType().descriptorString(), boot);
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        // fixme....
        var range = ((LambdaCode<A, B>) f).range();

        return range;
    }

    @Override
    public String toString() {
        return x + "\n" + f;
    }
}
