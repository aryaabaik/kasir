---
name: amana-pos-ui
description: >
  UI development skill for the AMANA POS application.
  Enforces a desktop-only, neo-brutalist design system with warm cream and charcoal tones,
  preserves all existing Spring Boot + Thymeleaf + PostgreSQL functionality,
  and follows a structured inspect-then-implement workflow.
  Use this skill when creating, redesigning, or modifying any AMANA POS page, component, or CSS.
---

# AMANA POS — UI Development Skill

## Project Overview

AMANA POS is a point-of-sale system for the Amana store, built with:

| Layer        | Technology                                       |
| ------------ | ------------------------------------------------ |
| Backend      | Spring Boot 4.1.1, Java 21, Maven                |
| Templates    | Thymeleaf (no layout dialect — every page is standalone) |
| Database     | PostgreSQL (`amana_db`), JPA with `ddl-auto=update` |
| Security     | Spring Security — roles `ADMIN` and `CASHIER`    |
| Frontend     | Plain CSS only (no JS, no CSS framework)         |
| Language     | Indonesian (Bahasa Indonesia) throughout UI      |

### Key Directories

```
src/main/java/com/kasir/amana/
├── Config/          SecurityConfig.java, AdminSeeder.java
├── Controller/
│   ├── Admin/       AdminTransactionController, CategoryController,
│   │                ProductController, ReportController,
│   │                StockMovementController, UserController
│   └── Kasir/       KasirTransactionController
├── Model/           Category, Payment, Product, ProductVariant, Purchase,
│                    PurchaseItem, StockMovement, Supplier, Transaction,
│                    TransactionItem, User
├── Repository/      One repository per entity
├── Service/         CategoryService, CheckoutService, PaymentService,
│                    ProductService, ProductVariantService, PurchaseItemService,
│                    PurchaseService, StockMovementService, SupplierService,
│                    TransactionItemService, TransactionService, UserService
└── AmanaApplication.java

src/main/resources/
├── static/css/      admin-dashboard.css, categories.css, kasir-dashboard.css,
│                    reports.css, transaction-history.css, transactions.css,
│                    user-create.css, users.css
├── templates/
│   ├── auth/        login.html
│   ├── admin/       dashboard.html, categories/, products/, reports/,
│   │                stock/, transactions/, users/
│   └── kasir/       dashboard.html, transactions/
└── application.properties
```

### Security Routes

- `/login`, `/css/**`, `/js/**`, `/images/**` → public
- `/admin/**` → `ROLE_ADMIN`
- `/kasir/**` → `ROLE_CASHIER`
- Post-login redirect: Admin → `/admin/dashboard`, Cashier → `/kasir/dashboard`

### Existing Conventions

- **No shared layout/fragments** — each HTML page is fully self-contained.
- **One CSS file per page or feature** — linked via `th:href="@{/css/...}"`.
- **No JavaScript files** — all logic is server-side.
- **No images or SVG assets** exist in the project.
- Flash messages and labels are in Bahasa Indonesia.

---

## 1 · Scope — Desktop Only

- **Target resolutions:** 1366×768 and 1920×1080.
- Do **not** implement mobile layouts or mobile-specific responsive behavior.
- Keep the desktop layout usable and non-overflowing at both target resolutions.
- Remove or ignore existing `@media` breakpoints aimed at mobile widths (≤ 850px) only when restyling a page. Do not break pages that are not being restyled.

---

## 2 · Design Tokens

Use these tokens consistently across every page. Define them as CSS custom properties in each standalone CSS file (since there is no shared layout).

