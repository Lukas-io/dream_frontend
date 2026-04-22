# The Lineage — Multi-Phase Backend Build Prompt

> A Spring Boot backend for a premium vintage/collector shoe marketplace.
> Built with TDD. Reviewed phase by phase.

-----

## Context (Read Before Every Phase)

**The Lineage** is a premium e-commerce platform for vintage and collector shoes. It combines two core ideas:

- **The Collector’s Ledger** — every shoe has a passport. An immutable record of every owner, every transfer, every authentication event. Buyers see the full history before they buy.
- **The Curation House** — not everyone can sell here. Sellers apply, curators vet them, and only approved sellers at verified tiers can list. Quality is enforced by the system, not by policy.

**Actors:**

- **Buyer** — browses publicly, needs an account to purchase, comment, or save items
- **Seller** — applies to the platform, gets curated and tiered, lists and manages inventory
- **Curator** — reviews seller applications, authenticates shoe listings, manages listing quality
- **Admin** — full platform control, dispute resolution, payouts, analytics

**Core Features:**

- Public catalog with rich filtering (era, brand, colorway, condition, rarity)
- Seller application and tiering system
- Shoe passport — immutable provenance chain per listing
- Inventory state machine (Available → Reserved → Sold → Unlisted)
- Cart, checkout, escrow-based payments, payouts
- Comments (threaded) on listings
- Reviews (post-transaction, structured)
- Notifications (mocked)
- Shipping (mocked)
- Admin dashboard layer

**Stack:** Java, Spring Boot, Spring Security, Spring Data JPA, PostgreSQL, JUnit 5, MockMvc, Testcontainers

-----

## Phase 0 — Product README

**Goal:** Write a world-class README that tells the story of The Lineage before a single line of code exists. This README will live at the root of the repository and is the first thing any developer, stakeholder, or reviewer will read.

**Instructions:**

Write a `README.md` for The Lineage that covers the following — in this order:

1. **The Name & The Idea**
   Open with what The Lineage is and why it exists. Not a feature list — a narrative. Every shoe has a lineage. This platform makes that lineage visible, verifiable, and valuable.
1. **The Problem**
   The vintage shoe market is full of fakes, vague condition descriptions, anonymous sellers, and zero accountability. Buyers have no way to verify what they’re getting. Sellers with real credibility have no way to signal it.
1. **The Solution**
   The Lineage solves this with two interlocking systems — the Collector’s Ledger (provenance) and the Curation House (seller quality). Together they create a marketplace where trust is structural, not assumed.
1. **The Actors**
   Describe each actor — Buyer, Seller, Curator, Admin — and what their relationship to the platform is. Keep it human, not technical.
1. **How It Works — The Core Flows**
   Walk through the key journeys in plain language:
- A seller applying and getting approved
- A shoe being submitted, authenticated, and listed
- A buyer browsing, reading the passport, and purchasing
- The escrow window and dispute process
- A drop event (if applicable)
1. **Feature Overview**
   A clean, grouped list of what the system does — not endpoints, just capabilities.
1. **System Architecture Overview**
   A high-level description of how the backend is structured — layers, responsibilities, key design decisions. Mention TDD, the escrow model, the immutable ledger pattern.
1. **Getting Started**
   How to clone, configure, and run the project locally. Include environment variables needed.
1. **Running Tests**
   How to run unit tests and integration tests separately.
1. **API Overview**
   A brief note that full API documentation is available (Swagger/OpenAPI) and where to find it when running locally.

**Tone:** Premium but human. Clear but not dry. This is the front door of a product with a story — write it like one.

-----

## Phase 1 — System Design

**Goal:** Produce all five system diagrams before any code is written. Each diagram should reflect the full feature scope discussed in the context above.

Work through them in this order — each one informs the next.

-----

### 1.1 — Entity Relationship Diagram (ERD)

Produce a complete ERD for The Lineage.

**Entities to include at minimum:**

- `User` — shared base for all actors, with role enum (BUYER, SELLER, CURATOR, ADMIN)
- `SellerProfile` — extended profile for sellers, includes tier and application status
- `SellerApplication` — the application a seller submits, with status lifecycle
- `Shoe` — the core catalog item, with passport ID, era, brand, colorway, condition grade, rarity score
- `ProvenanceRecord` — one entry per ownership event, linked to Shoe and User, immutable append-only
- `Listing` — the active sale record linked to a Shoe, with state (AVAILABLE, RESERVED, SOLD, UNLISTED)
- `Cart` — per user, contains CartItems
- `CartItem` — links a Listing to a Cart with a reservation timestamp
- `Order` — confirmed purchase, linked to Listing and Buyer
- `Payment` — linked to Order, tracks processor reference, escrow status, payout status
- `Comment` — on a Listing, with optional parent for threading
- `Review` — post-transaction, structured scores per dimension
- `Notification` — mocked, linked to User with type and payload
- `ShippingRecord` — mocked, linked to Order with status

