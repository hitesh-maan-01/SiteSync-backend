# SiteSync — Spring Boot Backend

REST API backend for the SiteSync construction site management platform. Serves the Flutter mobile app and Flutter Web client view.

**Frontend:** Flutter (separate repo)

---

## Tech Stack

| Layer       | Choice        |
|-------------|---------------|
| Framework   | Spring Boot 3 |
| Language    | Java 17       |
| Persistence | Spring Data JPA + Hibernate |
| Database    | PostgreSQL (hosted on Supabase) |
| Auth        | JWT (access + refresh tokens) + OTP via email |
| File Storage | `FileStorageService` (local / S3-compatible) |
| Push Notifications | Firebase Admin SDK (FCM) |
| Validation  | Jakarta Bean Validation (`@NotBlank`, `@NotNull`, etc.) |
| Build       | Maven         |

---

## Architecture
com.sitesync/
├── auth/ # JWT issuance, OTP, signup, login, supervisor invite
│ ├── controller/ # AuthController, UserProfileController
│ ├── dto/ # ApiResponse<T>, UserResponse, auth request DTOs
│ ├── entity/ # User, UserRole, OtpCode, RefreshToken
│ ├── exception/ # GlobalExceptionHandler (@RestControllerAdvice)
│ ├── repository/ # UserRepository, OtpCodeRepository, RefreshTokenRepository
│ └── service/ # AuthService, JwtService, UserProfileService
│
├── project/ # Project CRUD + geofence + overview stats
│ ├── controller/ # ProjectController, ProjectOverviewController
│ ├── dto/ # ProjectRequests, ProjectResponse, ProjectOverviewResponse
│ ├── entity/ # Project, ProjectStatus
│ └── service/ # ProjectService, ProjectOverviewService
│
├── milestone/ # Milestone lifecycle + daily progress logs
│ ├── controller/ # MilestoneController
│ ├── dto/ # MilestoneResponse, LogProgressRequest,
│ │ # MilestoneCreateDTO, MilestoneUpdateLogResponse
│ ├── entity/ # Milestone, MilestoneStatus, MilestoneUpdateLog
│ ├── repository/ # MilestoneRepository, MilestoneUpdateLogRepository
│ └── service/ # MilestoneService
│
├── delivery/ # Purchase orders (delivery scheduling + confirmation)
│ ├── controller/ # DeliveryController
│ ├── dto/ # PurchaseOrderRequests, PurchaseOrderResponse
│ ├── entity/ # PurchaseOrder, PurchaseOrderStatus
│ ├── repository/ # PurchaseOrderRepository
│ ├── scheduler/ # OverdueDeliveryScheduler (PENDING → LATE)
│ └── service/ # DeliveryService
│
├── inventory/ # Stock ledger (append-only, stock-in + stock-out)
│ ├── controller/ # InventoryController
│ ├── dto/ # StockLevelResponse, StockLedgerEntryResponse,
│ │ # StockRequests
│ ├── entity/ # StockLedgerEntry, StockTransactionType
│ └── service/ # InventoryService
│
├── site/ # Workers, attendance, materials, file storage
│ ├── controller/ # SiteController, AttendanceController
│ ├── dto/ # WorkerResponse, AttendanceResponse, MaterialResponse
│ ├── entity/ # Worker, Attendance, AttendanceStatus, AttendanceMethod,
│ │ # Material, MaterialUnit
│ ├── repository/ # WorkerRepository, AttendanceRepository, MaterialRepository
│ └── service/ # SiteService, AttendanceService, FileStorageService
│
├── vendor/ # Vendor directory (contractor-scoped)
│ ├── controller/ # VendorController
│ ├── dto/ # VendorRequests, VendorResponse
│ ├── entity/ # Vendor
│ ├── repository/ # VendorRepository
│ └── service/ # VendorService
│
└── notification/ # FCM push sender
└── service/ # PushNotificationService

---

## API Overview

All endpoints return:
```json
{
  "success": true,
  "data": { ... },
  "message": "OK",
  "errors": null
}
```
`errors` is a `Map<String, String>` populated on Bean Validation failures.