```css
:root {
  /* Surfaces */
  --bg-page:          #F5F5F0;   /* warm cream — page background       */
  --bg-surface:       #FFFFFF;   /* white — cards, panels               */
  --bg-surface-alt:   #EDEDE8;   /* slightly darker cream — table heads */
  --bg-hover:         #E8E8E3;   /* row/card hover                      */

  /* Text */
  --text-primary:     #15191B;   /* charcoal / near-black               */
  --text-secondary:   #5A5F63;   /* neutral gray — labels, hints        */
  --text-muted:       #8A8F93;   /* lighter gray — metadata, counts     */

  /* Accent */
  --accent:           #A8C7FA;   /* muted pastel blue — links, badges   */
  --accent-hover:     #8BB4F0;   /* slightly deeper blue on hover       */
  --accent-bg:        #EEF4FE;   /* faint blue tint for accent areas    */

  /* Borders & Shadows (neo-brutalist) */
  --border:           #15191B;   /* strong black outlines               */
  --border-light:     #D0D0CB;   /* subtle dividers inside panels       */
  --shadow-offset:    3px 3px 0  #15191B;  /* hard offset shadow        */
  --shadow-offset-sm: 2px 2px 0  #15191B;  /* smaller offset shadow     */

  /* Status — restrained palette */
  --status-warning:   #D94F4F;   /* soft red — warnings, errors, delete */
  --status-warning-bg:#FDEAEA;   /* faint red background                */
  --status-info-bg:   #EEF4FE;   /* faint blue background               */
  --status-neutral:   #F0C674;   /* warm amber — inactive/pending       */
  --status-neutral-bg:#FDF6E3;   /* faint amber background              */

  /* Typography */
  --font-family:      'Arial', 'Helvetica Neue', sans-serif;
  --font-mono:        'Consolas', 'Courier New', monospace;

  /* Spacing scale */
  --space-xs:   4px;
  --space-sm:   8px;
  --space-md:  16px;
  --space-lg:  24px;
  --space-xl:  32px;
  --space-2xl: 48px;

  /* Radius — restrained */
  --radius-sm:   4px;
  --radius-md:   6px;
  --radius-lg:   8px;
  --radius-pill: 999px;
}
```

### Absolute Rules

- **No green** — no `#b7f36b`, no green accents, no green status badges.
- **No vivid pink** — avoid saturated pinks entirely.
- **No dark theme** — replace the existing dark backgrounds (`#101713`, `#172019`) with `--bg-page` / `--bg-surface` when restyling.
- **Minimal palette** — cream, charcoal, pastel blue, neutral gray, soft red for warnings only.

---

## 3 · Visual Style — Neo-Brutalism

Apply these principles when styling any component:

| Element          | Treatment                                                           |
| ---------------- | ------------------------------------------------------------------- |
| Cards / Panels   | White background, `2px solid var(--border)`, `box-shadow: var(--shadow-offset)`, `border-radius: var(--radius-md)` |
| Buttons (primary)| `background: var(--accent)`, `color: var(--text-primary)`, `2px solid var(--border)`, `box-shadow: var(--shadow-offset-sm)`, bold weight |
| Buttons (danger) | `background: var(--status-warning)`, `color: #fff`, `2px solid var(--border)` |
| Tables           | Clean horizontal dividers with `var(--border-light)`, uppercase header labels sized 11–12px, comfortable row padding |
| Inputs           | `2px solid var(--border)`, `border-radius: var(--radius-md)`, focus ring with `var(--accent)` |
| Status badges    | Pill shape (`--radius-pill`), small text (12px), background tint + text color matching status |
| Links            | `color: var(--accent)`, underline on hover only                     |
| Typography       | Page titles 28–32px bold, section headings 18–20px bold, body 14px, labels/hints 12–13px |
| Spacing          | Use the spacing scale. Keep consistent `--space-lg` or `--space-xl` between sections |

### Avoid

- Excessive gradients, glow effects, or blur filters.
- Rounded corners larger than `--radius-lg` (8px) on most elements.
- More than 2 levels of shadow nesting.
- Decorative elements that serve no functional purpose.

---

## Motion & Animation Rules

- Use subtle, polished animations to make the desktop UI feel responsive and alive without distracting from POS tasks.
- CSS animations and transitions are allowed even when JavaScript is prohibited.
- Prefer CSS transitions for hover, focus, active, and dropdown states; use keyframes for short page-entry and staggered card entrance effects where appropriate.
- Use short durations: typically 150–250ms for interaction feedback and 300–500ms for initial entrance animations.
- Apply a subtle fade-in with a small upward movement to dashboard sections; stagger cards slightly only when it improves the experience.
- Neo-brutalist buttons should respond to hover and press with controlled changes to shadow and position, maintaining the bold outlined style.
- Tables should have subtle row-hover feedback. Navigation should clearly communicate hover, focus, and active states.
- Keep animations performant and restrained. Avoid excessive bouncing, spinning, flashing, parallax, large zooms, and continuous decorative motion.
- Do not animate every element independently or replay entrance animations on every minor interaction.
- Do not add JavaScript, animation libraries, external dependencies, or fake loading delays solely for animation. If a desired data-driven animation cannot be implemented cleanly with existing CSS and server-rendered HTML, explain the limitation and ask before introducing JavaScript or dependencies.
- Respect `prefers-reduced-motion: reduce` by disabling or reducing non-essential motion.
- Verify that animation does not cause layout shifts, clipping, overlap, or horizontal overflow at 1366x768 and 1920x1080.

