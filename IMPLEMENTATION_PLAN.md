# AMANA POS UI/UX Implementation Plan

## Current State Analysis

### Checkout Cart (Phase 1)
**Current Implementation:**
- Cart stored in JavaScript `Map` (line 341 in checkout.html)
- Page refresh = cart loss
- Failed checkout = cart loss
- No server-side persistence
- POST request sends variantId[] and quantity[] arrays

**Required Changes:**
- Add session-based cart storage
- Create CartItem entity/DTO for session storage
- Add cart persistence endpoints
- Restore cart on page load
- Clear cart only after successful checkout
- Keep cart on failed checkout

### Shift Status (Phase 2)
**Current State:**
- Shows "Shift Aktif" (line 63)
- No actual shift management
- Just authenticated session

**Required Changes:**
- Replace "Shift Aktif" with "Sesi Kasir Aktif"

### Discount Display (Phase 3)
**Current State:**
- Shows "Diskon Rp 0" (line 240-242)
- Transaction entity has discount field (CheckoutService line 70)
- No UI input for discount
- CheckoutService sets discount to ZERO (CheckoutService line 70)

**Required Changes:**
- Remove discount row from UI if not implemented
- OR expose discount input if backend supports it

### User Management (Phase 4)
**Current State:**
- Need to inspect UserController and user templates

### Feedback Dialogs (Phase 5)
**Current State:**
- Uses alert() at lines 695, 704, 731, 746, 960, 968
- Uses confirm() at line 746

### Variant Modal (Phase 6)
**Current State:**
- Modal exists but needs accessibility improvements
- No focus management
- No focus trap

### Pagination (Phase 7)
**Need to inspect:**
- UserController
- StockMovementController
- TransactionController
- ReportController

### Reports (Phase 8)
**Need to inspect:**
- ReportController
- Transaction entity for historical cost

---

## Implementation Order

### Phase 1: Cart Persistence (HIGH PRIORITY)
1. Create `CartSessionItem` DTO
2. Create `CartSessionService` for session management
3. Add cart session endpoints to `KasirTransactionController`
4. Modify checkout.html JavaScript to persist cart to session
5. Restore cart on page load from session
6. Clear session cart only on successful checkout
7. Add tests

### Phase 2: Session Status (HIGH PRIORITY)
1. Change "Shift Aktif" to "Sesi Kasir Aktif" in checkout.html
2. Apply same change to other cashier pages

### Phase 3: Discount UI (MEDIUM PRIORITY)
1. Inspect Transaction entity discount usage
2. Remove discount row from UI if not implemented

### Phase 4: User Management (MEDIUM PRIORITY)
1. Inspect user templates and controller
2. Display fullName column
3. Improve layout

### Phase 5: Feedback (MEDIUM PRIORITY)
1. Create notification component CSS
2. Replace alert() with inline notifications
3. Replace confirm() with modal confirmations

### Phase 6: Variant Modal Accessibility (MEDIUM PRIORITY)
1. Add focus management
2. Add focus trap
3. Add Escape key support
4. Improve ARIA labels

### Phase 7: Pagination (MEDIUM PRIORITY)
1. Inspect controllers
2. Add Spring Data Pageable where appropriate
3. Update templates with pagination controls

### Phase 8: Reports (MEDIUM PRIORITY)
1. Inspect ReportController
2. Add product-based reports
3. Check historical cost availability
4. Avoid false profit calculations

---

## Files to Create/Modify

### Phase 1 - Cart Persistence
**NEW FILES:**
- `src/main/java/com/kasir/amana/DTO/CartSessionItem.java`
- `src/main/java/com/kasir/amana/Service/CartSessionService.java`
- `src/test/java/com/kasir/amana/CartSessionTests.java`

**MODIFIED FILES:**
- `src/main/java/com/kasir/amana/Controller/Kasir/KasirTransactionController.java`
- `src/main/resources/templates/kasir/transactions/checkout.html`

### Phase 2 - Session Status
**MODIFIED FILES:**
- `src/main/resources/templates/kasir/transactions/checkout.html`
- `src/main/resources/templates/kasir/dashboard.html`

### Phase 3 - Discount
**MODIFIED FILES:**
- `src/main/resources/templates/kasir/transactions/checkout.html`

### Phases 4-8
To be determined after inspection.

---

## Testing Strategy

### Cart Persistence Tests
1. Add item to cart → refresh → cart restored
2. Failed checkout → cart preserved with error message
3. Successful checkout → cart cleared
4. Different cashier sessions → isolated carts
5. Insufficient stock validation preserved

### Integration Tests
1. Full checkout flow with session
2. Concurrent session isolation
3. Session timeout handling

---

## Starting Implementation Now

Beginning with Phase 1: Cart Persistence
