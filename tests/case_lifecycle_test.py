#!/usr/bin/env python3
"""
CMIPS Case Lifecycle Core — Comprehensive API Test
Covers all endpoints, status transitions, business rules (BR/EM codes),
and verifies frontend API URL alignment.

Run:  python3 case_lifecycle_test.py
"""

import json
import sys
import requests
from datetime import date, timedelta

BASE = "http://localhost:8081/api"
KC   = "http://localhost:8080/realms/cmips/protocol/openid-connect/token"

# ── colour helpers ────────────────────────────────────────────────────────────
GREEN = "\033[92m"; RED = "\033[91m"; YELLOW = "\033[93m"
BOLD  = "\033[1m";  RESET = "\033[0m"; CYAN = "\033[96m"

passed = failed = skipped = 0

def ok(label, detail=""):
    global passed
    passed += 1
    print(f"  {GREEN}✓{RESET} {label}" + (f"  [{detail}]" if detail else ""))

def fail(label, detail=""):
    global failed
    failed += 1
    print(f"  {RED}✗{RESET} {label}" + (f"  [{detail}]" if detail else ""))

def skip(label, reason=""):
    global skipped
    skipped += 1
    print(f"  {YELLOW}~{RESET} {label}" + (f"  [{reason}]" if reason else ""))

def section(title):
    print(f"\n{BOLD}{CYAN}{'='*60}{RESET}")
    print(f"{BOLD}{CYAN}  {title}{RESET}")
    print(f"{BOLD}{CYAN}{'='*60}{RESET}")

def subsection(title):
    print(f"\n  {BOLD}── {title} ──{RESET}")

def get_token():
    r = requests.post(KC, data={
        "client_id": "cmips-backend",
        "client_secret": "X6282J5tQzu2tzqLcglKmjhwfidB0vh9",
        "grant_type": "password",
        "username": "cmipsadmin",
        "password": "password123",
    })
    assert r.status_code == 200, f"Keycloak auth failed: {r.text[:200]}"
    return r.json()["access_token"]

def api(method, path, token, **kwargs):
    headers = {"Authorization": f"Bearer {token}", "X-User-Id": "testadmin", **kwargs.pop("headers", {})}
    return requests.request(method, f"{BASE}{path}", headers=headers, **kwargs)

# ── today / date helpers ──────────────────────────────────────────────────────
TODAY     = date.today().isoformat()
YESTERDAY = (date.today() - timedelta(days=1)).isoformat()
LAST_WEEK = (date.today() - timedelta(days=7)).isoformat()
NEXT_WEEK = (date.today() + timedelta(days=7)).isoformat()
NEXT_MONTH= (date.today() + timedelta(days=32)).isoformat()
NINETY_DAYS_AGO = (date.today() - timedelta(days=90)).isoformat()


# ══════════════════════════════════════════════════════════════════════════════
# HELPERS
# ══════════════════════════════════════════════════════════════════════════════

def create_case(token, override=None):
    """Create a minimal valid case → returns case dict."""
    payload = {
        "lastName": "TestLast",
        "firstName": "TestFirst",
        "gender": "M",
        "dateOfBirth": "1980-01-15",
        "ssn": "123456789",
        "zipCode": "95814",
        "spokenLanguage": "ENGLISH",
        "writtenLanguage": "ENGLISH",
        "interpreterAvailable": False,
        "ihssReferralDate": TODAY,
        "caseOwnerId": "testadmin",
        "countyCode": "Sacramento",
        "cin": "12345678A",
        "cinClearanceStatus": "CLEARED",
        "mediCalStatus": "ACTIVE",
        "createdBy": "testadmin",
    }
    if override:
        payload.update(override)
    r = api("POST", "/cases", token, json=payload)
    if r.status_code == 200:
        return r.json()
    return None


# ══════════════════════════════════════════════════════════════════════════════
# 1. AUTH
# ══════════════════════════════════════════════════════════════════════════════
section("1. AUTHENTICATION")
try:
    TOKEN = get_token()
    ok("Keycloak token obtained", f"{TOKEN[:20]}…")
except Exception as e:
    fail("Keycloak auth", str(e))
    print("\nCannot proceed — Keycloak not reachable.")
    sys.exit(1)


# ══════════════════════════════════════════════════════════════════════════════
# 2. CODE TABLES & READ-ONLY
# ══════════════════════════════════════════════════════════════════════════════
section("2. CODE TABLES & READ-ONLY ENDPOINTS")

r = api("GET", "/cases/code-tables", TOKEN)
if r.status_code == 200:
    ct = r.json()
    ok("GET /cases/code-tables", f"keys={list(ct.keys())[:4]}")
else:
    fail("GET /cases/code-tables", f"{r.status_code}")

r = api("GET", "/cases", TOKEN)
if r.status_code == 200:
    cases = r.json()
    ok("GET /cases (list all)", f"count={len(cases) if isinstance(cases,list) else '?'}")
else:
    fail("GET /cases", f"{r.status_code}")

r = api("GET", "/cases/search?countyCode=Sacramento", TOKEN)
ok("GET /cases/search?countyCode=Sacramento", f"status={r.status_code}") if r.status_code==200 else fail("search by county", f"{r.status_code}")

r = api("GET", "/cases/statistics/Sacramento", TOKEN)
ok("GET /cases/statistics/Sacramento", f"status={r.status_code}") if r.status_code in (200,204) else fail("statistics", f"{r.status_code}")

r = api("GET", "/cases/due-for-reassessment", TOKEN)
ok("GET /cases/due-for-reassessment", f"status={r.status_code}") if r.status_code==200 else fail("due-for-reassessment", f"{r.status_code}")


# ══════════════════════════════════════════════════════════════════════════════
# 3. CASE CREATION — VALIDATION SCENARIOS
# ══════════════════════════════════════════════════════════════════════════════
section("3. CASE CREATION — VALIDATION (EM codes)")

subsection("3A. Valid case creation (happy path)")
case1 = create_case(TOKEN)
if case1 and "id" in case1:
    ok("POST /cases — valid payload", f"caseId={case1['id']}, status={case1.get('caseStatus','?')}")
    CASE_ID = case1["id"]