- Preserve all existing routes, forms, Thymeleaf expressions, backend behavior, and security rules.

## 4 · Stack Constraints

1. **Plain CSS only** — no Tailwind, Bootstrap, or CSS-in-JS.
2. **No JavaScript** unless the page already uses it or a feature explicitly requires it and the user approves.
3. **Each page gets its own CSS file** in `src/main/resources/static/css/`.
4. **Templates remain standalone** — do not introduce Thymeleaf Layout Dialect or fragment-based layouts unless the user explicitly requests it.
5. **Do not add new Maven dependencies** without user approval.
6. **Keep Indonesian language** for all user-facing text.

---

## 5 · Preserve Existing Functionality

Before making any change, inspect the relevant files and confirm:

- [ ] All `th:action`, `th:href`, `th:text`, `th:each`, `th:if`, `th:classappend`, and other Thymeleaf expressions are preserved exactly.
- [ ] All `name` attributes on form inputs match what the controller expects.
- [ ] Form `method` (GET/POST) is unchanged.
- [ ] Route paths are unchanged.
- [ ] Role-based access and security annotations are not affected.
- [ ] No model attributes, service methods, or repository queries are removed or renamed.
- [ ] Flash message keys (`successMessage`, `errorMessage`, etc.) are kept.

**Never** recreate or replace existing models, repositories, services, controllers, routes, security configuration, or database schema without explicit user approval.

---

## 6 · Implementation Workflow

Follow this sequence for every UI task:

### Phase 1 — Inspect & Plan

1. Read the target template file(s) and their CSS file(s) in full.
2. Read the relevant controller to understand model attributes and routes.
3. If the page uses data from services/repositories, scan those to understand the data shape.
4. Summarize to the user:
   - Which files will be modified.
   - Which Thymeleaf expressions and form actions exist and must be preserved.
   - What CSS changes are planned.
   - Any risks or questions.
5. Wait for user confirmation before proceeding (unless the task is straightforward and low-risk).

### Phase 2 — Implement

6. Modify **only** the target page's HTML and CSS.
7. Apply design tokens from the token section above.
8. Reuse CSS patterns from previously styled pages where they match.
9. Do **not** touch unrelated pages or files.
10. Do **not** invent backend data or hardcode demo statistics — use real model attributes.

### Phase 3 — Verify

11. Run through the quality checklist below.
12. Run `mvnw.cmd compile` (or the appropriate build command) if backend files were touched.
13. Report what was changed, checks performed, and any unresolved issues.

---

## 7 · Quality Checklist

After every implementation, verify:

- [ ] **Thymeleaf integrity** — no broken `th:*` expressions, no mismatched `${}` references.
- [ ] **Route correctness** — all links and form actions point to valid controller mappings.
- [ ] **No duplicate styles** — the new CSS doesn't conflict with or duplicate declarations in other CSS files (each page is standalone, so duplication of resets/tokens is acceptable).
- [ ] **Layout at 1366×768** — no horizontal overflow, no cut-off content, tables scroll gracefully if needed.
- [ ] **Layout at 1920×1080** — content is well-centered, not stretched beyond the max-width container.
- [ ] **Design token consistency** — colors, spacing, borders, and shadows match the token definitions above.
- [ ] **No regressions** — pages that were not modified still work and look the same.
- [ ] **Accessibility basics** — form labels have matching `for`/`id`, sufficient color contrast (charcoal on cream easily passes WCAG AA), semantic HTML.
- [ ] **Indonesian text** — all user-facing labels remain in Bahasa Indonesia.

---

## 8 · User Control

- If a requested change **risks breaking existing functionality**, explain the risk and ask before proceeding.
- If a change **requires backend modifications** (new controller endpoints, new model fields, new services), describe what's needed and get approval first.
- If you are **unsure about a design decision**, present options with brief pros/cons rather than guessing.
- Never silently skip a requested feature — if something can't be done within the constraints, say so.

---

## Quick Reference — Existing CSS Files → Pages

