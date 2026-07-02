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
- [~] **A3 — Manage: privileged actions + multi-select** (~1.5d) — **single-app
  privileged actions done**: Force-stop · Enable/Disable · Clear-data · silent
  Uninstall now render in the detail pane, gated on `uiState.rootAvailable`, with
  destructive-confirm dialogs and a screen-level result pill (`actionResult`, which
  the VM already exposed but the UI never showed). *Multi-select / batch uninstall
  still open (VM has `selectionMode`/`batchUninstall`; no UI entry yet).*
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
- [x] **B3 — Safe-area / overscan + 10-foot type** (~0.5d) — whole `Type.kt` ramp
  lifted above the couch floor (added the missing `labelLarge` that drove nav labels
  + buttons at 14sp, plus `titleSmall`/`bodySmall`/`headline*`); nav-rail content
  inset past the ~5% overscan cut (rail widened to fit); stronger focus affordance on
  action rows (solid color-shift + 1.05 scale, destructive rows tinted `error`).
- [x] **B4 — Accent color presets** (~0.5d) — 5 mobile presets ported to TV via
  `ColorScheme.withAccent` (reuses the shared `AppThemePreset`), persisted to the same
  `theme_preset` dataStore key (mobile↔TV parity); swatch picker in Settings.
- [~] **B5 — TV-friendly text entry** (~0.5d) — Manage search field enlarged with a
  strong filled/primary focus cue (was a color-only mobile `OutlinedTextField`).
  *Voice search / alpha quick-filter chips still open.*
- [x] **B6 — Empty/error states + Extract polish** (~0.5d) — illustrated empty
  states for Manage (empty + no-match w/ Clear-search) and Local files (empty +
  scanning + Rescan/Receive actions); no-network state replaces the dead `0.0.0.0`
  QR; Extract progress/result moved into the reachable status pill (was a focus trap
  below the actions). *"Open Downloads" skipped — extract writes a `file://` DocumentFile
  and TV boxes lack a Downloads viewer.*
- [x] **B7 — Onboarding theme bridge + startup polish** (this pass) — shared `:core`
  onboarding rendered in stock light-purple on TV (it reads mobile-material3); added a
  TV-only `OnboardingThemeBridge` so it matches the orange/navy brand + honors dark
  mode. Single-instance splash (no double-fade, no replay on locale change), in-place
  locale switch via `recreate()`, tab survives config change, LanguageScreen `BackHandler`.

---

## Suggested order

1. ~~**A1 + B1 + B2** — fix real bugs + focus foundation.~~ ✅ done (`:tv:assembleDebug` green)
2. ~~**A2** — privileged root backend (highest leverage on rooted boxes).~~ ✅ done
3. ~~**A3 (single-app) + B3 + B4 + B6 + B7** — privileged detail actions + 10-foot
   polish + onboarding/startup.~~ ✅ done (`:tv:assembleDebug` green)
4. **A3 (multi-select)** — batch uninstall UI on top of the existing VM support.
5. **A4 + A5** — send-to-TV + URL. *(~2d)*
6. **A6 + B5 (voice)** — APK detail + TV-friendly input. *(~1d)*

Each item ships independently; keep `:tv:assembleDebug` green after each.
