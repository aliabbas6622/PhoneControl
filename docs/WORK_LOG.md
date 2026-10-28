# Work log and roadmap

Status notes for the current fork. Records what changed, how it was verified,
what is still broken, and what to pick up next. Commit under review:
`feat: redesign app and web portal UI, add built-in routes and password change`.

<br>

## 1. Web portal (main focus)

The browser side was restyled into a single dark "self-hosted console" theme so
the dashboard, login, and account pages read as one product with the app.

Design tokens (shared across all three HTML builders):

| Token | Value | Use |
| --- | --- | --- |
| `--bg` | `#0a0b0d` | page background |
| `--surface` | `#14171c` | cards, panels |
| `--surface-2` | `#1b1f26` | raised controls |
| `--border` | `#272d37` | hairlines |
| `--accent` | `#00d4aa` | brand teal, focus rings, active state |
| `--accent-bright` | `#5cffd9` | hover / emphasis |

Pages and what changed on each:

- **Dashboard** (`DashboardHtmlBuilder.kt`) — flame SVG brand mark, badge pills
  for route states, 44–48px touch targets, and a header `Change password` link
  to `/account`. The remaining stale `var(--cyan)` references (battery gauge
  stroke, memory bar fill, file link) were repointed at `var(--accent)`; the
  dashboard had no live accent variable, which is why those widgets rendered
  without color.
- **Login** (`LoginHtmlBuilder.kt`, new) — brand mark, single password field
  with Show/Hide, inline error in `role="alert"`, `?next=` round-trip so a
  protected deep link survives the sign-in redirect.
- **Change password** (`ChangePasswordHtmlBuilder.kt`, new) — current / new /
  confirm fields with per-field Show/Hide, live length rule hint, amber
  `Security note` about the token doubling as the access token, cancel back to
  `/api/dashboard`, and HTML-escaped server errors.

Accessibility and motion: `:focus-visible` rings on every control,
`aria-pressed` / `aria-expanded` on toggles, `role="alert"` for errors,
`prefers-reduced-motion` respected, `theme-color` `#0a0b0d`.

<br>

## 2. Android app

Followed the portal work rather than leading it, so both ends share one palette.

- `ui/theme/Color.kt`, `Theme.kt`, `Type.kt` — teal/flame accent, dark surfaces,
  tighter type scale.
- `SettingsScreen.kt` — new `SettingsCard` wrapper (14dp shape,
  `surfaceContainerLow` + `outlineVariant` border, 20dp padding) applied to all
  five cards; Remote Access moved to position 2; the access token sits in a
  monospace `surfaceContainerHighest` surface; `Change password` is a
  full-width tonal button; collapsible headers get 12dp vertical padding.
- `HomeAppBar.kt`, `HomeScreen.kt`, `RouteCard.kt`, `MainBottomBar.kt`,
  `LogsScreen.kt` — same accent, spacing, and elevation pass.

<br>

## 3. Authentication and password change

The token is now changeable from either end, which required one real design
decision: the access token *is* the credential, so changing it must invalidate
every existing session.

- `domain/model/AccessPassword.kt` (new) — the single validation boundary:
  trim, 8–128 chars, reject code points below `0x20` and `0x7F`; interior
  spaces allowed. `TOO_SHORT` / `TOO_LONG` / `INVALID_CHARACTERS`.
- `features/http/configuration/AuthConfiguration.kt` (new) — application-pipeline
  interceptor plus `finish()`. Public paths are `/`, `/login`, `/logout`,
  `/favicon.ico`; everything else needs the HttpOnly `hof_session` cookie
  (SameSite=Lax, `Secure` under https, 30-day max age) or a `Bearer` header.
  HTML requests get a `/login?next=…` redirect, API requests get `401`.
  `GET`/`POST /account` send `Cache-Control: no-store`; a wrong current
  password or a rejected new one returns `400` with the error rendered inline;
  success clears the cookie and redirects to `/login` so the caller cannot keep
  an authenticated page open.
- `SettingsRepository.setAccessToken()` — trims and writes under the existing
  mutex. `SettingsViewModel.changeAccessToken()` returns a
  `PasswordChangeResult`, and `refreshAccessToken()` re-reads the token on
  `ON_RESUME` (registered in `TabSettingsScreen`) so a password changed in the
  browser shows up in the app without a restart.
- `server.kt` / `ServerConfigurationService.kt` thread `accessTokenProvider`
  and `accessTokenUpdater` through, so the server never caches a stale token.

No credential is stored in this repository. The portal password was set on the
device through `/account`; use that page to rotate it.

<br>

## 4. Built-in API surface

Handlers live in `features/http/routing/routes/builtin/` as
`internal fun io.ktor.server.routing.Route.addXxxRoute(route: Route)` extension
functions. `BuiltInRouteRegistry` supplies the path/method/description metadata,
and the `RouteType` dispatch in `domain/model/Route.kt` picks the handler.

