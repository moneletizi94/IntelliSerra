
% TODO: doc
% infer(+List_Measures, -List_Actions)
infer([], ACTIONS):- 
	findall(X, action(X), ACTIONS), 
	retractall(measure(_, _)).

infer([measure(X, Y)|T], ACTIONS):- 
	assert(measure(X, Y)),
	infer(T, ACTIONS).

% string operators
'>'(string(X), string(Y)):- compare(string(X), string(Y), DIFF), DIFF > 0.
'>='(string(X), string(Y)):- compare(string(X), string(Y), DIFF), DIFF >= 0.
'<'(string(X), string(Y)):- compare(string(X), string(Y), DIFF), DIFF < 0.
'=<'(string(X), string(Y)):- compare(string(X), string(Y), DIFF), DIFF =< 0.

% string compare
compare(string(X), string(Y), DIFF):- atom(X), atom(Y), atom_codes(X, C1), atom_codes(Y, C2), compare(C1, C2, DIFF).
compare(L, [], DIFF):- !, size(L, DIFF).
compare([], L, DIFF):- !, size(L, S), DIFF is -S.
compare([H|T1], [H|T2], DIFF):- !, compare(T1, T2, DIFF).
compare([H1|_], [H2|_], DIFF):- DIFF is H1 - H2.

size([], 0).
size([H|T], S):- size(T, SS), S is SS + 1.