### Auth — `/api/auth/...`
| Method | Path            | Description |
|------|-------------------|-----------|
| POST | `/api/auth/login` | Password login |
| POST | `/api/auth/otp/request` | Request OTP |
| POST | `/api/auth/otp/verify` | Verify OTP + get session |
| POST | `/api/auth/register` | Contractor signup step 1 |
| POST | `/api/auth/register/verify` | Contractor signup step 2 |
| GET  | `/api/auth/me`    | Current user |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout`| Invalidate refresh token |
| POST | `/api/auth/supervisors/invite` | Invite supervisor |
| GET  | `/api/auth/supervisors` | List contractor's supervisors |
| POST | `/api/auth/fcm-token` | Register FCM device token |

### User Profile — `/api/v1/users/...`
| Method | Path                     | Description |
|--------|--------------------------|---|
| GET    | `/api/v1/users/me`       | Get own profile |
| POST   | `/api/v1/users/me/photo` | Upload profile photo (multipart) |
| POST   | `/api/v1/users/me/phone` | Link / update phone number |

### Projects — `/api/projects/...`
| Method | Path            | Description |
|--------|-----------------|---------------------------|
| GET    | `/api/projects` | Contractor's project list |
| POST   | `/api/projects` | Create project (+ initial milestones) |
| GET    | `/api/projects/{id}` | Project detail |
| PATCH  | `/api/projects/{id}` | Update status / supervisor / progress |
| GET    | `/api/projects/{id}/overview` | Live present/enrolled worker counts |

### Milestones — `/api/v1/milestones/...`
| Method | Path                   | Description |
|---     |------------------------|-----------------------|
| GET    | `/api/v1/milestones/project/{projectId}` | All milestones for project |
| PATCH  | `/api/v1/milestones/{id}/status` | Update status (sequence enforced) |
| POST   | `/api/v1/milestones/{id}/log-progress` | Log daily progress (+X%) |
| GET    | `/api/v1/milestones/{id}/logs` | Progress log history |

### Attendance — `/api/attendance/...`
| Method | Path | Description |
|---|---|---|
| GET | `/api/attendance/project/{projectId}` | All-time attendance |
| GET | `/api/attendance/project/{projectId}/today` | Today's attendance only |
| GET | `/api/attendance/project/{projectId}/date/{date}` | Attendance for specific date |
| POST | `/api/attendance/mark` (multipart) | Mark with photo + GPS |
| POST | `/api/attendance/mark` (JSON) | Mark manually |

### Deliveries — `/api/purchase-orders/...`
| Method | Path | Description |
|---|---|---|
| GET | `/api/purchase-orders/project/{projectId}` | All purchase orders |
| POST | `/api/purchase-orders` | Schedule delivery |
| POST | `/api/purchase-orders/{id}/deliver` | Mark delivered (multipart photo) |

### Inventory — `/api/inventory/...`
| Method | Path | Description |
|---|---|---|
| GET | `/api/inventory/{projectId}/stock` | Current stock (aggregated) |
| GET | `/api/inventory/{projectId}/ledger` | Full transaction ledger |
| POST | `/api/inventory/consume` | Log material consumption |

### Workers — `/api/site/...`
| Method | Path | Description |
|---|---|---|
| GET | `/api/site/workers/{projectId}` | Project worker roster |
| POST | `/api/site/workers` | Onboard worker |
| DELETE | `/api/site/workers/{workerId}` | Remove worker |

### Vendors — `/api/vendors/...`
| Method | Path | Description |
|---|---|---|
| GET | `/api/vendors` | Contractor's vendor list |
| POST | `/api/vendors` | Add vendor |
| DELETE | `/api/vendors/{id}` | Remove vendor |

---

## Database

PostgreSQL on Supabase. Schema migrations are numbered SQL files:

| Migration | Contents |
|---|---|
| `001_schema.sql` | users, projects, workers, vendors, attendance, materials, otp_codes, refresh_tokens + indexes |
| `002_delivery_inventory.sql` | purchase_orders, stock_ledger + indexes + RLS |
| `003_milestones.sql` | milestones table (UUID PK, PENDING/IN_PROGRESS/COMPLETED/DELAYED) |
| `004_attendance_date.sql` | `date` column on attendance + unique `(worker_id, date)` constraint |
| `005_milestone_progress.sql` | `progress_percent` on milestones + `milestone_update_logs` table |
| `006_project_fields.sql` | `client_name`, `estimated_budget` on projects |

**RLS:** Enabled on all tables with zero policies — only Spring Boot's privileged JDBC connection bypasses RLS. No direct Supabase client access.

**`ddl-auto: validate`** — Hibernate validates entity-to-table mapping at startup but never modifies the schema. All schema changes go through numbered SQL migration files.

---

## Key Design Decisions

### One-punch-per-day attendance
`attendance` has a unique constraint on `(worker_id, date)`. `AttendanceService` checks `existsByWorkerIdAndDate` before inserting and throws `IllegalArgumentException` (→ clean 400 via `GlobalExceptionHandler`) if the worker is already marked. The DB constraint is the authoritative guard against race conditions.

### Append-only inventory ledger
`stock_ledger` has no "current stock" row. Current stock = `SUM(quantity_delta)` computed on read by `aggregateStockByMaterial`. Stock-in is positive delta (delivery confirmed), stock-out is negative delta (consumption logged). `InventoryService.logConsumption` rejects if requested quantity exceeds computed current stock.

### Milestone sequence enforcement
`MilestoneService.assertSequenceUnblocked` counts milestones with a lower `sequenceOrder` still in `PENDING` status. If any exist, the status update is rejected with a clear message. Status transitions are forward-only (`assertForwardTransition`): `PENDING → IN_PROGRESS → COMPLETED`.

### Stored project progress
`Project.progressPercent` is stored (not live-computed) for read performance on contractor dashboards. Updated in the same `@Transactional` block as every milestone progress log: average of all milestones' `progressPercent` values.

### Supervisor–Project bidirectional sync
Both `projects.assigned_supervisor_id` and `users.assigned_project_id` are updated in the same `@Transactional` block in `ProjectService.updateProject`. Previous supervisor is unlinked before new one is assigned. Failure to write either side rolls back both.

### Delivery stock-in
Marking a purchase order `DELIVERED` triggers `InventoryService.recordStockIn` within the same `@Transactional` — so a `Project` is never marked delivered without its matching `stock_ledger` entry. A `Material` row is also created for backward compatibility with the material log screen.

### FCM notifications
`PushNotificationService.send` is fire-and-forget — failures are logged, never thrown. Business operations (milestone completion, attendance flags) are never rolled back due to a push failure.

### Error envelope
Every endpoint returns `ApiResponse<T>` (`{ success, data, message, errors }`). `GlobalExceptionHandler` (`@RestControllerAdvice`) maps all exception types — `IllegalArgumentException` → 400, `AccessDeniedException` → 403, `BadCredentialsException` → 401, `MethodArgumentNotValidException` → 400 with field errors map, uncaught exceptions → 500. The Flutter `ApiClient.mapErrorToFailure` branch-matches on HTTP status codes.

---

## Setup

### Prerequisites
- Java 17
- Maven
- PostgreSQL (or Supabase project)
- Firebase project with Admin SDK service account JSON

### Configuration (`application.properties` / `application-dev.properties`)

```properties
spring.datasource.url=jdbc:postgresql://<host>:<port>/<db>
spring.datasource.username=<user>
spring.datasource.password=<password>
spring.jpa.hibernate.ddl-auto=validate