**Show:** all relationships, cardinalities, and foreign keys clearly.

-----

### 1.2 — Use Case Diagram

Produce a Use Case Diagram for each actor.

**Buyer:**

- Browse and search listings
- View shoe passport / provenance
- Create account / login
- Add to cart
- Checkout and pay
- Track order
- Leave a review
- Post and reply to comments
- Save / wishlist a listing
- Raise a dispute

**Seller:**

- Submit seller application
- Manage listing (create, edit, unlist)
- Submit shoe for authentication
- View dashboard (orders, payouts, ratings)
- Respond to comments on listings
- Communicate with curator

**Curator:**

- Review seller applications (approve / reject)
- Authenticate shoe submissions
- Grade and score listings
- Flag or remove listings
- Assign seller tier

**Admin:**

- Full platform oversight
- Resolve disputes
- Manage payouts
- View analytics
- Manage users and roles
- Override curator decisions

-----

### 1.3 — Class Diagram

Produce a Class Diagram for the backend domain model.

Include:

- All entity classes with fields and types
- Enums (UserRole, SellerTier, ApplicationStatus, ListingState, PaymentStatus, EscrowStatus)
- Key service interfaces and their primary methods
- Relationships between classes (inheritance, association, composition)

Key design notes to reflect:

- `ProvenanceRecord` is append-only — no update method on its service
- `Listing` has a state machine — transitions must be explicit methods, not arbitrary setters
- `Payment` tracks escrow separately from payout

-----

### 1.4 — Activity Diagram

Produce Activity Diagrams for these four flows:

1. **Seller Onboarding** — from application submission to first listing
1. **Shoe Authentication** — from seller submission to live listing with passport
1. **Purchase & Escrow** — from add to cart through delivery confirmation and payout release
1. **Dispute Resolution** — from dispute raised to resolution and escrow decision

Each diagram should show decision points, parallel flows where relevant, and swimlanes per actor where applicable.

-----

### 1.5 — Sequence Diagram

Produce Sequence Diagrams for these flows:

1. **POST /listings** — Seller creates a listing. Show the full call chain: Controller → Service → Repository → Database, including authentication check and provenance record creation.
1. **POST /checkout** — Buyer completes purchase. Show cart validation, listing state transition to RESERVED, payment initiation, order creation, and notification trigger.
1. **POST /sellers/applications** — Seller submits application. Show validation, persistence, and curator notification.
1. **GET /listings/{id}** — Public endpoint. Show listing fetch, provenance chain assembly, and response construction.

-----

## Phase 2 — TDD Setup & Test Authoring

**Goal:** Scaffold the Spring Boot project and write all tests before any implementation. Tests drive everything.

### 2.1 — Project Scaffold

Set up the Spring Boot project with:

- Java 21
- Spring Boot 3.x
- Dependencies: Spring Web, Spring Data JPA, Spring Security, PostgreSQL Driver, Lombok, MapStruct, Testcontainers, JUnit 5, Spring Boot Test, MockMvc
- Package structure:
  
  ```
  com.thelineage
  ├── domain          # Entities and enums
  ├── repository      # Spring Data JPA interfaces
  ├── service         # Business logic interfaces and implementations
  ├── controller      # REST controllers
  ├── dto             # Request and response objects
  ├── mapper          # MapStruct mappers
  ├── security        # Auth config, JWT, filters
  ├── exception       # Global error handling
  └── config          # App and bean configuration
  ```
- `application.yml` with profiles: `local`, `test`
- Testcontainers configured for PostgreSQL on the test profile

### 2.2 — Write All Unit Tests First

For each service below, write the full unit test class before any implementation exists. Use Mockito for dependencies. Tests should be thorough — happy paths, edge cases, and failure cases.

**Services to test:**

- `UserService` — register, login, fetch profile, update role
- `SellerApplicationService` — submit, approve, reject, list pending
- `ListingService` — create, update state, fetch by ID, search/filter
- `ProvenanceService` — append record, fetch full chain (assert no update/delete methods exist)
- `CartService` — add item, remove item, reserve listing, release expired reservation
- `OrderService` — create from cart, fetch by ID, fetch by buyer
- `PaymentService` — initiate, confirm, release escrow, trigger refund
- `CommentService` — post, reply, fetch thread, flag
- `ReviewService` — submit, fetch by listing, fetch by seller
- `DisputeService` — open, resolve, fetch by order

