<p align="center">CAE Artifact</p>

---

<br>

# Clean Mapped Exceptions

A shared vocabulary for workflow failures, with separate public messages, diagnostic details, and original causes.

## Start here

Read the [concept guide](concept/README.md) to understand the problem, vocabulary, and intended behavior. Then choose a language project from the [implementation checklist](#implementations) below to explore its code and available usage instructions.

## Repository structure

| Path | Purpose |
| --- | --- |
| [`concept/`](concept/) | Language-independent explanation of the concept, starting with [`concept/README.md`](concept/README.md). |
| [`implementations/`](implementations/) | Language-specific project folders; see the checklist below for their current status. |
| [`.github/`](.github/) | Repository automation and GitHub configuration. |
| [`.gitignore`](.gitignore) | Files excluded from version control. |
| [`LICENSE`](LICENSE) | Apache License 2.0 terms for this repository. |

## CONCEPT and IMPLEMENTATIONS

**CONCEPT** explains what the component means and how it should behave, independently of a programming language or framework. It lives in [`concept/`](concept/).

**IMPLEMENTATIONS** translate that shared meaning into each language's native APIs and conventions. Source code, tests, build configuration, and language-specific usage documentation belong to the individual implementation project. This separation keeps the conceptual guide shared while allowing each implementation to evolve with its own tooling.

## Implementations

Checked items have implementation code in this repository. Unchecked items are reserved language folders containing only `.gitkeep`; they are placeholders, with no usable implementation yet. A check indicates code is present, not that a release has been published.

- [x] [Java](implementations/java/) — Implemented; Java 25 LTS and Maven 3.9+. See the [Java guide](implementations/java/README.md) for setup, usage, and build instructions.
- [ ] [C#](implementations/csharp/) — Placeholder; implementation not yet available.
- [ ] [Go](implementations/go/) — Placeholder; implementation not yet available.
- [ ] [Python](implementations/python/) — Placeholder; implementation not yet available.

This artifact succeeds CAE Mapped Exceptions. See the [Java migration guide](implementations/java/README.md#migrating-from-cae-mapped-exceptions).

## Repository automation

[Feature pull request automation](.github/FEATURE_PRS.md) opens a pull request to `main` when a `feature/*` branch receives commits. The guide describes the required repository permission and how existing pull requests are handled.

[Java publishing](.github/PUBLISHING.md) describes verification, signing, and Maven Central publication after release version changes reach `main`, including credential setup and dependency requirements.

Licensed under [Apache License 2.0](LICENSE).

<br>
<br>

---

<p align="center">clean software, made easy.</p>
