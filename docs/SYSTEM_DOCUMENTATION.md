# KSC Platform — System Documentation

**Cleaning, Fumigation & Property Management Platform** (service name: `ksc`)

This document describes the architecture, modules, data model, API, business rules and operational setup of the KSC backend. Interactive, always-current endpoint docs are also available from Swagger UI (see [API documentation](#7-api-documentation-swagger)).

---

## Table of contents

1. [Overview](#1-overview)
2. [Technology stack](#2-technology-stack)
3. [Architecture](#3-architecture)
4. [Project structure](#4-project-structure)
5. [Domain modules](#5-domain-modules)
6. [Data model](#6-data-model)
7. [API documentation (Swagger)](#7-api-documentation-swagger)
8. [API reference](#8-api-reference)
9. [Business rules](#9-business-rules)
10. [Enumerations](#10-enumerations)
11. [Security and permissions](#11-security-and-permissions)
12. [Seed data](#12-seed-data)
13. [Configuration](#13-configuration)
14. [Building and running](#14-building-and-running)
15. [Database setup](#15-database-setup)
16. [Implementation status and known issues](#16-implementation-status-and-known-issues)
17. [Development conventions](#17-development-conventions)

---

## 1. Overview

KSC is a Spring Boot backend that manages the full lifecycle of a cleaning / fumigation / property-management business:

```
Lead ──► Site ──(automatic)──► Quotation + Invoice ──► Payment (mobile money via payment service, or cash)
                                   │
                                   └─ accepted ──► Contract ──► Subscription ──► Job ──► Time logs, Checklists, Photos
                                                                                   └──► Feedback ──► Staff ratings
```

- **Leads** capture prospective clients and the service they are interested in.
- **Sites** are the client properties to be serviced. Each site selects a **service** (Cleaning, Fumigation, Property Management), a **cleaning depth** (required for Cleaning) and optional **add-ons**, which together determine its **total price**.
- **Quotations and invoices** are generated automatically when a site is registered, and regenerated when its pricing changes. Both can be downloaded as PDFs.
- **Payments** are collected by mobile money through the platform payment service (results arrive on Kafka) or recorded as cash; the invoice becomes PAID when covered.
- **Contracts** are created from an **accepted** quotation and move through DRAFT → SENT → SIGNED → ACTIVE → EXPIRED (or REJECTED / TERMINATED); **subscriptions** schedule recurring work under a contract.
- **Jobs** are individual service visits, carried out by a **crew**; **crew members** link staff to crews. Staff **clock in/out** with geofence validation.
- **Feedback** and **staff ratings** are collected after jobs.
- **Master data** (services, slots, cleaning depths, add-ons) configures what the business offers.
- **Dashboards** for super admins, clients, staff and supervisors are computed by PostgreSQL functions.

---

## 2. Technology stack

| Area | Technology |
|---|---|
| Language | Java 17 (`java.version` in `pom.xml`) |
| Framework | Spring Boot 3.3.3 |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL |
| Caching | Redis (Lettuce client, Spring Cache, 6 h TTL) |
| Messaging | Apache Kafka (`spring-kafka`); AMQP starter also on classpath |
| Service discovery / config | Spring Cloud Netflix Eureka client, Spring Cloud Config client |
| Inter-service calls | Spring Cloud OpenFeign, `RestTemplate` |
| API docs | springdoc-openapi 2.6.0 (Swagger UI) |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Security / utilities | Internal libraries `afriSecurity` and `afriUtils` |
| Documents | Apache PDFBox 2.0 (quotation/invoice PDFs), Apache Tika |
| Observability | Spring Boot Actuator, Micrometer + Prometheus, Zipkin (Brave) tracing |
| Other | Lombok 1.18.34, GraphQL starter, fuzzy-matcher |

---

## 3. Architecture

KSC runs as one microservice in a Spring Cloud ecosystem:

```
                 ┌──────────────────┐
                 │  Config Server   │  (CONFIG_SERVER_URI, default http://localhost:7000/)
                 └────────┬─────────┘
                          │ properties
┌──────────┐     ┌────────▼─────────┐     ┌──────────────┐
│  Eureka  │◄────┤   ksc service    ├────►│  PostgreSQL  │
└──────────┘     │  (Spring Boot)   │     └──────────────┘
                 │                  ├────►│    Redis     │ cache
                 │                  ├────►│    Kafka     │ events
                 └────────┬─────────┘     └──────────────┘
                          │ Feign / REST
                   other platform services
```

### Layering

Each module follows the same layered structure:

| Layer | Responsibility |
|---|---|
| `controllers` | REST endpoints, `@Permission` checks, request validation (`@Valid`), wraps results with `ApiResponseUtil` |
| `dto` | Request DTOs (`XxxDto`) and response DTOs (`XxxResponseDto`, built from the entity via a constructor) |
| `services` | Interface + `XxxServiceImpl` with business logic; `@Transactional` |
| `repository` | Spring Data `JpaRepository` interfaces |
| `entities` | JPA entities extending `BaseEntity` |

### Application bootstrap

`KscApplication` scans the packages `ksc.go.tz`, `afriUtils` and `afriSecurity`, and enables:

- JPA repositories in `ksc.go.tz`, `afriSecurity.permissions.repository` and `afriSecurity.auditLogs.repositories`
- Feign clients (`@EnableFeignClients`)
- Async processing (`@EnableAsync`)
- Scheduled tasks (`@EnableScheduling`)

### Common base entity

All entities extend `ksc.go.tz.common.BaseEntity<UUID>`:

| Field | Column | Notes |
|---|---|---|
| `id` | `id` | UUID, generated (`uuid_generate_v4()` default) |
| `createdBy` | `created_by` | Auditing (`@CreatedBy`) |
| `createdAt` | `created_at` | `@CreationTimestamp` |
| `updatedBy` | `updated_by` | Auditing (`@LastModifiedBy`) |
| `updatedAt` | `updated_at` | `@UpdateTimestamp` |
| `deletedAt` | `deleted_at` | Soft delete marker |
| `deleted` | `deleted` | Boolean, default `false` |
| `active` | `active` | Boolean, default `true` |

**Soft delete:** most entities have `@Where(clause = "deleted_at is null")`, so a row with `deleted_at` set is hidden from all queries. "Delete" endpoints set `deleted_at` instead of removing rows.

### Messaging (Kafka)

| Topic | Constant | Declared partitions / replicas | Usage |
|---|---|---|---|
| `audit.logs` | `KafkaTopic.AUDIT_LOGS` | 3 / 1 | Audit log events |
| `company.user_registration` | `KafkaTopic.COMPANY_USER_REGISTRATION` | 3 / 1 | Produced by `AfriTransProducer.sendUpdateCompanyUser` |
| `ksc.payment.results` | `KafkaTopic.PAYMENT_RESULTS` | 3 / 1 | **Consumed** by KSC (group `ksc-payment-results`): payment outcomes for system `KSC` from the payment service |

> **Topics are not created by KSC.** `KafkaConfig` declares a `NewTopic` bean for each topic above, but the shared config-server properties set `spring.kafka.admin.auto-create=false`, so those beans are ignored at startup. Each topic exists only if it was **created on the broker**, or if the broker has `auto.create.topics.enable=true` (Kafka's default, not confirmed for this broker). Before relying on payments, make sure `ksc.payment.results` exists, for example:
>
> ```bash
> kafka-topics.sh --bootstrap-server <broker>:9092 --create --if-not-exists --topic ksc.payment.results --partitions 3 --replication-factor 1
> ```

The payment results topic uses plain JSON strings (own string deserializer), independent of the service-wide Kafka serializer settings. See [Payment service integration](#911-payment-service-integration).

`AfriTransConsumer` exists but currently has no active listeners.

### Caching (Redis)

`RedisConfig` enables Spring Cache backed by Redis with string keys, JSON (Jackson) values and a default TTL of **6 hours**.

---

## 4. Project structure

```
src/main/java/ksc/go/tz/
├── KscApplication.java
├── common/                     BaseEntity, configs (OpenAPI, Redis, RestTemplate, file folders), Kafka
├── enums/                      Shared enumerations
├── leads/                      Lead capture and tracking
├── sitesAndAssests/            Client sites
├── quotation/                  Quotes (auto-generated on site registration)
├── contractAndSubscriptions/   Contracts and subscriptions
├── job/                        Jobs, crews and crew members, checklists, time tracking, photos, feedback, staff ratings
├── billing/                    Invoices (generation + read endpoints) and payments
├── masterData/                 Services, slots, cleaning depths, add-ons, seeders, pricing
├── DocumentManagement/         File upload / download
├── dashboard/                  Super admin and client dashboards (computed by PostgreSQL functions); reports not built
src/main/resources/db/functions/   Dashboard SQL functions, installed at startup
├── HRM/, feedbackEngine/, fieldOperation/, mobileApp/   Placeholders (no source files yet)
src/main/resources/
├── application.properties
└── newFeatures                 Feature notes
```

---

## 5. Domain modules

### 5.1 Master data (`masterData`)

Reference data that configures the business offering.

| Entity | Purpose |
|---|---|
| **Service** | A service line offered (Cleaning, Fumigation, Property Management) |
| **Slot** | An available date/time range for booking |
| **CleaningDepth** | A cleaning level (e.g. Shallow, Medium, Deep) with its own price |
| **AddOn** | An optional extra (e.g. Window Cleaning) with its own price |

All four support create, list, get by id, update, soft delete and status change (`ACTIVE` / `INACTIVE`).

`PricingItemResolver` (shared component) looks up and validates cleaning depths and add-ons by ID and calculates the total price. It is used by the Sites module.

### 5.2 Sites (`sitesAndAssests`)

A **site** is a client property. On registration the site records its type, area, rooms, address, GPS coordinates, security/access info, **one required service** (from master data), a **cleaning depth** (required for Cleaning sites, optional for Fumigation and Property Management), **zero or more add-ons**, and a server-calculated **total price**. See [Site pricing](#91-site-pricing).

The site's latitude/longitude are also stored as `plotCoordinates` (JSON) and are used for the clock-in geofence.

### 5.3 Leads (`leads`)

Prospective clients: name, phone, email, message, service of interest (`LeadServiceType`), source (`LeadSource`) and status (`LeadStatus`). Supports a paginated listing with sorting and filtering by status, source and service line.

### 5.4 Quotation (`quotation`)

A **quote** belongs to a site and holds a quote number, requester, client tier, hourly rate, estimated hours, a min/max price range, validity date, service type, frequency, area, status (`QuoteStatus`) and **line items**. Quotes can be **sent** and **accepted**.

When a site is registered, `QuoteService.generateForSite` creates its quote automatically. See [Quotation and invoice generation](#99-quotation-and-invoice-generation).

### 5.5 Contracts and subscriptions (`contractAndSubscriptions`)

- **Contract:** created from one **ACCEPTED** quote. Holds a contract number, status (`ContractStatus`), client, site, service type, business info (TIN, BRELA number), personal ID number, frequency, contract value, terms, start/end dates, and who signed / rejected / terminated it and when. The contract document is generated as a PDF on request. See [Contract lifecycle](#95-contracts).
- **Subscription:** recurring service under a contract: status, recurrence, shift preference, crew size, price per cycle and next run date. Lifecycle actions: cancel, pause, resume, renew. Responses now include `subscriptionId`.

### 5.6 Jobs (`job`)

| Entity | Purpose |
|---|---|
| **Job** | A service visit: contract, optional subscription, site, crew, service type, job type, scheduled start/end, status |
| **Crew** | A team with a name, supervisor and zone |
| **JobCheckList** / **JobChecklistItem** | A checklist template on a job and its individual tasks (label, complete, notes) |
| **JobPhoto** | Photos captured for a job (storage URL, capture time, sync flag) |
| **TimeLog** | Staff clock-in/out with geofence status |
| **Feedback** | Client rating and comment for a job, with dispute status |
| **StaffRating** | Per-staff rating linked to a feedback |

### 5.7 Billing (`billing`)

- **Invoice:** generated from a quote for a site (`InvoiceService.generateForQuote`). Holds an invoice number, line items, amount due, issue and due dates, status and PDF URLs. The contract link is optional and is set once a contract exists.
- **Payment:** belongs to an invoice; amount, method (`PaymentMethod`), provider reference, status (`PaymentStatus`), paid time.

`InvoiceController` exposes read and PDF endpoints. `PaymentController` starts mobile-money payments through the payment service, records cash payments and lists payments. See [Payment service integration](#911-payment-service-integration).

### 5.8 Document management (`DocumentManagement`)

Upload and download of files (multipart or base64), stored on disk under `file.upload-dir`, with sub-folders per `DocumentType` configured via `file.upload.paths.*`. Metadata is stored in the `uploads` table.

---

## 6. Data model

### 6.1 Entity relationships

```
services ◄──(N:1)── sites
cleaning_depths ◄──(N:1, optional)── sites ──(N:M via site_add_ons)──► add_ons
                             ▲
                             │ N:1
quotes ──────────────────────┘
  ▲
  │ 1:1
contracts ──► quotes (one live contract per quote), sites
  ▲   ▲
  │   └── subscriptions
  ├──(optional)── invoices ◄── payments
  │               invoices ──► quotes, sites
  │
jobs ──► contracts, sites, subscriptions (optional), crews (optional)
  ▲
  ├── crews ◄── crew_members (staff)
  ├── job_checklists ◄── checklist_items
  ├── job_photos
  ├── time_logs
  └── feedbacks ◄── staff_ratings

Standalone: leads, services, slots, uploads
```

### 6.2 Tables

Every table also has the [base entity columns](#common-base-entity).

#### `services`
| Column | Type | Notes |
|---|---|---|
| `service_name` | varchar | required |
| `price` | numeric(15,2) | optional fixed price per site in TZS (e.g. Fumigation, Property Management); null = no fixed charge |
| `status` | varchar (`Status`) | required |

#### `slots`
| Column | Type | Notes |
|---|---|---|
| `start_date_time` | timestamp | required |
| `end_date_time` | timestamp | required, after start |
| `status` | varchar (`Status`) | required |

#### `cleaning_depths` and `add_ons`
Both tables have the same columns:

| Column | Type | Notes |
|---|---|---|
| `name` | varchar | required, unique (case-insensitive, enforced in service layer) |
| `price` | numeric(15,2) | required, ≥ 0, TZS |
| `description` | varchar | optional |
| `status` | varchar (`Status`) | required |

#### `sites`
| Column | Type | Notes |
|---|---|---|
| `site_owner_id` | varchar | required; set to the creating user's ID |
| `property_type` | varchar | |
| `site_type` | varchar | e.g. residential, commercial |
| `area_sqm` | numeric | |
| `service_id` | uuid → `services.id` | **required**: service requested for the site |
| `cleaning_depth_id` | uuid → `cleaning_depths.id` | required for Cleaning sites; null allowed for Fumigation / Property Management |
| `total_price` | numeric(15,2) | **required**, server-calculated |
| `room_count` | integer | |
| `address_area` | varchar | |
| `plot_coordinates` | jsonb | `{latitude, longitude}` |
| `longitude` | varchar | required |
| `latitude` | varchar | required |
| `is_secured` | boolean | |
| `access_type` | varchar | |

#### `site_add_ons` (join table)
| Column | Type |
|---|---|
| `site_id` | uuid → `sites.id` |
| `add_on_id` | uuid → `add_ons.id` |

#### `leads`
`full_name`, `phone_number`, `email`, `message`, `service_type_interest` (`LeadServiceType`), `source` (`LeadSource`), `status` (`LeadStatus`).

#### `quotes`
`quote_number` (unique), `site_id` (required), `requested_by`, `client_tier`, `hourly_rate`, `estimated_hours`, `price_min`, `price_max`, `valid_until`, `service_type` (`LeadServiceType`), `frequency` (`Frequency`), `area_sqm`, `status` (`QuoteStatus`).

#### `quote_items` / `invoice_items` (line items)
| Column | Type | Notes |
|---|---|---|
| `quote_id` / `invoice_id` | uuid | owning quote or invoice |
| `line_no` | integer | order of the line |
| `description` | varchar | e.g. "Cleaning depth: Deep", "Add-on: Window Cleaning" |
| `quantity` | integer | |
| `unit_price` | numeric(15,2) | TZS |
| `amount` | numeric(15,2) | quantity × unit price |

#### `contracts`
`contract_number` (unique), `quote_id` (required; one live contract per quote, enforced in code), `site_id`, `client_id`, `service_type`, `status` (`ContractStatus`), `signature_status` (`SignatureStatus`), `business_info`, `business_tin`, `business_brela_no`, `personal_id_no`, `frequency`, `contract_value`, `terms`, `pdf_url` (optional), `paid_at`, `start_date`, `end_date`, `sent_at`, `signed_at`, `signed_by`, `rejected_at`, `rejection_reason`, `terminated_at`, `termination_reason`.

#### `subscriptions`
`contract_id` (required), `status` (`SubscriptionStatus`), `recurrence`, `shift_preference`, `crew_size`, `price_per_cycle`, `next_run_date`.

#### `jobs`
`contract_id` (required), `subscription_id`, `site_id` (required), `crew_id`, `service_type`, `job_type` (`JobType`), `scheduled_start`, `scheduled_end`, `status` (`JobStatus`).

#### `crews` / `crew_members`
- `crews`: `crew_name`, `supervisor_id` (supervisor's user ID), `supervisor_name`, `zone_name`.
- `crew_members`: `crew_id` (required), `staff_id` (staff user ID, same as `time_logs.staff_id`), `staff_name`, `member_role`, `joined_on`. One live membership per crew and staff member (enforced in code).

#### `job_checklists` / `checklist_items`
- `job_checklists`: `job_id` (required), `template_name`
- `checklist_items`: `checklist_id` (required), `task_label`, `complete`, `notes`

#### `job_photos`
`job_id` (required), `image`, `storage_url`, `captured_at`, `synced`.

#### `time_logs`
`job_id` (required), `staff_id`, `clock_in`, `clock_out`, `geofence_zone_in`, `geofence_zone_out`, `clock_in_status`, `clock_out_status` (`TimeLogStatus`).

#### `feedbacks` / `staff_ratings`
- `feedbacks`: `job_id` (required), `submitted_by`, `service_rating`, `comment`, `dispute_status` (`DisputeStatus`)
- `staff_ratings`: `feedback_id` (required), `staff`, `rating`, `comment`

#### `invoices` / `payments`
- `invoices`: `invoice_number` (unique), `quote_id`, `site_id`, `contract_id` (optional), `issue_date`, `due_date`, `amount_due`, `amount_paid`, `billing_period_start`, `billing_period_end`, `status`, `pdf_local_url`, `pdf_url`
- `payments`: `invoice_id` (required), `amount`, `currency`, `method` (`PaymentMethod`), `payer_phone`, `provider_ref` (provider reference or cash receipt number), `status` (`PaymentStatus`), `failure_reason`, `notes`, `receipt_number` (unique, set on SUCCESS), `payment_reference` (payment service reference), `payment_url` (card/checkout link), `control_number` (BillPay), `paid_at`

#### `uploads`
`file_name`, `file_type` (`DocumentType`), `file_description`, `kb_size`.

---

## 7. API documentation (Swagger)

The OpenAPI spec is generated at runtime by springdoc from controller and DTO annotations.

| Resource | URL |
|---|---|
| Swagger UI | `http://<host>:<port>/swagger-ui/index.html` |
| OpenAPI JSON | `http://<host>:<port>/v3/api-docs` |

The Master Data (Services, Slots, Cleaning Depths, Add-ons), Sites, Contracts, Invoices, Payments and Dashboards controllers carry `@Tag` groups, endpoint descriptions and `@Schema` field descriptions/examples. Other modules (leads, quotes, subscriptions, jobs, files) currently document a summary per endpoint only.

---

## 8. API reference

- **Base path:** `/api/v1`, except file management which uses `/api/file-upload`
- **Authentication:** all endpoints take the caller's identity from the Spring Security `Authentication` (resolved by `afriSecurity`'s `AuthDetailsExtractor`).
- **Authorization:** each endpoint is protected by an `@Permission` code (listed below).
- **Response format:** responses are wrapped by `afriUtils`' `ApiResponseUtil.ApiResponseEntity`, which carries the payload, a message and a `ResponseEnum` status (e.g. `SUCCESS`, `NOT_FOUND`).
- **Errors:** business-rule violations throw `AfriException` with a readable message (e.g. "Site not found").
- **IDs:** all IDs are UUID strings. An invalid UUID returns an error such as "Invalid add-on ID: …".
- **Dates:** ISO-8601, e.g. `2026-10-05T08:00:00`.

### 8.1 Master data

#### Services
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/services` | `SAVE_SERVICE` | Create a service |
| GET | `/services` | `VIEW_SERVICE` | List services |
| GET | `/services/{id}` | `VIEW_SERVICE_BY_ID` | Get a service |
| PUT | `/service/{id}` | `UPDATE_SERVICE` | Update a service (note: singular path) |
| DELETE | `/services/{id}` | `DELETE_SERVICE` | Soft delete |
| PATCH | `/services/{serviceId}/status?status=ACTIVE\|INACTIVE` | `CHANGE_SERVICE_STATUS` | Change status |

Request body:
```json
{ "serviceName": "Fumigation", "price": 150000.00 }
```
`price` is optional (must not be negative). When set, every site with this service is charged it once, as a "Service: …" line on its quotation and invoice. Leave it empty for Cleaning, which is priced by cleaning depth. The response includes `price`.

#### Slots
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/slots` | `SAVE_SLOT` | Create an available slot |
| GET | `/slots` | `VIEW_SLOT` | List slots |
| GET | `/slots/{id}` | `VIEW_SLOT_BY_ID` | Get a slot |
| PUT | `/slots/{id}` | `UPDATE_SLOT` | Update a slot |
| DELETE | `/slots/{id}` | `DELETE_SLOT` | Soft delete |
| PATCH | `/slots/{id}/status?status=ACTIVE\|INACTIVE` | `CHANGE_SLOT_STATUS` | Change status |

Request body:
```json
{
  "startDateTime": "2026-10-05T08:00:00",
  "endDateTime": "2026-10-05T12:00:00"
}
```
Response:
```json
{
  "slotId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "startDateTime": "2026-10-05T08:00:00",
  "endDateTime": "2026-10-05T12:00:00",
  "status": "ACTIVE"
}
```

#### Cleaning depths
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/cleaning-depths` | `SAVE_CLEANING_DEPTH` | Create |
| GET | `/cleaning-depths` | `VIEW_CLEANING_DEPTH` | List |
| GET | `/cleaning-depths/{id}` | `VIEW_CLEANING_DEPTH_BY_ID` | Get one |
| PUT | `/cleaning-depths/{id}` | `UPDATE_CLEANING_DEPTH` | Update |
| DELETE | `/cleaning-depths/{id}` | `DELETE_CLEANING_DEPTH` | Soft delete |
| PATCH | `/cleaning-depths/{id}/status?status=ACTIVE\|INACTIVE` | `CHANGE_CLEANING_DEPTH_STATUS` | Change status |

#### Add-ons
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/add-ons` | `SAVE_ADD_ON` | Create |
| GET | `/add-ons` | `VIEW_ADD_ON` | List |
| GET | `/add-ons/{id}` | `VIEW_ADD_ON_BY_ID` | Get one |
| PUT | `/add-ons/{id}` | `UPDATE_ADD_ON` | Update |
| DELETE | `/add-ons/{id}` | `DELETE_ADD_ON` | Soft delete |
| PATCH | `/add-ons/{id}/status?status=ACTIVE\|INACTIVE` | `CHANGE_ADD_ON_STATUS` | Change status |

Request body (cleaning depths and add-ons):
```json
{
  "name": "Window Cleaning",
  "price": 20000.00,
  "description": "Interior and exterior window and glass cleaning"
}
```
Response:
```json
{
  "addOnId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Window Cleaning",
  "price": 20000.00,
  "description": "Interior and exterior window and glass cleaning",
  "status": "ACTIVE"
}
```
(The cleaning depth response uses `cleaningDepthId` instead of `addOnId`.)

### 8.2 Sites
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/sites` | `SAVE_SITE` | Register a site |
| GET | `/sites` | `VIEW_SITE` | List sites |
| GET | `/sites/{id}` | `VIEW_SITE_BY_ID` | Get a site |
| PUT | `/sites/{id}` | `UPDATE_SITE` | Update a site (full replacement; validated) |
| DELETE | `/sites/{id}` | `DELETE_SITE` | Soft delete |

Request body:
```json
{
  "siteType": "residential",
  "areaSqm": 120.5,
  "roomCount": 4,
  "addressArea": "Mikocheni, Dar es Salaam",
  "longitude": "39.2408",
  "latitude": "-6.7618",
  "secured": true,
  "accessType": "Gate pass",
  "serviceId": "<service uuid>",
  "cleaningDepthId": "<cleaning depth uuid>",
  "addOnIds": ["<add-on uuid>", "<add-on uuid>"]
}
```
Required: `siteType`, `areaSqm`, `roomCount`, `addressArea`, `longitude`, `latitude`, `serviceId`, and `cleaningDepthId` **for Cleaning sites only**. The service must exist and be ACTIVE (see `GET /services`). Fumigation and Property Management sites may omit `cleaningDepthId`.

Response (abridged):
```json
{
  "siteId": "…",
  "siteType": "residential",
  "areaSqm": 120.5,
  "roomCount": 4,
  "addressArea": "Mikocheni, Dar es Salaam",
  "plotCoordinates": "{latitude=-6.7618, longitude=39.2408}",
  "longitude": "39.2408",
  "latitude": "-6.7618",
  "secured": true,
  "accessType": "Gate pass",
  "service": { "serviceId": "…", "serviceName": "Cleaning", "status": "ACTIVE" },
  "cleaningDepth": { "cleaningDepthId": "…", "name": "Deep", "price": 120000.00, "description": "…", "status": "ACTIVE" },
  "addOns": [ { "addOnId": "…", "name": "Window Cleaning", "price": 20000.00, "description": "…", "status": "ACTIVE" } ],
  "totalPrice": 140000.00
}
```

### 8.3 Leads
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/leads` | `SAVE_LEAD` | Create a lead |
| GET | `/leads/pagination` | `VIEW_LEAD` | Paginated list; sort, filter by status, source, service line |
| GET | `/leads` | `VIEW_LEAD` | List leads |
| PUT | `/leads/{id}` | `UPDATE_LEAD` | Update a lead |
| DELETE | `/leads/{id}` | `DELETE_LEAD` | Soft delete |
| PUT | `/leads/{leadId}/status` | `CHANGE_LEAD_STATUS` | Change lead status |

### 8.4 Quotes
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/quotes` | `SAVE_QUOTE` | Create a quote for a site |
| GET | `/quotes` | `VIEW_QUOTE` | List quotes |
| GET | `/quotes/{id}` | `VIEW_QUOTE_BY_ID` | Get a quote (with its site and line items) |
| PUT | `/quotes/{id}` | `UPDATE_QUOTE` | Update a quote |
| DELETE | `/quotes/{id}` | `DELETE_QUOTE` | Soft delete |
| POST | `/quotes/{id}/send` | `SEND_QUOTE` | Mark quote as SENT |
| POST | `/quotes/{id}/accept` | `ACCEPT_QUOTE` | Mark quote as ACCEPTED |
| GET | `/quotes/{id}/pdf` | `DOWNLOAD_QUOTE_PDF` | Download the quotation as a PDF |

### 8.5 Invoices
| Method | Path | Permission | Description |
|---|---|---|---|
| GET | `/invoices` | `VIEW_INVOICE` | List invoices |
| GET | `/invoices/{id}` | `VIEW_INVOICE_BY_ID` | Get an invoice |
| GET | `/sites/{siteId}/invoices` | `VIEW_SITE_INVOICES` | Invoices for a site |
| GET | `/invoices/{id}/pdf` | `DOWNLOAD_INVOICE_PDF` | Download the invoice as a PDF |

Invoices are created automatically when a site is registered; there is no create endpoint yet.

Response:
```json
{
  "invoiceId": "…",
  "invoiceNumber": "INV-20261002-7K3Q9A",
  "quoteId": "…",
  "siteId": "…",
  "contractId": null,
  "items": [
    { "description": "Cleaning depth: Deep", "quantity": 1, "unitPrice": 120000.00, "amount": 120000.00 },
    { "description": "Add-on: Window Cleaning", "quantity": 1, "unitPrice": 20000.00, "amount": 20000.00 }
  ],
  "amountDue": 140000.00,
  "issueDate": "2026-10-02",
  "dueDate": "2026-10-16",
  "status": "PENDING"
}
```

### 8.6 Payments
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/invoices/{invoiceId}/payments` | `PAY_INVOICE` | Pay an invoice by mobile money through the payment service |
| POST | `/invoices/{invoiceId}/payments/cash` | `RECORD_CASH_PAYMENT` | Record cash already received (saved as SUCCESS immediately) |
| GET | `/invoices/{invoiceId}/payments` | `VIEW_INVOICE_PAYMENTS` | Payments for an invoice |
| GET | `/payments` | `VIEW_PAYMENT` | List payments |
| GET | `/payments/{id}` | `VIEW_PAYMENT_BY_ID` | Get a payment (poll this for the result) |
| GET | `/payments/{id}/receipt` | `DOWNLOAD_PAYMENT_RECEIPT` | Download the PDF receipt of a SUCCESS payment |

Request body:
```json
{
  "method": "MOBILE_MONEY",
  "payerPhone": "0712345678",
  "payerName": "Jane Doe",
  "payerEmail": "jane@example.com",
  "amount": 140000.00
}
```
- `method` decides what the client does next:

| `method` | Sent to the payment service as | What to show the client |
|---|---|---|
| `MOBILE_MONEY` | `MOBILE_MONEY` | "Confirm the payment prompt on your phone" (PIN prompt on `payerPhone`) |
| `CARD` | `CHECKOUT` | Open `paymentUrl` (hosted page; card payments there are USD-only, so TZS card payments go through checkout) |
| `CHECKOUT` | `CHECKOUT` | Open `paymentUrl`; the client chooses how to pay |
| `BILLPAY` | `BILLPAY` | "Pay control number `controlNumber` via mobile money, bank or agent" |
| `BANK` | `BILLPAY` | Same control number, paid at the bank |
| `CASH` | (not sent) | Use the cash endpoint below |
| `PAYPAL`, `STRIPE`, `OTHER` | (not sent) | Rejected: not supported by the payment service |

- `payerPhone` is required for **every** method (the payment service requires it); format `07XXXXXXXX`, `2557XXXXXXXX` or `+255 …`.
- With provider `SELCOM`, only `MOBILE_MONEY` and `CHECKOUT`/`CARD` work, and `payerEmail` is required; `BILLPAY`/`BANK` need ClickPesa.
- `amount` is optional (defaults to the remaining balance).
- `payerName` is optional (defaults to "KSC invoice <number>"). `payerEmail` is optional, but required if the provider is SELCOM.

Cash request body (`POST /invoices/{invoiceId}/payments/cash`):
```json
{
  "amount": 140000.00,
  "receiptNumber": "RCPT-004512",
  "receivedAt": "2026-10-02T09:30:00",
  "notes": "Paid to site supervisor"
}
```
`receiptNumber` is required; `amount` defaults to the remaining balance; `receivedAt` defaults to now and cannot be in the future.

Response (immediately after the request; the status changes when the payment service replies):
```json
{
  "paymentId": "…",
  "invoiceId": "…",
  "invoiceNumber": "INV-20261002-7K3Q9A",
  "amount": 140000.00,
  "currency": "TZS",
  "method": "BANK",
  "payerPhone": "0712345678",
  "status": "PENDING",
  "providerRef": null,
  "failureReason": null,
  "notes": null,
  "receiptNumber": null,
  "paymentReference": "PAYS5XADEP6HELUDUY47",
  "paymentUrl": null,
  "controlNumber": "55042914871931",
  "requestedAt": "2026-10-02T10:00:00",
  "paidAt": null
}
```
`paymentUrl` is set for `CARD`/`CHECKOUT`, `controlNumber` for `BILLPAY`/`BANK`; `paymentReference` is the payment service's reference to quote to support. `receiptNumber` is set once the payment succeeds.

The invoice response also includes `amountPaid` and `balanceDue`.

### 8.7 Contracts
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/contracts` | `SAVE_CONTRACT` | Create a DRAFT contract from an ACCEPTED quote |
| GET | `/contracts/pagination` | `VIEW_CONTRACTS` | Paginated list; filters `status`, `serviceLine`, `clientId`; `sortBy` createdAt, startDate, endDate, contractValue, status, contractNumber |
| GET | `/contracts` | `VIEW_CONTRACT` | List contracts |
| GET | `/contracts/expiring?days=30` | `VIEW_EXPIRING_CONTRACTS` | ACTIVE contracts ending within `days` (0–365), soonest first |
| GET | `/contracts/{id}` | `VIEW_CONTRACT_BY_ID` | Get a contract |
| PUT | `/contracts/{id}` | `UPDATE_CONTRACT` | Update a DRAFT contract |
| DELETE | `/contracts/{id}` | `DELETE_CONTRACT` | Soft delete a DRAFT or REJECTED contract (creator only) |
| POST | `/contracts/{id}/send` | `SEND_CONTRACT` | DRAFT → SENT |
| POST | `/contracts/{id}/approve` | `APPROVE_CONTRACT` | SENT → SIGNED (or ACTIVE): record the client's signature |
| POST | `/contracts/{id}/reject` | `REJECT_CONTRACT` | SENT → REJECTED, with a reason |
| POST | `/contracts/{id}/terminate` | `TERMINATE_CONTRACT` | SIGNED/ACTIVE → TERMINATED, with a reason |
| GET | `/contracts/{id}/pdf` | `DOWNLOAD_CONTRACT_PDF` | Download the contract as a PDF |
| GET | `/contracts/{id}/invoices` | `VIEW_CONTRACT_INVOICES` | Invoices linked to the contract, newest first (same fields as the invoice API) |
| GET | `/contracts/{id}/subscriptions` | `VIEW_CONTRACT_SUBSCRIPTIONS` | Subscriptions under the contract, newest first (the contract is not repeated in each item) |
| GET | `/contracts/{id}/payments` | `VIEW_CONTRACT_PAYMENTS` | Payments (mobile money and cash) for any of the contract's invoices, newest first |

Create / update body:
```json
{
  "quoteId": "<ACCEPTED quote uuid>",
  "clientId": "<optional; defaults to the site owner>",
  "startDate": "2026-10-15",
  "endDate": "2027-10-14",
  "frequency": "MONTHLY",
  "contractValue": 2040000.00,
  "businessInfo": "Mikocheni Apartments Ltd",
  "businessTin": "123-456-789",
  "businessBrelaNo": "BRELA-154872",
  "personalIdNo": null,
  "terms": null
}
```
`startDate` and `endDate` are required (end on or after start). `frequency` defaults to the quote's frequency (or ONCE) and `contractValue` to the quote total. `quoteId` is ignored on update. `terms` is printed on the PDF; default terms are used when it is empty.

Approve (sign) body: `{ "signedBy": "Jane Doe", "signedAt": "2026-10-10T14:30:00" }` (`signedAt` optional, defaults to now, cannot be in the future).
Reject / terminate body: `{ "reason": "Client moved to another provider" }`.

### 8.8 Subscriptions
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/subscriptions` | `SAVE_SUBSCRIPTION` | Create a subscription under a contract |
| GET | `/subscriptions` | `VIEW_SUBSCRIPTIONS` | List |
| GET | `/subscriptions/{subscriptionId}` | `VIEW_SUBSCRIPTION_BY_ID` | Get one |
| PUT | `/subscriptions/{subscriptionId}` | `UPDATE_SUBSCRIPTION` | Update (not implemented) |
| POST | `/subscriptions/{subscriptionId}/cancel` | `CANCEL_SUBSCRIPTION` | → CANCELLED |
| POST | `/subscriptions/{subscriptionId}/pause` | `PAUSE_SUBSCRIPTION` | → PAUSED |
| POST | `/subscriptions/{subscriptionId}/resume` | `RESUME_SUBSCRIPTION` | → RESUMED |
| POST | `/subscriptions/{subscriptionId}/renew` | `RENEW_SUBSCRIPTION` | → RENEWED |

### 8.9 Jobs
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/jobs` | `CREATE_JOB` | Create a job |
| GET | `/jobs` | `VIEW_ALL_JOBS` | List jobs |
| GET | `/jobs/{jobId}` | `VIEW_JOB_BY_ID` | Get a job |
| PUT | `/jobs/{jobId}` | `UPDATE_JOB_BY_ID` | Update a job |

#### Crews
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/crews` | `CREATE_CREW` | Create a crew |
| GET | `/crews` | `VIEW_ALL_CREWS` | List crews |
| GET | `/crews/{crewId}` | `VIEW_CREW_BY_ID` | Get a crew |
| PUT | `/crews/{crewId}` | `UPDATE_CREW_BY_ID` | Update a crew |
| DELETE | `/crews/{crewId}` | `DELETE_CREW_BY_ID` | Delete a crew |
| POST | `/crews/{crewId}/supervisor/{supervisorId}` | `ASSIGN_SUPERVISOR_TO_CREW` | Assign supervisor |
| POST | `/crews/{crewId}/members` | `ADD_CREW_MEMBER` | Add a staff member to a crew: `{ "staffId", "staffName", "memberRole", "joinedOn" }` |
| GET | `/crews/{crewId}/members` | `VIEW_CREW_MEMBERS` | List a crew's members |
| GET | `/staff/{staffId}/crews` | `VIEW_CREW_MEMBERS` | Crews a staff member belongs to |
| DELETE | `/crews/{crewId}/members/{staffId}` | `REMOVE_CREW_MEMBER` | Remove a staff member from a crew (soft delete) |

#### Checklists
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/jobs/{jobId}/checklists` | `CREATE_CHECKLIST` | Create a checklist for a job |
| GET | `/checklist-items/{checklistItemId}` | `VIEW_CHECKLIST_ITEM_BY_ID` | Get checklist item |
| PUT | `/checklist-items/{checklistItemId}` | `UPDATE_CHECKLIST_ITEM_BY_ID` | Update checklist item |
| DELETE | `/checklist-items/{checklistItemId}` | `DELETE_CHECKLIST_ITEM_BY_ID` | Delete checklist item |

Other checklist operations (list/get/update/delete checklist, create/list checklist items) exist as methods but have **no HTTP mapping** yet.

#### Time tracking
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/jobs/{jobId}/clock-in` | `CLOCK_IN` | Clock in (geofenced) |
| POST | `/jobs/{jobId}/clock-out` | `CLOCK_OUT` | Clock out (geofenced) |
| GET | `/jobs/{jobId}/time-logs` | `VIEW_TIME_LOGS` | Time logs for a job (not implemented) |
| GET | `/time-logs/{id}` | `VIEW_TIME_LOG_BY_ID` | Get a time log (not implemented) |
| PUT | `/time-logs/{id}` | `UPDATE_TIME_LOG` | Update a time log (not implemented) |
| GET | `/staff/{staffId}/attendance` | `VIEW_STAFF_ATTENDANCE` | Staff attendance (not implemented) |

#### Feedback
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/feedbacks` | `ADD_FEEDBACK` | Submit feedback |
| GET | `/feedbacks` | `VIEW_ALL_FEEDBACK` | List feedback |
| GET | `/feedbacks/{feedbackId}` | `VIEW_FEEDBACK_BY_ID` | Get feedback |
| PUT | `/feedbacks/{feedbackId}` | `UPDATE_FEEDBACK` | Update feedback |
| DELETE | `/feedbacks/{feedbackId}` | `DELETE_FEEDBACK` | Delete feedback |
| GET | `/jobs/{jobId}/feedbacks` | `VIEW_FEEDBACK_BY_JOB_ID` | Feedback for a job |

#### Staff ratings
| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/staff-ratings` | `ADD_STAFF_RATING` | Add a rating |
| GET | `/staff-ratings` | `VIEW_ALL_STAFF_RATINGS` | List ratings |
| GET | `/staff-ratings/{id}` | `VIEW_STAFF_RATING_BY_ID` | Get a rating |
| PUT | `/staff-ratings/{id}` | `UPDATE_STAFF_RATING` | Update a rating |
| DELETE | `/staff-ratings/{id}` | `DELETE_STAFF_RATING` | Delete a rating |
| GET | `/staff/{staffId}/ratings` | `VIEW_STAFF_RATINGS` | Ratings for a staff member |
| GET | `/staff/{staffId}/rating-summary` | `VIEW_STAFF_RATING_SUMMARY` | Rating summary for a staff member |
| GET | `/jobs/{jobId}/staff-ratings` | `VIEW_JOB_STAFF_RATINGS` | Ratings for a job |
| GET | `/jobs/{jobId}/staff-ratings/summary` | `VIEW_JOB_STAFF_RATING_SUMMARY` | Rating summary for a job |

### 8.10 File management (`/api/file-upload`)
| Method | Path | Description |
|---|---|---|
| POST | `/uploads` | Upload a multipart file |
| POST | `/uploads/base64/multipart` | Upload base64 content (multipart form) |
| POST | `/uploads/base64` | Upload base64 content |
| GET | `/download/file` | Download a file |
| GET | `/download/base64/{id}` | Download as base64 by upload ID |
| GET | `/download/base64/names/{fileName}` | Download as base64 by file name |
| GET | `/download/names/{fileName}` | Download by file name |

### 8.11 Dashboards
| Method | Path | Permission | Description |
|---|---|---|---|
| GET | `/dashboard/admin?from=2026-09-04&to=2026-10-03` | `VIEW_ADMIN_DASHBOARD` | Super admin dashboard; `from`/`to` optional (default last 30 days, max 366 days) |
| GET | `/dashboard/client` | `VIEW_CLIENT_DASHBOARD` | Dashboard of the signed-in client |
| GET | `/dashboard/clients/{clientId}` | `VIEW_ADMIN_DASHBOARD` | Any client's dashboard, for super admins |
| GET | `/dashboard/staff` | `VIEW_STAFF_DASHBOARD` | Dashboard of the signed-in staff member |
| GET | `/dashboard/staff/{staffId}` | `VIEW_ADMIN_DASHBOARD` | Any staff member's dashboard, for super admins |
| GET | `/dashboard/supervisor?from=&to=` | `VIEW_SUPERVISOR_DASHBOARD` | Dashboard of the signed-in supervisor; period optional (default last 30 days) |
| GET | `/dashboard/supervisors/{supervisorId}?from=&to=` | `VIEW_ADMIN_DASHBOARD` | Any supervisor's dashboard, for super admins |

The response `data` is the JSON returned by the database function; see [Dashboards](#913-dashboards).

### 8.12 Not yet exposed
`ReportController` and `JobPhotoController` exist but define no endpoints.

---

## 9. Business rules

### 9.1 Site pricing

Implemented in `SiteServiceImpl.applyPricing` using `PricingItemResolver`:

1. `cleaningDepthId` depends on the site's **service**:
   - **Cleaning** (any service whose name maps to a `*CLEANING` type): **required** ("Cleaning depth must be provided for Cleaning sites").
   - **Fumigation, Property Management** and other services: **optional**. If given, it is priced as usual.
   - When given, it must be a valid UUID of an existing cleaning depth with status `ACTIVE`.
2. `addOnIds` is **optional** (may be empty). Every ID must be a valid UUID of an existing add-on with status `ACTIVE`; duplicate IDs are collapsed. If any ID is missing the whole request fails ("One or more add-ons not found").
3. **Total price** is calculated on the server:

   ```
   totalPrice = (service.price, or 0 if none) + (cleaningDepth.price, or 0 if none) + Σ addOn.price
   ```

   Examples: Cleaning site, Deep (120,000) + Window Cleaning (20,000) = **140,000 TZS**. Fumigation site (service price 150,000) + Window Cleaning (20,000) = **170,000 TZS**.
4. `totalPrice` is **stored** on the site when it is created or updated. Later changes to cleaning-depth or add-on prices do **not** change existing sites until the site itself is updated.
5. On update, `addOnIds` **replaces** the site's existing add-ons.
6. `totalPrice` is read-only. Clients cannot set it.

### 9.2 Master data
- Cleaning depth and add-on **names are unique** (case-insensitive) among non-deleted records; prices must be **≥ 0**.
- New services, slots, cleaning depths and add-ons start as `ACTIVE`.
- `INACTIVE` cleaning depths and add-ons cannot be selected for sites.
- Slot `endDateTime` must be strictly after `startDateTime`.
- Status change accepts `ACTIVE` or `INACTIVE` (case-insensitive); anything else returns "Invalid status value".

### 9.3 Leads
- Status can be changed directly via `PUT /leads/{leadId}/status` to any `LeadStatus` value.

### 9.4 Quotes
- A quote requires an existing site.
- `send` sets status `SENT`; `accept` sets status `ACCEPTED`. An accepted quote locks the site's pricing (see [9.9](#99-quotation-and-invoice-generation)).

### 9.5 Contracts
**Lifecycle** (`ContractStatus`):

```
DRAFT ──send──► SENT ──approve──► SIGNED ──start date──► ACTIVE ──end date──► EXPIRED
                  │                   │                     │
                  └──reject──► REJECTED                     │
                                      └─────terminate───────┴──► TERMINATED
```

| Action | Allowed from | Result |
|---|---|---|
| Create | quote is **ACCEPTED** and has no live contract | DRAFT, signature PENDING, number `CT-yyyyMMdd-XXXXXX` |
| Update | DRAFT | dates, frequency, value, client and business details, terms |
| Delete | DRAFT, REJECTED (creator only) | soft delete; the quote can get a new contract |
| Send | DRAFT | SENT, `sentAt` |
| Approve (sign) | SENT, end date not passed | signature SIGNED, `signedBy`/`signedAt`; status **ACTIVE** if the start date has arrived, otherwise **SIGNED** |
| Reject | SENT | REJECTED, signature REJECTED, `rejectionReason` |
| Terminate | SIGNED, ACTIVE | TERMINATED, `terminationReason` |

Any other transition is rejected with a message naming the current and allowed statuses.

**On create**, the contract takes its site and service type from the quote, `clientId` defaults to the site owner, `frequency` to the quote's frequency (or ONCE), and `contractValue` to the quote total. The quote's non-cancelled invoices that have no contract yet are **linked to the new contract** (`invoices.contract_id`). The quote was already ACCEPTED, so the site's pricing is locked (see 9.9).

**Daily lifecycle job** (`ContractLifecycleJob`, cron `ksc.contract.lifecycle-cron`, default 00:15 every day, plus one run about a minute after startup):
- SIGNED contracts whose start date has arrived become **ACTIVE** (or **EXPIRED** if their end date has also passed).
- ACTIVE contracts whose end date has passed become **EXPIRED**.

**Contract PDF** (`GET /contracts/{id}/pdf`, `ContractPdfRenderer`, PDFBox): company header with contract number, date and status; service provider and client details (business info, TIN, BRELA, ID number, site); contract details (service, period, frequency, value, quotation); the quote's line items as scope and pricing; terms (the contract's own, or default placeholder terms); and signature blocks, showing who signed for the client once signed. Generated on request; `pdf_url` is optional and not used by KSC.

### 9.6 Subscriptions
- A subscription requires a contract.
- Lifecycle actions set status directly: cancel → `CANCELLED`, pause → `PAUSED`, resume → `RESUMED`, renew → `RENEWED`.

### 9.7 Jobs
- Creating a job requires a contract, subscription, site and crew.

### 9.8 Clock-in / clock-out geofence
Implemented in `TimeLogServiceImpl`:
- The request must include the user's coordinates (`latitude,longitude`), and the job's site must have coordinates configured.
- The distance between the user and the site is calculated (great-circle distance in metres).
- If the distance is **more than 10 metres**, clock-in/out is rejected ("You are out of the job site geofence zone…").
- Otherwise the time log is saved with status `IN_SITE`.

---

### 9.9 Quotation and invoice generation
When a site is registered (`POST /sites`), `SiteServiceImpl.addSite` does the following in **one transaction**. If any step fails, nothing is saved.

1. Saves the site with its calculated `totalPrice` (see [Site pricing](#91-site-pricing)).
2. **Generates a quotation** (`QuoteService.generateForSite`). Skipped entirely, with no quote or invoice, when the site has **nothing priced**: no service price, no cleaning depth and no add-ons; the response then has no quote/invoice IDs. Updating the site later to add a priced item generates them.
   - number `QT-yyyyMMdd-XXXXXX`
   - status **DRAFT**, service type from the site's service (`Cleaning` → `CLEANING`, `Fumigation` → `FUMIGATION`, `Property Management` → `PROPERTY_MANAGEMENT`; unmatched names → `OTHER`), frequency `ONCE`, area from the site
   - line items: the service's fixed price (if it has one, e.g. "Service: Fumigation"), then the cleaning depth (if any), then one per add-on (sorted by name), each quantity 1 at its current price
   - `priceMin` = `priceMax` = site `totalPrice`
   - valid for **30 days**
3. **Generates an invoice** from that quotation (`InvoiceService.generateForQuote`):
   - number `INV-yyyyMMdd-XXXXXX`
   - status **PENDING**, linked to the quote and site (no contract yet)
   - a copy of the quote's line items, `amountDue` = quote total
   - issue date today, due in **14 days**
4. Returns the site with `quoteId`, `quoteNumber`, `invoiceId` and `invoiceNumber`.

`GET /sites` and `GET /sites/{id}` also return `quoteId`, `quoteNumber`, `invoiceId` and `invoiceNumber` for each site's **current** documents: its latest quote that is not EXPIRED or REJECTED, and its latest invoice that is not CANCELLED. They are `null` when the site has none (e.g. nothing priced). The list loads them with two queries in total, not per site.

`GET /sites/{id}` additionally embeds the full documents:
- `quote`: the current quotation (same fields as the quote API, including `items`, but without repeating the site).
- `invoice`: the current invoice, including `items`, `amountDue`, `amountPaid`, `balanceDue`, dates and status.
- `payments`: **every** payment for any of the site's invoices (including superseded/cancelled ones), newest first, with the same fields as the payments API. Each carries its `invoiceNumber`. Empty list when there are none.

`quote` and `invoice` are omitted when the site has none. None of the three is included in the list.

```json
{
  "siteId": "…",
  "service": { "serviceId": "…", "serviceName": "Fumigation", "price": 150000.00, "status": "ACTIVE" },
  "totalPrice": 150000.00,
  "quoteId": "…", "quoteNumber": "QT-20261002-7K3Q9A",
  "invoiceId": "…", "invoiceNumber": "INV-20261002-M4P2XR",
  "quote": {
    "quoteId": "…", "quoteNumber": "QT-20261002-7K3Q9A", "siteId": "…",
    "serviceLine": "FUMIGATION", "status": "DRAFT", "validUntil": "2026-11-01",
    "priceMin": 150000.00, "priceMax": 150000.00,
    "items": [ { "description": "Service: Fumigation", "quantity": 1, "unitPrice": 150000.00, "amount": 150000.00 } ]
  },
  "invoice": {
    "invoiceId": "…", "invoiceNumber": "INV-20261002-M4P2XR", "quoteId": "…", "siteId": "…", "contractId": null,
    "items": [ { "description": "Service: Fumigation", "quantity": 1, "unitPrice": 150000.00, "amount": 150000.00 } ],
    "amountDue": 150000.00, "amountPaid": 0, "balanceDue": 150000.00,
    "issueDate": "2026-10-02", "dueDate": "2026-10-16", "status": "PENDING"
  },
  "payments": [
    {
      "paymentId": "…", "invoiceId": "…", "invoiceNumber": "INV-20261002-M4P2XR",
      "amount": 150000.00, "currency": "TZS", "method": "MOBILE_MONEY", "payerPhone": "0712345678",
      "status": "PROCESSING", "providerRef": null, "failureReason": null, "notes": null,
      "requestedAt": "2026-10-02T10:00:00", "paidAt": null
    }
  ]
}
```

Line items are stored copies, so later price changes don't alter existing quotes or invoices.

#### On site update (`PUT /sites/{id}`)
The update recalculates the site's line items and total from the selected cleaning depth and add-ons at **current** prices, and compares them with the site's *current quote* (its latest quote that is not EXPIRED or REJECTED):

| Situation | Result |
|---|---|
| Line items, total and service type unchanged | Site saved; quote and invoice untouched |
| Service type changed (e.g. Cleaning → Fumigation) | Treated like a pricing change: documents are regenerated (subject to the same locks below) |
| Pricing changed, current quote DRAFT or SENT, no settled invoice | Open quotes (DRAFT/SENT) → **EXPIRED**, PENDING invoices → **CANCELLED**, then a new DRAFT quote and PENDING invoice are generated with new numbers. Their IDs/numbers are returned in the response. |
| Pricing changed, current quote **ACCEPTED** | Update **rejected**: "Quotation QT-… for this site has been accepted, so its cleaning depth and add-ons can no longer be changed." |
| Pricing changed, a site invoice is neither PENDING nor CANCELLED (PARTIALLY_PAID or PAID) | Update **rejected** with a similar message naming the invoice |
| Pricing changed, a payment for the site is still in progress (PENDING/PROCESSING) | Update **rejected** until the payment service reports the result |
| Site has no quote yet (registered before this feature) | Treated as a pricing change: a quote and invoice are generated |

"Pricing changed" also covers a master-data price change: if an add-on's price was edited after the quote was issued, the next site update regenerates the documents at the new price. Superseded quotes and invoices are kept for history. Changes to non-price fields (address, access type, rooms, etc.) never regenerate documents; the PDFs always show the current site details.

### 9.10 Quotation and invoice PDFs
`GET /quotes/{id}/pdf` and `GET /invoices/{id}/pdf` return an A4 PDF (`application/pdf`, downloaded as `<number>.pdf`).

- PDFs are **generated on request** from the stored record by `BillingPdfRenderer` (Apache PDFBox), so they always match the current quote/invoice. They are not saved to disk; `pdf_url` / `pdf_local_url` stay unused.
- Layout: company header, document title and number, date, valid-until/due date, status, **Bill to** (site address, type, area, rooms, service, site reference), a line-item table (description, quantity, unit price, amount), the TZS total, and notes (validity or payment terms, quote reference).
- Long item lists continue onto further pages with the table header repeated; long descriptions wrap. Characters the built-in PDF fonts cannot print are shown as `?`.
- Manually created quotes without line items print a single service line at the upper price estimate, with a note showing the price range.
- Company details on the documents come from the `ksc.company.*` properties (see [Configuration](#13-configuration)).

### 9.11 Payment service integration
KSC does not talk to payment providers itself. It starts payments through the platform **payment service** (Eureka name `payment-service`) over REST, and receives outcomes on Kafka. The payment service's own contract is in `payment-service/docs/INTEGRATION.md`.

```
Client ──POST /invoices/{id}/payments──► KSC ──POST /api/v2/payments/{provider}──► Payment service ──► ClickPesa / Selcom
   (mobile money, card/checkout link, BillPay control number)
                                          ▲                                               │
                                          └──────────── ksc.payment.results ◄─────────────┘
```

**1. Paying an invoice** (`POST /invoices/{invoiceId}/payments`). `PaymentServiceImpl.initiatePayment` runs in three steps so the HTTP call is never inside a database transaction:

1. **Save** (transaction 1): checks the invoice is PENDING or PARTIALLY_PAID with no payment in progress, resolves the amount, and saves a `Payment` with status **PENDING**.
2. **Call the payment service** (no transaction): `POST /api/v2/payments/{provider}` via the Feign client `PaymentServiceClient`, forwarding the caller's bearer token (the platform has no service-to-service token). At the payment service a payment belongs to the **system plus reference** (`KSC` + the KSC payment ID), not to the caller: any authenticated caller can retry that reference or read its status with `GET /api/v2/payments/{provider}/{reference}?system=KSC`, and each attempt records who started it. KSC itself never reads payments back; results arrive on Kafka.
3. **Apply the answer** (transaction 2), only if the payment is still PENDING (a Kafka result that arrived first always wins):

| Payment service answer | Payment becomes |
|---|---|
| Accepted with `data.status` PROCESSING | **PROCESSING**: the client confirms the prompt on their phone (mobile money) |
| Accepted with `data.status` CREATED/PENDING | **PENDING**: waiting for the client to pay via `paymentUrl` or `controlNumber`, which are stored on the payment along with `paymentReference` |
| Accepted but `data.status` FAILED/CANCELLED | **FAILED** with the payment service's message |
| HTTP 4xx (validation, gateway rejection, duplicate) | **FAILED** with the error `message` |
| HTTP 5xx, timeout or connection error | Stays **PROCESSING**: the request may still have reached the payment service, so KSC waits for its Kafka result instead of allowing a second attempt (which could prompt the client twice) |

Request sent to the payment service:
```json
{
  "system": "KSC",
  "reference": "<KSC payment UUID>",
  "method": "MOBILE_MONEY",
  "amount": 140000.00,
  "currency": "TZS",
  "description": "Payment for invoice INV-20261002-7K3Q9A",
  "customerName": "Jane Doe",
  "customerEmail": "jane@example.com",
  "phoneNumber": "0712345678",
  "phone": "0712345678",
  "customer": { "name": "Jane Doe", "phone": "0712345678", "email": "jane@example.com" }
}
```
`reference` is the KSC payment ID; it is how results are matched back.

**Timeout sweep** (`PaymentResultTimeoutJob`): payments that are still CREATED, PENDING or PROCESSING too long after they were created are marked **FAILED**. The limit depends on the method: **mobile money** `ksc.payment.result-timeout-minutes` (default **120**); **link / control-number methods** (CARD, CHECKOUT, BILLPAY, BANK) `ksc.payment.hosted-result-timeout-minutes` (default **780**, because the payment service waits up to 12 hours for those clients to pay). They get the reason "No result from the payment service". This frees invoices (and site pricing) whose request never reached the payment service. The payment service fails unanswered phone prompts after about 30 minutes and always sends a final event for payments it knows about, so a real payment normally resolves well before the sweep. A SUCCESS that arrives after the sweep is still recorded (see late payments below). The job uses the same in-progress status set as the "payment in progress" checks, so the two cannot drift.

**2. Receiving the result** (`ksc.payment.results`, consumer group `ksc-payment-results`). Events for any system other than `KSC` are skipped.

| Event `status` | Effect in KSC |
|---|---|
| `PROCESSING` | Payment → PROCESSING; provider reference stored |
| `SUCCESS` | Payment → SUCCESS with `paidAt` and provider reference; the amount is added to the invoice's `amountPaid`, **capped at the remaining balance**. Invoice → **PAID** when fully covered, otherwise **PARTIALLY_PAID** |
| `FAILED` / `CANCELLED` | Payment → FAILED / CANCELLED with `failureReason`; invoice unchanged |
| `REFUNDED` | Currently ignored (logged) |

Safeguards:
- **Late payments:** a payment that is FAILED or CANCELLED can still become SUCCESS if the money arrives late; it is recorded. Any amount beyond what the invoice still owes (e.g. staff already took cash) is not counted and is logged at ERROR as needing a refund.
- **Redelivery:** repeated events for a settled payment (SUCCESS→SUCCESS, FAILED→FAILED/CANCELLED) are ignored.
- Results for an unknown payment, with an unknown status, or that cannot be parsed are logged and skipped.
- A SUCCESS for an invoice CANCELLED meanwhile is recorded on the payment only and logged as an error (refund needed).

Event KSC reads (fields used; others are ignored):
```json
{
  "eventId": "5c0f8e1e-6a8e-4d55-9a8f-2f8c3b1d7e21",
  "system": "KSC",
  "reference": "<KSC payment UUID>",
  "status": "SUCCESS",
  "amount": 140000,
  "currency": "TZS",
  "providerReference": "MP261001.1234.A12345",
  "method": "MOBILE_MONEY",
  "paidAt": "2026-10-02T10:15:30",
  "failureReason": null
}
```
`reference` maps to `paymentId` and `providerReference` to `providerRef` (`PaymentResultMessage`, via `@JsonAlias`).

**Cash payments** (`POST /invoices/{invoiceId}/payments/cash`) bypass the payment service:
- Same invoice checks as above (PENDING or PARTIALLY_PAID, no payment in progress, amount within the balance).
- Saved straight away as a `CASH` payment with status **SUCCESS**; the receipt number is stored in `providerRef`, plus `paidAt` (= `receivedAt`) and optional `notes`.
- The invoice's `amountPaid` and status are updated exactly as for a SUCCESS result.
- The same receipt number cannot be recorded twice for one invoice.

### 9.12 Payment receipts
`GET /payments/{id}/receipt` returns an A4 PDF receipt (`ReceiptPdfRenderer`, PDFBox), downloaded as `<receiptNumber>.pdf`.

- Only for payments with status **SUCCESS** (mobile money, card/checkout, BillPay or cash); any other status is rejected ("a receipt is only available once it has succeeded").
- **Receipt number** `RCT-yyyyMMdd-XXXXXX` is assigned when the payment succeeds: on the payment service's SUCCESS result, or immediately for cash. Payments that succeeded before receipts existed get a number on their first receipt request. The number is returned as `receiptNumber` in payment responses and never changes.
- Content: company header with receipt number, date and PAID status; **Received from** (the invoice's site); the **amount received**; payment details (paid on, method, transaction reference or cash receipt number, payment service reference, payer phone, note); and the invoice's total, **paid to date** (as of the download date), **balance remaining** and status.
- Generated on request from current data, like quotations, invoices and contracts. Nothing is stored except the receipt number.

### 9.13 Dashboards
All dashboards are computed **entirely in PostgreSQL** by database functions; `DashboardService` calls them through `JdbcTemplate` and returns their JSON unchanged (money amounts keep their exact decimals).

| Function | Used by | Returns |
|---|---|---|
| `ksc_admin_dashboard(p_from date, p_to date)` | `GET /dashboard/admin` | `jsonb` with company-wide figures |
| `ksc_client_dashboard(p_client_id text)` | `GET /dashboard/client`, `GET /dashboard/clients/{clientId}` | `jsonb` for one client |
| `ksc_staff_dashboard(p_staff_id text)` | `GET /dashboard/staff`, `GET /dashboard/staff/{staffId}` | `jsonb` for one staff member |
| `ksc_supervisor_dashboard(p_supervisor_id text, p_from date, p_to date)` | `GET /dashboard/supervisor`, `GET /dashboard/supervisors/{supervisorId}` | `jsonb` for one supervisor's crews |

**Installation.** The function source lives in `src/main/resources/db/functions/` (one `CREATE OR REPLACE FUNCTION` per file, run in file-name order). `DashboardFunctionInstaller` runs every file when the application is ready, so the database always has the version that matches the code. Disable with `ksc.dashboard.install-functions=false` (then run the files by hand, e.g. `psql -f 01_ksc_admin_dashboard.sql`). An install failure is logged and does not stop the application; the endpoints then answer "Dashboard is unavailable: database function … failed or is not installed". The database user needs permission to create functions in the schema (on PostgreSQL 15+ the `public` schema only allows this for its owner by default).

All queries ignore soft-deleted rows (`deleted_at` set). Both functions are `plpgsql`, so a missing column is reported when the function runs rather than when it is installed.

**Super admin** (`ksc_admin_dashboard`). `from`/`to` (inclusive) bound the *in-period* figures; everything else is the current position.

| Key | Contents |
|---|---|
| `period` | `from`, `to` |
| `sites` | `total`, `newInPeriod`, `byService` (`service`, `count`, `totalValue`) |
| `leads` | `total`, `newInPeriod`, `byStatus` |
| `quotes` | `byStatus` |
| `contracts` | `byStatus`, `activeCount`, `activeValue`, `expiringIn30Days` |
| `subscriptions` | `byStatus` |
| `jobs` | `byStatus`, `upcoming` (scheduled in the future, not completed/cancelled/failed) |
| `invoices` | (cancelled excluded) `count`, `totalInvoiced`, `totalPaid`, `outstanding`, `overdueCount`, `overdueAmount` (PENDING/PARTIALLY_PAID past their due date), `byStatus` (all statuses) |
| `payments` | `receivedInPeriod`, `countInPeriod` (SUCCESS by `paidAt`), `byMethod` (`method`, `count`, `amount`), `failedInPeriod`, `inProgress` |
| `revenueByMonth` | the last 12 calendar months (`month` `YYYY-MM`, `amount`), months with no payments included as 0 |
| `recentPayments` | the 10 latest successful payments (`paymentId`, `receiptNumber`, `invoiceNumber`, `amount`, `method`, `paidAt`) |

**Client** (`ksc_client_dashboard`). A client is identified by their user ID: their sites are the sites they registered (`sites.site_owner_id`), and their contracts are those with `client_id` = the client or on one of their sites. Invoices and payments are those for their sites or contracts.

| Key | Contents |
|---|---|
| `sites` | `total`, `list` (`siteId`, `addressArea`, `siteType`, `service`, `totalPrice`) |
| `contracts` | `byStatus`, `current` (DRAFT/SENT/SIGNED/ACTIVE: number, status, service, dates, value, `expiringSoon`) |
| `invoices` | (cancelled excluded) `totalInvoiced`, `totalPaid`, `outstanding`, `overdueCount`, `overdueAmount`, `open` (PENDING/PARTIALLY_PAID with `balanceDue`, `dueDate`, `overdue`) |
| `payments` | `totalPaid`, `inProgress`, `recent` (10 latest, any status, with `receiptNumber` when paid) |
| `upcomingJobs` | next 5 scheduled jobs on their sites (`scheduledStart`, `scheduledEnd`, `status`, `serviceType`, `addressArea`) |

A client with no data gets the same structure with zeros and empty lists.

**Staff** (`ksc_staff_dashboard`). A staff member is identified by their user ID. Jobs are assigned to **crews**, so their jobs are the jobs of crews they belong to (`crew_members`); attendance comes from their time logs and ratings from `staff_ratings`.

| Key | Contents |
|---|---|
| `crews` | crews they belong to (`crewName`, `zone`, `supervisorName`, `role`) |
| `today` | today's jobs of their crews (`status`, `serviceType`, times, `addressArea`, `crewName`) |
| `upcomingJobs` | next 10 open jobs from tomorrow on |
| `clockedIn` | their open time log (job, `clockIn`, `clockInStatus`, `addressArea`), or `null` |
| `attendanceLast30Days` | `daysWorked`, `clockIns`, `jobsWorked`, `hoursWorked`, `outOfSiteEvents`, `missingClockOuts` (still clocked in from a previous day) |
| `jobsByStatusLast30Days` | their crews' jobs scheduled in the last 30 days, by status |
| `ratings` | `average`, `count`, `recent` (5 latest with comment) |

**Supervisor** (`ksc_supervisor_dashboard`). A supervisor is identified by their user ID; their crews are those with `crews.supervisor_id` = them. `from`/`to` (inclusive) bound the period figures.

| Key | Contents |
|---|---|
| `crews` | their crews with `members` (`staffId`, `staffName`, `role`) |
| `jobs` | `byStatusInPeriod`, `todayCount`, `today`, `late` (end time passed but not completed/cancelled/failed), `upcoming` (next 10) |
| `attendance` | `onSiteNow` (open clock-ins on their jobs, with staff name and site), `clockInsInPeriod`, `hoursInPeriod`, `outOfSiteEventsInPeriod`, `missingClockOuts` |
| `staffPerformance` | per crew member (plus anyone else who clocked in on their jobs in the period): `jobsWorked`, `hoursWorked`, `averageRating`, `ratingsCount` |
| `feedback` | `countInPeriod`, `averageServiceRating`, `openDisputes` (OPEN / UNDER_REVIEW) |
| `checklists` | for jobs scheduled in the period: `itemsInPeriod`, `completedItems`, `completionRate` (%) |

Staff or supervisors with no data get the same structure with zeros, `null` averages and empty lists.

## 10. Enumerations

| Enum | Values |
|---|---|
| `Status` | ACTIVE, INACTIVE |
| `Currency` | TZS, USD |
| `LeadServiceType` | DEEP_CLEANING, OFFICE_CLEANING, RESIDENTIAL_CLEANING, COMMERCIAL_CLEANING, OTHER, CLEANING, FUMIGATION, PEST_CONTROL, PROPERTY_MANAGEMENT |
| `LeadSource` | WEBSITE, REFERRAL, SOCIAL_MEDIA, WALK_IN, OTHER |
| `LeadStatus` | NEW, CONTACTED, CONVERTED, LOST |
| `QuoteStatus` | DRAFT, SENT, ACCEPTED, REJECTED, EXPIRED |
| `Frequency` | ONCE, DAILY, WEEKLY, BI_WEEKLY, MONTHLY, QUARTERLY, YEARLY |
| `ContractStatus` | DRAFT, SENT, SIGNED, ACTIVE, EXPIRED, TERMINATED, REJECTED |
| `SignatureStatus` | PENDING, SIGNED, REJECTED |
| `SubscriptionStatus` | ACTIVE, PAUSED, CANCELLED, EXPIRED, RESUMED, RENEWED, PENDING, FAILED |
| `JobType` | SERVICE_JOB, ONE_TIME |
| `JobStatus` | SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, ASSIGNED, FAILED |
| `TimeLogStatus` | IN_SITE, OUT_OF_SITE |
| `DisputeStatus` | NONE, OPEN, UNDER_REVIEW, RESOLVED, REJECTED |
| `PaymentMethod` | CASH, BANK, MOBILE_MONEY, CARD, CHECKOUT, BILLPAY, PAYPAL, STRIPE, OTHER |
| `PaymentStatus` | CREATED, PENDING, PROCESSING, SUCCESS, FAILED, CANCELLED, REFUND_PENDING, REFUNDED |
| `DocumentType` | CONTRACT |
| `WorkFlowStatus` | PENDING, APPROVED, RETURNED |
| `MailEncryption` | NONE, SSL, TLS |
| `UserRoles` | CUSTOMER, DRIVER |

Enums with display names (e.g. `JobStatus.IN_PROGRESS` → "In Progress") expose them via `getDisplayName()`. API requests and the database use the constant name.

---

## 11. Security and permissions

- Authentication and authorization are provided by the internal **`afriSecurity`** library.
- `AuthDetailsExtractor.getUserId(authentication)` resolves the current user's UUID; it is stored as `created_by` / `updated_by`.
- Each endpoint declares `@Permission(name = "...", code = "...")`. The permission codes in [API reference](#8-api-reference) must be granted to a user's role for the call to succeed. Permission and audit-log tables come from `afriSecurity` (`afriSecurity.permissions.repository`, `afriSecurity.auditLogs.repositories`).
- Dashboards identify the caller by their user ID: a **client** by the sites they registered, a **staff** member by their crew memberships, a **supervisor** by `crews.supervisor_id`.

### Permissions reference

Every permission code used by the API, generated from the controllers. Grant codes to roles in `afriSecurity`; a call without the endpoint's code is refused. File-upload endpoints declare no permission.

#### Master data (24 codes)

| Code | Endpoints |
|---|---|
| `CHANGE_ADD_ON_STATUS` | `PATCH /add-ons/{id}/status` |
| `CHANGE_CLEANING_DEPTH_STATUS` | `PATCH /cleaning-depths/{id}/status` |
| `CHANGE_SERVICE_STATUS` | `PATCH /services/{serviceId}/status` |
| `CHANGE_SLOT_STATUS` | `PATCH /slots/{id}/status` |
| `DELETE_ADD_ON` | `DELETE /add-ons/{id}` |
| `DELETE_CLEANING_DEPTH` | `DELETE /cleaning-depths/{id}` |
| `DELETE_SERVICE` | `DELETE /services/{id}` |
| `DELETE_SLOT` | `DELETE /slots/{id}` |
| `SAVE_ADD_ON` | `POST /add-ons` |
| `SAVE_CLEANING_DEPTH` | `POST /cleaning-depths` |
| `SAVE_SERVICE` | `POST /services` |
| `SAVE_SLOT` | `POST /slots` |
| `UPDATE_ADD_ON` | `PUT /add-ons/{id}` |
| `UPDATE_CLEANING_DEPTH` | `PUT /cleaning-depths/{id}` |
| `UPDATE_SERVICE` | `PUT /service/{id}` |
| `UPDATE_SLOT` | `PUT /slots/{id}` |
| `VIEW_ADD_ON` | `GET /add-ons` |
| `VIEW_ADD_ON_BY_ID` | `GET /add-ons/{id}` |
| `VIEW_CLEANING_DEPTH` | `GET /cleaning-depths` |
| `VIEW_CLEANING_DEPTH_BY_ID` | `GET /cleaning-depths/{id}` |
| `VIEW_SERVICE` | `GET /services` |
| `VIEW_SERVICE_BY_ID` | `GET /services/{id}` |
| `VIEW_SLOT` | `GET /slots` |
| `VIEW_SLOT_BY_ID` | `GET /slots/{id}` |


#### Sites (5 codes)

| Code | Endpoints |
|---|---|
| `DELETE_SITE` | `DELETE /sites/{id}` |
| `SAVE_SITE` | `POST /sites` |
| `UPDATE_SITE` | `PUT /sites/{id}` |
| `VIEW_SITE` | `GET /sites` |
| `VIEW_SITE_BY_ID` | `GET /sites/{id}` |


#### Leads (5 codes)

| Code | Endpoints |
|---|---|
| `CHANGE_LEAD_STATUS` | `PUT /leads/{leadId}/status` |
| `DELETE_LEAD` | `DELETE /leads/{id}` |
| `SAVE_LEAD` | `POST /leads` |
| `UPDATE_LEAD` | `PUT /leads/{id}` |
| `VIEW_LEAD` | `GET /leads`, `GET /leads/pagination` |


#### Quotations (8 codes)

| Code | Endpoints |
|---|---|
| `ACCEPT_QUOTE` | `POST /quotes/{id}/accept` |
| `DELETE_QUOTE` | `DELETE /quotes/{id}` |
| `DOWNLOAD_QUOTE_PDF` | `GET /quotes/{id}/pdf` |
| `SAVE_QUOTE` | `POST /quotes` |
| `SEND_QUOTE` | `POST /quotes/{id}/send` |
| `UPDATE_QUOTE` | `PUT /quotes/{id}` |
| `VIEW_QUOTE` | `GET /quotes` |
| `VIEW_QUOTE_BY_ID` | `GET /quotes/{id}` |


#### Invoices and payments (10 codes)

| Code | Endpoints |
|---|---|
| `DOWNLOAD_INVOICE_PDF` | `GET /invoices/{id}/pdf` |
| `DOWNLOAD_PAYMENT_RECEIPT` | `GET /payments/{id}/receipt` |
| `PAY_INVOICE` | `POST /invoices/{invoiceId}/payments` |
| `RECORD_CASH_PAYMENT` | `POST /invoices/{invoiceId}/payments/cash` |
| `VIEW_INVOICE` | `GET /invoices` |
| `VIEW_INVOICE_BY_ID` | `GET /invoices/{id}` |
| `VIEW_INVOICE_PAYMENTS` | `GET /invoices/{invoiceId}/payments` |
| `VIEW_PAYMENT` | `GET /payments` |
| `VIEW_PAYMENT_BY_ID` | `GET /payments/{id}` |
| `VIEW_SITE_INVOICES` | `GET /sites/{siteId}/invoices` |


#### Contracts and subscriptions (23 codes)

| Code | Endpoints |
|---|---|
| `APPROVE_CONTRACT` | `POST /contracts/{id}/approve` |
| `CANCEL_SUBSCRIPTION` | `POST /subscriptions/{subscriptionId}/cancel` |
| `DELETE_CONTRACT` | `DELETE /contracts/{id}` |
| `DOWNLOAD_CONTRACT_PDF` | `GET /contracts/{id}/pdf` |
| `PAUSE_SUBSCRIPTION` | `POST /subscriptions/{subscriptionId}/pause` |
| `REJECT_CONTRACT` | `POST /contracts/{id}/reject` |
| `RENEW_SUBSCRIPTION` | `POST /subscriptions/{subscriptionId}/renew` |
| `RESUME_SUBSCRIPTION` | `POST /subscriptions/{subscriptionId}/resume` |
| `SAVE_CONTRACT` | `POST /contracts` |
| `SAVE_SUBSCRIPTION` | `POST /subscriptions` |
| `SEND_CONTRACT` | `POST /contracts/{id}/send` |
| `TERMINATE_CONTRACT` | `POST /contracts/{id}/terminate` |
| `UPDATE_CONTRACT` | `PUT /contracts/{id}` |
| `UPDATE_SUBSCRIPTION` | `PUT /subscriptions/{subscriptionId}` |
| `VIEW_CONTRACT` | `GET /contracts` |
| `VIEW_CONTRACTS` | `GET /contracts/pagination` |
| `VIEW_CONTRACT_BY_ID` | `GET /contracts/{id}` |
| `VIEW_CONTRACT_INVOICES` | `GET /contracts/{id}/invoices` |
| `VIEW_CONTRACT_PAYMENTS` | `GET /contracts/{id}/payments` |
| `VIEW_CONTRACT_SUBSCRIPTIONS` | `GET /contracts/{id}/subscriptions` |
| `VIEW_EXPIRING_CONTRACTS` | `GET /contracts/expiring` |
| `VIEW_SUBSCRIPTIONS` | `GET /subscriptions` |
| `VIEW_SUBSCRIPTION_BY_ID` | `GET /subscriptions/{subscriptionId}` |


#### Jobs, crews and field work (38 codes)

| Code | Endpoints |
|---|---|
| `ADD_CREW_MEMBER` | `POST /crews/{crewId}/members` |
| `ADD_FEEDBACK` | `POST /feedbacks` |
| `ADD_STAFF_RATING` | `POST /staff-ratings` |
| `ASSIGN_SUPERVISOR_TO_CREW` | `POST /crews/{crewId}/supervisor/{supervisorId}` |
| `CLOCK_IN` | `POST /jobs/{jobId}/clock-in` |
| `CLOCK_OUT` | `POST /jobs/{jobId}/clock-out` |
| `CREATE_CHECKLIST` | `POST /jobs/{jobId}/checklists` |
| `CREATE_CREW` | `POST /crews` |
| `CREATE_JOB` | `POST /jobs` |
| `DELETE_CHECKLIST_ITEM_BY_ID` | `DELETE /checklist-items/{checklistItemId}` |
| `DELETE_CREW_BY_ID` | `DELETE /crews/{crewId}` |
| `DELETE_FEEDBACK` | `DELETE /feedbacks/{feedbackId}` |
| `DELETE_STAFF_RATING` | `DELETE /staff-ratings/{id}` |
| `REMOVE_CREW_MEMBER` | `DELETE /crews/{crewId}/members/{staffId}` |
| `UPDATE_CHECKLIST_ITEM_BY_ID` | `PUT /checklist-items/{checklistItemId}` |
| `UPDATE_CREW_BY_ID` | `PUT /crews/{crewId}` |
| `UPDATE_FEEDBACK` | `PUT /feedbacks/{feedbackId}` |
| `UPDATE_JOB_BY_ID` | `PUT /jobs/{jobId}` |
| `UPDATE_STAFF_RATING` | `PUT /staff-ratings/{id}` |
| `UPDATE_TIME_LOG` | `PUT /time-logs/{id}` |
| `VIEW_ALL_CREWS` | `GET /crews` |
| `VIEW_ALL_FEEDBACK` | `GET /feedbacks` |
| `VIEW_ALL_JOBS` | `GET /jobs` |
| `VIEW_ALL_STAFF_RATINGS` | `GET /staff-ratings` |
| `VIEW_CHECKLIST_ITEM_BY_ID` | `GET /checklist-items/{checklistItemId}` |
| `VIEW_CREW_BY_ID` | `GET /crews/{crewId}` |
| `VIEW_CREW_MEMBERS` | `GET /crews/{crewId}/members`, `GET /staff/{staffId}/crews` |
| `VIEW_FEEDBACK_BY_ID` | `GET /feedbacks/{feedbackId}` |
| `VIEW_FEEDBACK_BY_JOB_ID` | `GET /jobs/{jobId}/feedbacks` |
| `VIEW_JOB_BY_ID` | `GET /jobs/{jobId}` |
| `VIEW_JOB_STAFF_RATINGS` | `GET /jobs/{jobId}/staff-ratings` |
| `VIEW_JOB_STAFF_RATING_SUMMARY` | `GET /jobs/{jobId}/staff-ratings/summary` |
| `VIEW_STAFF_ATTENDANCE` | `GET /staff/{staffId}/attendance` |
| `VIEW_STAFF_RATINGS` | `GET /staff/{staffId}/ratings` |
| `VIEW_STAFF_RATING_BY_ID` | `GET /staff-ratings/{id}` |
| `VIEW_STAFF_RATING_SUMMARY` | `GET /staff/{staffId}/rating-summary` |
| `VIEW_TIME_LOGS` | `GET /jobs/{jobId}/time-logs` |
| `VIEW_TIME_LOG_BY_ID` | `GET /time-logs/{id}` |


#### Dashboards (4 codes)

| Code | Endpoints |
|---|---|
| `VIEW_ADMIN_DASHBOARD` | `GET /dashboard/admin`, `GET /dashboard/clients/{clientId}`, `GET /dashboard/staff/{staffId}`, `GET /dashboard/supervisors/{supervisorId}` |
| `VIEW_CLIENT_DASHBOARD` | `GET /dashboard/client` |
| `VIEW_STAFF_DASHBOARD` | `GET /dashboard/staff` |
| `VIEW_SUPERVISOR_DASHBOARD` | `GET /dashboard/supervisor` |


### Suggested grants for dashboard users

A starting point for the four dashboard personas; adjust to your organisation.

| Role | Dashboard | Typical additional codes |
|---|---|---|
| Super admin | `VIEW_ADMIN_DASHBOARD` (also opens any client, staff or supervisor dashboard) | all codes |
| Supervisor | `VIEW_SUPERVISOR_DASHBOARD` | `VIEW_CREW_MEMBERS`, `ADD_CREW_MEMBER`, `REMOVE_CREW_MEMBER`, `VIEW_ALL_JOBS`, `VIEW_JOB_BY_ID`, `UPDATE_JOB_BY_ID`, `VIEW_TIME_LOGS`, `VIEW_STAFF_RATINGS`, `VIEW_STAFF_RATING_SUMMARY` |
| Staff | `VIEW_STAFF_DASHBOARD` | `CLOCK_IN`, `CLOCK_OUT`, `VIEW_JOB_BY_ID` |
| Client | `VIEW_CLIENT_DASHBOARD` | `SAVE_SITE`, `VIEW_SITE_BY_ID`, `DOWNLOAD_QUOTE_PDF`, `DOWNLOAD_INVOICE_PDF`, `PAY_INVOICE`, `VIEW_PAYMENT_BY_ID`, `DOWNLOAD_PAYMENT_RECEIPT`, `DOWNLOAD_CONTRACT_PDF` |

Note: endpoints such as `GET /sites/{id}` or `GET /invoices/{id}` check the permission code only, not whether the record belongs to the caller. Only the dashboards scope data to the signed-in user, so grant record-level codes to clients with that in mind.

---

## 12. Seed data

Seeders in `masterData/seeders` run at application startup (`CommandLineRunner`). Each record is inserted **only if no record with the same name exists** (case-insensitive), so restarts are safe. Editing a value in a seeder does **not** update a row that already exists; use the API instead.

| Seeder | Records |
|---|---|
| `ServiceSeeder` | Cleaning (no fixed price), Fumigation (150,000), Property Management (200,000). If a service already exists **without** a price, the seed price is filled in once; a price set through the API is never overwritten |
| `CleaningDepthSeeder` | Shallow (50,000), Medium (80,000), Deep (120,000) |
| `AddOnSeeder` | Window Cleaning (20,000), Carpet Cleaning (35,000), Sofa Cleaning (30,000), Fridge Cleaning (15,000), Oven Cleaning (15,000) |

Prices are in TZS. The cleaning-depth and add-on prices and the add-on list are **placeholders** to be replaced with real business values.

---

## 13. Configuration

`src/main/resources/application.properties` holds bootstrap settings. Everything else (datasource, file paths, etc.) is expected from the **Spring Cloud Config Server**.

| Property | Default / source | Purpose |
|---|---|---|
| `spring.application.name` | `ksc` | Service name (Eureka, config server) |
| `spring.config.import` | `optional:configserver:${CONFIG_SERVER_URI:http://localhost:7000/}` | Config server location |
| `spring.profiles.active` | `${PROFILE:default}` | Active profile |
| `spring.redis.host` / `port` / `password` | application.properties | Redis connection |
| `spring.kafka.bootstrap-servers` | application.properties | Kafka brokers |
| `spring.datasource.*` | config server | PostgreSQL connection |
| `file.upload-dir` | config server | Root folder for uploaded files |
| `file.upload.paths.<DOCUMENT_TYPE>` | config server | Sub-folder per document type |
| `ksc.company.name` | `KSC Limited` | Company name on quotation/invoice PDFs |
| `ksc.company.tagline` | `Cleaning, Fumigation & Property Management` | Tagline under the company name |
| `ksc.company.address` | `Dar es Salaam, Tanzania` | Address on PDFs |
| `ksc.company.email` | `ksc@gmail.com` | Contact email on PDFs |
| `ksc.company.phone` | empty (hidden) | Contact phone on PDFs |
| `ksc.payment.provider` | `CLICKPESA` | Provider used for payments (`CLICKPESA` or `SELCOM`; Selcom needs the payer's email and whole TZS amounts) |
| `ksc.payment.system` | `KSC` | KSC's system code at the payment service |
| `ksc.payment.result-timeout-minutes` | `120` | Minutes without a result before an in-progress **mobile money** payment is marked FAILED |
| `ksc.payment.hosted-result-timeout-minutes` | `780` | Same, for CARD / CHECKOUT / BILLPAY / BANK payments (the client pays later via a link or control number) |
| `ksc.payment.timeout-check-interval-ms` | `600000` (10 min) | How often the timeout sweep runs |
| `ksc.payment.timeout-check-initial-delay-ms` | `120000` (2 min) | Delay before the first sweep after startup |
| `ksc.contract.lifecycle-cron` | `0 15 0 * * *` (00:15 daily) | When the contract lifecycle job runs |
| `ksc.contract.lifecycle-initial-delay-ms` | `60000` (1 min) | Delay before the one-off lifecycle run after startup |
| `ksc.dashboard.install-functions` | `true` | Install/refresh the dashboard database functions at startup |

### Environment variables

| Variable | Default | Purpose |
|---|---|---|
| `CONFIG_SERVER_URI` | `http://localhost:7000/` | Config server URL |
| `PROFILE` | `default` | Spring profile |
| `JAVA_OPTS` | empty | JVM options (Docker) |

---

## 14. Building and running

### Requirements
- **JDK 17 or 21.** The project currently uses **Lombok 1.18.34, which does not work on JDK 25+** (the build fails with hundreds of "cannot find symbol" errors for getters/setters). Either build with JDK 17/21 or upgrade Lombok to 1.18.40 or later.
- Maven (wrapper `./mvnw` included)
- Access to the `afriSecurity` and `afriUtils` artifacts (internal Maven repository)
- Running PostgreSQL, Redis, Kafka, Config Server and Eureka

### Build
```bash
export JAVA_HOME=<path to JDK 17 or 21>
./mvnw clean package
```

### Run the tests
```bash
export JAVA_HOME=<path to JDK 17 or 21>
./mvnw test -Dtest='PricingItemResolverTest,ContractServiceImplTest,PaymentServiceImplTest,PaymentResultListenerTest,PaymentResultTimeoutJobTest,DashboardServiceTest'
```

| Test class | Covers |
|---|---|
| `PricingItemResolverTest` | Cleaning-depth rules per service, service fixed price, line items and totals |
| `ContractServiceImplTest` | Create from accepted quote, lifecycle transitions, daily activate/expire, validation |
| `PaymentServiceImplTest` | Starting payments via the payment service, Kafka results, late SUCCESS with balance cap, cash |
| `PaymentResultListenerTest` | Parsing payment-service events, skipping other systems and bad messages |
| `PaymentResultTimeoutJobTest` | Failing payments with no result after the timeout (per method) |
| `DashboardServiceTest` | All four dashboards: parameters, period defaults and limits, exact amounts, missing-function error |

These are plain unit tests (Mockito) and need no database, Kafka or config server. `KscApplicationTests.contextLoads` starts the whole application, so a plain `./mvnw test` fails unless the config server and database are reachable.

### Run locally
```bash
CONFIG_SERVER_URI=http://localhost:7000/ PROFILE=default ./mvnw spring-boot:run
```

### Docker
The `Dockerfile` uses `eclipse-temurin:17-jdk` and runs the packaged jar:
```bash
./mvnw clean package
docker build -t ksc .
docker run -e CONFIG_SERVER_URI=http://config:7000/ -e PROFILE=prod ksc
```

### Health and metrics
Spring Boot Actuator is enabled. Prometheus metrics are exposed through Micrometer, and traces are sent via Zipkin (Brave) when configured.

---

## 15. Database setup

`spring.jpa.hibernate.ddl-auto` is not set in this repository, and there is no Flyway/Liquibase. The schema is managed outside the codebase, or by Hibernate through the config-server properties (`afri-config-repo`).

### Relaxed NOT NULL constraints (run by hand)

If the database is maintained by `ddl-auto=update`, Hibernate **adds** new tables and columns automatically but **never removes or relaxes** a constraint that already exists. Columns that used to be required and are now optional keep their `NOT NULL`, and inserts fail with an error such as:

```
Data integrity violation: ERROR: null value in column "contract_id" of relation "invoices" violates not-null constraint
```

Run these once against the KSC database (they change no data, only allow empty values):

```sql
-- Invoices are created from quotes; the contract is attached later
ALTER TABLE invoices ALTER COLUMN contract_id DROP NOT NULL;

-- Fumigation / Property Management sites may have no cleaning depth
ALTER TABLE sites ALTER COLUMN cleaning_depth_id DROP NOT NULL;

-- Contracts: the PDF is generated on request, so pdf_url is optional
ALTER TABLE contracts ALTER COLUMN pdf_url DROP NOT NULL;

-- Contracts: one live contract per quote is enforced in code, so a deleted DRAFT
-- contract's quote can get a new one. Drop the old unique key on quote_id, if any.
DO $$
DECLARE c record;
BEGIN
  FOR c IN SELECT con.conname
           FROM pg_constraint con
           JOIN pg_attribute att ON att.attrelid = con.conrelid AND att.attnum = ANY (con.conkey)
           WHERE con.conrelid = 'contracts'::regclass AND con.contype = 'u'
             AND att.attname = 'quote_id' AND array_length(con.conkey, 1) = 1
  LOOP
    EXECUTE format('ALTER TABLE contracts DROP CONSTRAINT %I', c.conname);
  END LOOP;
END $$;

-- Slots no longer belong to a site (only if that column was ever created)
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_name = 'slots' AND column_name = 'site_id') THEN
    ALTER TABLE slots ALTER COLUMN site_id DROP NOT NULL;
  END IF;
END $$;
```

| Column | Symptom if not relaxed |
|---|---|
| `invoices.contract_id` | Creating a site fails (its invoice has no contract yet) |
| `sites.cleaning_depth_id` | Creating a Fumigation / Property Management site without a cleaning depth fails |
| `slots.site_id` | Creating a slot fails (slots no longer link to a site) |
| `contracts.pdf_url` | Creating a contract fails (no PDF URL is supplied any more) |
| unique key on `contracts.quote_id` | Re-creating a contract for a quote whose DRAFT contract was deleted fails |

When a future change makes a required column optional, add its `DROP NOT NULL` here.

### Full schema for these features

When the schema is managed by hand, ensure these objects exist:

```sql
CREATE TABLE services (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_name varchar(255) NOT NULL,
    status varchar(50) NOT NULL,
    created_by uuid, created_at timestamp, updated_by uuid, updated_at timestamp,
    deleted_at timestamp, deleted boolean DEFAULT false, active boolean DEFAULT true
);

CREATE TABLE slots (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    start_date_time timestamp NOT NULL,
    end_date_time timestamp NOT NULL,
    status varchar(50) NOT NULL,
    created_by uuid, created_at timestamp, updated_by uuid, updated_at timestamp,
    deleted_at timestamp, deleted boolean DEFAULT false, active boolean DEFAULT true
);

CREATE TABLE cleaning_depths (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    name varchar(255) NOT NULL,
    price numeric(15,2) NOT NULL,
    description varchar(255),
    status varchar(50) NOT NULL,
    created_by uuid, created_at timestamp, updated_by uuid, updated_at timestamp,
    deleted_at timestamp, deleted boolean DEFAULT false, active boolean DEFAULT true
);

CREATE TABLE add_ons (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    name varchar(255) NOT NULL,
    price numeric(15,2) NOT NULL,
    description varchar(255),
    status varchar(50) NOT NULL,
    created_by uuid, created_at timestamp, updated_by uuid, updated_at timestamp,
    deleted_at timestamp, deleted boolean DEFAULT false, active boolean DEFAULT true
);

-- Site pricing
ALTER TABLE services ADD COLUMN price numeric(15,2);
ALTER TABLE sites ADD COLUMN service_id uuid REFERENCES services(id);
ALTER TABLE sites ADD COLUMN cleaning_depth_id uuid REFERENCES cleaning_depths(id);
ALTER TABLE sites ADD COLUMN total_price numeric(15,2);
-- Back-fill existing rows, then:
-- ALTER TABLE sites ALTER COLUMN service_id SET NOT NULL;
-- (cleaning_depth_id stays nullable: Fumigation / Property Management sites may have none)
-- ALTER TABLE sites ALTER COLUMN total_price SET NOT NULL;

CREATE TABLE site_add_ons (
    site_id uuid NOT NULL REFERENCES sites(id),
    add_on_id uuid NOT NULL REFERENCES add_ons(id),
    PRIMARY KEY (site_id, add_on_id)
);

-- Contract lifecycle (new columns; added automatically under ddl-auto=update)
ALTER TABLE contracts ADD COLUMN contract_number varchar(32) UNIQUE;
ALTER TABLE contracts ADD COLUMN site_id uuid REFERENCES sites(id);
ALTER TABLE contracts ADD COLUMN status varchar(20);
ALTER TABLE contracts ADD COLUMN terms varchar(4000);
ALTER TABLE contracts ADD COLUMN sent_at timestamp;
ALTER TABLE contracts ADD COLUMN signed_at timestamp;
ALTER TABLE contracts ADD COLUMN signed_by varchar(255);
ALTER TABLE contracts ADD COLUMN rejected_at timestamp;
ALTER TABLE contracts ADD COLUMN rejection_reason varchar(255);
ALTER TABLE contracts ADD COLUMN terminated_at timestamp;
ALTER TABLE contracts ADD COLUMN termination_reason varchar(255);
-- Existing contracts (if any) predate statuses: mark them DRAFT so they can be reviewed
UPDATE contracts SET status = 'DRAFT' WHERE status IS NULL;

-- Quotation and invoice generation
ALTER TABLE quotes ADD COLUMN quote_number varchar(32) UNIQUE;

CREATE TABLE quote_items (
    quote_id uuid NOT NULL REFERENCES quotes(id),
    line_no integer NOT NULL,
    description varchar(255) NOT NULL,
    quantity integer NOT NULL,
    unit_price numeric(15,2) NOT NULL,
    amount numeric(15,2) NOT NULL,
    PRIMARY KEY (quote_id, line_no)
);

ALTER TABLE invoices ALTER COLUMN contract_id DROP NOT NULL;
ALTER TABLE invoices ADD COLUMN invoice_number varchar(32) UNIQUE;
ALTER TABLE invoices ADD COLUMN quote_id uuid REFERENCES quotes(id);
ALTER TABLE invoices ADD COLUMN site_id uuid REFERENCES sites(id);
ALTER TABLE invoices ADD COLUMN issue_date date;
ALTER TABLE invoices ADD COLUMN due_date date;

ALTER TABLE invoices ADD COLUMN amount_paid numeric(19,2) DEFAULT 0;

ALTER TABLE payments ADD COLUMN currency varchar(3);
ALTER TABLE payments ADD COLUMN payer_phone varchar(255);
ALTER TABLE payments ADD COLUMN failure_reason varchar(255);
ALTER TABLE payments ADD COLUMN notes varchar(255);
ALTER TABLE payments ADD COLUMN payment_reference varchar(255);
ALTER TABLE payments ADD COLUMN payment_url varchar(1000);
ALTER TABLE payments ADD COLUMN control_number varchar(255);
ALTER TABLE payments ADD COLUMN receipt_number varchar(32) UNIQUE;

-- Crew membership (staff dashboard)
CREATE TABLE crew_members (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    crew_id uuid NOT NULL REFERENCES crews(id),
    staff_id varchar(255) NOT NULL,
    staff_name varchar(255),
    member_role varchar(255),
    joined_on date,
    created_by uuid, created_at timestamp, updated_by uuid, updated_at timestamp,
    deleted_at timestamp, deleted boolean DEFAULT false, active boolean DEFAULT true
);

CREATE TABLE invoice_items (
    invoice_id uuid NOT NULL REFERENCES invoices(id),
    line_no integer NOT NULL,
    description varchar(255) NOT NULL,
    quantity integer NOT NULL,
    unit_price numeric(15,2) NOT NULL,
    amount numeric(15,2) NOT NULL,
    PRIMARY KEY (invoice_id, line_no)
);
```

> The `uuid_generate_v4()` default requires the `uuid-ossp` extension: `CREATE EXTENSION IF NOT EXISTS "uuid-ossp";`

---

## 16. Implementation status and known issues

### Not built yet
| Area | Methods |
|---|---|
| Contracts | `/contracts/{id}/jobs` listing endpoint not built yet |
| Subscriptions | update (returns `null`) |
| Time logs | get logs for job, get by ID, update, staff attendance (return `null`) |
| Crews, feedback, staff ratings | some methods in `CrewServiceImpl`, `FeedBackServiceImpl`, `StaffRatingServiceImpl` (return `null`) |
| Payments | no refund endpoint; `REFUNDED` results from the payment service are ignored |
| Reports, job photos | no endpoints |
| Invoices | no manual create/update/cancel/send endpoints (invoices are generated from quotations); recurring billing for subscriptions does not create invoices yet |
| Checklists | several controller methods have no HTTP mapping |

### Known issues
| Issue | Location | Effect |
|---|---|---|
| Update service path is singular `/service/{id}` | `ServiceController` | Inconsistent with other `/services` endpoints |
| `plotCoordinates.toString()` without null check | `SiteResponseDto` | Sites saved without coordinates fail when read |
| Lombok 1.18.34 incompatible with JDK 25+ | `pom.xml` | Build fails on newer JDKs |
| Redis host/password and Kafka host hard-coded | `application.properties` | Credentials in source control; move to config server / secrets |
| `DistanceCalculatorService` (Google Distance Matrix) commented out | `common` | Not in use |

---

## 17. Development conventions

When adding a new module or resource, follow the existing pattern (see `masterData/` as the reference):

1. **Entity** in `entities/`, extending `BaseEntity<UUID>`, with `@Table`, `@Where(clause = " deleted_at is null")` and an `@Enumerated(EnumType.STRING)` `Status` where relevant.
2. **DTOs** in `dto/`: `XxxDto` with Jakarta validation, and `XxxResponseDto` with a constructor taking the entity. Add `@Schema` descriptions and examples.
3. **Repository** in `repository/` extending `JpaRepository<Xxx, UUID>`.
4. **Service** interface + `XxxServiceImpl` (`@Transactional`), throwing `AfriException` for business errors and parsing UUIDs safely.
5. **Controller** under `/api/v1` with `@Tag`, `@Operation`, `@Permission`, `@Valid` request bodies, and `{id}` path variables matching `@PathVariable("id")`.
6. Soft delete by setting `deletedAt` (and `deleted = true`); never hard delete.
7. Seed reference data with an idempotent `CommandLineRunner` in `seeders/`.