jwt.secret=<256-bit-hex-secret>
jwt.access-token-expiry-ms=900000
jwt.refresh-token-expiry-ms=2592000000

firebase.credentials-path=classpath:firebase-service-account.json

file.storage.base-path=/var/sitesync/uploads
```

### Run

```bash
./mvnw spring-boot:run
```

API available at `http://localhost:8080`.

---

## Status

| Area                                           | Status |
|------------------------------------------------|---|
| Auth (JWT + OTP + refresh + supervisor invite) | ✅ Done |
| User profile (GET + photo + phone update)      | ✅ Done |
| Projects (CRUD + geofence + milestone builder) | ✅ Done |
| Project overview (live worker counts)          | ✅ Done |
| Workers (onboard / list / remove)              | ✅ Done |
| Vendors (add / list / remove)                  | ✅ Done |
| Attendance (photo-GPS + manual + date-scoped queries) | ✅ Done |
| One-punch-per-day constraint                   | ✅ Done |
| Materials (log + list)                         | ✅ Done |
| Deliveries (schedule + mark-delivered + auto stock-in) | ✅ Done |
| Overdue delivery scheduler (PENDING → LATE)    | ✅ Done |
| Inventory (stock levels + ledger + consumption) | ✅ Done |
| Milestones (sequence-enforced status + progress logs) | ✅ Done |
| Project progress recompute (transactional)      | ✅ Done |
| Supervisor–project bidirectional sync           | ✅ Done |
| FCM push (milestone complete + attendance flag) | ✅ Done |
| Global exception handler                        | ✅ Done |
| Issue Log / Snag List                           | ⬜ Planned |
| Contractor analytics API                        | ⬜ Planned |
| Client web view API                             | ⬜ Planned |
| Rate limiting                                   | ⬜ Planned |
| Multi-tenancy / contractor isolation audit      | ⬜ Planned |

---

## License
Proprietary — internal project.