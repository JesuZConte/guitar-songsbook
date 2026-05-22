# ADR-023: License choice — GPL v3

## Status
Accepted

## Context

Before the public Play Store launch (v1.4.1), the project license was re-evaluated. The original GPL v3 choice had never been formally documented. The alternatives considered were:

- **MIT** — permissive, repo can be private, widely used
- **All rights reserved (proprietary)** — maximum legal protection, private repo
- **GPL v3** — copyleft, repo must be public, derivatives must also be open source

The evaluation was triggered by two concerns:
1. Whether the public repo represented a security risk
2. Whether the license was appropriate for a commercial project with IAP plans (v2)

A dependency audit confirmed that no project dependency is GPL-licensed. All libraries (Jetpack, Room, Compose, Kotlin, Gson, Firebase SDK, AdMob) use Apache 2.0 or proprietary SDK terms, both of which are compatible with any license choice. There is no external obligation to use GPL.

## Decision

**Keep GPL v3 with a public repository.**

### Reasoning

**1. Copyleft keeps the chain open**
GPL v3's defining property is that derivatives must also be distributed under GPL with their source code published. This means if someone forks Cancionero, their fork must also be open and auditable. MIT would break this chain — it allows the next person to close the source, making their version unauditable by the community.

**2. Auditable forks are safer for users**
A closed-source fork (possible under MIT) could introduce malicious modifications — credential harvesting, cryptocurrency theft, ad fraud — with no way for users or the community to detect it. GPL forces any distributed fork into the open, where such modifications would be visible.

**3. Philosophical alignment**
The project maintainer has a background in open source culture (ThoughtWorks). The principle that software which is free today should remain free tomorrow — and not be locked down by whoever copies it next — is a deliberate value choice, not just a legal strategy.

**4. The public repo is not a security risk**
The security review conducted on the same date (ADR-022) confirmed that no sensitive credentials are exposed in the repository. The Firebase API key is restricted by certificate fingerprint. The keystore is outside version control. A public repo with GPL and clean credential hygiene presents no meaningful security risk.

**5. Protection comes from execution, not secrecy**
The competitive advantage of Cancionero is not in the code being secret — it is in the product being built first, the UX being refined, and the roadmap being executed. GPL does not weaken this position. A competitor who forks the code still has to build the product, acquire users, and maintain it.

## Alternatives rejected

**MIT:** Rejected because it allows closed-source derivatives. A fork could introduce malicious code with no obligation to publish it, and could build a commercial closed product on top of the open work without contributing back.

**All rights reserved (proprietary):** Rejected on ethical grounds. The project benefits from open source libraries (Apache 2.0) and the broader open source ecosystem. Keeping the project open is the right reciprocal gesture. Proprietary also creates an obligation to keep the repo private, which conflicts with the GPL reasoning above.

## Consequences

- The repository remains public on GitHub.
- Any distributed derivative of Cancionero must be open source under GPL v3.
- The branding ("Cancionero") is separately reserved via copyright — GPL does not grant the right to use the name.
- Security hygiene (ADR-022) must be maintained as the codebase evolves: no credentials in version control, API keys restricted, keystore outside the repo.
