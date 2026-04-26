# Design Documents — The Lineage

This folder is the canonical view of the system before you read any code.
All diagrams use Mermaid, which renders inline on GitHub.

## Reading order

| File | What it answers |
|---|---|
| [`1.1-erd.md`](1.1-erd.md) | What data exists and how it relates. **Start here.** |
| [`1.2-use-cases.md`](1.2-use-cases.md) | Who can do what. The actor → action map. |
| [`1.3-class-diagram.md`](1.3-class-diagram.md) | How the domain model and service interfaces are organised in code. |
| [`1.4-activity-diagrams.md`](1.4-activity-diagrams.md) | The four major business workflows (onboarding, authentication, purchase + escrow, dispute). |
| [`1.5-sequence-diagrams.md`](1.5-sequence-diagrams.md) | Per-flow call chains: controller → service → repository → DB, including the auth, curator-authentication, dispute-resolution, and cart-expiry flows. |
| [`1.6-architecture.md`](1.6-architecture.md) | Runtime architecture: HTTP layers, where the JWT filter sits, how DI is wired. Best fast-onboarding diagram. |

If you only have time for two, read **1.6** (architecture) and **1.5** (sequences).

## Diagram conventions

- **ERD** uses standard crow's-foot notation. Marker abbreviations: `PK` primary key, `FK` foreign key, `UK` unique constraint.
- **Class diagrams** are split into a domain block (entities + enums) and a service-interfaces block. Both render in `1.3`.
- **Sequence diagrams** name participants by their concrete Java class so you can grep the codebase from a diagram.
- **Activity diagrams** with multiple actors use Mermaid `subgraph` lanes; some are wide and may scroll horizontally on narrow screens.

## How these docs relate to code

| Diagram concept | Source of truth in code |
|---|---|
| ERD entities | `src/main/java/com/thelineage/domain/*.java` |
| Enums (UserRole, ListingState, …) | same package |
| Services + interfaces | `src/main/java/com/thelineage/service/*.java` |
| Use-case → endpoint map | `src/main/java/com/thelineage/controller/*.java` |
| Role-based access in 1.2 | `src/main/java/com/thelineage/security/SecurityConfig.java` |