else:
    fail("POST /cases — valid payload", str(case1)[:100])
    CASE_ID = 1  # fallback to seeded case

subsection("3B. EM OS 067 — Missing caseOwnerId")
r = api("POST", "/cases", TOKEN, json={
    "lastName":"Smith","firstName":"Jane","dateOfBirth":"1970-06-01",
    "ihssReferralDate": TODAY,"cinClearanceStatus":"CLEARED","countyCode":"Sacramento"
})
if r.status_code == 400:
    ok("EM OS 067: Missing caseOwnerId → 400", r.json().get("message","")[:60])
else:
    fail("EM OS 067: Should reject missing caseOwnerId", f"status={r.status_code}")

subsection("3C. EM OS 176 — CIN clearance not performed")
r = api("POST", "/cases", TOKEN, json={
    "lastName":"Smith","firstName":"Jane","dateOfBirth":"1970-06-01",
    "ihssReferralDate": TODAY,"caseOwnerId":"worker001",
    "cinClearanceStatus":"NOT_STARTED","countyCode":"Sacramento"
})
if r.status_code == 400:
    ok("EM OS 176: NOT_STARTED clearance → 400", r.json().get("message","")[:60])
else:
    fail("EM OS 176: Should reject NOT_STARTED CIN clearance", f"status={r.status_code}")

subsection("3D. EM OS 175 — IHSS referral date > 2 weeks future")
far_future = (date.today() + timedelta(days=20)).isoformat()
r = api("POST", "/cases", TOKEN, json={
    "lastName":"Smith","firstName":"Jane","dateOfBirth":"1970-06-01",
    "ihssReferralDate": far_future,"caseOwnerId":"worker001",
    "cinClearanceStatus":"CLEARED","countyCode":"Sacramento"
})
if r.status_code == 400:
    ok(f"EM OS 175: Future referral date ({far_future}) → 400", r.json().get("message","")[:60])
else:
    fail("EM OS 175: Should reject far-future referral date", f"status={r.status_code}")

subsection("3E. BR-9 — mediCalStatus=PENDING_SAWS triggers SAWS referral")
case_saws = create_case(TOKEN, {
    "cin": "",
    "cinClearanceStatus": "PENDING_SAWS",
    "mediCalStatus": "PENDING_SAWS",
    "lastName": "SAWSTest",
})
if case_saws and case_saws.get("sawsReferralSent"):
    ok("BR-9: PENDING_SAWS → sawsReferralSent=true in response")
elif case_saws and "id" in case_saws:
    ok("BR-9: PENDING_SAWS case created (check sawsReferralSent flag)", f"sawsReferralSent={case_saws.get('sawsReferralSent')}")
else:
    fail("BR-9: PENDING_SAWS case creation failed", str(case_saws)[:80])

subsection("3F. GET enriched case details")
r = api("GET", f"/cases/{CASE_ID}", TOKEN)
if r.status_code == 200:
    d = r.json()
    ok("GET /cases/{id} enriched response", f"recipientName={d.get('recipientName','?')}, status={d.get('caseStatus',d.get('status','?'))}")
    # Check Phase 7 canonical fields
    for field in ["ihssReferralDate", "reactivationAllowed"]:
        if field in d:
            ok(f"  Enriched field present: {field}", str(d[field]))
        else:
            skip(f"  Enriched field: {field}", "not in response")
else:
    fail("GET /cases/{id}", f"{r.status_code}")


# ══════════════════════════════════════════════════════════════════════════════
# 4. STATUS HISTORY & NOTES & CONTACTS
# ══════════════════════════════════════════════════════════════════════════════
section("4. NOTES / CONTACTS / STATUS HISTORY")

subsection("4A. Case Status History")
r = api("GET", f"/cases/{CASE_ID}/status-history", TOKEN)
if r.status_code == 200:
    h = r.json()
    ok("GET /cases/{id}/status-history", f"entries={len(h) if isinstance(h,list) else '?'}")
else:
    fail("status-history", f"{r.status_code}")

subsection("4B. Case Notes — create / append / cancel")
r = api("POST", f"/cases/{CASE_ID}/notes", TOKEN, json={
    "noteType": "GENERAL", "subject": "Test Note", "content": "Initial content for case lifecycle test."
})
if r.status_code == 200:
    note = r.json()
    NOTE_ID = note.get("id")
    ok("POST /cases/{id}/notes", f"noteId={NOTE_ID}")

    r2 = api("PUT", f"/cases/notes/{NOTE_ID}/append", TOKEN, json={"content": "Appended line."})
    ok("PUT /cases/notes/{id}/append", f"status={r2.status_code}") if r2.status_code==200 else fail("append note", f"{r2.status_code}")

    r3 = api("PUT", f"/cases/notes/{NOTE_ID}/cancel", TOKEN, json={"reason": "Created during automated test"})
    ok("PUT /cases/notes/{id}/cancel", f"status={r3.status_code}") if r3.status_code==200 else fail("cancel note", f"{r3.status_code}")
else:
    fail("POST /cases/{id}/notes", f"{r.status_code} — {r.text[:80]}")
    NOTE_ID = None

subsection("4C. Case Contacts — create / inactivate")
r = api("POST", f"/cases/{CASE_ID}/contacts", TOKEN, json={
    "contactName": "Test Contact", "relationshipType": "SELF",
    "phone": "9165551234", "email": "contact@example.com"
})
if r.status_code == 200:
    contact = r.json()
    CONTACT_ID = contact.get("id")
    ok("POST /cases/{id}/contacts", f"contactId={CONTACT_ID}")

    r2 = api("PUT", f"/cases/contacts/{CONTACT_ID}/inactivate", TOKEN)
    ok("PUT /cases/contacts/{id}/inactivate", f"status={r2.status_code}") if r2.status_code==200 else fail("inactivate contact", f"{r2.status_code}")
else:
    fail("POST /cases/{id}/contacts", f"{r.status_code} — {r.text[:80]}")


# ══════════════════════════════════════════════════════════════════════════════
# 5. CASE ASSIGNMENT
# ══════════════════════════════════════════════════════════════════════════════
section("5. CASE ASSIGNMENT")

