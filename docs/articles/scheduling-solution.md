# Solving the defense scheduling problem

## Model the actual problem

Each defense is a variable. Its domain is a finite collection of eligible room/start-time candidates. The committee is supplied in advance: exactly three distinct professors, including the advisor, plus at least one student. The solver chooses a time and room; it does not choose examiners or optimize fairness.

This is a constraint satisfaction problem. Berkeley's [backtracking explanation](https://inst.eecs.berkeley.edu/~cs188/textbook/csp/solving.html) provides background for assigning one variable at a time and undoing choices when a conflict prevents completion. The sections below specify this implementation, including what its search budget does and does not guarantee.

## Input to domain boundary

`TsvScheduleSource` parses three UTF-8 files with exact headers. Parse failures include the file and row where available. Dates/times are parsed once; `Planning` constructors enforce composition and temporal invariants. The source then provides a request with copied collections.

Input boundaries are 1 MiB per file, at most 200 defenses and 5,000 candidate slots. These are implementation limits for candidate storage/recursive depth, not mathematical properties of timetabling. Every referenced professor/student needs explicit availability. Duplicate members are rejected before converting input to sets, so a repeated professor cannot silently disappear.

A candidate `[09:00, 11:00)` means “start at 09:00 in this room, with space until 11:00.” It does not mean the algorithm may invent a 09:15 start. Supply another row for that start. For a 45-minute defense, the resulting reservation is `[09:00, 09:45)`.

## Unary eligibility

For each defense and supplied candidate, the solver first checks that the candidate has enough duration. It trims the reservation to the defense duration, then checks that **every** role-qualified participant has at least one availability window containing the entire reservation, on the same date. Duplicate resulting room/date/start/end reservations are removed.

Availability rows are alternatives. Two adjacent availability windows do not automatically merge: `[09:00, 09:30)` plus `[09:30, 10:00)` does not admit a 60-minute defense unless a containing window is also supplied. Professor and student IDs occupy separate namespaces through `Person.role`.

## Binary conflict rule

Let `A` and `B` be assignments. Their times overlap exactly when their dates match, `A.start < B.end`, and `B.start < A.end`. If times overlap, a conflict exists when the room matches or the assignments share a qualified professor/student.

Intervals are half-open. A defense ending at 10:00 can be followed immediately by one starting at 10:00. The implementation does not reserve breaks/travel time; include that in your candidate/availability choices if needed. Two defenses in different rooms may run simultaneously only when all participants are disjoint.

## Search procedure

1. Build the eligible candidate list for each defense.
2. Sort defenses once by eligible-candidate count, ascending. Ties keep input order.
3. Visit the next defense; try candidates in their supplied order.
4. Reject candidates conflicting with any selected assignment.
5. Add a compatible assignment and recurse. If the remainder is infeasible, remove it and try the next candidate.
6. Return the first complete schedule, exhaustion or the node-limit/interruption status. Return no partial assignments for an unsuccessful search.

The initial ordering is inspired by the “most constrained first” idea. It is **not dynamic MRV**: the solver does not recompute each remaining defense's domain after every assignment. It also does not implement LCV, forward checking or arc consistency. See Berkeley's [ordering chapter](https://inst.eecs.berkeley.edu/~cs188/textbook/csp/ordering.html) for those distinctions.

## Walk through the checked-in example

In `examples/feasible`, D01 (“Water, safely”) needs 60 minutes, D02 (“Accessible library”) needs 45 and D03 (“Community energy”) needs 60. There are two rooms and candidates at 09:00 and 10:00.

D01 is placed in Room A at 09:00. D02 cannot share that room at 09:00, so it uses Room B; its professors/students are disjoint from D01. D03 shares professors with both earlier defenses and must wait until 10:00. It then uses Room A. The result contains three assignments and four visited search nodes: the empty prefix and three successfully extended prefixes.

The `examples/infeasible` dataset offers only one room/start candidate. At most one of the required overlapping reservations can occupy it, so no complete schedule exists in that candidate set.

`SchedulerTest.backtracksWhenFirstCandidatePreventsACompleteSchedule` exercises a less trivial case: a 60-minute defense has two starts, while a 30-minute defense with a shared professor must occur earlier. The first choice blocks the latter. Search undoes that choice, moves the longer defense later and completes both. This is tested search, rather than a greedy placement described as an optimizer.

## Result semantics and guarantees

- `FEASIBLE`: all supplied defenses are assigned and supported constraints hold. Empty input produces an empty feasible plan with an explanatory presentation message.
- `INFEASIBLE`: search exhausted the eligible candidate combinations. This proves no complete schedule exists **within the supplied candidates and implemented constraints**.
- `LIMIT_REACHED`: a recursive entry would exceed the configured node budget, or its thread is interrupted. Feasibility remains unknown. CLI/GUI ship with 100,000 visited nodes.

The budget counts entered search states, including the root and a completed prefix. It is not a wall-clock timeout. Candidate preprocessing and scans of conflicting candidates are outside that count. Interruption is checked on recursive entry, not during every parsing/preprocessing step. Do not advertise a fixed response-time guarantee.

Completeness follows from enumerating every eligible candidate for each defense and backtracking after each failed compatible prefix, provided neither budget nor interruption terminates the search. The solver returns the first feasible result and has no objective function; global optimality is not claimed.

## Cost and limitations

With `n` defenses, up to `s` supplied slots, `k` participants per defense and up to `w` availability windows per person, eligibility preprocessing is approximately `O(n s k w)`. Search is exponential in the worst case. With at most `b` eligible candidates per defense, an illustrative upper bound is `O(b^n n k)` because candidate conflict checks inspect the selected prefix and participant sets. Candidate storage is `O(n b)`, with an `O(n)` recursive path, apart from input and temporary collections. These are structural estimates, not benchmark measurements.

The solver does not support expertise-based examiner selection, soft preferences, fairness objectives, travel time, implicit existing reservations, timezone conversion or overnight defenses. Existing reservations must be removed from availability/candidates. A date/room migration is required before writing results into the legacy JPA representation.

## Verify the full path

```sh
./mvnw clean verify
java -Djava.awt.headless=true -jar target/CommitteeTimetable-2.0.0-app.jar --input examples/feasible
java -Djava.awt.headless=true -jar target/CommitteeTimetable-2.0.0-app.jar --input examples/infeasible
```

The second command exits with code 2. Use-case tests run with in-memory ports; filesystem/CLI tests exercise actual synthetic TSV/CSV files. Domain tests cover the three resource conflicts, availability/duration boundaries, backtracking, input limits and search exhaustion. Architecture checks and formatting run in CI. PostgreSQL tests are separately enabled as described in the [README](../../README.md#postgresql-integration).
