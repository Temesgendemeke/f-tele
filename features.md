# Features

Roadmap for fully offline protection against scam SMS and telecom spam. Everything here runs
on-device — no INTERNET permission, no accounts, no telemetry.

> Status: the pattern engine is shipped. See `modules/sms-muter/patterns.ts` (matching), mirrored in
> `Patterns.kt`, with configurable packs in `patterns.config.ts` and tests in `patterns.test.ts`.

## 1. Core rule engine upgrades

- [x] Regex + wildcard rules (`^\+234.*`, `.*loan.*`) beyond fixed substring rule types
- [ ] Rule priority + ordering (drag to reorder, first match wins, "stop processing" flag)
- [x] Rule types: contains, exact, starts-with, ends-with, wildcard, regex (number-range still todo)
- [ ] Rule groups/categories with per-group enable toggles (Bank OTP, Promo, Scam words) — packs + per-rule toggle exist, no group toggle yet
- [ ] Import/export rules as JSON/CSV via file picker (offline backup and share)
- [x] In-app regex tester (preview a sample SMS against a rule before saving)

## 2. Scam protection (offline heuristics)

- [ ] Risk scoring: combine signals (unknown sender + shortened link + urgency/threat words + brand impersonation)
- [x] Scam phrase/word lists (starter pack shipped; expand as needed)
- [ ] Homoglyph/leetspeak normalization before matching (`0TP`, `fr33`, `V1AGRA`, full-width chars)
- [ ] Link detection: extract URLs, flag shorteners (bit.ly, t.co, tinyurl) and bundled local scam-domain list
- [ ] Impersonation check: brand keywords whose sender ID does not match the real sender-ID list
- [ ] Number-spoofing hints: sender claims a bank but number is not in that bank's known set
- [ ] Flood/duplicate detection: auto-mute a sender after N messages in a window; cluster broadcast spam
- [ ] Multi-language keyword lists (transliterated patterns, e.g. Hinglish/Yoruba/Arabizi)
- [x] OTP protection: hard allow real OTP/verification patterns so filters never eat login codes

## 3. Telecom / carrier spam protection

- [ ] Header-class rules: mute by SMS sender-ID category (promotional `AD-*`, transactional `JD-*`, operator) via local per-country prefix mapping
- [ ] Operator/carrier sender lists (plan-expiry, data-balance, recharge, roaming, VAS/subscription offers)
- [ ] Short-code classifier (treat 5-6 digit codes differently from personal numbers)
- [ ] Circle/region lists: mute offers from known telecom bulk senders
- [ ] "Only personal contacts" mode: non-contacts quarantined unless allowlisted

## 4. Custom blocking (user control)

- [ ] One-tap block/allow from Activity log, extended to regex/number-range suggestions
- [ ] Contact-aware presets: always allow contacts, always block non-contacts
- [ ] Time-based rules: silence promos at night / office hours
- [ ] Number-range and prefix blocks across a country code
- [ ] Bulk selection in the log to create rules from many messages at once
- [ ] Duplicate/conflict detection to keep rule lists clean
- [ ] Per-thread mute (mute one conversation only)

## 5. Stronger architecture options

- [ ] Become the default SMS app: block at the SMS layer (no notification blip), never display spam
- [ ] Quarantine vault: blocked messages go to an encrypted local store instead of the inbox
- [ ] Encrypted storage for rules and logs
- [ ] Call screening: block/mute calls from the same scam numbers offline
- [ ] Remove `INTERNET` permission entirely as a verifiable privacy guarantee
- [ ] Dual-SIM aware rules

## 6. UX, stats & control

- [ ] Explainability: "muted because: link + threat word + unknown sender (score 82)"
- [ ] Learn from corrections: "Not spam" builds a local allowlist; "Spam" reinforces sender reputation (on-device only)
- [ ] Stats dashboard: blocked count, top offenders, per-rule hit counts
- [ ] Daily/weekly blocked digest notification
- [ ] Onboarding wizard for notification access + default-SMS-app setup

## Suggested build order

- [ ] 1. Regex/wildcards + rule priority + import/export (unlocks everything else)
- [ ] 2. Risk scoring + scam phrase lists + link detection + OTP protection
- [ ] 3. Telecom header-class + short-code + non-contact modes
- [ ] 4. Quarantine vault, then default-SMS-app role, then call screening