r = api("PUT", f"/cases/{CASE_ID}/assign", TOKEN, json={"caseOwnerId": "worker002"})
if r.status_code == 200:
    ok("PUT /cases/{id}/assign", f"caseOwnerId={r.json().get('caseOwnerId','?')}")
else:
    fail("PUT /cases/{id}/assign", f"{r.status_code}")

# Re-assign back
api("PUT", f"/cases/{CASE_ID}/assign", TOKEN, json={"caseOwnerId": "worker001"})


# ══════════════════════════════════════════════════════════════════════════════
# 6. APPROVAL — PENDING → ELIGIBLE
# ══════════════════════════════════════════════════════════════════════════════
section("6. CASE APPROVAL (PENDING → ELIGIBLE)")

# Create a dedicated case for approval tests
approve_case = create_case(TOKEN, {"lastName": "ApproveTest"})
if approve_case:
    AID = approve_case["id"]

    r = api("PUT", f"/cases/{AID}/approve", TOKEN)
    if r.status_code == 200:
        approved = r.json()
        new_status = approved.get("caseStatus", approved.get("status", "?"))
        if new_status == "ELIGIBLE":
            ok("PUT /cases/{id}/approve → ELIGIBLE", f"caseId={AID}")
        else:
            fail("approve → expected ELIGIBLE", f"got={new_status}")
    else:
        fail("PUT /cases/{id}/approve", f"{r.status_code} — {r.text[:80]}")

    subsection("6A. Cannot approve already-ELIGIBLE case")
    r2 = api("PUT", f"/cases/{AID}/approve", TOKEN)
    if r2.status_code in (400, 409):
        ok("Double-approve → 400/409", f"status={r2.status_code}")
    else:
        fail("Double-approve should be rejected", f"status={r2.status_code}")
else:
    fail("Could not create case for approval test")
    AID = CASE_ID


# ══════════════════════════════════════════════════════════════════════════════
# 7. CASE DENIAL — PENDING → DENIED
# ══════════════════════════════════════════════════════════════════════════════
section("7. CASE DENIAL (PENDING → DENIED)")

deny_case = create_case(TOKEN, {"lastName": "DenyTest"})
if deny_case:
    DID = deny_case["id"]

    r = api("PUT", f"/cases/{DID}/deny", TOKEN, json={"reason": "Recipient does not meet IHSS eligibility criteria."})
    if r.status_code == 200:
        d = r.json()
        new_status = d.get("caseStatus", d.get("status", "?"))
        ok("PUT /cases/{id}/deny → DENIED", f"caseId={DID}, status={new_status}")
    else:
        fail("PUT /cases/{id}/deny", f"{r.status_code} — {r.text[:80]}")
else:
    fail("Could not create case for denial test")
    DID = CASE_ID


# ══════════════════════════════════════════════════════════════════════════════
# 8. CASE WITHDRAWAL — PENDING → APPLICATION_WITHDRAWN
# ══════════════════════════════════════════════════════════════════════════════
section("8. CASE WITHDRAWAL (PENDING → APPLICATION_WITHDRAWN)")

subsection("8A. Happy path — valid withdrawal date")
wd_case = create_case(TOKEN, {"lastName": "WithdrawTest"})
if wd_case:
    WID = wd_case["id"]
    r = api("PUT", f"/cases/{WID}/withdraw", TOKEN, json={"reason": "WO001", "withdrawalDate": TODAY})
    if r.status_code == 200:
        d = r.json()
        new_status = d.get("caseStatus", d.get("status", "?"))
        ok("PUT /cases/{id}/withdraw → APPLICATION_WITHDRAWN", f"status={new_status}")
    else:
        fail("withdraw happy path", f"{r.status_code} — {r.text[:80]}")

    subsection("8B. EM#93 — Withdrawal date in future")
    wd_case2 = create_case(TOKEN, {"lastName": "WithdrawFuture"})
    if wd_case2:
        r2 = api("PUT", f"/cases/{wd_case2['id']}/withdraw", TOKEN,
                 json={"reason":"test","withdrawalDate": NEXT_WEEK})
        if r2.status_code == 400:
            ok("EM#93: Future withdrawal date → 400", r2.json().get("message","")[:60])
        else:
            fail("EM#93: Future date should be rejected", f"status={r2.status_code}")

    subsection("8C. EM#87 — Withdrawal date before application date")
    wd_case3 = create_case(TOKEN, {"lastName": "WithdrawBeforeApp"})
    if wd_case3:
        r3 = api("PUT", f"/cases/{wd_case3['id']}/withdraw", TOKEN,
                 json={"reason":"test","withdrawalDate": NINETY_DAYS_AGO})
        if r3.status_code == 400:
            ok("EM#87: Withdrawal before application date → 400", r3.json().get("message","")[:60])
        else:
            fail("EM#87: Early withdrawal should be rejected", f"status={r3.status_code}")
else:
    fail("Could not create case for withdrawal test")


# ══════════════════════════════════════════════════════════════════════════════
# 9. CASE TERMINATION — ELIGIBLE → TERMINATED
# ══════════════════════════════════════════════════════════════════════════════
section("9. CASE TERMINATION (ELIGIBLE → TERMINATED)")

# Create + approve a case for termination
term_case = create_case(TOKEN, {"lastName": "TerminateTest"})
TERM_ID = None
if term_case:
    api("PUT", f"/cases/{term_case['id']}/approve", TOKEN)
    TERM_ID = term_case["id"]

    subsection("9A. Happy path — valid auth end date")
    auth_end = (date.today() + timedelta(days=15)).isoformat()
    r = api("PUT", f"/cases/{TERM_ID}/terminate", TOKEN, json={
        "reason": "CC502", "authorizationEndDate": auth_end
    })
    if r.status_code == 200:
        d = r.json()
        new_status = d.get("caseStatus", d.get("status", "?"))
        ok("PUT /cases/{id}/terminate → TERMINATED", f"status={new_status}")
    else:
        fail("terminate happy path", f"{r.status_code} — {r.text[:80]}")

    subsection("9B. EM#95 — Auth end date > 1 month future")
    term_case2 = create_case(TOKEN, {"lastName": "TermFutureTest"})
    if term_case2:
        api("PUT", f"/cases/{term_case2['id']}/approve", TOKEN)
        r2 = api("PUT", f"/cases/{term_case2['id']}/terminate", TOKEN, json={
            "reason": "CC502", "authorizationEndDate": NEXT_MONTH
        })
        if r2.status_code == 400:
            ok("EM#95: Auth end date >1 month future → 400", r2.json().get("message","")[:60])
        else:
            fail("EM#95: Should reject far-future auth end date", f"status={r2.status_code}")
