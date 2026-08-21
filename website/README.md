# DAD'S CARE Logistics Solutions Website

A modern, production-ready static website for **DAD'S CARE Logistics Solutions Pvt. Ltd.** Built with Next.js 14+ (App Router), TypeScript, and Tailwind CSS, configured for static export and optimized for deployment on Vercel's Hobby plan.

## 🚀 Features

- **Next.js 14+** with App Router
- **TypeScript** for type safety
- **Tailwind CSS** for responsive styling
- **Static Export** (`output: 'export'`) for free hosting
- **SEO Optimized** with meta tags and Open Graph
- **Mobile-First** responsive design
- **Smooth animations** and transitions
- **Contact form** ready for integration with Formspree
- **Google Maps** embed for location

## 📋 Pages

### Home Page (`/`)
- Hero section with gradient background
- Services overview (4 core offerings)
- Why Choose Us (6 differentiators)
- Geographies section with India map
- About section
- Call-to-action section

### Contact Page (`/contact`)
- Contact information
- Contact form with validation
- Google Maps embed

## 🛠️ Tech Stack

- **Framework:** Next.js 16.1.6
- **Language:** TypeScript
- **Styling:** Tailwind CSS v4
- **Font:** Inter (Google Fonts)
- **Deployment:** Vercel

## 🏃‍♂️ Getting Started

### Prerequisites

- Node.js 18+ and npm installed
- Git

### Installation

1. Clone the repository:
```bash
git clone https://github.com/ajaypandey03/dads-care-website.git
cd dads-care-website
```

2. Install dependencies:
```bash
npm install
```

3. Run the development server:
```bash
npm run dev
```

