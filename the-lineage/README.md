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

**Sellers** are the supply side, but they aren't anonymous. Every seller applies, is reviewed by a curator, and is assigned a **tier** that reflects their track record and the quality of their submissions. The four tiers are:

| Tier | Meaning |
|---|---|
| `TIER_1` | Recently approved. Limited listing volume; subject to extra scrutiny. |
| `TIER_2` | Established sellers in good standing. Default tier for ongoing activity. |
| `TIER_3` | High-volume, consistently positive review history. Receives reduced authentication latency. |
| `VERIFIED_HOUSE` | Vetted auction houses, archives, or institutions. Their submissions get fast-tracked authentication and a distinct badge in the catalog. |

Sellers list shoes, respond to comments, ship orders, and build a public reputation over time.

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

> *Drops — scheduled reveals with tier-gated early access for high-demand listings — are on the roadmap and not yet implemented.*

---

## Feature Overview

**Catalog & Discovery**
- Public browsing with filters by era, brand, colorway, condition, rarity
- Per-listing passport view with complete provenance chain

**Seller Lifecycle**
- Application submission and review
- Tier assignment by curators
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
- Curator queue for applications and authentications
- Admin endpoints for dispute resolution, escrow release, refunds, and role overrides
- In-app notifications (listed via `GET /notifications/me` — delivery mocked)
- Mocked shipping workflow (seller-driven)

> **Roadmap, not shipped yet.** Wishlist / saved listings, seller analytics dashboards, full-text search, drop events, and a dedicated refresh-token rotation database are currently out of scope.

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

**Design documents.** Diagrams covering the data model, actor permissions, runtime architecture, and the major flows live under [`docs/design/`](docs/design/README.md). Start there if you want to see the system before reading code.

---

## Getting Started

### Prerequisites

| Tool | Minimum version | Notes |
|---|---|---|
| Java | 21 | `java -version` should report 21+ |
| Maven | 3.9+ | Or use the bundled wrapper: `./mvnw` |
| Docker | any recent | For local PostgreSQL **and** the Testcontainers integration test |

### Quickstart

```bash
git clone <repo-url>
cd the-lineage

# 1. Copy env template and (optionally) edit
cp .env.example .env
# Generate a JWT secret if you didn't already:
# openssl rand -base64 48

# 2. Start PostgreSQL
docker compose up -d

# 3. Boot the API (loads .env, runs the local profile, seeds dev users)
LINEAGE_JWT_SECRET=$(grep ^LINEAGE_JWT_SECRET .env | cut -d= -f2-) \
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The API is served on `http://localhost:8080`. Open <http://localhost:8080/scalar> for the interactive API reference.

### Dev seed users

When running with the `local` profile, `DevDataSeeder` creates four accounts on first boot. **All passwords are `password123`.** Use them to exercise every role without running SQL by hand.

| Email | Role | Notes |
|---|---|---|
| `admin@lineage.test` | `ADMIN` | Can hit `/admin/**` |
| `curator@lineage.test` | `CURATOR` | Can hit `/curator/**` |
| `seller@lineage.test` | `SELLER` | Already approved, `TIER_2` |
| `buyer@lineage.test` | `BUYER` | Can shop, comment, raise disputes |

The seeder is idempotent: existing accounts are not overwritten.

### Get a JWT and call a protected endpoint

```bash
# 1. Log in and capture the access token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"seller@lineage.test","password":"password123"}' \
  | jq -r .accessToken)

# 2. Use it
curl -s http://localhost:8080/orders/me \
  -H "Authorization: Bearer $TOKEN" | jq .
```

In the Scalar UI, click the lock icon at the top, paste the access token, and every endpoint's "Try it" form will use it automatically.

### Environment variables

**Required to boot (non-test profiles):**

| Variable | Purpose |
|---|---|
| `LINEAGE_JWT_SECRET` | HMAC-SHA256 signing key (must be ≥ 32 bytes). Boot fails if unset. |

**Optional / tunable:**

| Variable | Default | Purpose |
|---|---|---|
| `LINEAGE_DB_URL` | `jdbc:postgresql://localhost:5432/lineage` | JDBC URL |
| `LINEAGE_DB_USER` | `lineage` | DB user |
| `LINEAGE_DB_PASSWORD` | `lineage` | DB password |
| `LINEAGE_JWT_ACCESS_TTL_MIN` | `15` | Access token lifetime (minutes) |
| `LINEAGE_JWT_REFRESH_TTL_DAYS` | `7` | Refresh token lifetime (days) |

A canonical reference of every variable lives in [`.env.example`](.env.example).

### Ports

| Port | Service |
|---|---|
| `8080` | API + Scalar/Swagger UI |
| `5432` | PostgreSQL (when started via `docker compose`) |

### Profiles

| Profile | DB | Purpose |
|---|---|---|
| `local` | PostgreSQL on `localhost:5432`, schema auto-updated | Day-to-day development. Runs `DevDataSeeder`. |
| `test` | PostgreSQL via Testcontainers, schema recreated | Used by integration tests. Hard-coded JWT secret so tests don't need env setup. |

---

## Running Tests

| Command | What runs | Needs Docker? |
|---|---|---|
| `./mvnw test` | Unit tests only (Mockito, no infra) | No |
| `./mvnw verify` | Unit tests **and** the Testcontainers integration test | Yes — Docker daemon must be running |

