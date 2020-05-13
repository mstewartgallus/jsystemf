package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;

public record CurriedApplyThunk<A>(Body<A>body) implements ThunkTerm<A> {
    public CurriedApplyThunk {
        Objects.requireNonNull(body);
    }

    @Override
    public Type<A> type() throws TypeCheckException {
        return body.type();
    }

    @Override
    public Term<A> stepThunk() {
        return body.stepThunk();
    }

    @Override
    public String toString() {
        return "(" + body + ")";
    }

    public interface Body<A> {
        Type<A> type() throws TypeCheckException;

        Term<A> stepThunk();
    }

    public static record ApplyBody<A, B>(Body<F<A, B>>f, Term<A>x) implements Body<B> {
        public ApplyBody {
            Objects.requireNonNull(f);
            Objects.requireNonNull(x);
        }

        @Override
        public Type<B> type() throws TypeCheckException {
            return ((FunctionNormal<A, B>) f.type()).range();
        }

        @Override
        public Term<B> stepThunk() {
            return Term.apply(f.stepThunk(), x);
        }

        @Override
        public String toString() {
            return f + " " + x;
        }
    }

    public record MonoBody<A>(Term<A>body) implements Body<A> {
        public MonoBody {
            Objects.requireNonNull(body);
        }

        @Override
        public Type<A> type() throws TypeCheckException {
            return body.type();
        }

        @Override
        public Term<A> stepThunk() {
            return body;
        }

        @Override
        public String toString() {
            return body.toString();
        }
    }
}
