# CMIPS Manual Testing Guide: Initial Contact → Case Creation

> **DSD Section 20 Aligned** — All error codes use `EM OS XXX` format. All business rules use `BR OS XX` format.
> Organized to match the Playwright test suite structure (`initial-contact.spec.ts` — 71 tests).

## Prerequisites

- Docker containers running: `cd cmipsapplication && docker-compose up -d`
- Frontend: http://localhost:3000
- Backend: http://localhost:8081
- Keycloak: http://localhost:8080
- Login credentials: `cmipsadmin` / `password123`

---

## LOGIN

1. Open http://localhost:3000
2. You'll see the **"Login to Your Account"** page (Keycloak)
3. Enter: Username = `cmipsadmin`, Password = `password123`
4. Click **"Login"** button
5. **"Login to Your Account"** disappears, you land on the dashboard/workspace

---

## SR: PERSON SEARCH — REFERRAL PATH (9 tests)

**Page:** http://localhost:3000/persons/search/referral
**Corresponds to:** Tests SR-001 through SR-009

### SR-001 | Page renders correctly

1. Navigate to http://localhost:3000/persons/search/referral
2. **Expected:**
   - Page title: **"Person Search — New Referral"**
   - **"Search Criteria"** panel visible
   - Fields visible: **Last Name**, **First Name**, **Date of Birth**, **SSN (masked)**
   - Buttons: **"Search"** and **"Clear"**

### SR-002 | Last Name required

1. Click **"Search"** without entering any fields
2. **Expected:** Browser alert: *"Last Name is required"*

### SR-003 | SSN masking

1. In **SSN (masked)** field, type: `234000001` (9 digits)
2. **Expected:** Display shows `XXXXX0001` — first 5 digits masked as X, last 4 visible

### SR-004 | No-match search (BR-3)

1. Type Last Name: `ZzzPlaywrightNoMatch9999`
2. Click **"Search"**
3. **Expected:**
   - Message: *"No matching persons found"*
   - **"Create New Referral"** button visible

### SR-005 | Search with results (BR-2)

1. Type Last Name: `Smith`
2. Click **"Search"**
3. **Expected:**
   - Results panel shows **"Results (N)"** with count
   - **"Create New Referral"** button always visible after search

### SR-006 | PersonType badges in results

1. Search for `Smith`
2. **Expected:** Results table has **"Person Type"** column header with badges in each row

### SR-007 | Create New Referral navigation

1. Search for any name, then click **"Create New Referral"**
2. **Expected:** Navigates to http://localhost:3000/persons/referral/new

### SR-008 | Clear button

1. Search for `Smith` (results appear)
2. Click **"Clear"**
3. **Expected:** Last Name field cleared, results panel hidden

### SR-009 | Soundex near-match (BR-4)

1. Type Last Name: `Smyth` (phonetic variant of Smith, >= 3 chars)
2. Click **"Search"**
3. **Expected:** Results appear (Soundex matches) OR "No matching persons found" OR near-match/phonetic banner

---

## SA: PERSON SEARCH — APPLICATION PATH (3 tests)

**Page:** http://localhost:3000/persons/search/application
**Corresponds to:** Tests SA-001 through SA-003

### SA-001 | Page title

1. Navigate to http://localhost:3000/persons/search/application
2. **Expected:**
   - Title: **"Person Search — New Application"**
   - Subtitle: *"Search for an existing person before starting a new application"*

### SA-002 | Create New Application navigation

1. Search Last Name: `ZzzAppTestNoMatch9999`
2. Click **"Create New Application"**
3. **Expected:** Navigates to URL containing `/applications/new` and `source=new`

### SA-003 | Select existing person

1. Search Last Name: `Smith`
2. Click **"Select"** on any result row
3. **Expected:** Navigates to URL containing `/applications/new`, `source=existing`, and `recipientId=`

---

## CR: CREATE REFERRAL — DSD CI-67784 (19 tests)

**Page:** http://localhost:3000/persons/referral/new
**Corresponds to:** Tests CR-001 through CR-019

> **DSD Note:** Referral Reason, Referring Worker, Referring Agency, Program Type, Assigned Worker were **removed**. Gender and DOB are **optional** for Referral (required only for Application).

### CR-001 | Page structure — 5 sections + County panel

1. Navigate to http://localhost:3000/persons/referral/new
2. **Expected sections:**
   - **"Create Referral (CI-67784)"** page title
   - **Section 1 — Referral Information**
   - **Section 2 — Person Demographics**
   - **Section 3 — Residence Address**
   - **Section 4 — Mailing Address**
   - **Section 5 — Phone / Contact**
   - **County** (standalone panel, NOT in any section)
3. **NOT expected:** Section 6 — Program Information (removed)

### CR-002 | EM OS 200 — Referral Date required

1. Clear the Referral Date field
2. Click **"Save Referral"**
3. **Expected:** Error `EM OS 200` visible + red banner: *"Please correct the highlighted errors below."*

### CR-003 | EM OS 001 — Referral Source required

1. Fill Referral Date: `2026-01-15`, leave Referral Source empty
2. Click **"Save Referral"**
3. **Expected:** Error `EM OS 001` visible

### CR-004 | DSD — Removed fields not present

1. Look at Section 1 — Referral Information
2. **Expected:**
   - Only **Referral Date *** and **Referral Source *** fields present
   - No textarea (Referral Reason was removed)
   - No Referring Worker, Referring Agency, Program Type, Assigned Worker fields

### CR-005 | EM OS 005 — Last Name required

1. Fill Referral Date + Source
2. Fill First Name: `TestFirst`, leave Last Name empty
3. Click **"Save Referral"**
4. **Expected:** Error `EM OS 005` visible

### CR-006 | EM OS 006 — First Name required

1. Fill Referral Date + Source
2. Fill Last Name: `TestLast`, leave First Name empty
3. Click **"Save Referral"**
4. **Expected:** Error `EM OS 006` visible