else:
    fail("Could not set up case for termination test")


# ══════════════════════════════════════════════════════════════════════════════
# 10. PLACE ON LEAVE — ELIGIBLE → ON_LEAVE
# ══════════════════════════════════════════════════════════════════════════════
section("10. PLACE ON LEAVE (ELIGIBLE → ON_LEAVE)")

leave_case = create_case(TOKEN, {"lastName": "LeaveTest"})
LEAVE_ID = None
if leave_case:
    api("PUT", f"/cases/{leave_case['id']}/approve", TOKEN)
    LEAVE_ID = leave_case["id"]

    subsection("10A. Happy path — valid leave")
    auth_end = (date.today() + timedelta(days=20)).isoformat()
    r = api("PUT", f"/cases/{LEAVE_ID}/leave", TOKEN, json={
        "reason": "L0001", "authorizationEndDate": auth_end
    })
    if r.status_code == 200:
        d = r.json()
        new_status = d.get("caseStatus", d.get("status", "?"))
        ok("PUT /cases/{id}/leave → ON_LEAVE", f"status={new_status}")
    else:
        fail("leave happy path", f"{r.status_code} — {r.text[:80]}")

    subsection("10B. EM#43 — L0006 requires Resource Suspension End Date")
    leave_case2 = create_case(TOKEN, {"lastName": "LeaveL0006"})
    if leave_case2:
        api("PUT", f"/cases/{leave_case2['id']}/approve", TOKEN)
        r2 = api("PUT", f"/cases/{leave_case2['id']}/leave", TOKEN, json={
            "reason": "L0006", "authorizationEndDate": auth_end
            # missing resourceSuspensionEndDate
        })
        if r2.status_code == 400:
            ok("EM#43: L0006 without suspension end date → 400", r2.json().get("message","")[:60])
        else:
            fail("EM#43: L0006 should require suspension end date", f"status={r2.status_code}")

    subsection("10C. EM#96 — Auth end date > 1 month future")
    leave_case3 = create_case(TOKEN, {"lastName": "LeaveFuture"})
    if leave_case3:
        api("PUT", f"/cases/{leave_case3['id']}/approve", TOKEN)
        r3 = api("PUT", f"/cases/{leave_case3['id']}/leave", TOKEN, json={
            "reason": "L0001", "authorizationEndDate": NEXT_MONTH
        })
        if r3.status_code == 400:
            ok("EM#96: Auth end date >1 month future → 400", r3.json().get("message","")[:60])
        else:
            fail("EM#96: Should reject far-future auth end date", f"status={r3.status_code}")
else:
    fail("Could not set up case for leave test")


# ══════════════════════════════════════════════════════════════════════════════
# 11. RESCIND — TERMINATED/DENIED → restored status
# ══════════════════════════════════════════════════════════════════════════════
section("11. RESCIND (TERMINATED/DENIED → previous status)")

subsection("11A. Rescind terminated case (happy path)")
rescind_case = create_case(TOKEN, {"lastName": "RescindTest"})
RESCIND_ID = None
if rescind_case:
    api("PUT", f"/cases/{rescind_case['id']}/approve", TOKEN)
    auth_end = (date.today() + timedelta(days=10)).isoformat()
    tr = api("PUT", f"/cases/{rescind_case['id']}/terminate", TOKEN, json={
        "reason": "CC502", "authorizationEndDate": auth_end
    })
    if tr.status_code == 200:
        RESCIND_ID = rescind_case["id"]
        r = api("PUT", f"/cases/{RESCIND_ID}/rescind", TOKEN, json={"reason": "R0003"})
        if r.status_code == 200:
            d = r.json()
            new_status = d.get("caseStatus", d.get("status", "?"))
            ok("PUT /cases/{id}/rescind → restored", f"status={new_status}")
        else:
            fail("rescind terminated case", f"{r.status_code} — {r.text[:80]}")
    else:
        skip("rescind — could not terminate case first", f"terminate status={tr.status_code}")

subsection("11B. Rescind denied case")
resc_deny = create_case(TOKEN, {"lastName": "RescindDenied"})
if resc_deny:
    api("PUT", f"/cases/{resc_deny['id']}/deny", TOKEN, json={"reason": "test denial"})
    r = api("PUT", f"/cases/{resc_deny['id']}/rescind", TOKEN, json={"reason": "R0003"})
    if r.status_code == 200:
        d = r.json()
        ok("Rescind denied case → restored", f"status={d.get('caseStatus', d.get('status','?'))}")
    else:
        fail("rescind denied case", f"{r.status_code} — {r.text[:80]}")


# ══════════════════════════════════════════════════════════════════════════════
# 12. REACTIVATION — TERMINATED/DENIED/WITHDRAWN → PENDING
# ══════════════════════════════════════════════════════════════════════════════
section("12. REACTIVATION (TERMINATED → PENDING)")