The integration test (`ListingIntegrationTest`) brings up real PostgreSQL via Testcontainers, exercises the create-listing flow end-to-end through MockMvc, and asserts at the database level. Skip it on machines without Docker by sticking with `./mvnw test`.

---

## API Overview

Full request/response documentation is available when the service is running locally:

- **Scalar API Reference** — <http://localhost:8080/scalar> *(primary; modern reading-first UI)*
- **Swagger UI** — <http://localhost:8080/swagger-ui.html> *(included for tooling compatibility)*
- **OpenAPI spec** — <http://localhost:8080/v3/api-docs>

Use Scalar for browsing; use the OpenAPI JSON if you want to generate clients or import into Postman.

### Endpoint cheat sheet

"Public" = callable with no JWT. "Any auth" = any valid bearer token. Role-named rows require that role on the token; role-gated endpoints return **`403`** when a token is present but the role is wrong (and **`401`** when there's no token or it's invalid). Ownership-based rules (e.g. "seller must be the owner") are enforced inside the controller and also return `403`.

| Endpoint(s) | Access | Notes |
|---|---|---|
| `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh` | public | `register` always creates a `BUYER`. `refresh` rotates both tokens. |
| `GET /listings`, `GET /listings/{id}` | public | Catalog browse + passport view. |
| `GET /listings/{id}/comments`, `GET /listings/{id}/reviews` | public | |
| `GET /sellers/{id}`, `GET /sellers/{id}/reviews` | public | Seller public profile + their review history. |
| `GET /users/me`, `GET /notifications/me` | any auth | The caller's own profile / notification feed. |
| `POST /sellers/applications`, `GET /sellers/applications/pending` | any auth (submit) / curator queue (read) | Become a seller / review queue. |
| `POST /sellers/me/shoes` | `SELLER` (approved) | Submit a shoe for authentication. |
| `POST /listings`, `DELETE /listings/{id}` | `SELLER` (and owner for delete) | |
| `POST /listings/{id}/comments`, `POST /comments/{id}/replies`, `POST /comments/{id}/flag` | any auth | |
| `GET /cart`, `POST /cart/items`, `DELETE /cart/items/{id}` | `BUYER` | |
| `POST /checkout` | `BUYER` | Reserve → pay → mark sold in one call. |
| `GET /orders/me`, `GET /orders/{id}`, `POST /orders/{id}/confirm` | any auth (ownership-gated in controller) | `confirm` is buyer-only. |
| `POST /orders/{id}/ship`, `POST /orders/{id}/delivered` | any auth (seller / admin) | `ship` takes `{carrier, trackingNumber}`. |
| `POST /orders/{id}/reviews` | `BUYER` (buyer-of-order only) | Only on a `COMPLETED` order; one review per order. |
| `POST /disputes`, `GET /orders/{id}/disputes` | `BUYER` (open) / any auth (read) | |
| `POST /curator/applications/{id}/approve`, `…/reject`, `POST /curator/shoes/{id}/authenticate`, `GET /curator/applications/pending` | `CURATOR` or `ADMIN` | |
| `POST /admin/disputes/{id}/resolve`, `POST /admin/payments/{id}/release-escrow`, `POST /admin/payments/{id}/refund`, `PUT /admin/users/{id}/role` | `ADMIN` | `resolve` side-effects the payment: refund for `RESOLVED_BUYER`, release escrow for `RESOLVED_SELLER`. |
| `GET /v3/api-docs`, `GET /scalar`, `GET /swagger-ui.html` | public | Docs. |

The `SecurityConfig` is the source of truth for role gating; per-endpoint `@ApiResponse` annotations document every documented failure code in Scalar.

### Payments are mocked

There is no real payment processor. `PaymentService.confirm()` simulates capture by setting `paymentStatus = CAPTURED, escrowStatus = HELD`. `releaseEscrow` and `refund` mutate the same record. This is deliberate so you can exercise the full purchase + dispute flow locally without external accounts.

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `Failed to bind ... PostgreSQL` on boot | Postgres not running, or wrong host/port | `docker compose up -d`, then re-run; or set `LINEAGE_DB_URL` |
| `Could not resolve placeholder 'LINEAGE_JWT_SECRET'` | Env var not set | `cp .env.example .env` and export it, or pass it inline |
| `WeakKeyException: signing key size is N bits ... must be ≥ 256 bits` | JWT secret is too short | Use at least 32 bytes; `openssl rand -base64 48` produces a safe value |
| Port 8080 already in use | Another app on 8080 | Stop the other app, or set `server.port` (e.g. `--server.port=8090`) |
| Port 5432 already in use | Local Postgres already running | Stop it, or change the host port mapping in `docker-compose.yml` |
| `class file ... has been compiled by a more recent version of Java` | Wrong JDK active | `java -version` must show 21+; switch via `jenv`/`sdk use java 21` |
| Integration test hangs at `Waiting for ...` | Docker daemon not running | Start Docker; integration test depends on Testcontainers |
| 401 in Scalar after login | Token not pasted into the Authorize dialog | Click the lock icon in Scalar, paste the `accessToken` from the login response |

---

*The Lineage — Every pair has a history. This platform makes it permanent.*