### CR-007 | Gender is optional for Referral (DSD)

1. Fill: Referral Date, Source, Last Name, First Name — leave Gender empty
2. Click **"Save Referral"**
3. **Expected:** `EM OS 008` does **NOT** appear (Gender is optional for Referral)
4. (Will still fail on EM OS 080 address/phone — that's expected)

### CR-008 | EM OS 003 — Future DOB rejected

1. Fill required fields + Date of Birth: `2099-12-31`
2. Click **"Save Referral"**
3. **Expected:** Error `EM OS 003` + text contains *"future"*

### CR-009 | EM OS 004 — DOB > 120 years ago rejected

1. Fill required fields + Date of Birth: `1900-01-01`
2. Click **"Save Referral"**
3. **Expected:** Error `EM OS 004` + text contains *"120 years"*

### CR-010 | SSN masking

1. In **SSN (optional)** field, type: `234567890`
2. **Expected:** Display shows `XXXXX7890` (first 5 masked, last 4 visible, 9 chars total)

### CR-011 | EM OS 010 — SSN starting with 9

1. Fill required fields, enter SSN: `900000001`
2. Click **"Save Referral"**
3. **Expected:** Error `EM OS 010` visible

### CR-012 | EM OS 010 — All identical digits

1. Fill required fields, enter SSN: `111111111`
2. Click **"Save Referral"**
3. **Expected:** Error `EM OS 010` visible

### CR-013 | EM OS 010 — Short SSN (< 9 digits)

1. Fill required fields, enter SSN: `12345678` (8 digits)
2. Click **"Save Referral"**
3. **Expected:** Error `EM OS 010` visible

### CR-014 | EM OS 080 — Address or Phone required

1. Fill: Referral Date, Source, Last Name, First Name, County
2. Do NOT fill residence address or phone
3. Click **"Save Referral"**
4. **Expected:** Error `EM OS 080` visible

### CR-015 | EM OS 210 — County required

1. Fill: Referral Date, Source, Last Name, First Name, Residence Address (via modal)
2. Do NOT select County
3. Click **"Save Referral"**
4. **Expected:** Error `EM OS 210` visible

### CR-016 | Address Verification Modal structure

1. Click **"Enter / Verify Address"** button in Section 3
2. **Expected modal contents:**
   - Title: **"Residence Address Verification"**
   - Fields: Street Number (`e.g. 1234`), Street Name (`e.g. Main St`), City (`e.g. Sacramento`), ZIP (`e.g. 95814`)
   - **"Verify Address with CASS"** button — DISABLED until Street Name + ZIP filled
   - Close button: **"×"**
3. Click **"×"** to dismiss

### CR-017 | Referral Source — 40 DSD values

1. Open the **Referral Source** dropdown
2. **Expected:** At least 41 options (1 placeholder + 40 values)
3. Spot-check: `Self`, `Adult Protective Services`, `Anonymous` all present

### CR-018 | Same as Residence checkbox

1. In Section 4 — Mailing Address, check **"Same as Residence Address"**
2. **Expected:** Mailing address "Enter / Verify Address" button hidden

### CR-019 | Happy path — full referral creation

1. Fill all required fields:
   - Referral Date: `2026-01-15`, Source: `Self`
   - Last Name: unique value (e.g., `PWTEST`), First Name: `Playwright`
   - Residence Address via modal (Street Name + City + ZIP, skip verification)
   - County: `Sacramento`
2. Click **"Save Referral"**
3. **Expected:**
   - Navigates to `/recipients/{id}` (RecipientDetailPage)
   - Name displayed (UPPERCASE per BR OS 28/29/30)
   - Blue **"Open Referral"** badge visible

---

## AV: ADDRESS VERIFICATION MODAL — CASS (6 tests)

**Page:** Accessed from Create Referral or Application wizard
**Corresponds to:** Tests AV-001 through AV-006

> Open the modal by clicking **"Enter / Verify Address"** on the Create Referral page.

### AV-001 | Modal structure

1. Open the Address Verification Modal
2. **Expected:**
   - Street Name field (`e.g. Main St`)
   - ZIP field (`e.g. 95814`)
   - Unit Type dropdown with `APT` option
   - **"Verify Address with CASS"** button — DISABLED
   - **"Skip Verification"** button visible

### AV-002 | Verify button enables

1. Fill Street Name: `Oak Ave` (ZIP still empty)
2. **Expected:** Verify button DISABLED
3. Fill ZIP: `95814`
4. **Expected:** Verify button ENABLED

### AV-003 | CASS match — green banner (EM OS 178)

1. Fill: Street Name = `Main St`, City = `Sacramento`, ZIP = `95814`
2. Click **"Verify Address with CASS"**
3. **Expected:**
   - Green banner: **"Address Verified"**
   - Text: *"CASS confirms this address"*
   - **"Use This Address"** button visible

### AV-004 | Use This Address — modal closes with CASS badge

1. After CASS verification succeeds (AV-003), click **"Use This Address"**
2. **Expected:**
   - Modal closes
   - Residence section shows **"CASS Verified"** badge (green)

### AV-005 | Skip Verification — unverified (EM OS 177)

1. Fill address fields, click **"Skip Verification"**
2. **Expected:**
   - Modal closes (non-blocking)
   - Address saved with **"Unverified"** indicator

### AV-006 | Close (×) — dismisses without saving

1. Fill Street Name: `Should Not Save`
2. Click **"×"** button
3. **Expected:**
   - Modal closes
   - Address section still shows *"No address entered"*

---

## APP: APPLICATION WIZARD — 4-STEP (25 tests)

**Page:** http://localhost:3000/applications/new
**Corresponds to:** Tests APP-001 through APP-025

> **Important:** Step 2 validation is **sequential** — the first failing field stops validation. You must fix errors in order: Last Name → First Name → DOB → Gender → SSN/Blank SSN Reason → Ethnicity → Spoken Language → Mailing Address.

### Step 1: Duplicate Check

#### APP-001 | Step indicator

1. Navigate to http://localhost:3000/applications/new
2. **Expected:** 4 steps visible: **Duplicate Check** | **Applicant Info** | **CIN Clearance** | **Review & Submit**
3. Step 1 panel header: **"Step 1: Duplicate Check"**

#### APP-002 | Last Name required

1. Click **"Check for Duplicates"** without entering Last Name
2. **Expected:** Error: *"Last Name is required"*

#### APP-003 | SSN masking in Step 1

1. In SSN field (placeholder `XXX-XX-####`), type: `234000001`
2. **Expected:** Display shows `XXXXX0001`

#### APP-004 | No duplicates found

1. Last Name: `ZzzPlaywrightNoDup9999`, click **"Check for Duplicates"**
2. **Expected:**
   - *"No duplicate records found"*
   - **"Continue as New Applicant"** button visible

#### APP-005 | Step 1 → Step 2 transition

1. After no duplicates found, click **"Continue as New Applicant"**
2. **Expected:**
   - **"Applicant Demographics"** panel visible (Step 2)
   - Step 1 indicator shows checkmark **✓**

#### APP-006 | Duplicate results (BR-1)

1. Last Name: `Smith`, click **"Check for Duplicates"**
2. **Expected:** Either "Use This Person" buttons per row (if duplicates found) OR "No duplicate records found"

### Step 2: Applicant Demographics

> **Navigate to Step 2:** Last Name → Check for Duplicates → Continue as New Applicant

#### APP-007 | EM OS 005 — Last Name required

1. Clear Last Name in Step 2
2. Click **"Next: CIN Clearance"**
3. **Expected:** Error `EM OS 005` or *"Last Name is required"*

#### APP-008 | EM OS 006 — First Name required

1. Fill Last Name: `TestLast`, leave First Name empty
2. Click **"Next: CIN Clearance"**
3. **Expected:** Error `EM OS 006` or *"First Name is required"*

#### APP-009 | EM OS 003 — Future DOB

1. Fill Last Name + First Name, Date of Birth: `2099-01-01`
2. Click **"Next: CIN Clearance"**
3. **Expected:** Error `EM OS 003` or *"future"*

#### APP-010 | EM OS 010 — SSN starting with 9

1. Fill: Last Name, First Name, DOB: `1990-01-15`, Gender: `Male`
2. In **SSN *** field, type: `900000001`
3. Click **"Next: CIN Clearance"**
4. **Expected:** Error `EM OS 010`

#### APP-011 | EM OS 010 — All-same SSN

1. Same setup as APP-010, SSN: `222222222`
2. **Expected:** Error `EM OS 010`

#### APP-012 | EM OS 010 — Short SSN

1. Same setup as APP-010, SSN: `12345678` (8 digits)
2. **Expected:** Error `EM OS 010`

#### APP-013 | Address Verification Modal in Step 2

1. In Step 2, click **"Verify Residence Address (CASS)"**
2. **Expected:** Modal opens with title **"Verify Residence Address"**
3. Close with **"×"**

#### APP-014 | Same as Residence checkbox

1. In Step 2, check **"Same as Residence Address"**
2. **Expected:** **"Verify Mailing Address (CASS)"** button hidden

#### APP-015 | BR-14 — Changing demographics clears CIN

1. Fill ALL required Step 2 fields:
   - Last Name: `Smith`, First Name: `John`
   - DOB: `1990-01-15`, Gender: `Male`
   - Blank SSN Reason: `Refused to Provide`
   - Ethnicity: `White`, Spoken Language: `English`
   - Check "Same as Residence Address"
2. Click **"Next: CIN Clearance"** → arrives at Step 3
3. Click **"Back"** → returns to Step 2
4. Change Last Name to `Johnson`
5. Click **"Next: CIN Clearance"** again
6. **Expected:** CIN badge shows **"Not Performed"**, CIN field empty (cleared)

### Step 2: Required Fields for Application (DSD CI-67788)

All required Step 2 fields that must be filled to advance to Step 3:

| Field Label | Required Value | EM Code if Missing |
|-------------|---------------|-------------------|
| Last Name * | Any text | EM OS 005 |
| First Name * | Any text | EM OS 006 |
| Date of Birth | Valid date (not future, not > 120yr) | EM OS 003 |
| Gender * | Male/Female/Non-Binary/Unknown | EM OS 008 |
| SSN * (or Blank SSN Reason) | 9-digit SSN OR select Blank SSN Reason | EM OS 013 |
| Ethnicity * | Any option | EM OS 009 |
| Spoken Language * | Any option | EM OS 011 |
| Mailing Address | Enter address OR check "Same as Residence" | EM OS 018 |

**Not required in Step 2:** Title, Suffix, Middle Name, Gender Identity, Sexual Orientation, Written Language, Medi-Cal Pseudo Number, Residence Address Type, Meets Residency Requirements

### Step 3: CIN Clearance

> **Navigate to Step 3:** Fill all required Step 2 fields → "Next: CIN Clearance"

#### APP-016 | CIN badge starts at "Not Performed"

1. Arrive at Step 3
2. **Expected:**
   - Badge: **"Not Performed"** (gray)
   - CIN input with placeholder *"Run CIN Clearance →"*
   - 🔍 button with title **"Run SCI CIN Clearance"**

#### APP-017 | CIN Search Modal (OI transaction)

1. Click the 🔍 button (**"Run SCI CIN Clearance"**)
2. **Expected:** CIN Search Modal opens — *"Statewide Client Index"* / *"CIN Search"*
3. Click **"Cancel"** in the modal
4. **Expected:** Badge changes to **"No CIN Match"** (orange)

#### APP-018 | BR-9: No CIN Match — S1/SAWS banner

1. After CIN Search → Cancel (No CIN Match):
2. **Expected:** Informational banner mentioning *"S1 referral"* or *"SAWS"* or *"no CIN found"*

#### APP-019 | EM OS 210 — County required before Review

1. In Step 3, do NOT select County
2. Click **"Next: Review"**
3. **Expected:** Error `EM OS 210` or *"county required"*

#### APP-020 | Step 3 → Step 4 transition

1. Select County: `Sacramento`
2. Click **"Next: Review"**
3. **Expected:**
   - **"Review: Applicant Information"** panel visible
   - **"Review: CIN & Case Details"** panel visible
   - Step 3 indicator shows checkmark **✓**

### Step 3: Case Details Fields

| Field Label | Notes |
|-------------|-------|
| Client Index Number (CIN) | Read-only, populated by CIN clearance |
| Program Type | Dropdown: IHSS, PCSP, WPCS, IHSS + PCSP |
| County * | Required — 58 California counties |
| Case Opening Date | Date input |
| Assigned Worker | Text input (worker username) |
| IHSS Referral Date | Date input |
| Written Language | Text input |
| Interpreter Available | Yes/No dropdown |

### Step 4: Review & Submit

> **Navigate to Step 4:** Fill Step 2 + Step 3 (County required) → "Next: Review"

#### APP-021 | Review shows demographics and county

1. Arrive at Step 4
2. **Expected:**
   - Name displayed (First + Last)
   - County: `Sacramento`
   - Buttons: **"Save Application Only"**, **"Create Application + Case"**, **"Back"**

#### APP-022 | CIN not performed warning

1. Arrive at Step 4 without performing CIN clearance
2. **Expected:** Warning: *"CIN clearance has not been performed"*

#### APP-023 | EM OS 176 — Create App+Case blocked without CIN

1. Click **"Create Application + Case"** (without CIN clearance)
2. **Expected:** Error `EM OS 176` or *"CIN Clearance must be performed"*

#### APP-024 | BR-9 — Create without CIN (after No CIN Match)

1. Do CIN clearance → Cancel → No CIN Match
2. Fill County, proceed to Step 4
3. Click **"Create Application + Case"**
4. **Expected:** Modal appears with text about *"without CIN"* or *"no CIN"* or *"SAWS"* or *"S1"*
5. Cancel the modal

#### APP-025 | ?source=existing&recipientId — pre-filled at Step 2

1. Navigate to: `http://localhost:3000/applications/new?recipientId={id}&source=existing`
   (Use a valid recipientId from `/recipients`)
2. **Expected:**
   - Starts at Step 2 (Step 1 skipped)
   - Step 1 indicator shows checkmark **✓**
   - Banner: *"Pre-filled from existing record"*
   - Fields pre-populated from the existing person's data

---

## PH: PERSON HOME — RECIPIENT DETAIL PAGE (11 tests)

**Page:** http://localhost:3000/recipients/{id}
**Corresponds to:** Tests PH-001 through PH-011

> **Setup:** Create a fresh referral first (CR-019 happy path) to get an Open Referral person.

### PH-001 | Open Referral badge

1. After creating a referral, view the Person Home page
2. **Expected:** Blue **"Open Referral"** badge visible

### PH-002 | Open Referral action buttons

1. On an Open Referral person:
2. **Expected visible:** **"Start Application"**, **"Close Referral"**, **"Edit"**
3. **Expected NOT visible:** "Re-open Referral", "Continue Application"

### PH-003 | Close Referral Modal (BR-20)

1. Click **"Close Referral"**
2. **Expected:**
   - Modal opens with **"Reason *"** dropdown
   - Close Referral submit button is **DISABLED** (no reason selected)
3. Select reason: `Declined Services`
4. **Expected:** Submit button becomes **ENABLED**
5. Click **"Cancel"** — modal closes without action

### PH-004 | Close Referral → Closed Referral badge

1. Click **"Close Referral"**, select reason: `No Longer Eligible`, submit
2. **Expected:**
   - Badge changes to gray **"Closed Referral"**
   - **"Re-open Referral"** and **"Start Application"** buttons visible
   - **"Close Referral"** button NOT visible

### PH-005 | BR-8 — Closed Referral can start Application

1. On a Closed Referral person, click **"Start Application"**
2. **Expected:** Navigates to `/applications/new?source=existing&recipientId={id}`

### PH-006 | Re-open Referral Modal

1. On a Closed Referral person, click **"Re-open Referral"**
2. **Expected:**
   - Modal opens with date field pre-filled with **today's date**
   - Re-open Referral button is **ENABLED**
3. Click **"Cancel"**

### PH-007 | Re-open → badge changes back to Open Referral

1. On a Closed Referral person, click **"Re-open Referral"** → submit
2. **Expected:**
   - Badge changes back to blue **"Open Referral"**
   - **"Start Application"** and **"Close Referral"** buttons visible

### PH-008 | Overview tab — PersonType badge

1. Click **"Overview"** tab
2. Look for **Person Type:** row
3. **Expected:** Shows styled badge **"Open Referral"** (not plain text)

### PH-009 | Start Application URL

1. On an Open Referral person, click **"Start Application"**
2. **Expected:** URL contains `recipientId={id}` and `source=existing`
3. **Expected:** Banner: *"Pre-filled from existing record"*

### PH-010 | SideNav "New Referral" link

1. Go to http://localhost:3000/workspace
2. Expand the side navigation (click ▶ if collapsed)
3. Click **"New Referral"**
4. **Expected:** Navigates to `/persons/search/referral`
5. **Expected:** Page title: **"Person Search — New Referral"**

### PH-011 | SideNav "New Application" link

1. Go to http://localhost:3000/workspace
2. Expand the side navigation
3. Click **"New Application"**
4. **Expected:** Navigates to `/persons/search/application`
5. **Expected:** Page title: **"Person Search — New Application"**

---

## CC: CREATE CASE — DSD Gap Fixes (8 tests)

**Page:** http://localhost:3000/cases/new
**Covers:** EM OS 067, EM OS 176 (referral date), EM OS 186, new Case Home fields

> **DSD Gap Fixes Applied:**
> - EM OS 175 (future referral date block) was **CANCELLED** by ASR Sprint 43
> - IHSS Referral Date now allows post-dating up to **2 weeks** from today
> - EM OS 067: Assigned Worker is now **required** on Create Case
> - EM OS 186: Informational SAWS referral notice on case creation without CIN
> - PR00901A: Full-data payload to Payroll (15 fields per DSD pages 245-247)
> - BR OS 13: IH18 sent to MEDS when case created with active Medi-Cal CIN
> - BR OS 16: S8 sent to SAWS for non-excluded aid codes (not 10, 20, 60)
> - BR OS 44: Person Contact record created on case save
> - Case Home: 11 new DSD fields displayed

### CC-001 | Page renders — Assigned Worker marked required

1. Navigate to http://localhost:3000/cases/new
2. **Expected:**
   - Page title: **"New Case"**
   - **Assignment** panel shows **"Assigned Worker *"** (asterisk = required)
   - Fields: Last Name, First Name, Gender, Date of Birth, SSN, CIN Clearance, County, Spoken/Written Language, Interpreter Available, Assigned Worker, IHSS Referral Date

### CC-002 | EM OS 067 — Assigned Worker required

1. Fill: Last Name = `TESTWORKER`, First Name = `JANE`, County = `19`
2. Leave **Assigned Worker** empty
3. Click **"Save"**
4. **Expected:** Red error banner: `EM OS 067: Assigned Worker must be indicated.`
5. Case is NOT created

### CC-003 | IHSS Referral Date — 2-week post-dating allowed

1. Fill: Last Name = `TESTDATE`, First Name = `JOHN`, County = `19`, Assigned Worker = `worker001`
2. Set IHSS Referral Date to **10 days from today** (within 2-week limit)
3. Click **"Save"** (will hit EM OS 176 for CIN clearance — that's OK, we're testing date validation doesn't block)
4. **Expected:** No date-related error appears. The error, if any, should be about CIN clearance (EM OS 176), **NOT** about the referral date.

### CC-004 | IHSS Referral Date — beyond 2 weeks blocked

1. Fill: Last Name = `TESTDATE`, First Name = `JOHN`, County = `19`, Assigned Worker = `worker001`
2. Set IHSS Referral Date to **3 weeks from today** (e.g., 21 days in the future)
3. Click **"Save"**
4. **Expected:** Red error banner: `IHSS Referral Date cannot be more than 2 weeks from today.`
5. Case is NOT created

### CC-005 | EM OS 176 — CIN clearance required

1. Fill all required fields (Last Name, First Name, County, Assigned Worker)
2. Do NOT perform CIN clearance (leave CIN badge as "Not Performed")
3. Click **"Save"**
4. **Expected:** Error: `EM OS 176: CIN Clearance must be performed before saving.`

### CC-006 | EM OS 186 — No CIN Match → SAWS referral notice

1. Fill: Last Name = `NOCIN`, First Name = `TEST`, County = `19`, Assigned Worker = `worker001`
2. Click 🔍 → CIN Search Modal → **Cancel** (no CIN match)
3. CIN badge shows **"No CIN Match"** (orange)
4. Click **"Save"**
5. **Expected:** "Create Case Without CIN" modal appears (BR-9)
6. Click **"Continue"** in the modal
7. **Expected:**
   - Navigates to Case Home (`/cases/{id}`)
   - **Blue informational banner** visible: `EM OS 186: CIN not selected, Medi-Cal Eligibility Referral will be sent to SAWS.`
   - Banner has a **×** dismiss button

### CC-007 | EM OS 186 banner dismiss

1. After CC-006, on the Case Home page with the blue EM OS 186 banner
2. Click the **×** button on the blue banner
3. **Expected:** Banner disappears
4. Refresh the page
5. **Expected:** Banner does NOT reappear (it was a one-time informational message)

### CC-008 | Case Home — new DSD fields visible

1. Navigate to any existing case: http://localhost:3000/cases/15 (or use the case from CC-006)
2. Click **"Overview"** tab (should be default)
3. **Expected — Case Details panel shows all these fields:**

| Field Label | Expected Value |
|-------------|---------------|
| Case Number | Format: `XX-YYYYMMDD-XXXXX` |
| Status | PENDING (badge) |
| County | The county code entered |
| Case Owner | Worker username |
| CIN | CIN value or `—` |
| **IHSS Referral Date** | Date entered or `—` |
| **District Office** | Value or `—` |
| **Interpreter Available** | `Yes`, `No`, or `—` |
| **Medi-Cal Status** | `ACTIVE`, `PENDING_SAWS`, or `—` |
| **Medi-Cal Aid Code** | Aid code or `—` |
| **Medi-Cal Elig. Referral Date** | Date or `—` |
| **Companion Case** | Value or `—` |
| **State Hearing** | Value or `—` |
| **Household Members** | Number or `—` |
| **Mail Designee** | Value or `—` |
| Created | Date/time |
| Last Modified | Date/time |

> **Bold fields** are newly added DSD Case Home fields. For newly created cases, most will show `—` (null). The key ones with values will be:
> - IHSS Referral Date (if entered during case creation)
> - Medi-Cal Status (`PENDING_SAWS` for no-CIN cases, `ACTIVE` for CIN-cleared cases)
> - Interpreter Available (if set during creation)

---

## CC-API: CREATE CASE — API Verification (5 tests)

> These tests verify the backend directly via `curl`. Use token auth or test from a REST client.

### CC-API-001 | EM OS 067 via API

```bash
curl -s -X POST "http://localhost:8081/api/cases" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"lastName":"TEST","firstName":"JANE","countyCode":"19","caseOwnerId":"","cin":"12345678A","cinClearanceStatus":"CLEARED","mediCalStatus":"ACTIVE","createdBy":"cmipsadmin"}'
```
**Expected:** `400` → `{"error": "EM OS 067: Assigned worker must be entered."}`

### CC-API-002 | IHSS Referral Date > 2 weeks via API

```bash
# Use a date 21 days from today
curl -s -X POST "http://localhost:8081/api/cases" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"lastName":"TEST","firstName":"JANE","countyCode":"19","caseOwnerId":"worker001","cin":"12345678A","cinClearanceStatus":"CLEARED","mediCalStatus":"ACTIVE","ihssReferralDate":"2026-03-25","createdBy":"cmipsadmin"}'
```
**Expected:** `400` → `{"error": "EM OS 176: IHSS Referral Date may not be more than two weeks in the future."}`

### CC-API-003 | EM OS 186 — PENDING_SAWS response via API

```bash
curl -s -X POST "http://localhost:8081/api/cases" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"lastName":"TEST","firstName":"JOHN","countyCode":"19","caseOwnerId":"worker001","cin":"","cinClearanceStatus":"NO_MATCH","mediCalStatus":"PENDING_SAWS","ihssReferralDate":"2026-02-28","createdBy":"cmipsadmin"}'
```
**Expected:** `200` → Response contains:
```json
{
  "case": { "id": ..., "caseNumber": "...", ... },
  "infoMessage": "EM OS 186: CIN not selected, Medi-Cal Eligibility Referral will be sent to SAWS.",
  "sawsReferralSent": true
}
```

### CC-API-004 | GET /cases/{id} — new DSD fields

```bash
curl -s "http://localhost:8081/api/cases/{id}" -H "Authorization: Bearer $TOKEN"
```
**Expected:** Response includes all new fields:
- `districtOffice`, `interpreterAvailable`, `mediCalInitialEligibilityNotificationDate`
- `companionCase`, `stateHearing`, `numberOfHouseholdMembers`, `mailDesignee`
- `countyUse1`, `countyUse2`, `countyUse3`, `countyUse4`
- `spokenLanguage`, `writtenLanguage` (from recipient)

### CC-API-005 | Valid referral date within 2 weeks succeeds

```bash
# Use a date 10 days from today
curl -s -X POST "http://localhost:8081/api/cases" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"lastName":"VALID","firstName":"DATE","countyCode":"19","caseOwnerId":"worker001","cin":"98765432B","cinClearanceStatus":"CLEARED","mediCalStatus":"ACTIVE","aidCode":"30","ihssReferralDate":"2026-03-10","createdBy":"cmipsadmin"}'
```
**Expected:** `200` → Case created successfully with the post-dated referral date

---

## HAPPY PATH: Complete End-to-End Flow

Fastest path from login to case creation:

| Step | Action | Expected |
|------|--------|----------|
| 1 | Login as `cmipsadmin` / `password123` | Dashboard loads |
| 2 | Click "New Referral" in sidebar | Person Search — New Referral page |
| 3 | Search Last Name: `NEWCLIENT`, click Search | "No matching persons found" |
| 4 | Click "Create New Referral" | Create Referral form |
| 5 | Fill: Date=`2026-01-15`, Source=`Self` | Section 1 complete |
| 6 | Fill: Last=`NEWCLIENT`, First=`SARAH` | Section 2 partial |
| 7 | Click "Enter / Verify Address" → fill Sacramento address → Skip Verification | Address saved (Unverified) |
| 8 | Check "Same as Residence", fill phone: `9165550001` | Sections 4+5 done |
| 9 | Select County=`Sacramento` | County panel done |
| 10 | Click "Save Referral" | Navigates to Person Home, blue "Open Referral" badge |
| 11 | Click "Start Application" | Application Wizard Step 2, pre-filled |
| 12 | Fill required fields: DOB=`1990-01-15`, Gender=`Female`, Blank SSN Reason=`Refused to Provide`, Ethnicity=`White`, Spoken Language=`English`, check "Same as Residence Address" | Step 2 complete |
| 13 | Click "Next: CIN Clearance" | Step 3 — CIN "Not Performed" |
| 14 | Click 🔍 → CIN Search Modal → Search SCI → Select CIN (or Cancel for No Match) | CIN Cleared or No CIN Match |
| 15 | Select County=`Sacramento`, click "Next: Review" | Step 4 — Review page |
| 16 | Click "Create Application + Case" | Case created (or "Create Without CIN" modal if no CIN) |
| 17 | Verify Case Detail: PENDING badge, case number format `XX-YYYYMMDD-NNNNN` | Case Home page |
| 18 | Check Case Details panel: IHSS Referral Date, Medi-Cal Status, Interpreter Available visible | New DSD fields displayed |
| 19 | If no CIN: blue EM OS 186 banner visible, dismissible with × | SAWS referral info message |

---

## VALIDATION ERROR REFERENCE

### Create Referral Validations (sequential — first error stops)

| Order | Field | EM Code | Error Message |
|-------|-------|---------|---------------|
| 1 | Referral Date | EM OS 200 | Referral Date is required |
| 2 | Referral Source | EM OS 001 | Referral Source is required |
| 3 | Last Name | EM OS 005 | Last Name is required |
| 4 | First Name | EM OS 006 | First Name is required |
| 5 | DOB (if provided) | EM OS 003 | Date of Birth cannot be in the future |
| 5 | DOB (if provided) | EM OS 004 | Date of Birth cannot be more than 120 years ago |
| 6 | SSN (if provided) | EM OS 010 | SSN must be exactly 9 digits / cannot begin with 9 / all same digits |
| 7 | SSN + Blank SSN Reason | EM OS 007 | SSN must be blank when Blank SSN Reason is indicated |
| 8 | Address or Phone | EM OS 080 | At least a Residence Address or Phone is required |
| 9 | County | EM OS 210 | County is required |

> **Note for Referral:** Gender (EM OS 008), DOB, Ethnicity, Spoken Language are all **optional**.

### Application Step 2 Validations (sequential — first error stops)

| Order | Field | EM Code | Error Message |
|-------|-------|---------|---------------|
| 1 | Last Name | EM OS 005 | Last Name is required |
| 2 | First Name | EM OS 006 | First Name is required |
| 3 | Date of Birth | EM OS 003 | Date of Birth must be indicated for Application |
| 3 | DOB future | EM OS 003 | Date of Birth cannot be in the future |
| 3 | DOB > 120yr | EM OS 004 | Date of Birth cannot be more than 120 years ago |
| 4 | Gender | EM OS 008 | Gender must be indicated |
| 5 | SSN or Blank SSN Reason | EM OS 013 | Either SSN or Blank SSN Reason must be indicated |
| 5 | SSN + Blank SSN Reason | EM OS 007 | SSN must be blank when Blank SSN Reason is indicated |
| 5 | SSN format | EM OS 010 | SSN must be 9 digits / Invalid SSN / cannot begin with 9 |
| 6 | Ethnicity | EM OS 009 | Ethnicity must be indicated |
| 7 | Spoken Language | EM OS 011 | Spoken Language must be indicated |
| 8 | Mailing Address | EM OS 018 | Mailing Address or Same as Residence Address must be indicated |
| 9 | Phone (if provided) | EM OS 264/265 | Phone must be 10 digits |
| 10 | Email (if provided) | EM OS 267 | Not a valid email address |

> **Note for Application:** Written Language is **optional** (not validated in Step 2).

### Application Step 3 Validations

| Field | EM Code | Error Message |
|-------|---------|---------------|
| County | EM OS 210 | County is required |
| IHSS Referral Date | ~~EM OS 175~~ | **CANCELLED** (ASR Sprint 43). Post-dating up to 2 weeks now allowed. |

### Create Case Validations (Case Create Page)

| Field | EM Code | Error Message |
|-------|---------|---------------|
| Assigned Worker | EM OS 067 | Assigned worker must be entered |
| IHSS Referral Date > 2 weeks | EM OS 176 | IHSS Referral Date may not be more than two weeks in the future |
| CIN Clearance not performed | EM OS 176 | CIN Clearance must be performed before saving |
| No CIN selected (info) | EM OS 186 | CIN not selected, Medi-Cal Eligibility Referral will be sent to SAWS |

### Application Step 4 Validations

| Condition | EM Code | Error Message |
|-----------|---------|---------------|
| CIN not performed, click "Create App+Case" | EM OS 176 | CIN Clearance must be performed before creating a case |
| CIN = No Match, click "Create App+Case" | — | Modal: Create Case without CIN (BR-9 path) |

---

## FIELD REFERENCE

### Create Referral — All Fields by Section

**Section 1 — Referral Information:**
| Field | Label | Type | Required |
|-------|-------|------|----------|
| Referral Date | Referral Date * | Date input | Yes |
| Referral Source | Referral Source * | Dropdown (40 values) | Yes |

**Section 2 — Person Demographics:**
| Field | Label | Type | Required |
|-------|-------|------|----------|
| Title | Title | Dropdown (Mr/Mrs/Ms/Miss/Dr/Rev/Hon/Other) | No |
| Last Name | Last Name * | Text | Yes |
| First Name | First Name * | Text | Yes |
| Middle Name | Middle Name | Text | No |
| Suffix | Suffix | Dropdown (Jr/Sr/I/II/III/IV/V/Esq/MD/PhD) | No |
| Date of Birth | Date of Birth | Date input | No (optional for Referral) |
| Gender | Gender | Dropdown (Male/Female/Non-Binary/Unknown) | No (optional for Referral) |
| Gender Identity | Gender Identity | Dropdown | No |
| Sexual Orientation | Sexual Orientation | Dropdown | No |
| SSN | SSN (optional) | Text, masked (XXXXX####) | No |
| Blank SSN Reason | Blank SSN Reason | Dropdown | No (mutually exclusive with SSN) |
| Spoken Language | Spoken Language | Dropdown | No |
| Other Spoken Language | Other Spoken Language (specify) | Text (conditional) | No |
| Written Language | Written Language | Dropdown | No |
| Other Written Language | Other Written Language (specify) | Text (conditional) | No |
| Ethnicity | Ethnicity | Dropdown | No |
| Medi-Cal Pseudo Number | Medi-Cal Pseudo Number | Text (max 14 chars) | No |

**Section 3 — Residence Address:**
| Field | Label | Type | Required |
|-------|-------|------|----------|
| Address | Enter / Verify Address | Button → Modal | Yes (address OR phone) |
| Residence Address Type | Residence Address Type | Dropdown | No |
| Meets Residency Req | Meets Residency Requirements | Checkbox | No |

**Section 4 — Mailing Address:**
| Field | Label | Type | Required |
|-------|-------|------|----------|
| Same as Residence | Same as Residence Address | Checkbox | No |
| Address | Enter / Verify Address | Button → Modal | No |

**Section 5 — Phone / Contact:**
| Field | Label | Type | Required |
|-------|-------|------|----------|
| Home Phone | Home Phone | Text (10 digits) | No (phone OR address) |
| Cell Phone | Cell Phone | Text (10 digits) | No |
| Work Phone | Work Phone | Text (10 digits) | No |
| Email | Email Address | Text | No |

**County Panel (standalone):**
| Field | Label | Type | Required |
|-------|-------|------|----------|
| County | County * | Dropdown (58 counties) | Yes |

### Application Wizard Step 2 — Key Differences from Referral

| Field | Referral | Application |
|-------|----------|-------------|
| SSN label | SSN (optional) | SSN * (or select Blank SSN Reason) |
| DOB | Optional | **Required** (EM OS 003) |
| Gender | Optional | **Required** (EM OS 008) |
| SSN/Blank SSN Reason | Optional | **Required** (EM OS 013) |
| Ethnicity | Optional | **Required** (EM OS 009) |
| Spoken Language | Optional | **Required** (EM OS 011) |
| Mailing Address | Optional | **Required** (EM OS 018) |
| Written Language | Optional | Optional |

---

## SSN / BLANK SSN REASON INTERLOCK (EM OS 007)

Both the Create Referral and Application forms enforce mutual exclusion:

1. **Enter SSN** → Blank SSN Reason dropdown becomes **disabled**
2. **Select Blank SSN Reason** → SSN field is **cleared and disabled**
3. **Both filled** → Error: `EM OS 007: SSN must be blank when Blank SSN Reason is indicated`

Blank SSN Reason options:
- Refused to Provide
- Unable to Obtain
- Applied for but not yet received
- Non-citizen

---

## PERSON TYPE LIFECYCLE

```
OPEN_REFERRAL → CLOSED_REFERRAL → (Re-open) → OPEN_REFERRAL
OPEN_REFERRAL → APPLICANT → RECIPIENT
CLOSED_REFERRAL → APPLICANT → RECIPIENT  (BR-8: allowed)
RECIPIENT → OPEN_REFERRAL  (BR-19: BLOCKED)
```

### Badge Colors
| PersonType | Badge Color | Background | Text |
|------------|-------------|------------|------|
| Open Referral | Blue | #bee3f8 | #2b6cb0 |
| Closed Referral | Gray | #e2e8f0 | #4a5568 |
| Applicant | Orange | #feebc8 | #c05621 |
| Recipient | Green | #c6f6d5 | #276749 |

### Close Referral Reasons
- Declined Services
- No Longer Eligible
- Moved / Unable to Locate
- Services Completed
- Unable to Contact

---

## QUICK REFERENCE: All EM Codes

| Code | Rule | Where Tested |
|------|------|-------------|
| EM OS 001 | Referral Source required | CR-003 |
| EM OS 003 | DOB cannot be future / required for App | CR-008, APP-009 |
| EM OS 004 | DOB cannot be > 120 years ago | CR-009 |
| EM OS 005 | Last Name required | CR-005, APP-007 |
| EM OS 006 | First Name required | CR-006, APP-008 |
| EM OS 007 | SSN + Blank SSN Reason mutual exclusion | SSN Interlock section |
| EM OS 008 | Gender required (Application only) | CR-007 (negative), APP Step 2 |
| EM OS 009 | Ethnicity required (Application only) | APP Step 2 |
| EM OS 010 | SSN format (9 digits, not all same, not start with 9) | CR-011/012/013, APP-010/011/012 |
| EM OS 011 | Spoken Language required (Application only) | APP Step 2 |
| EM OS 013 | SSN or Blank SSN Reason required (Application only) | APP Step 2 |
| EM OS 018 | Mailing Address or Same as Residence (Application only) | APP Step 2 |
| EM OS 024 | Street name required (CASS) | AV section |
| EM OS 080 | Residence Address or Phone required | CR-014 |
| EM OS 067 | Assigned Worker required on Create Case | CC-002 |
| EM OS 175 | ~~CANCELLED~~ (ASR Sprint 43) — no longer enforced | — |
| EM OS 176 | CIN clearance required / Referral date > 2 weeks | APP-023, CC-004, CC-005 |
| EM OS 186 | SAWS referral notice (no CIN selected) — informational | CC-006 |
| EM OS 177 | Address verification — skip = unverified | AV-005 |
| EM OS 178 | CASS match = Address Verified | AV-003 |
| EM OS 200 | Referral Date required | CR-002 |
| EM OS 210 | County required | CR-015, APP-019 |
| EM OS 264/265 | Phone must be 10 digits | APP Step 2 |
| EM OS 267 | Valid email format | APP Step 2 |

---

## TEST DATA

### SSN Test Values

| SSN | Purpose | Expected |
|-----|---------|----------|
| `123456789` | Valid SSN | Accepted |
| `234567890` | Valid SSN (masking test) | Shows XXXXX7890 |
| `900000001` | Starts with 9 (EM OS 010) | Rejected |
| `111111111` | All same digits (EM OS 010) | Rejected |
| `12345678` | 8 digits, < 9 (EM OS 010) | Rejected |
| (blank) | Optional for referral | Accepted |

### DOB Test Values

| DOB | Purpose | Expected |
|-----|---------|----------|
| `1990-01-15` | Valid DOB | Accepted |
| `2099-12-31` | Future (EM OS 003) | Rejected |
| `1900-01-01` | > 120 years (EM OS 004) | Rejected |
| (blank) | Optional for referral | Accepted |

### Referral Sources (40 values)

Self, Parent/Guardian, Legal Guardian, Spouse/Partner, Sibling, Other Relative, Friend/Neighbor, Doctor/Physician, Hospital/Clinic, Social Worker, School, Probation/Parole, Law Enforcement, Adult Protective Services, Child Protective Services, Regional Center, Community Organization, Faith Organization, Mental Health Provider, Substance Abuse Program, Home Health Agency, Nursing Home, Assisted Living, Developmental Disability Program, Area Agency on Aging, Food Bank/Pantry, Housing Authority, Veteran Services, Medicare, Medi-Cal, Insurance Company, Employer, Employment Agency, Rehabilitation Program, Court/Judge, Department of Labor, Department of Motor Vehicles, Other Government Agency, Anonymous, Unknown

### California Counties (58)

Alameda, Alpine, Amador, Butte, Calaveras, Colusa, Contra Costa, Del Norte, El Dorado, Fresno, Glenn, Humboldt, Imperial, Inyo, Kern, Kings, Lake, Lassen, Los Angeles, Madera, Marin, Mariposa, Mendocino, Merced, Modoc, Mono, Monterey, Napa, Nevada, Orange, Placer, Plumas, Riverside, Sacramento, San Benito, San Bernardino, San Diego, San Francisco, San Joaquin, San Luis Obispo, San Mateo, Santa Barbara, Santa Clara, Santa Cruz, Shasta, Sierra, Siskiyou, Solano, Sonoma, Stanislaus, Sutter, Tehama, Trinity, Tulare, Tuolumne, Ventura, Yolo, Yuba