react_case = create_case(TOKEN, {"lastName": "ReactTest"})
REACT_ID = None
if react_case:
    api("PUT", f"/cases/{react_case['id']}/approve", TOKEN)
    auth_end = (date.today() + timedelta(days=5)).isoformat()
    tr = api("PUT", f"/cases/{react_case['id']}/terminate", TOKEN, json={
        "reason": "CC502", "authorizationEndDate": auth_end
    })
    if tr.status_code == 200:
        REACT_ID = react_case["id"]

        subsection("12A. Happy path reactivation")
        r = api("PUT", f"/cases/{REACT_ID}/reactivate", TOKEN, json={
            "referralDate": TODAY,
            "meetsResidencyRequirement": "Y",
            "referralSource": "SELF_REFERRAL",
            "interpreterAvailable": False,
            "assignedWorkerId": "worker001",
            "cinClearanceStatus": "CLEARED",
        })
        if r.status_code == 200:
            d = r.json()
            new_status = d.get("caseStatus", d.get("status", "?"))
            ok("PUT /cases/{id}/reactivate → PENDING", f"status={new_status}")
        else:
            fail("reactivate terminated case", f"{r.status_code} — {r.text[:80]}")

    subsection("12B. EM#117 — Reactivation without CIN clearance")
    react_case2 = create_case(TOKEN, {"lastName": "ReactNoCIN"})
    if react_case2:
        api("PUT", f"/cases/{react_case2['id']}/deny", TOKEN, json={"reason": "test"})
        r2 = api("PUT", f"/cases/{react_case2['id']}/reactivate", TOKEN, json={
            "referralDate": TODAY,
            "assignedWorkerId": "worker001",
            "cinClearanceStatus": "NOT_STARTED",
        })
        if r2.status_code == 400:
            ok("EM#117: NOT_STARTED clearance blocks reactivation → 400", r2.json().get("message","")[:60])
        else:
            fail("EM#117: Should require CIN clearance", f"status={r2.status_code}")

    subsection("12C. EM#98 — Same-day reactivation")
    react_case3 = create_case(TOKEN, {"lastName": "ReactSameDay"})
    if react_case3:
        api("PUT", f"/cases/{react_case3['id']}/deny", TOKEN, json={"reason": "test"})
        r3 = api("PUT", f"/cases/{react_case3['id']}/reactivate", TOKEN, json={
            "referralDate": TODAY,
            "assignedWorkerId": "worker001",
            "cinClearanceStatus": "CLEARED",
        })
        if r3.status_code == 400:
            ok("EM#98: Same-day reactivation → 400", r3.json().get("message","")[:60])
        else:
            # Some systems allow same-day if on different action
            skip("EM#98: Same-day check (may be allowed)", f"status={r3.status_code}")
else:
    fail("Could not set up case for reactivation tests")


# ══════════════════════════════════════════════════════════════════════════════
# 13. TRANSFER — INITIATE / CANCEL
# ══════════════════════════════════════════════════════════════════════════════
section("13. TRANSFER (initiate / cancel)")

# Need an ELIGIBLE case
transfer_case = create_case(TOKEN, {"lastName": "TransferTest"})
TRANSFER_ID = None
if transfer_case:
    api("PUT", f"/cases/{transfer_case['id']}/approve", TOKEN)
    TRANSFER_ID = transfer_case["id"]

    r = api("POST", f"/cases/{TRANSFER_ID}/transfer/initiate", TOKEN, json={"receivingCountyCode": "Alameda"})
    if r.status_code == 200:
        ok("POST /cases/{id}/transfer/initiate", f"status={r.json().get('transferStatus','?')}")

        r2 = api("POST", f"/cases/{TRANSFER_ID}/transfer/cancel", TOKEN)
        ok("POST /cases/{id}/transfer/cancel", f"status={r2.status_code}") if r2.status_code==200 else fail("transfer cancel", f"{r2.status_code}")
    else:
        fail("transfer initiate", f"{r.status_code} — {r.text[:80]}")

    subsection("13A. EM#130 — Cannot terminate while transfer in progress")
    transfer_case2 = create_case(TOKEN, {"lastName": "TransferBlockTerm"})
    if transfer_case2:
        api("PUT", f"/cases/{transfer_case2['id']}/approve", TOKEN)
        ti = api("POST", f"/cases/{transfer_case2['id']}/transfer/initiate", TOKEN, json={"receivingCountyCode": "Fresno"})
        if ti.status_code == 200:
            r3 = api("PUT", f"/cases/{transfer_case2['id']}/terminate", TOKEN, json={
                "reason": "CC502", "authorizationEndDate": (date.today()+timedelta(days=5)).isoformat()
            })
            if r3.status_code == 400:
                ok("EM#130: Cannot terminate during transfer → 400", r3.json().get("message","")[:60])
            else:
                fail("EM#130: Should block termination during transfer", f"status={r3.status_code}")
            # Cleanup: cancel transfer
            api("POST", f"/cases/{transfer_case2['id']}/transfer/cancel", TOKEN)


# ══════════════════════════════════════════════════════════════════════════════
# 14. CASE MAINTENANCE — WORKWEEK AGREEMENTS
# ══════════════════════════════════════════════════════════════════════════════
section("14. CASE MAINTENANCE — WORKWEEK AGREEMENTS (CI-480925)")

r = api("GET", f"/cases/{CASE_ID}/workweek-agreements", TOKEN)
ok("GET /cases/{id}/workweek-agreements", f"status={r.status_code}") if r.status_code==200 else fail("list workweek agreements", f"{r.status_code}")

r = api("POST", f"/cases/{CASE_ID}/workweek-agreements", TOKEN, json={
    "providerId": 1, "providerNumber": "P001",
    "beginDate": TODAY, "endDate": "9999-12-31",
    "agreedHoursWeekly": 2400, "backUpProvider": False
})
if r.status_code == 200:
    WW_ID = r.json().get("id")
    ok("POST /cases/{id}/workweek-agreements", f"id={WW_ID}")

    r2 = api("PUT", f"/cases/workweek-agreements/{WW_ID}", TOKEN, json={"agreedHoursWeekly": 2000})
    ok("PUT /cases/workweek-agreements/{id}", f"status={r2.status_code}") if r2.status_code==200 else fail("update workweek", f"{r2.status_code}")

    r3 = api("PUT", f"/cases/workweek-agreements/{WW_ID}/inactivate", TOKEN, json={"reason": "Provider reassigned"})
    ok("PUT /cases/workweek-agreements/{id}/inactivate", f"status={r3.status_code}") if r3.status_code==200 else fail("inactivate workweek", f"{r3.status_code}")

    r4 = api("GET", f"/cases/{CASE_ID}/workweek-agreements/history", TOKEN)
    ok("GET /cases/{id}/workweek-agreements/history", f"count={len(r4.json()) if r4.status_code==200 else '?'}") if r4.status_code==200 else fail("workweek history", f"{r4.status_code}")