4. Open [http://localhost:3000](http://localhost:3000) in your browser

## 📦 Building for Production

### Static Export

This project is configured for static export, which generates static HTML/CSS/JS files:

```bash
npm run build
```

The static files will be generated in the `out/` directory.

### Testing the Build Locally

After building, you can serve the static files locally:

```bash
npx serve@latest out
```

## 🚀 Deploying to Vercel

### Option 1: Deploy via Vercel Dashboard (Recommended)

1. Push your code to GitHub
2. Go to [Vercel Dashboard](https://vercel.com/new)
3. Click "Import Project"
4. Select your GitHub repository
5. Vercel will auto-detect Next.js and configure the build settings
6. Click "Deploy"

### Option 2: Deploy via Vercel CLI

1. Install Vercel CLI:
```bash
npm i -g vercel
```

2. Run the deploy command:
```bash
vercel
```

3. Follow the prompts to link your project

4. Deploy to production:
```bash
vercel --prod
```

## 🌐 Custom Domain Setup

### On Vercel:

1. Go to your project dashboard on Vercel
2. Click on "Settings" → "Domains"
3. Add your custom domain (e.g., `dads-care.com`)
4. Vercel will provide DNS records to configure

### DNS Configuration:

Add these records to your domain provider:

**For root domain (dads-care.com):**
- Type: `A`
- Name: `@`
- Value: `76.76.21.21`

**For www subdomain:**
- Type: `CNAME`
- Name: `www`
- Value: `cname.vercel-dns.com`

**Note:** DNS propagation can take up to 48 hours.

## 📧 Setting Up Contact Form with Formspree

1. Go to [Formspree.io](https://formspree.io) and create an account
2. Create a new form and get your form endpoint
3. Update `src/components/ContactForm.tsx`:

```tsx
// Replace this line:
<form onSubmit={handleSubmit}>

// With:
<form action="https://formspree.io/f/YOUR_FORM_ID" method="POST">
```

4. The form will now send submissions to your email

### Environment Variables (Optional)

If you want to use environment variables for form configuration:

1. Create `.env.local`:
```
NEXT_PUBLIC_FORMSPREE_ID=your_form_id_here
```

2. Update the form action:
```tsx
<form action={`https://formspree.io/f/${process.env.NEXT_PUBLIC_FORMSPREE_ID}`} method="POST">
```

## 📁 Project Structure

```
├── public/
│   └── images/          # Static images
├── src/
│   ├── app/
│   │   ├── layout.tsx   # Root layout with Header/Footer
│   │   ├── page.tsx     # Home page
│   │   ├── globals.css  # Global styles
│   │   └── contact/
│   │       └── page.tsx # Contact page
│   └── components/
│       ├── Header.tsx           # Navigation header
│       ├── Footer.tsx           # Site footer
│       ├── Hero.tsx             # Hero section
│       ├── Services.tsx         # Services grid
│       ├── WhyChooseUs.tsx      # Differentiators
│       ├── Geographies.tsx      # India map
│       ├── About.tsx            # About section
│       ├── CallToAction.tsx     # CTA section
│       ├── ContactForm.tsx      # Contact form
│       └── MapEmbed.tsx         # Google Maps
├── next.config.ts       # Next.js configuration
├── tailwind.config.js   # Tailwind configuration
├── tsconfig.json        # TypeScript configuration
└── package.json         # Dependencies
```

## 🔐 Customer Dashboard (`/dashboard/*`) — Phase 3

Alongside the public marketing pages above, this app also hosts Dad's Care's authenticated
customer dashboard, reachable via the **Login** button in the header. It's the same domain
and the same static export (`output: 'export'`) — the dashboard routes are plain client
components (`"use client"`) that talk directly to `dadscare-backend`'s REST API over
`fetch`, gated by a JWT kept in `localStorage`. There's no Next.js server involved, so
hosting stays exactly as cheap as the marketing site's today.

| Route | What it does |
|---|---|
| `/login` | Email/password sign-in against `POST /api/v1/auth/login` |
| `/dashboard` | Live godown/shutter status per site (`GET /api/v1/sites`, `/sites/{id}/shutter-units`) — `ORG_ADMIN`/`SITE_MANAGER`/`VIEWER` land here; `OPERATOR` is redirected straight to `/dashboard/operate` instead (see "Roles" below) |
| `/dashboard/operate` | The website's Opening/Closing operator form — pick a godown + shutter, fill stock lines/truck entries/labor/remarks/custom fields, submit to actually open or close the lock |
| `/dashboard/alerts` | Alert feed — search by device ref/alert ref, filter by godown, classification badges, and "Was this correct?" feedback that stops re-asking once answered |
| `/dashboard/reports` | Filterable (godown/device/status/direction) aggregation over the alerts + unlock-requests APIs, with CSV and PDF export |
| `/dashboard/admin/godowns` | Add/edit godowns (sites) and their shutters, and register + map Digital Lock devices to a shutter — see below |
| `/dashboard/admin/masters` | Product/Transporter master data CRUD (view-only for `VIEWER`) |
| `/dashboard/admin/users` | Invite/suspend teammates, with a choice of auto-generated or admin-set initial password — see "Passwords" below |
| `/dashboard/account` | Self-service password change (reachable from the sidebar's user block) |
| `/dashboard/platform/organizations` | **Platform admins only.** List + onboard customer organizations — see below. |
| `/dashboard/platform/organizations/manage?id=` | **Platform admins only.** Edit one organization and manage its users — see below. |

### Roles and what they can do

Every `User` has a `Role` (`ORG_ADMIN`, `SITE_MANAGER`, `OPERATOR`, `VIEWER`), already
carried in the JWT (`JwtAuthFilter` grants a `ROLE_<role>` authority) since Phase 1 — but
until now nothing actually checked it. `@EnableMethodSecurity` + `@PreAuthorize` on every
mutating controller method now enforces this matrix for real:

| Capability | ORG_ADMIN | SITE_MANAGER | OPERATOR | VIEWER |
|---|---|---|---|---|
| View Godown Status, Alerts, Reports (+ export) | ✅ | ✅ | ✅ | ✅ |
| Submit "Was this correct?" alert feedback | ✅ | ✅ | ✅ | ✅ |
| **Operate a shutter** (`/dashboard/operate`) | ✅ | ✅ | ✅ | ❌ |
| Manage Godowns/Shutters/Devices | ✅ | ✅ | ❌ | ❌ |
| Manage Master Data | ✅ | ✅ | ❌ | view-only |
| Manage Team (invite/suspend/change roles) | ✅ | ❌ | ❌ | ❌ |
| Own account (change own password) | ✅ | ✅ | ✅ | ✅ |
| Platform (cross-org onboarding) | separate `platformAdmin` flag on `User`, orthogonal to `Role` | | | |

Reads stay open to every authenticated org member — tenant isolation (organizationId
scoping) is already the real data boundary there. Only **mutating** endpoints are
role-gated: `MasterDataController` (create/update/deactivate), `SiteController`/
`ShutterUnitController`/`DeviceController` (create/update), `UserAdminController` (every
method — Team roster is identity/PII data, scoped `ORG_ADMIN`-only even for reads), and
`UnlockRequestController.create` (the actual "operate a shutter" action — `VIEWER`
excluded is the fix for the underlying bug: previously *any* authenticated role could
submit an unlock request). `AccessDeniedException` (thrown by `@PreAuthorize` failures)
maps to `403` via `GlobalExceptionHandler`, same response shape as every other error.

**Frontend enforcement is UX, not the security boundary** — `AuthContext` exposes
`canManage`/`canOperate`/`isOrgAdmin` derived booleans; `DashboardSidebar` only renders
nav links a role can actually use, and each gated page (`/dashboard/operate`,
`/dashboard/admin/godowns`, `/dashboard/admin/users`) independently re-checks and shows a
plain "Not available" message on direct URL access — reusing the same fallback pattern
the Platform pages established. `/dashboard/admin/masters` stays reachable for `VIEWER`
but hides the add-forms and Remove buttons. The real boundary is always the backend `403`.

Verified with `RoleAuthorizationTest` (`dadscare-backend`, a `@WebMvcTest` exercising the
real `JwtAuthFilter`/`JwtService`/`SecurityConfig` — every other test in this codebase is
a Mockito service-unit test that never touches Spring Security) plus hand-verification in
the browser as all four seeded demo roles.

### What Master Data is for

`/dashboard/admin/masters` manages `ProductMaster` (product name + unit, e.g. "Cement
Bags" / "bags") and `TransporterMaster` (transporter name + code) — org-scoped reference
lists, not telemetry or alert data. Their only consumer today is `dadscare-mobile`'s
Opening/Closing Form: when a godown operator raises or lowers a shutter, the app's stock
lines and truck entries are picked from these lists (dropdowns) rather than typed as free
text. That keeps naming consistent across every submission — the same product is always
called the same thing — which matters because that data flows into the `GodownForm`/
`StockLine`/`TruckEntry` records a `CONFIRMED` alert links to, and from there into the
WhatsApp notification template's inventory/logistics section and into reports. In short:
maintain Master Data once per org, and every operator's form afterward stays consistent
with it.

### Alerts and Reports — filters and repeat-feedback prevention

- **Alerts filters**: a search box (matches device ref or the alert's sequence code) and a
  godown dropdown, both client-side over the already-fetched `/api/v1/alerts` response.
- **"Was this correct?"**: `AlertDto` now carries `feedbackCorrect` (`null` = unanswered,
  otherwise the most recent answer), computed from
  `FeedbackEntryRepository.findFirstByAlertIdOrderByCreatedAtDesc`. Once an alert has been
  answered, the row shows "✓ Confirmed correct" / "✗ Marked incorrect" instead of the
  buttons — confirmed by refetching from the backend, not just local component state, so
  this survives a page reload and is consistent across whoever else on the team looks at
  it. `FeedbackEntry` itself is still an append-only audit log (unchanged) — this only
  changes what the UI *shows*, not whether resubmission is technically possible via the API.
- **Reports filters**: godown, device (derived from the alerts actually loaded, not a
  separate device-list call), classification, and direction — all client-side, recomputing
  both the stat cards and the table. `AlertDto` also now carries `deviceRef`/`siteId`/
  `siteName` directly (previously just a bare `deviceId`), which both this page and Alerts
  needed to filter/display without extra round trips.
- **PDF export**: alongside the existing CSV export, `jspdf` + `jspdf-autotable` (new
  client-side-only deps) generate a real downloadable PDF of the currently-filtered alert
  table — same data as the CSV, different format.

### Dashboard shell — sidebar, not a top-bar menu

`/dashboard/*` uses a persistent left sidebar (`src/components/dashboard/DashboardSidebar.tsx`),
not a horizontal nav bar — a deliberate "admin app" feel, distinct from the public site's
top-bar `Header`. Desktop gets a fixed `w-64` dark sidebar with the real logo (on a white
card, since the logo itself has no dark-mode background), icon + label nav, active-route
highlighting, and the signed-in user + Logout pinned at the bottom. Below the `md` breakpoint
it collapses to a slim top bar (logo + hamburger) that opens the same sidebar content as a
slide-in drawer overlay. `dashboard/layout.tsx` composes it in a `flex` row with the page
content, rather than stacking a nav bar above a `container mx-auto` column.

### Brand colors

The dashboard's palette (`--color-brand-*` tokens in `globals.css`, Tailwind v4 `@theme`) is
sampled directly from `public/images/logo.png` — brick red from the "DAD'S CARE" wordmark
(primary: buttons, active nav, focus rings), green from "Logistics Solutions" (secondary:
used for sensitive actions like "Reset password"), and the truck icon's amber (the
**Platform** nav item, so a platform-admin-only area reads as visually distinct). The public
marketing pages keep their own established blue/orange theme (see "Design Theme" below) —
this rebrand is scoped to the post-login dashboard, not a site-wide change.

### Passwords — self-service change, and admin-set-or-generated on create

- **Self-service:** `PUT /api/v1/me/password` (`ChangePasswordRequest`: currentPassword +
  newPassword) — requires knowing the current password, since there's no email/reset-link
  flow. Reachable at `/dashboard/account`, linked from the sidebar's user block (the gear
  icon at the bottom).
- **Admin-created users:** every "create a user" flow (`POST /api/v1/users` team invite,
  `POST /api/v1/platform/organizations` first admin, `POST /api/v1/platform/organizations/
  {id}/users`) now takes an optional `password` field. The shared
  `PasswordModeField` component (`src/components/dashboard/PasswordModeField.tsx`) renders a
  radio choice — **Auto-generate** (unchanged behavior: a random password is returned once
  in the response) or **Set manually** (the admin types it; the response's
  `temporaryPassword` comes back `null`, and the UI shows a "you already set it" message
  instead of a password to copy). No email is sent either way — there's still no SMTP
  integration, so the admin relays it out-of-band regardless of which mode they pick.

### Godown Status — search, live open/closed state, and richer detail

`/dashboard` (the live view, distinct from `/dashboard/admin/godowns` below) now:

- Has a search box filtering godowns by name/code/address (client-side — org site counts
  are small).
- Fetches every site's shutter units **eagerly** on load (not lazily on expand, like the
  original Phase 3 version), so each collapsed godown card can show a one-line live summary
  ("2 shutters — 1 open, 1 closed") and a red dot if anything's open, without the customer
  needing to open every card first.
- Expanding a card shows, per shutter: an Open/Closed/Unknown badge, the device's Online/
  Offline + battery badges (as before), and **last opened / last closed timestamps**.

The Open/Closed/Unknown state and timestamps are new backend-derived fields
(`ShutterUnitDto.currentState/lastOpenedAt/lastClosedAt`, `SiteService`) — not stored
columns, but computed from the shutter's device's most recent `LOCK_OPEN` vs `LOCK_CLOSE`
`RawEvent` (`RawEventRepository.findFirstByDeviceIdAndEventTypeOrderByEventTimestampDesc`),
the same event-sourced-state approach the rest of this platform already uses (see "Lock vs
Shutter" in Confluence) rather than a new field that could drift from what the events
actually say. `UNKNOWN` means no lock events have arrived yet, or no device is mapped.

### Godowns management (`/dashboard/admin/godowns`)

Previously there was no way to create a godown, add a shutter to it, or register/map a
Digital Lock device at all — every site/shutter/device in this repo's demo data was seeded
directly via SQL. This screen closes that gap with real CRUD:

- **Godowns**: add a new one (name, godown code, address); edit any existing one (name,
  code, address, ACTIVE/INACTIVE) inline.
- **Shutters**: per-godown, add a shutter (label) and toggle its active status. A shutter
  with no device mapped says so, pointing at the Devices section below it.
- **Devices**: register a new Digital Lock by its Velosyss device ref (the exact string
  Velosyss's webhook payloads will carry — see `WebhookSignatureVerifier`/`WebhookService`),
  optionally mapping it to an unassigned shutter at registration time; every existing
  device's mapping can be changed via a per-row dropdown, which only offers shutters that
  are unassigned (or already this device's own shutter) — the backend
  (`ShutterUnitAlreadyMappedException`, 409) rejects double-mapping either way, so the
  dropdown filtering is UX, not the only guard.
- Backend: `SiteController`/`ShutterUnitController`/`DeviceController` (all under
  `com.dadscare.backend.site`), all org-scoped through the same `TenantContext` pattern as
  the rest of the codebase — no new authorization model needed here.

### Platform admins — onboarding and managing customer organizations

Every normal user (even `ORG_ADMIN`) is scoped to one `Organization` (tenant). Onboarding a
*new* customer — creating the `Organization` itself and its first admin user — and later
managing any org on the platform (edit its details, add/suspend its users, reset a locked-out
user's password) is a separate, cross-tenant capability: a `platformAdmin` boolean on `User`
(`is_platform_admin` column, `V5__platform_admin.sql`), carried as a JWT claim and checked by
`PlatformOrganizationService.requirePlatformAdmin()` on every call. There's no self-serve way
to become one — it's set directly in the database for Dad's Care's own staff.

- `GET/POST /api/v1/platform/organizations` — list every org, or onboard a new one
  (name/slug/codePrefix + first admin's name/email/phone). Create returns the new org, its
  admin user, and a one-time temporary password, same convention as `/api/v1/users`.
- `GET/PUT /api/v1/platform/organizations/{id}` — fetch or edit one org's name/codePrefix/
  active flag. `slug` is intentionally not editable here (it's a stable identifier).
- `GET/POST /api/v1/platform/organizations/{id}/users` — list, or directly add, a user inside
  any org (not just the caller's own).
- `PUT /api/v1/platform/organizations/{id}/users/{userId}` — change a user's role/status
  (e.g. un-suspend someone the org's own admin locked out by mistake).
- `POST /api/v1/platform/organizations/{id}/users/{userId}/reset-password` — support tool:
  generates a new temporary password for a user who's lost access, returned once.
- Every one of the above 403s for a non-platform-admin caller. The dashboard's **Platform**
  nav link and the two platform pages only render for `isPlatformAdmin` users
  (`AuthContext`) — a plain "not available" message otherwise — but that's UX, not the
  security boundary; the API's `403` is.
- The org list's **Manage** link opens `/dashboard/platform/organizations/manage?id={id}` — a
  query param, not a `[id]` dynamic route segment, since this is a static export
  (`output: 'export'`) with no `generateStaticParams` story for an org id that doesn't exist
  at build time. Wrapped in `Suspense` per Next's requirement for `useSearchParams` in a
  statically-exported page.
- To seed the first platform admin for a fresh environment, insert directly into `users` with
  `is_platform_admin = TRUE` (and any `Organization` — a "Dad's Care Internal" one is the
  obvious choice) rather than building an endpoint for it, since a
  create-your-own-super-admin endpoint would be a bootstrapping backdoor.

Route groups: `(public)/` holds the marketing pages under the shared `Header`/`Footer`
layout; `/dashboard/*` has its own layout + sidebar and an auth guard that redirects to
`/login` when there's no valid session. `AuthProvider` (`src/context/AuthContext.tsx`) wraps
the whole app at the root layout so both the public header's Login link and the dashboard's
guard can read auth state.

Since this is a static export, dynamic route segments (e.g. a `/dashboard/sites/[siteId]`
page) aren't used — the site list expands inline per-card instead of navigating, which
sidesteps the `generateStaticParams` requirement entirely.

Configure the backend origin via `NEXT_PUBLIC_API_BASE_URL` (see `.env.example`) — it's
inlined at build time like any other `NEXT_PUBLIC_*` var. `dadscare-backend`'s CORS config
(`app.cors.allowed-origins`) already includes `http://localhost:3000` for local dev.

**Known limitations, not yet built:** no role-based UI/API gating (any authenticated org
member can reach `/dashboard/admin/*`, matching the rest of the backend's current
authorization posture); master-data "Remove" is one-way (the list APIs only return
`active=true` rows, so there's no reactivate path from this UI yet); invited/created users
still have no email sent to them regardless of password mode — the admin always relays it
directly; self-service password change has no "forgot password" recovery path (only a
platform admin's org-management "Reset password" tool, or a fellow org admin re-inviting
isn't possible today — there's no delete-user flow either).

## 🎨 Design Theme

- **Primary Color:** Blue (#1E40AF)
- **Accent Color:** Orange (#F97316)
- **Background:** White / Light gray
- **Style:** Clean, corporate, professional

## 📱 Responsive Design

The website is fully responsive and optimized for:
- Mobile devices (320px+)
- Tablets (768px+)
- Desktops (1024px+)
- Large screens (1280px+)

## 🔍 SEO Features

- Semantic HTML
- Meta descriptions
- Open Graph tags
- Structured data ready
- Fast page load times
- Mobile-friendly

## 📄 License

© 2025 DAD'S CARE Logistics Solutions Pvt. Ltd. All Rights Reserved.

## 📞 Contact

**DAD'S CARE Logistics Solutions Pvt. Ltd.**
- **Email:** info@dadscare.co.in
- **Phone:** +91 83093 24525
- **Address:** A-15/2, Sulabh Awas Yojana, Transport Nagar, Lucknow, U.P. – 226012
- **GSTIN:** 09AALCD4009J1ZI

## 🤝 Support

For technical support or inquiries about the website, please contact: info@dadscare.co.in
