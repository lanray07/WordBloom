# Puzzle engine

The pack contains 24 authored letter bags and 72 target-answer slots, with some vocabulary recurring as difficulty grows. It does not claim 72 unique words. Six puzzles are authored in each band: beginner targets 3–4 letters, intermediate 4–6, advanced 5–8, expert 7–9 in this pack. Each world contains four successive levels.

The inventory is a deliberately small, authored English allowlist rather than a comprehensive dictionary. Only listed targets and bonus words are accepted. `Words.suitable` and the sensitive-word deny set supplement that allowlist. No corpus-derived frequency ranking or human linguistic certification is claimed.

The generator sorts answers by length, places an initial word, and searches perpendicular matching-letter crossings. Candidates must cross an existing answer, agree at occupied cells, avoid parallel overlap and unintended adjacency, and keep cells before/after the answer empty. It favours compact layouts and uses seeded shuffling for ties. A puzzle is returned only if every target is placed, otherwise bounded retries fail explicitly.

Letter bags are multisets. Repeated characters need distinct tile instances. Target, bonus, repeat and invalid results are distinct. The gesture observer preserves the original down; the pure engine interpolates sampled segments so fast swipes retain crossed tiles in order. Backtracking removes the last tile. Hints use actual placed answers. Replays preserve historical completion and do not grant repeat rewards. Invalid feedback preserves letter order; only explicit shuffle requests change it.

JUnit validates every target bag, all layouts, crossing consistency and connectivity, determinism over 16 seeds per authored bag, repeated letters, NFC, impossible inputs, verdicts and daily identity. Automated structural correctness does not establish vocabulary familiarity or enjoyable difficulty.

The 300-level target is deliberately pending. Expand with reviewed original seeds and rerun validation; do not pad the pack with cosmetically shifted copies to reach a number.
