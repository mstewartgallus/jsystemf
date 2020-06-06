cbpv(load(Binder), return(load(Binder))).
cbpv('$'(F, X), '$'(G, thunk(Y))) :- cbpv(F, G), cbpv(X, Y).
cbpv(λ(Domain, Binder, Body),
     λ(NewDomain, Binder, NewBody)) :-
     cbpv(Body, NewBody),
     cbpv_type(Domain, NewDomain).

%% Fixme.. do a proper transformation...
%% Fixme... how to handle partial application ?
cbpv_type(T, T).