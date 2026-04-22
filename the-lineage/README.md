# The Lineage

> *Every pair has a history. This platform makes it permanent.*

---

## The Name & The Idea

**The Lineage** is a home for shoes that have lived a life.

Not every sneaker is a commodity. A 1985 Air Jordan 1 isn't the same object as the one sitting in a warehouse today — it has been worn, kept, traded, re-laced, photographed on feet that matter. It has a **lineage**. The problem is that almost no marketplace honors that. Shoes are flattened into thumbnails and condition grades, and the story — the part that makes a pair worth ten times another identical pair — evaporates the moment someone takes a photo and writes a price.

The Lineage exists to keep that story intact. Every shoe on this platform carries a passport: a permanent, append-only record of every owner, every transfer, every authentication event, every condition assessment. You don't just buy a pair. You inherit a chain.

---

## The Problem

The vintage and collector shoe market is broken in three specific ways:

1. **Fakes are indistinguishable from real.** Reproduction quality has outpaced the average buyer's ability to tell them apart. Even experienced collectors get burned.
2. **Condition is a lie told in adjectives.** "Great shape," "lightly worn," "DS" — these mean whatever the seller wants them to mean, and by the time the box arrives, the money is already gone.
3. **Trust has nowhere to live.** A seller with a twenty-year reputation on forums has no way to carry that reputation onto a marketplace. A first-time buyer with a real $1,200 offer has no way to separate themselves from the scammers.

Every other solution to this — returns policies, buyer protection, ratings — treats the symptoms. The Lineage treats the structure.

---

## The Solution

The Lineage is built on two interlocking systems:

**The Collector's Ledger** is the provenance layer. Every listing is backed by an immutable chain of records. When a shoe is submitted, authenticated, sold, shipped, received, resold — each event becomes a permanent entry. Nothing is overwritten. Nothing is deleted. A buyer looking at a listing doesn't just see a price and photos; they see every hand the pair has passed through on this platform, and every verification event along the way.

**The Curation House** is the seller layer. You don't get to sell here by signing up. You apply, you're vetted by curators, you're assigned a tier, and your listings are authenticated before they go live. A seller's standing on The Lineage is not an opinion — it is a credential backed by a human review process and a track record visible to everyone.

Together these produce something the market hasn't had: a place where **trust is structural, not assumed**.

---

## The Actors

**Buyers** come to find pairs they can believe in. They can browse anonymously, but anything that costs money — purchasing, saving, commenting, reviewing — requires an account. A buyer's job on the platform is to read the passport, ask questions, and buy with confidence.

**Sellers** are the supply side, but they aren't anonymous. Every seller applies, is reviewed by a curator, and is assigned a tier that reflects their track record and the quality of their submissions. Sellers list shoes, respond to comments, ship orders, and build a public reputation over time.

**Curators** are the gatekeepers. They review seller applications, authenticate individual shoe submissions, grade condition, and assign rarity scores. They are the humans behind the guarantee — the reason a buyer can trust what the ledger says.

**Admins** run the platform. They resolve disputes, release or reverse escrow, manage payouts, oversee curators, and handle the edge cases that don't fit cleanly into any of the automated flows.

---

## How It Works — The Core Flows

**A seller applies and gets approved.**
A prospective seller creates an account and submits an application: their history, their inventory, references, proof of past transactions. A curator reviews it, asks questions if needed, and either approves with a tier assignment or declines. Approved sellers get a dashboard and can begin submitting shoes.

**A shoe is submitted, authenticated, and listed.**
The seller submits a shoe with full details and photographs. A curator authenticates it — confirming it's genuine, grading condition, assigning a rarity score. On approval, a passport is created and the listing goes live in the `AVAILABLE` state. The first provenance record is written: *Listed by seller X, authenticated by curator Y, on date Z*.

**A buyer browses, reads the passport, and purchases.**
A buyer finds the listing, reviews the full provenance chain, asks questions in the comments if they want. When they add it to their cart, the listing transitions to `RESERVED` for the duration of checkout. On payment, funds move into escrow, an order is created, and the listing becomes `SOLD`. A new provenance record is written.

**The escrow window and dispute process.**
Funds stay in escrow until the buyer confirms receipt (or a configurable window elapses with no dispute). If the buyer raises a dispute — wrong pair, misrepresented condition, damage in transit — the escrow holds and admins investigate. Resolution either releases funds to the seller or refunds the buyer, and the provenance record notes the outcome.

**Drop events.**
Certain high-demand listings can be published as drops: a scheduled reveal with a shared start time, a purchase queue, and tier-gated early access for repeat buyers. The ledger and curation rules are unchanged — drops just change how the listing becomes visible.