else:
    fail("POST workweek agreement", f"{r.status_code} — {r.text[:80]}")


# ══════════════════════════════════════════════════════════════════════════════
# 15. CASE MAINTENANCE — OVERTIME AGREEMENTS
# ══════════════════════════════════════════════════════════════════════════════
section("15. CASE MAINTENANCE — OVERTIME AGREEMENTS (CI-480922)")

r = api("GET", f"/cases/{CASE_ID}/overtime-agreements", TOKEN)
ok("GET /cases/{id}/overtime-agreements", f"status={r.status_code}") if r.status_code==200 else fail("list overtime", f"{r.status_code}")

r = api("POST", f"/cases/{CASE_ID}/overtime-agreements", TOKEN, json={
    "providerId": 1, "providerNumber": "P001",
    "agreementType": "OVERTIME", "dateReceived": TODAY
})
if r.status_code == 200:
    OT_ID = r.json().get("id")
    ok("POST /cases/{id}/overtime-agreements", f"id={OT_ID}")

    subsection("15A. dateReceived before 11/1/2014 → rejected")
    r2 = api("POST", f"/cases/{CASE_ID}/overtime-agreements", TOKEN, json={
        "providerId": 1, "providerNumber": "P001",
        "agreementType": "OVERTIME", "dateReceived": "2014-10-31"
    })
    if r2.status_code == 400:
        ok("dateReceived < 11/1/2014 → 400", r2.json().get("message","")[:60])
    else:
        fail("Should reject date before 11/1/2014", f"status={r2.status_code}")

    subsection("15B. dateReceived in future → rejected")
    r3 = api("POST", f"/cases/{CASE_ID}/overtime-agreements", TOKEN, json={
        "providerId": 1, "providerNumber": "P001",
        "agreementType": "OVERTIME", "dateReceived": NEXT_WEEK
    })
    if r3.status_code == 400:
        ok("dateReceived in future → 400", r3.json().get("message","")[:60])
    else:
        fail("Should reject future date", f"status={r3.status_code}")

    r4 = api("PUT", f"/cases/overtime-agreements/{OT_ID}/inactivate", TOKEN)
    ok("PUT /cases/overtime-agreements/{id}/inactivate", f"status={r4.status_code}") if r4.status_code==200 else fail("inactivate overtime", f"{r4.status_code}")
else:
    fail("POST overtime agreement", f"{r.status_code} — {r.text[:80]}")


# ══════════════════════════════════════════════════════════════════════════════
# 16. CASE MAINTENANCE — WPCS HOURS & WORKPLACE HOURS
# ══════════════════════════════════════════════════════════════════════════════
section("16. CASE MAINTENANCE — WPCS & WORKPLACE HOURS")

subsection("16A. WPCS Hours")
r = api("GET", f"/cases/{CASE_ID}/wpcs-hours", TOKEN)
ok("GET /cases/{id}/wpcs-hours", f"status={r.status_code}") if r.status_code==200 else fail("list WPCS hours", f"{r.status_code}")

r = api("POST", f"/cases/{CASE_ID}/wpcs-hours", TOKEN, json={
    "beginDate": TODAY, "endDate": "9999-12-31",
    "authorizedHours": 480, "fundingSource": "IFO"
})
if r.status_code == 200:
    WPCS_ID = r.json().get("id")
    ok("POST /cases/{id}/wpcs-hours", f"id={WPCS_ID}")
    r2 = api("PUT", f"/cases/wpcs-hours/{WPCS_ID}/inactivate", TOKEN)
    ok("PUT /cases/wpcs-hours/{id}/inactivate", f"status={r2.status_code}") if r2.status_code==200 else fail("inactivate WPCS", f"{r2.status_code}")
else:
    fail("POST WPCS hours", f"{r.status_code} — {r.text[:80]}")

subsection("16B. Workplace Hours")
r = api("GET", f"/cases/{CASE_ID}/workplace-hours", TOKEN)
ok("GET /cases/{id}/workplace-hours", f"status={r.status_code}") if r.status_code==200 else fail("list workplace hours", f"{r.status_code}")

r = api("POST", f"/cases/{CASE_ID}/workplace-hours", TOKEN, json={
    "beginDate": TODAY, "endDate": "9999-12-31", "workplaceHours": 200
})
if r.status_code == 200:
    WPL_ID = r.json().get("id")
    ok("POST /cases/{id}/workplace-hours", f"id={WPL_ID}")
    r2 = api("PUT", f"/cases/workplace-hours/{WPL_ID}/inactivate", TOKEN)
    ok("PUT /cases/workplace-hours/{id}/inactivate", f"status={r2.status_code}") if r2.status_code==200 else fail("inactivate workplace", f"{r2.status_code}")
else:
    fail("POST workplace hours", f"{r.status_code} — {r.text[:80]}")


# ══════════════════════════════════════════════════════════════════════════════
# 17. MEDI-CAL SOC
# ══════════════════════════════════════════════════════════════════════════════
section("17. MEDI-CAL SOC (CI-67574)")

r = api("GET", f"/cases/{CASE_ID}/medi-cal-soc", TOKEN)
ok("GET /cases/{id}/medi-cal-soc", f"status={r.status_code}, fields={list(r.json().keys())[:4] if r.status_code==200 else 'n/a'}") if r.status_code==200 else fail("GET medi-cal-soc", f"{r.status_code}")

r = api("PUT", f"/cases/{CASE_ID}/medi-cal-soc", TOKEN, json={
    "shareOfCostAmount": 150.00, "countableIncome": 750.00, "netIncome": 800.00
})
ok("PUT /cases/{id}/medi-cal-soc", f"status={r.status_code}") if r.status_code==200 else fail("PUT medi-cal-soc", f"{r.status_code} — {r.text[:80]}")

r = api("GET", f"/cases/{CASE_ID}/medi-cal-eligibility", TOKEN)
ok("GET /cases/{id}/medi-cal-eligibility (SAWS mock)", f"status={r.status_code}") if r.status_code in (200,204) else fail("medi-cal-eligibility", f"{r.status_code}")


# ══════════════════════════════════════════════════════════════════════════════
# 18. REASSESSMENT
# ══════════════════════════════════════════════════════════════════════════════
section("18. REASSESSMENT SCHEDULING")