| CSS File                 | Template(s)                                                    |
| ------------------------ | -------------------------------------------------------------- |
| `admin-dashboard.css`    | `admin/dashboard.html`                                         |
| `categories.css`         | `admin/categories/index.html`, `create.html`, `admin/products/*`, `admin/stock/index.html` |
| `kasir-dashboard.css`    | `kasir/dashboard.html`                                         |
| `reports.css`            | `admin/reports/index.html`                                     |
| `transaction-history.css`| `kasir/transactions/history.html`                              |
| `transactions.css`       | `admin/transactions/*`, `kasir/transactions/checkout.html`     |
| `user-create.css`        | `admin/users/create.html`, `admin/users/edit.html`             |
| `users.css`              | `admin/users/index.html`                                       |



---
name: amana-pos-ui
description: Design and refine AMANA POS Admin and Cashier interfaces with consistent, purposeful retail UI/UX while preserving existing Spring Boot, Thymeleaf, security, and transaction functionality.
compatibility: opencode
metadata:
  project: amana-pos
  framework: spring-boot-thymeleaf
  scope: admin-and-cashier-ui
---

# AMANA POS — UI/UX Design Skill

## 1. Purpose

This skill defines the visual design system and implementation standards for the AMANA POS application.

AMANA POS is a clothing retail Point of Sale application built with:

- Java and Spring Boot
- Thymeleaf
- Spring Security
- PostgreSQL
- HTML and CSS
- Existing JavaScript where required

The goal is to create a polished, professional retail management application that feels deliberately designed for a real clothing store.

This skill applies whenever working on:
- Admin Dashboard
- Admin Products
- Admin Categories
- Admin Stock
- Admin Transactions
- Admin Reports
- Admin Users
- Cashier POS
- Cashier Dashboard
- Cashier Transaction History
- Checkout
- Receipt

The visual identity of Admin and Cashier must remain distinct.

Do not force every page into the same layout or component structure.

---

## 2. Core Design Philosophy

Follow these principles:

1. Design for actual daily retail operations.
2. Prioritize clarity, readability, hierarchy, and usability.
3. Use spacing and typography to organize information.
4. Use components only when they improve functionality.
5. Avoid unnecessary cards and decorative elements.
6. Make every button and interactive element meaningful.
7. Use real application data only.
8. Maintain consistency without making every page look identical.
9. Prefer small, deliberate refinements over unnecessary redesigns.
10. The final result must not look like a generic AI-generated dashboard.

The guiding principle is:

**Less decoration, better hierarchy, clearer information, stronger usability.**

---

## 3. Two Separate Visual Identities

### 3.1 Admin Identity

Admin uses a restrained scrapbook editorial identity inspired by:

- Warm ivory paper
- Kraft paper
- Muted navy
- Ocean blue
- Soft gold
- Subtle paper layering
- Selective torn-paper details
- Small botanical illustrations
- Subtle washi tape

The visual treatment must remain elegant and controlled.

Use scrapbook details only where they add character, such as:
- Page header
- Selected section labels
- Small decorative accents
- Empty-state illustrations

Do not decorate every card, row, button, or table.

Admin is primarily for management, inventory control, reporting, and administration.

### 3.2 Cashier Identity

Cashier must look and behave like actual retail POS software.

Use a different visual direction:

- Charcoal or dark teal sidebar
- Light neutral workspace
- White functional surfaces
- Deep teal primary actions
- Restrained borders
- Clean typography
- Compact layout
- Consistent icons

Cashier should prioritize operational speed.

Its main workflow is:

Search product → Select variant → Add to cart → Review total → Payment → Receipt.

Do not copy the Admin scrapbook layout into the cashier POS screen.

### 3.3 Shared Design Quality

Both interfaces must share:
- Typography quality
- Consistent spacing
- Accessible contrast
- Button hierarchy
- Input sizing
- Icon consistency
- Focus states
- Quality of tables and forms

They may use different colors and layouts while maintaining the same overall design quality.

---

## 4. Design Tokens

Use centralized CSS variables when appropriate.

### Admin palette

- Paper: #F7F2E8
- Paper Light: #FFFCF6
- Kraft: #C7A77B
- Kraft Dark: #A9875C
- Navy: #244766
- Ocean: #477FA3
- Blue Wash: #DCE8EE
- Muted Gold: #C7A45D
- Text: #26333A
- Muted Text: #6D7778
- Border: #E5DCCE
- Surface: #FFFFFF
- Danger: #B84E43