---

## Feature Overview

**Catalog & Discovery**
- Public browsing with filters by era, brand, colorway, condition, rarity
- Full-text search
- Per-listing passport view with complete provenance chain
- Saved / wishlist items for signed-in buyers

**Seller Lifecycle**
- Application submission and review
- Tier assignment and re-tiering
- Seller dashboards: inventory, orders, payouts, ratings
- Public seller profiles

**Authentication & Provenance**
- Per-listing curator authentication workflow
- Condition grading and rarity scoring
- Append-only provenance records — no edits, no deletes
- Passport lookup for every listing

**Commerce**
- Cart with time-bound reservations
- Checkout with mocked payment processor
- Escrow-based fund holding
- Automatic payout release on confirmation or timeout

**Community & Accountability**
- Threaded comments on listings
- Structured post-transaction reviews (multi-dimension scores)
- Flagging and moderation
- Dispute submission and resolution

**Platform Operations**
- Admin dashboards for disputes, payouts, and analytics
- Curator queue for applications and authentications
- Audit log for every state-changing action
- Mocked notifications and shipping

---

## System Architecture Overview

The Lineage backend is a layered Spring Boot application, designed for clarity of responsibility and test-first development.

**Layers:**
- **Domain** — JPA entities and enums. No business logic.
- **Repository** — Spring Data JPA interfaces. Custom queries for search and filtering.
- **Service** — All business logic. Services depend on repositories, never on controllers.
- **Controller** — REST endpoints. DTO in, DTO out. No entity leaves this layer.
- **Security** — JWT filter chain, role- and ownership-based authorization.
- **Exception** — Global `@ControllerAdvice` with structured error responses.
- **Mapper** — MapStruct mappers between entities and DTOs.

**Key design decisions:**
- **Test-first.** Every service is specified by its unit tests before implementation begins. An integration test against a real PostgreSQL instance (via Testcontainers) validates the full stack for the listing creation flow.
- **Immutable ledger.** `ProvenanceRecord` has no update or delete method anywhere in the service layer. Records are appended, period.
- **Explicit state machine.** `Listing` transitions (`AVAILABLE → RESERVED → SOLD → UNLISTED`) are named methods on the service, not arbitrary setters. Invalid transitions throw.
- **Escrow separate from payout.** `Payment` tracks two independent states — whether funds have cleared into escrow, and whether they have been released to the seller. One never implies the other.
- **Role and ownership both enforced.** Every protected endpoint checks both the caller's role and, where relevant, whether they own the resource being modified.

**Stack:** Java 21, Spring Boot 3.x, Spring Security, Spring Data JPA, PostgreSQL, JUnit 5, MockMvc, Testcontainers, MapStruct, Lombok.

---

## Getting Started

**Prerequisites:**
- Java 21
- Maven 3.9+
- Docker (for running PostgreSQL locally and for Testcontainers)

**Clone and run:**
```bash
git clone <repo-url>
cd the-lineage

# Start PostgreSQL (any method works; docker-compose file below is easiest)
docker run --name lineage-pg -e POSTGRES_DB=lineage \
  -e POSTGRES_USER=lineage -e POSTGRES_PASSWORD=lineage \
  -p 5432:5432 -d postgres:16

# Run with the local profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Environment variables:**

| Variable | Purpose | Default |
|---|---|---|
| `LINEAGE_DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/lineage` |
| `LINEAGE_DB_USER` | DB user | `lineage` |
| `LINEAGE_DB_PASSWORD` | DB password | `lineage` |
| `LINEAGE_JWT_SECRET` | HMAC signing key (32+ bytes) | *(required)* |
| `LINEAGE_JWT_ACCESS_TTL_MIN` | Access token lifetime | `15` |
| `LINEAGE_JWT_REFRESH_TTL_DAYS` | Refresh token lifetime | `7` |

The API is served on `http://localhost:8080`.

---

## Running Tests

**Unit tests** (fast, no infrastructure):
```bash
./mvnw test
```

**Integration tests** (spins up PostgreSQL via Testcontainers — Docker must be running):
```bash
./mvnw verify -Dspring.profiles.active=test
```

**All tests:**
```bash
./mvnw verify
```

---

## API Overview

Full request/response documentation is available when the service is running locally:

- **Scalar API Reference**: `http://localhost:8080/scalar` (primary — modern reference UI)
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI spec**: `http://localhost:8080/v3/api-docs`

Public endpoints (no auth) cover catalog browsing and reading comments/reviews. All write operations and user-specific reads require a JWT issued via `POST /auth/login`.

---

*The Lineage — Every pair has a history. This platform makes it permanent.*
