# TV app — feature & UI/UX roadmap

Follow-up to `TV_PLAN.md` (architecture / phasing) and `UI_UX_PLAN.md` (mobile
audit). Scope: grow the shipping `:tv` app with new capability **and** 10-foot /
D-pad polish, reusing `:core`. Generated 2026-07-02 from a full read of the
current `:tv` + `:core` + `:mobile` sources.

Status legend: `[ ]` pending · `[~]` in progress · `[x]` done

Target audience decision: **many target boxes are rooted** → privileged install /
manage (A2, A3) is weighted high.

---

## Findings in current code (motivating this roadmap)

- 🐛 `ReceiveScreen.kt` collects `installResult` but never renders it — after an
  install the user gets no in-app success/failure feedback (only the system
  package-installer dialog). **→ A1.**
- ⚠️ Manage detail pane (`ManageScreen.kt`) is a `Column().verticalScroll()` with
  the `InfoBlock`s (storage/type/status) placed *below* the focusable actions;
  non-focusable content below the last focusable element is unreachable by D-pad.
  **→ B1.**
- No install progress while a large APK/bundle is written to the session — only a
  static "Installing…" label. **→ B2.**
- TV only uses the non-privileged `PackageInstaller` session; `:mobile`'s Root
  (`RootInstallController`) / Shizuku (`ShizukuInstallController`) stack is unused
  on TV even though TV boxes are often rooted. **→ A2/A3.**
- No "Send to TV" QR scan from mobile (only browser upload); no URL downloader.
  **→ A4/A5.**
- Theme has System/Light/Dark only; mobile has 5 accent presets. **→ B4.**

---

## Track A — new features

- [x] **A1 — Install-result feedback (fix)** (~0.5d) — `installResult` is now a
  sealed `InstallOutcome`; `InstallStatusOverlay` renders a bottom pill (Installing
  → Installed ✓ / Failed …) with Retry + Dismiss, success auto-dismisses after 3s,
  strings from resources. Received hero clears on success.
- [x] **A2 — Privileged install backend on TV (Root)** (~2d) — added Compose-free
  `RootShell` (`su -c`, no libsu dep so the mobile `store` flavor stays libsu-free)
  + `RootInstaller` (stage → `pm install-create -r`/`install-write`(+`cat` pipe
  fallback)/`install-commit`) in `:core`. `ReceiveViewModel` prefers the silent
  root path when available + enabled, falls back to the `PackageInstaller` session.
  Settings gained a "Silent install (Root)" toggle (default on) + live root status;
  the result overlay shows "Installed silently ✓".
  *(Shizuku path deferred — root covers the rooted-box target; Shizuku needs the
  rikka dep + reflection and is a smaller TV audience.)*
- [ ] **A3 — Manage: privileged actions + multi-select** (~1.5d) — when Root/
  Shizuku ready: Force-stop · Enable/Disable · Clear-data · silent Uninstall in the
  detail pane; a "Select" mode for batch uninstall (D-pad friendly, no long-press).
- [ ] **A4 — "Send to TV" QR scan from mobile** (~1d) — mobile scans the TV QR →
  parses URL+token → multipart-uploads the chosen APK. Browser upload stays as
  fallback.
- [ ] **A5 — Install from URL / link** (~1d) — URL entry (or receive a URL from the
  phone to avoid TV typing) → `DownloadManager` → into the install flow.
- [ ] **A6 — APK detail + permissions pre-install** (~0.5d) — expand
  `ApkDetailsDialog`: permissions list, minSdk-incompatibility warning,
  installed / update / downgrade badge.

## Track B — UI/UX (10-foot, D-pad)

- [x] **B1 — Focus fixes + default focus** (~1d) — Manage detail-pane focus trap
  fixed (metadata moved above the actions so the actions are the last focusable
  block); first app row gets a one-shot `FocusRequester` on each Manage visit.
  (Cross-tab focus *restoration* still open — deferred to a later polish pass.)
- [x] **B2 — Install progress bar** (~0.5d) — `ApkInstaller.install` gained an
  `onProgress`/`totalBytes` callback (manual buffered copy); `ReceiveViewModel`
  exposes `installProgress: Float?`; the A1 overlay shows a determinate
  `LinearProgressIndicator` (indeterminate when size is unknown).
- [ ] **B3 — Safe-area / overscan + 10-foot type** (~0.5d) — 5% edge-safe padding,
  minimum legible type sizes, consistent focus border/glow via tv-material3.
- [ ] **B4 — Accent color presets** (~0.5d) — port the 5 mobile presets into TV
  Settings, persisted in the shared `dataStore`.
- [ ] **B5 — TV-friendly text entry** (~0.5d) — voice search (leanback) / alpha
  quick-filter chips for Manage; keep the typed field as secondary.
- [ ] **B6 — Empty/error states + Extract polish** (~0.5d) — illustrated empty
  states; after Extract show the output path + "open Downloads".

---

## Suggested order

1. ~~**A1 + B1 + B2** — fix real bugs + focus foundation.~~ ✅ done (`:tv:assembleDebug` green)
2. ~~**A2** — privileged root backend (highest leverage on rooted boxes).~~ ✅ done
3. **A3** — Manage privileged + batch. *(~1.5d)*
4. **B3 + B4 + B6** — 10-foot polish. *(~1.5d)*
5. **A4 + A5** — send-to-TV + URL. *(~2d)*
6. **A6 + B5** — APK detail + TV-friendly input. *(~1d)*

Each item ships independently; keep `:tv:assembleDebug` green after each.