### Cashier palette

- Sidebar: #142D2B
- Sidebar Surface: #173633
- Primary: #1F6258
- Primary Hover: #285F57
- Workspace: #F5F6F2
- Surface: #FFFFFF
- Text: #202D31
- Secondary Text: #718087
- Border: #E3E8E4
- Warning: #B96A28
- Danger: #B83C3C

### Shared typography

Preferred:
- Nunito Sans, only when already available locally.
- Existing project font assets.
- System fallback when the preferred font is unavailable.

Suggested fallback:

`Inter, "Segoe UI", Roboto, Arial, sans-serif`

Do not add external font imports or CDN dependencies.

Suggested font hierarchy:
- Page title: 23–28px
- Section title: 16–19px
- Body: 13–15px
- Table text: 12–14px
- Secondary labels: 11–12px

Use font weights intentionally:
- 400: Body text
- 500: Secondary information
- 600: Navigation and table emphasis
- 700: Page titles and important values

Do not make every element bold.

---

## 5. Anti-AI-Slop Rules

These rules are mandatory.

Never:
- Put every section inside a card.
- Add decorative cards merely to fill empty space.
- Add random illustrations.
- Add excessive icon chips.
- Use unnecessary gradient backgrounds.
- Apply heavy shadows to everything.
- Use thick black borders.
- Repeat badge components for ordinary text.
- Make every heading uppercase.
- Create fake charts, statistics, or percentages.
- Add fake recent activities.
- Use random avatars or placeholder user information.
- Add animations to every element.
- Make all pages look like the same generic dashboard template.
- Increase page height without a functional reason.

Avoid nested cards and unnecessary container-within-container layouts.

Prefer:
- Natural page hierarchy
- Direct content sections
- Clean tables
- Purposeful whitespace
- Fine dividers
- Consistent spacing
- Minimal decorative accents
- Clear action hierarchy

Whitespace must feel intentional, not like missing content.

---

## 6. Typography and Visual Consistency

Typography must remain comfortable for daily use.

Rules:
- Use consistent font families.
- Use a predictable size scale.
- Keep labels readable.
- Align monetary values consistently.
- Use tabular numerals where useful.
- Use monospace only for identifiers such as invoice numbers and SKUs.
- Do not use decorative serif fonts for operational data.
- Do not mix unrelated font families.

Currency formatting must use Indonesian conventions.

Example:

`Rp 300.000`

Never produce:

`Rp 300000.000`

Use a consistent formatter based on the actual application values and locale.

Do not change stored monetary values just to fix display formatting.

---

## 7. Admin Page Design Rules

### 7.1 Dashboard

Dashboard should present meaningful management information.

Prioritize:
- Today's sales
- Transaction count
- Active products
- Low stock
- Monthly summary
- Recent transactions
- Stock warnings

Do not invent metrics or trends.

Use selective scrapbook details in the header or a small number of accents.

Keep tables clean.

### 7.2 Products

Product management must remain table-first.

Do not replace the product listing with a card grid.

The table may contain:
- Product image
- Product code
- Product name
- Category
- Variant count
- Size and color information
- Price information
- Total stock
- Status
- Actions

Product thumbnail rules:
- Consistent dimensions
- object-fit: cover
- Neutral image background
- No distorted images
- Consistent placeholder for products without images

Preserve product search, category filter, status filter, and existing actions.

Create, edit, and detail pages must use a consistent visual system.

### 7.3 Categories

Use a simple, readable list or table.

Prioritize category names and existing descriptions.

Do not create unnecessary category cards or fake statistics.

### 7.4 Stock

Prioritize:
- Product
- Variant
- Size
- Color
- Available stock
- Minimum stock

Low stock must be noticeable but not visually aggressive.

Keep stock movement actions clear.

### 7.5 Transactions

Use a clean transaction table.

Keep visible:
- Invoice
- Date and time
- Cashier
- Total
- Status
- Existing detail or receipt action

Use consistent number formatting and restrained status badges.

### 7.6 Reports

Prioritize period selection, actual financial summaries, and existing report data.

Do not add fake charts or percentages.

Use existing chart libraries only if already installed and supported.

### 7.7 Users

Keep account and role information readable.

Use consistent forms and tables.

Never display passwords or sensitive authentication information.