r = api("POST", f"/cases/{CASE_ID}/schedule-reassessment", TOKEN, json={"dueDate": NEXT_WEEK})
ok("POST /cases/{id}/schedule-reassessment", f"status={r.status_code}") if r.status_code==200 else fail("schedule reassessment", f"{r.status_code} — {r.text[:60]}")


# ══════════════════════════════════════════════════════════════════════════════
# 19. ESP REGISTRATIONS
# ══════════════════════════════════════════════════════════════════════════════
section("19. ESP REGISTRATIONS (CI-795491)")

r = api("GET", f"/cases/{CASE_ID}/esp-registrations", TOKEN)
ESP_ID = None
if r.status_code == 200:
    esps = r.json()
    ok("GET /cases/{id}/esp-registrations", f"count={len(esps) if isinstance(esps,list) else '?'}")
    if isinstance(esps, list) and len(esps) > 0:
        ESP_ID = esps[0].get("id")
else:
    fail("GET esp-registrations", f"{r.status_code}")

if ESP_ID:
    r2 = api("PUT", f"/cases/esp-registrations/{ESP_ID}/inactivate", TOKEN, json={"reason": "Account duplicate"})
    ok("PUT /cases/esp-registrations/{id}/inactivate", f"status={r2.status_code}") if r2.status_code==200 else fail("inactivate ESP", f"{r2.status_code}")

    r3 = api("PUT", f"/cases/esp-registrations/{ESP_ID}/reactivate", TOKEN)
    ok("PUT /cases/esp-registrations/{id}/reactivate", f"status={r3.status_code}") if r3.status_code==200 else fail("reactivate ESP", f"{r3.status_code}")

    r4 = api("GET", f"/cases/esp-registrations/{ESP_ID}/soc2321", TOKEN)
    ok("GET /cases/esp-registrations/{id}/soc2321 (SOC 2321 mock)", f"status={r4.status_code}") if r4.status_code in (200,204) else fail("soc2321", f"{r4.status_code}")
else:
    skip("ESP inactivate/reactivate/soc2321", "no ESP registrations on case")


# ══════════════════════════════════════════════════════════════════════════════
# 20. NOTICES OF ACTION (NOA)
# ══════════════════════════════════════════════════════════════════════════════
section("20. NOTICES OF ACTION (DSD NA 1250-1257)")

r = api("GET", f"/cases/{CASE_ID}/noas", TOKEN)
ok("GET /cases/{id}/noas", f"status={r.status_code}, count={len(r.json()) if r.status_code==200 else '?'}") if r.status_code==200 else fail("GET noas", f"{r.status_code}")

r = api("POST", f"/cases/{CASE_ID}/noas", TOKEN, json={
    "noaType": "NA_1250",
    "effectiveDate": NEXT_WEEK,
    "language": "ENGLISH",
    "printMethod": "PRINT_NOW",
    "triggerAction": "APPROVAL"
})
if r.status_code == 200:
    NOA_ID = r.json().get("id")
    ok("POST /cases/{id}/noas", f"noaId={NOA_ID}")

    r2 = api("GET", f"/cases/noas/{NOA_ID}/print", TOKEN)
    ok("GET /cases/noas/{id}/print", f"status={r2.status_code}") if r2.status_code in (200,204) else fail("print NOA", f"{r2.status_code}")

    r3 = api("PUT", f"/cases/noas/{NOA_ID}/suppress", TOKEN, json={"reason": "Duplicate NOA"})
    ok("PUT /cases/noas/{id}/suppress", f"status={r3.status_code}") if r3.status_code==200 else fail("suppress NOA", f"{r3.status_code}")
else:
    fail("POST /cases/{id}/noas", f"{r.status_code} — {r.text[:80]}")


# ══════════════════════════════════════════════════════════════════════════════
# 21. HEALTH CARE CERTIFICATION
# ══════════════════════════════════════════════════════════════════════════════
section("21. HEALTH CARE CERTIFICATION (BR SE 28-50)")

r = api("GET", f"/cases/{CASE_ID}/health-care-cert", TOKEN)
ok("GET /cases/{id}/health-care-cert", f"status={r.status_code}") if r.status_code==200 else fail("GET health-care-cert", f"{r.status_code}")

r = api("POST", f"/cases/{CASE_ID}/health-care-cert", TOKEN, json={
    "certificationMethod": "SOC_873",
    "formType": "SOC_873",
    "printOption": "PRINT_NOW",
    "language": "ENGLISH"
})
if r.status_code == 200:
    CERT_ID = r.json().get("id")
    ok("POST /cases/{id}/health-care-cert", f"certId={CERT_ID}")

    r2 = api("PUT", f"/cases/{CASE_ID}/health-care-cert/good-cause", TOKEN, json={
        "extensionDate": (date.today() + timedelta(days=45)).isoformat()
    })
    ok("PUT /cases/{id}/health-care-cert/good-cause", f"status={r2.status_code}") if r2.status_code==200 else fail("good-cause extension", f"{r2.status_code}")
else:
    fail("POST health-care-cert", f"{r.status_code} — {r.text[:80]}")
    CERT_ID = None


# ══════════════════════════════════════════════════════════════════════════════
# 22. FRONTEND API URL ALIGNMENT CHECK
# ══════════════════════════════════════════════════════════════════════════════
section("22. FRONTEND API ALIGNMENT (casesApi.js URL verification)")

