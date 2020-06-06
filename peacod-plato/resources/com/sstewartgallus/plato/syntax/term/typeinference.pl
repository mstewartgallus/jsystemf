:- dynamic data/2.

hole_value(HoleName, HoleValue) :-
    solve(Holes),
    member(HoleName-HoleValue, Holes).

solve(Holes) :-
    data(Holes, Constraints),
    maplist(evaluate_constraint, Constraints).

evaluate_constraint(A = B) :-
    A = B.