---

## 8. Cashier Page Design Rules

### 8.1 Cashier POS

The POS screen must prioritize speed and clarity.

Desktop layout:
- Dark sidebar on the left
- Product catalog in the main workspace
- Persistent cart on the right

Keep the search field easy to reach.

Product catalog should be visually compact and readable.

Each parent Product must appear only once.

Do not display one catalog tile for every ProductVariant.

### 8.2 Product Variant Selection

Variant selection must follow two stages:

Step 1: Select Color.

Step 2: Select Size.

Rules:
- Display each available color only once.
- After selecting a color, show only sizes available for that color.
- Never generate combinations that do not exist.
- Disable unavailable or out-of-stock variants.
- Display actual price and stock for the selected variant.
- Reset size selection when color changes.
- Preserve the exact ProductVariant ID.

Different variants can have different:
- Prices
- Stock quantities
- SKUs
- Active status

Never identify a selected variant using only text labels.

### 8.3 Cart

Cart must show:
- Product name
- Selected color
- Selected size
- Unit price
- Quantity
- Line subtotal
- Item count
- Final total

Rules:
- Different variants must remain separate cart lines.
- Adding the same variant must follow existing cart behavior.
- Quantity cannot exceed actual stock.
- Cart totals must reflect trusted application calculations.
- Checkout cannot proceed with an invalid or empty cart.

### 8.4 Payment

Preserve existing payment methods.

For cash payment:
- Cash received must be a non-negative rupiah amount.
- Use suitable whole-number input validation where appropriate.
- Cash received must cover the actual transaction total.
- Calculate change correctly.
- Validate again on the server.
- Prevent accidental duplicate checkout submission.

Do not clear cart contents before the server confirms successful payment.

### 8.5 Cashier Dashboard

The dashboard is an operational shift overview, not a marketing landing page.

Prefer:
- Compact title: Ringkasan Shift
- Actual sales today
- Actual transaction count
- Recent cashier transactions
- Primary action to open POS

Avoid:
- Two oversized explanatory cards
- Long introductory descriptions
- Fake shift statistics
- Decorative charts
- Large meaningless empty areas

Prefer a compact horizontal summary and a meaningful transaction table.

### 8.6 Cashier Transaction History

Use a clean table.

Columns should reflect available data:
- Invoice
- Transaction time
- Total
- Status
- Receipt action

Ensure monetary formatting is correct.

Respect the authenticated cashier's data access rules.

### 8.7 Receipt

Receipt should be readable and suitable for printing.

Preserve:
- Actual invoice number
- Transaction time
- Cashier information where available
- Product details
- Size and color
- Quantity
- Unit price
- Subtotal
- Discount if supported
- Payment information
- Cash received and change if supported

Do not fabricate receipt fields.

Keep print styling isolated from the rest of the application.

Use valid Thymeleaf expressions.

Avoid malformed literal substitution syntax.

---

## 9. Tables

Tables are fundamental to AMANA POS.

Use:
- Consistent row heights
- Clear column headers
- Fine dividers
- Balanced padding
- Proper number alignment
- Readable font sizes
- Subtle row hover states
- Compact actions

Do not:
- Put every cell inside a mini-card.
- Use heavy grid borders.
- Hide important fields to avoid overflow.
- Make action buttons too small.

If horizontal scrolling is necessary, constrain it to the table wrapper.

Do not create page-wide horizontal scrolling.

---

## 10. Forms, Modals, and Controls

Forms:
- Labels must remain visible.
- Do not rely only on placeholders.
- Group related fields logically.
- Use consistent input heights.
- Keep validation messages close to the relevant input.
- Primary and secondary actions must be visually distinct.

Modals:
- Compact and focused.
- Clear title and close action.
- Proper internal scrolling when needed.
- Avoid excessively tall dialogs.
- Ensure controls are reachable.
- Do not allow backgrounds to remain interactive when the modal is active.

Buttons:
- Primary actions should be obvious.
- Secondary actions should be quieter.
- Destructive actions must be visually distinct.
- Disabled states must be clear.
- Every visible button must perform its advertised action.

---

## 11. Icons and Images

Icons:
- Use the project's existing icon library when available.
- Otherwise use consistent inline SVG.
- Keep stroke width and dimensions consistent.
- Do not use emoji as operational icons.
- Do not use Unicode square characters as icons.