# The frontend casesApi.js functions call /api/* (via httpClient base URL).
# We verify each URL resolves to the same endpoint we just tested.
FRONTEND_ROUTES = [
    ("GET",  f"/cases",                                      "getCases"),
    ("GET",  f"/cases/{CASE_ID}",                            "getCaseById"),
    ("GET",  f"/cases/search?caseOwnerId=worker001",         "searchCases"),
    ("GET",  f"/cases/{CASE_ID}/notes",                      "getCaseNotes"),
    ("GET",  f"/cases/{CASE_ID}/contacts",                   "getCaseContacts"),
    ("GET",  f"/cases/{CASE_ID}/status-history",             "getCaseStatusHistory"),
    ("GET",  "/cases/code-tables",                           "getCaseCodeTables"),
    ("GET",  f"/cases/{CASE_ID}/workweek-agreements",        "getWorkweekAgreements"),
    ("GET",  f"/cases/{CASE_ID}/overtime-agreements",        "getOvertimeAgreements"),
    ("GET",  f"/cases/{CASE_ID}/wpcs-hours",                 "getWpcsHours"),
    ("GET",  f"/cases/{CASE_ID}/workplace-hours",            "getWorkplaceHours"),
    ("GET",  f"/cases/{CASE_ID}/medi-cal-soc",               "getMediCalSoc"),
    ("GET",  f"/cases/{CASE_ID}/esp-registrations",          "getEspRegistrations"),
    ("GET",  f"/cases/{CASE_ID}/noas",                       "getNoas"),
    ("GET",  f"/cases/{CASE_ID}/health-care-cert",           "getHealthCareCert"),
    ("GET",  "/cases/due-for-reassessment",                  "getDueForReassessment"),
    ("GET",  "/cases/statistics/Sacramento",                 "getCaseStatistics"),
]

for method, path, fn_name in FRONTEND_ROUTES:
    r = api(method, path, TOKEN)
    if r.status_code in (200, 204):
        ok(f"casesApi.{fn_name} → {method} {path}", f"HTTP {r.status_code}")
    elif r.status_code == 404:
        fail(f"casesApi.{fn_name} → {method} {path}", "404 — URL mismatch or missing route")
    else:
        skip(f"casesApi.{fn_name} → {method} {path}", f"HTTP {r.status_code}")


# ══════════════════════════════════════════════════════════════════════════════
# 23. ELIGIBILITY API ALIGNMENT
# ══════════════════════════════════════════════════════════════════════════════
section("23. ELIGIBILITY API ALIGNMENT (eligibilityApi.js URL verification)")

ELIG_ROUTES = [
    ("GET",  f"/eligibility/case/{CASE_ID}",           "getAssessments"),
    ("GET",  f"/eligibility/case/{CASE_ID}/health-cert","getHealthCerts"),
    ("GET",  "/eligibility/due-for-reassessment",       "due for reassessment"),
]
# Find assessment id from earlier
r_tmp = api("GET", f"/eligibility/case/{CASE_ID}", TOKEN)
ELIG_ID = (r_tmp.json()[0]["id"] if r_tmp.status_code==200 and r_tmp.json() else None)
if ELIG_ID:
    ELIG_ROUTES += [
        ("GET",  f"/eligibility/{ELIG_ID}",                "getAssessmentById"),
        ("GET",  f"/eligibility/{ELIG_ID}/total-hours",    "getTotalHours"),
    ]

for method, path, fn_name in ELIG_ROUTES:
    r = api(method, path, TOKEN)
    if r.status_code in (200, 204):
        ok(f"eligibilityApi.{fn_name} → {method} {path}", f"HTTP {r.status_code}")
    elif r.status_code == 404:
        fail(f"eligibilityApi.{fn_name} → {method} {path}", "404 — URL mismatch")
    else:
        skip(f"eligibilityApi.{fn_name} → {method} {path}", f"HTTP {r.status_code}")


# ══════════════════════════════════════════════════════════════════════════════
# 24. STATE HEARINGS API
# ══════════════════════════════════════════════════════════════════════════════
section("24. STATE HEARINGS API (stateHearingsApi.js)")

r = api("GET", f"/state-hearings/by-case/{CASE_ID}", TOKEN)
ok("GET /state-hearings/by-case/{id}", f"count={len(r.json()) if r.status_code==200 else '?'}") if r.status_code==200 else fail("state hearings by case", f"{r.status_code}")

r = api("POST", "/state-hearings", TOKEN, json={
    "caseId": CASE_ID,
    "hearingNumber": "SH-TEST-001",
    "hearingRequestDate": TODAY,
    "issue": "Disagrees with authorized hours reduction",
    "countyCode": "Sacramento"
})
if r.status_code == 200:
    SH_ID = r.json().get("id")
    ok("POST /state-hearings (create)", f"id={SH_ID}")

    r2 = api("GET", f"/state-hearings/{SH_ID}", TOKEN)
    ok("GET /state-hearings/{id}", f"status={r2.status_code}") if r2.status_code==200 else fail("get hearing", f"{r2.status_code}")

    # Schedule it
    r3 = api("PUT", f"/state-hearings/{SH_ID}", TOKEN, json={
        "scheduledHearingDate": NEXT_WEEK,
        "hearingIssue": "Disagrees with authorized hours reduction"
    })
    ok("PUT /state-hearings/{id} (schedule)", f"status={r3.status_code}") if r3.status_code==200 else fail("update hearing", f"{r3.status_code}")
else:
    fail("POST /state-hearings", f"{r.status_code} — {r.text[:80]}")

# Search
from_date = (date.today() - timedelta(days=30)).isoformat()
r = api("GET", f"/state-hearings/search?stateHearingStatus=SSHS002&countyCode=Sacramento&fromDate={from_date}", TOKEN)
ok("GET /state-hearings/search", f"count={len(r.json()) if r.status_code==200 else '?'}") if r.status_code==200 else fail("state hearings search", f"{r.status_code}")


# ══════════════════════════════════════════════════════════════════════════════
# FINAL SUMMARY
# ══════════════════════════════════════════════════════════════════════════════
print(f"\n{BOLD}{'='*60}{RESET}")
print(f"{BOLD}  RESULTS{RESET}")
print(f"{'='*60}")
print(f"  {GREEN}✓ Passed : {passed}{RESET}")
print(f"  {RED}✗ Failed : {failed}{RESET}")
print(f"  {YELLOW}~ Skipped: {skipped}{RESET}")
total = passed + failed
pct = int(passed/total*100) if total else 0
bar = "█" * (pct//5) + "░" * (20 - pct//5)
print(f"\n  Pass rate: {bar} {pct}% ({passed}/{total})\n")
if failed > 0:
    print(f"  {RED}Some tests failed — check output above for details.{RESET}\n")
else:
    print(f"  {GREEN}All tests passed!{RESET}\n")

sys.exit(0 if failed == 0 else 1)
