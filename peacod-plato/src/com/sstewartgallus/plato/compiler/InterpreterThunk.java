package com.sstewartgallus.plato.compiler;

import com.sstewartgallus.plato.ir.cps.Action;
import com.sstewartgallus.plato.runtime.Continuation;
import com.sstewartgallus.plato.runtime.type.Stk;
import com.sstewartgallus.plato.runtime.type.U;

final class InterpreterThunk<A> extends U<A> {
    private final Interpreter.Environment env;
    private final Action<A> body;

    InterpreterThunk(Interpreter.Environment env, Action<A> body) {
        this.env = env;
        this.body = body;
    }


    @Override
    public String toString() {
        return "(thunk " + body + " " + env + ")";
    }

    @Override
    public <C> void enter(Continuation<C> context, Stk<A> action) {
        throw null;
    }
}