This change set added 22 files there (21 device-control routers plus the shared
`BuiltinRouteUtils.kt`) covering `/api/battery` `/api/sensors` `/api/device`
`/api/system` `/api/wifi` `/api/clipboard` `/api/contacts` `/api/camera`
`/api/location` `/api/brightness` `/api/volume` `/api/flashlight`
`/api/vibrate` `/api/ring` `/api/speak` `/api/toast` `/api/apps`
`/api/wallpaper` `/api/qr` `/api/echo` `/files`. At `HEAD` all 31 registered
handlers are wired, from `/api/status` through `/api/wifi`.

Six endpoints became newly *advertised* in the registry by this commit:
`/files`, `/api/sensors`, `/api/brightness`, `/api/toast`, `/api/system`,
`/api/wallpaper`. The rest of the paths were already listed there.

`/files` is the shared inbox: `FileDropRouter.kt`, `FileDropService.kt`, and
`data/storage/FileDropStorage.kt`.

Unverified history note: at `HEAD~1` `Route.kt` already referenced handler names
whose router files were not in the tree. Whether upstream `HEAD~1` built cleanly
was never checked — this fork's build is green, and nothing here claims more than
that observation.


<br>

## 5. Verification

Ran and observed:

- `./gradlew assembleDebug testDebugUnitTest` with
  `JAVA_HOME=C:/Users/aliab/.jdks/jbr-21.0.11` — build green, `AccessPasswordTest`
  (8 cases) and the updated `BuiltInRouteRegistryTest` passing.
- Installed on a Samsung R8VY402N79D (720×1600) and screenshotted the redesigned
  login, dashboard, and change-password pages; exercised the dialog validation
  path (a mistyped confirm really does produce "Passwords don't match").
- Change-password round-trip both directions — phone → portal and portal →
  phone — confirming the old token is rejected, the new one authenticates, the
  session cookie is cleared, and `/api/dashboard` returns `200`. Original token
  restored afterwards.
- Redesigned login page rendered in real mobile Chrome over the tunnel, not just
  in curl output.

Not verified / out of scope:

- Ghidra, IDA, or native binaries — this is a Kotlin/Compose source change set.
- Tablet and foldable layouts; only the 720×1600 phone was driven.
- Concurrent multi-client sessions after a password change (existing cookies are
  invalidated on the next request, but that was not load-tested).

<br>

## 6. Known issues, deliberately not fixed

1. **Settings switches render OFF on a fresh process.** DataStore really holds
   `auto_start=true` and `background_service_enabled=true` — confirmed by dumping
   `files/datastore/settings.preferences_pb` via `run-as` + `od`. The correct
   state appears after the first DataStore write or an `ON_RESUME`. Suspected in
   the initial `loadSettings()` `combine` emission timing; root cause not
   proven, and a downstream UI patch would have hidden the producing boundary,
   so it is left open.
2. **Unauthenticated `GET /account` returns `401` for non-browser `Accept`
   headers.** Browsers do get the `/login?next=…` redirect. Behavior is arguably
   correct for API clients; noted because it surprised during testing.

<br>

## 7. What to do next

In rough priority order:

1. **Fix the fresh-process switch state** at the repository/view-model boundary,
   not in the composable, and add a regression that reads DataStore before the
   first write.
2. **Per-route permission gating.** The routers currently inherit the whole
   app's granted permissions; a browser client can reach `/api/camera` or
   `/api/contacts` whenever the server is up. An opt-in "which routes may be
   remote-triggered" list is the honest fix, plus a `423`-style response when a
   route is disabled but its Android permission is missing.
3. **Rate limiting and lockout** on `/login` — the password is the only
   credential and is guessable by brute force on any reachable network.
4. **Session policy**: a real session list with revocation, and a "this device
   changed the password" notification, so the shared token/credential model
   stops being invisible.
5. **CSRF hardening review** for `POST /account` (SameSite=Lax is protection,
   not a token).
6. **Tablet/foldable and landscape layouts**, plus a light theme that keeps the
   portal contrast ratio.
7. **Screenshot refresh**: `docs/screenshot_*.png` predate the redesign, and
   `README.md` still describes the old UI.
8. **Move the `RouteType` dispatch out of `domain/model/Route.kt`.** The domain
   model imports HTTP routing extension functions, so the dependency runs the
   wrong direction; a routing-layer registry lookup would let the built-in
   routers be added or tested without touching `Route.kt`.
9. **Upstream sync**: `origin` is `zahidaz/HTTPOnFire` and this fork's `main`
   now tracks the `phonecontrol` remote. Decide whether to open a PR upstream or
   stay a private fork before the divergence grows.

<br>

## 8. Build and run

```bash
export JAVA_HOME="C:/Users/aliab/.jdks/jbr-21.0.11"
./gradlew assembleDebug testDebugUnitTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Toolchain is pinned by the Gradle wrapper, the Android Gradle plugin, and
`compileSdk` in `app/build.gradle.kts`, with versions in `gradle/libs.versions.toml`;
there is no Node toolchain in this repository. Remote access needs the phone's
tunnel or LAN address; the app shows the actual device IP in the app bar.
