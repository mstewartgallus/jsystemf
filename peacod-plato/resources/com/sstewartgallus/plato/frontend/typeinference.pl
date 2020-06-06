%% Fixme.. set the default environment outside ..
typecheck(Expr, Result) :-
    infer(Expr, Result, [
         builtin/inc - '$'('$'(builtin/fn, builtin/int), builtin/int),
         builtin/'+' - '$'('$'(builtin/fn, builtin/int), '$'('$'(builtin/fn, builtin/int),builtin/int))
    ]).

infer(Expr, Result, Env) :-
    create_holes(Expr, Result),
    check_types(Result, Env).

%% Definition of our little implementation of System F
systemf(Package/Name, Env) :- lookup(Package/Name - _, Env).
systemf('$'(F, X), Env) :-
    systemf(F, Env),
    systemf(X, Env).
systemf(λ(Domain, Binder, Body), Env) :-
    atomic(Binder),
    systemf_type(Domain),
    insert(local/Binder - Domain, Env, NewEnv),
    systemf(Body, NewEnv).

systemf_type(Package/Name) :- atomic(Package), atomic(Name).
systemf_type('$'(F, X)) :- systemf_type(F), systemf_type(X).

create_holes(Package/Name, Package/Name - _).
create_holes('$'(F, X), '$'(G, Y) - _) :- create_holes(F, G), create_holes(X, Y).
create_holes(λ(Domain, Binder, Body),
      λ(Domain, Binder, NewBody) - _) :-
      create_holes(Body, NewBody).

% fixme... just rename to istyped or something ...
check_types(Package/Name - Type, Env) :-
    lookup(Package/Name - Type, Env).
check_types('$'(F - Type, X - Domain) - Range, Env) :-
    Type = '$'('$'(builtin/fn, Domain), Range),
    check_types(F - Type, Env),
    check_types(X - Domain, Env).
check_types(λ(Domain, Binder, Body - Range) - Type, Env) :-
    Type = '$'('$'(builtin/fn, Domain), Range),
    insert(local/Binder - Domain, Env, NewEnv),
    check_types(Body - Range, NewEnv).

% An environment is just a list of key - value pairs...
insert(Key - Value, Env, [Key - Value | Env]).

lookup(Key - Value, [Key - Value | _]).
lookup(Key - Value, [NotKey - _ | Tail]) :-
    Key \= NotKey,
    lookup(Key - Value, Tail).