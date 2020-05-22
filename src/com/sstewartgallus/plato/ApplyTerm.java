package com.sstewartgallus.plato;

import com.sstewartgallus.ext.variables.VarValue;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.constant.*;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;

public record ApplyTerm<A, B>(Term<F<A, B>>f, Term<A>x) implements ThunkTerm<B>, Term<B> {
    public ApplyTerm {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public <X> State<X> step(Interpreter<B, X> interpreter) {
        var theX = x;
        return interpreter.eval(f, fValue -> {
            var fLambda = ((LambdaTerm<A, B>) fValue);
            return fLambda.apply(theX);
        });
    }

    @Override
    public Term<B> visitChildren(Visitor visitor) {
        return Term.apply(visitor.term(f), visitor.term(x));
    }

    @Override
    public void jit(ClassDesc thisClass, ClassVisitor classVisitor, MethodVisitor mw, Map<VarValue<?>, VarData> varDataMap) {

        // fixme.. unroll multiple applications.
        f.jit(thisClass, classVisitor, mw, varDataMap);

        mw.visitInsn(Opcodes.ACONST_NULL);

        x.jit(thisClass, classVisitor, mw, varDataMap);

        // fixme....
        var range = ((LambdaTerm<A, B>) f).range();
        var t = range.erase();
        var methodTypeDesc = methodType(t, this.f.type().erase(), Void.class, this.x.type().erase()).describeConstable().get();

        var mt = MethodTypeDesc.ofDescriptor(methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class).descriptorString());
        var bsm = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, AsmUtils.CD_TermBootstraps, "invoke", mt);
        var indy = DynamicCallSiteDesc.of(bsm, "CALL", methodTypeDesc);

        Handle boot = AsmUtils.toHandle(indy.bootstrapMethod());
        mw.visitInvokeDynamicInsn(indy.invocationName(), indy.invocationType().descriptorString(), boot);
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        // fixme....
        var range = ((LambdaTerm<A, B>) f).range();

        f.type().unify(x.type().to(range));

        return range;
    }

}