**Test naming convention:** `methodName_scenario_expectedResult`
Example: `createListing_whenSellerNotApproved_throwsForbiddenException`

-----

## Phase 3 — Implementation

**Goal:** Implement all layers against the failing tests from Phase 2. Nothing is built speculatively — every class exists to make a test pass.

### Order of implementation:

1. **Domain layer** — Entities, enums, relationships. Annotate with JPA. No business logic here.
1. **Repository layer** — Spring Data JPA interfaces. Add custom query methods where needed.
1. **Service layer** — Implement each service interface. Business logic lives here. Inject repositories only.
1. **Security layer** — JWT filter, UserDetailsService implementation, SecurityFilterChain config. Define public vs protected routes explicitly.
1. **Controller layer** — REST controllers, one per domain. Use DTOs for all input and output. No entity exposure.
1. **Exception handling** — Global `@ControllerAdvice` with structured error responses.
1. **Mappers** — MapStruct mappers between entities and DTOs.

### Public vs Protected Routes:

**Public (no auth required):**

- `GET /listings` — browse catalog
- `GET /listings/{id}` — listing detail + passport
- `GET /listings/{id}/comments` — read comments
- `GET /listings/{id}/reviews` — read reviews
- `GET /sellers/{id}` — seller public profile

**Protected (auth required, role-gated):**

- `POST /cart/**` — BUYER
- `POST /checkout` — BUYER
- `POST /listings` — SELLER (approved only)
- `PUT /listings/{id}` — SELLER (owner only)
- `POST /sellers/applications` — authenticated user
- `POST /curator/**` — CURATOR
- `POST /admin/**` — ADMIN
- `POST /listings/{id}/comments` — authenticated user
- `POST /orders/{id}/reviews` — BUYER (completed order only)
- `POST /disputes` — BUYER

### Security notes:

- Rate limit all public GET endpoints
- JWT tokens are short-lived (15 min access, 7 day refresh)
- Every protected endpoint validates role AND ownership where applicable
- Audit log every state-changing action (listing state, payment, dispute resolution)

-----

## Phase 4 — Integration Test: Add Product

**Goal:** Write a single, thorough integration test for the Add Product (create listing) flow. This is not a unit test — it uses the full Spring context and a real PostgreSQL instance via Testcontainers.

### Test class: `ListingIntegrationTest`

**Setup:**

- Spin up PostgreSQL via Testcontainers
- Run all migrations / schema creation
- Seed: one approved seller user, one curator user, one admin user
- Obtain JWT for the seller via `/auth/login`

**Test cases to cover:**

1. `createListing_withValidPayload_returns201AndListingWithPassport`
- POST to `/listings` with full valid body
- Assert 201 response
- Assert listing is in AVAILABLE state
- Assert a ProvenanceRecord was created for this listing
- Assert the listing is retrievable via GET `/listings/{id}`
1. `createListing_withUnapprovedSeller_returns403`
- Use a seller whose application is PENDING
- Assert 403 Forbidden
1. `createListing_withMissingRequiredFields_returns400`
- POST with incomplete body
- Assert 400 with field-level validation errors
1. `createListing_withoutAuth_returns401`
- POST with no JWT
- Assert 401 Unauthorized
1. `createListing_thenFetchPassport_passportChainIsCorrect`
- Create listing
- GET `/listings/{id}`
- Assert provenance chain contains exactly one record with correct seller and event type LISTED

**Assert at the database level where possible** — not just response bodies. Query the repository directly to confirm state.

-----

## Review Checkpoints

After each phase, pause and review before proceeding:

- **Phase 0 review:** Does the README tell the story clearly? Would a non-technical stakeholder understand the product?
- **Phase 1 review:** Do the diagrams agree with each other? Are all actors, entities, and flows represented consistently?
- **Phase 2 review:** Do the tests cover happy paths AND failure cases? Is the naming convention consistent?
- **Phase 3 review:** Do all unit tests pass? Are there any exposed entities in controller responses? Are public vs protected routes correctly configured?
- **Phase 4 review:** Does the integration test cover the full request-to-database cycle? Are all 5 test cases present and passing?

-----

*The Lineage — Every pair has a history. This platform makes it permanent.*
