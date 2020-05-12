package com.sstewartgallus.pass1;

import com.sstewartgallus.plato.*;

import java.util.Objects;

// fixme... slow ?
public record CurriedApplyValue<A, B>(Body<A, B>f, Term<A>x) implements ThunkTerm<B> {
    public CurriedApplyValue {
        Objects.requireNonNull(f);
        Objects.requireNonNull(x);
    }

    @Override
    public Type<B> type() throws TypeCheckException {
        return ((FunctionNormal<A, B>) f.type()).range();
    }

    @Override
    public Term<B> stepThunk() {
        return f.apply(x);
    }

    public interface Body<A, B> {
        Type<F<A, B>> type() throws TypeCheckException;

        Term<B> apply(Term<A> x);
    }

    public static record ApplyBody<A, B, C>(Body<A, F<B, C>>f, Term<A>x) implements Body<B, C> {
        public ApplyBody {
            Objects.requireNonNull(f);
            Objects.requireNonNull(x);
        }

        @Override
        public Type<F<B, C>> type() throws TypeCheckException {
            return toTerm().type();
        }

        private Term<F<B, C>> toTerm() {
            return new CurriedApplyValue<>(f, x);
        }

        @Override
        public Term<C> apply(Term<B> y) {
            return Term.apply(f.apply(x), y);
        }
    }

    public record MonoBody<A, B>(Term<F<A, B>>body) implements Body<A, B> {
        @Override
        public Type<F<A, B>> type() throws TypeCheckException {
            return body.type();
        }

        @Override
        public Term<B> apply(Term<A> x) {
            return ((LambdaValue<A, B>) Interpreter.normalize(body)).apply(x);
        }
    }
}