Images:
- Use actual uploaded product images.
- Preserve aspect ratio.
- Provide consistent placeholders.
- Use appropriate alternative text.
- Do not use unrelated decorative stock images as actual product data.

---

## 12. Accessibility and Interaction

Maintain:
- Clear keyboard focus states
- Sufficient contrast
- Accessible labels
- Readable text
- Logical tab navigation
- Visible loading and disabled states
- Proper error and success feedback

Use subtle transitions only when helpful.

Respect:

`prefers-reduced-motion: reduce`

Avoid JavaScript solely for decoration.

Keyboard shortcuts must not interfere with ordinary typing or barcode scanner input.

---

## 13. Responsive Layout

Primary desktop targets:
- 1366 × 768
- 1920 × 1080

Rules:
- Keep layout balanced.
- Prevent overlapping content.
- Keep navigation usable.
- Prevent page-wide horizontal overflow.
- Keep cart controls accessible.
- Allow controlled scrolling in long product catalogs and tables.
- Avoid fixed heights that hide important content.
- Do not shrink text excessively to force everything onto one screen.

---

## 14. Backend and Functional Guardrails

This project uses existing Spring Boot and Thymeleaf architecture.

Before modifying anything:
1. Inspect the actual controller.
2. Inspect the actual service.
3. Inspect the repository.
4. Inspect the relevant entity and relationships.
5. Inspect the Thymeleaf template.
6. Inspect stylesheet usage.

Never invent:
- Routes
- Entity fields
- Model attributes
- Service methods
- Database columns
- Payment methods
- Statistics
- User data

Preserve:
- Existing database schema
- Authentication
- Authorization
- Role restrictions
- CSRF
- Form methods and actions
- Product code generation
- Product variants
- Stock calculations
- Transaction calculations
- Invoice generation
- Payment validation
- Transaction persistence

Never enable Open Session in View merely to hide lazy-loading errors.

Prevent LazyInitializationException by loading required relationships safely or mapping data into DTOs within a transaction.

Avoid changing shared CSS globally without checking which pages depend on it.

Use page-specific stylesheets where appropriate.

---

## 15. Implementation Workflow

Always follow this workflow.

### Stage 1 — Inspect

Identify:
- Actual route
- Controller mapping
- Template path
- Model attributes
- Relevant service/repository
- Existing CSS
- Shared fragments
- Related interactions

Do not assume file names.

### Stage 2 — Plan

Create a concise implementation plan.

Identify exactly which files need changes.

Avoid unnecessary backend modifications.

### Stage 3 — Implement

- Edit the actual existing files.
- Preserve working features.
- Keep changes scoped.
- Avoid duplicate CSS.
- Do not introduce mock data.
- Do not rewrite unrelated modules.

Do not stop after producing a plan when implementation was requested.

### Stage 4 — Review

Inspect the final diff.

Check for:
- Unintended changes
- Broken Thymeleaf expressions
- Missing routes
- Invalid bindings
- CSS conflicts
- Broken form actions
- Security regressions

### Stage 5 — Verify

Run:

`.\mvnw.cmd compile`

`.\mvnw.cmd test-compile`

Run relevant tests when appropriate.

Browser verification must be reported separately from compile verification.

Never claim a page was browser-tested unless it was actually opened and inspected.

---

## 16. Anti-Regression Rules

- Never overwrite user changes without inspection.
- Never reset or clean the working tree.
- Never delete existing functionality to simplify design.
- Never redesign Admin while working on Cashier unless explicitly requested.
- Never redesign Cashier while working on Admin unless explicitly requested.
- Never change database schema for a purely visual request.
- Never use fake transactions or hardcoded prices.
- Never claim a successful build without running it.
- Never assume a working compile means the Thymeleaf page renders correctly.

---

## 17. Definition of Done

A UI/UX task is complete only when:

- The visual result follows the correct Admin or Cashier identity.
- The page is readable and well aligned.
- There is no unnecessary card clutter.
- Important information is easy to scan.
- All visible interactions work.
- Existing functionality is preserved.
- Routes and Thymeleaf bindings remain valid.
- No unintended backend or security changes occurred.
- Compile and test-compile results are reported accurately.
- Remaining browser checks are clearly identified.

The final design must feel like a deliberate, professional clothing retail application.

**Do not optimize for visual novelty. Optimize for usability, consistency, clarity, and operational quality.**
