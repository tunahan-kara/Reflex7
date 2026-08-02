# Reflex7 v1.1.0

Reflex7 is an irritating but fair retro browser game about reflexes, attention, memory, and reading the instruction that is actually on screen. It is a static, local-only project with Turkish and English interfaces. There are no accounts, online leaderboards, analytics, or backend services.

Reflex7; refleks, dikkat, hafıza ve ekrandaki gerçek talimatı okumaya dayanan, sinir bozucu ama adil bir retro tarayıcı oyunudur. Türkçe varsayılan dildir; İngilizce arayüz de eksiksizdir.

## Gameplay

- 22 registered mechanics across reaction, inhibition, timing, visual, arithmetic, memory, sequence, and deception categories
- Fair anticipation tasks: Wait, Last Second Instruction, and Patience Countdown
- Weighted task selection with immediate-repeat prevention and recent-category penalties
- Six compatible difficulty modifiers and five multi-round global rules
- 7-second and 4-second modes, with task timings clamped to remain completable
- Mouse, touch, Pointer Events, Enter, and Space input
- Explicit pause with `P` or `Escape`; changing tabs uses the same pause path and requires a deliberate resume
- Persistent mechanic discovery, concise first-run guidance, and a bilingual How to Play panel
- Optional generated Web Audio beeps with a saved on/off preference
- Responsive mobile layout and reduced-motion support

Instructions can be deceptive, but the correct response is always determined by visible information and the active rule. Cosmetic decoys are non-interactive and never change task correctness.

## Modes, score, and combo

The two modes keep independent best levels and best scores in the current browser. Response time is normalized by the task duration, so the time component does not inherently favor the 7-second mode.

For every successful task:

```text
base = 100
     + level × 5
     + task difficulty × 25
     + modifier count × 20
     + 15 when a global rule is active
     + floor(clamp(remaining time / task duration, 0, 1) × 100)

combo multiplier = 1 + min(max(combo - 1, 0), 10) × 0.05
awarded score    = round(base × combo multiplier)
```

The combo increases after each consecutive success, appears from ×2 onward, and resets on failure. It changes score only; it never changes task logic or timing.

## Controls and pause behavior

- Pointer/touch: activate the visible task control. Hold tasks use pointer capture and complete on the matching release.
- Keyboard: focus the main control and use `Enter` or `Space`.
- Pause/resume: press `P`, `Escape`, or the pause button.
- Background tab: the session pauses automatically but never resumes automatically.

Pausing freezes the monotonic main timer and tracked task timeouts. A short input guard after resume prevents the resume action from reaching the task. Paused wall-clock time is excluded from the displayed session duration and cannot increase the response-time score.

## Languages and local data

Turkish is the default. TR/EN controls are available on the menu and result screen, and changes take effect immediately. All interface, generated task, failure, rule, modifier, onboarding, pause, and result text comes from the centralized dictionaries in `script.js`.

The guarded `localStorage` values are:

- `reflex7_language`
- `reflex7_sound`
- `reflex7_tip_seen`
- `reflex7_discoveries`
- `reflex7_best_level_7` and `reflex7_best_score_7`
- `reflex7_best_level_4` and `reflex7_best_score_4`
- `reflex7_best` — legacy best-level value, read only for one-way migration

Malformed or unavailable storage does not prevent play; preferences and records simply may not survive a new browser session. Scores are not uploaded and should not be treated as server-verified.

## Run locally

Clone the repository:

```bash
git clone https://github.com/tunahan-kara/Reflex7.git
cd Reflex7
```

The basic game can be opened directly from `index.html`. To test the PWA, service worker, manifest, offline route, and HTTP caching behavior, serve the repository over localhost instead:

```bash
python -m http.server 8080
```

Open `http://localhost:8080`. Service workers require localhost or HTTPS and do not register from `file://` URLs.

There is no package manager, framework, compilation, or build output directory.

## Tests

Node.js is optional for gameplay but required for the deterministic checks:

```bash
node --check script.js
node --check sw.js
node tests/task-engine.test.js
git diff --check
```

The harness covers translation parity, all 22 mechanics, timing gates, task/category selection, modifiers, global rules, scoring, combo, per-mode records, legacy migration, denied storage, pointer-release isolation, transition input locking, timeout cleanup and pause/resume, tab visibility, clean retry state, audio preference/cooldown/channel behavior, discoveries, onboarding, manifest assets, service-worker assets, reduced motion, narrow/short viewport rules, and DOM reference consistency.

## PWA and offline behavior

`manifest.webmanifest` provides local SVG icons and standalone metadata. `sw.js` uses the versioned `reflex7-v1.1.0-r2` cache and stores only the local application shell, optimized background, icons, and offline page. It does not cache Google Fonts. Navigations are network-first, local assets are cache-first with background refresh, and activation removes older Reflex7 caches.

In a supported browser on localhost or HTTPS, use the browser's Install App action. For update testing, reload once after a new service worker activates; the HTML, manifest, and worker receive no-cache headers on Cloudflare Pages.

## Static and Cloudflare Pages deployment

Any static host can serve the repository as-is while preserving relative paths. For Cloudflare Pages:

1. Push the repository to GitHub and choose **Workers & Pages → Create → Pages → Connect to Git**.
2. Select the repository. Leave the build command empty and set the output directory to the repository root (`.`).
3. Deploy. `_headers` supplies the security and cache policy; `404.html` provides the static not-found page.
4. For a custom domain, open the Pages project's **Custom domains**, add the hostname, and follow Cloudflare's DNS validation steps.
5. Replace the reserved `https://example.invalid/reflex7/` canonical URL in `index.html` with the final absolute production URL. No real domain is hardcoded before one is known.

Cloudflare normally purges changed deployment assets automatically. The service worker uses a release-specific cache name; bump it for each release. If an emergency stale client remains, deploy a new cache version rather than reusing the old name.

The Content Security Policy allows local assets, Web Audio, data/blob media, and Google Fonts styles/font files. It blocks objects, frames, forms, and unnecessary browser capabilities.

## Asset optimization

`bg.png` is the optimized 1,744,573-byte background used on desktop and mobile. Screens at 380px or less use a CSS gradient and do not request the background image.

## Project structure

- `index.html` — semantic application shell, menus, HUD, pause/result dialogs, metadata, and PWA links
- `style.css` — visual identity system, layout, task presentation, urgency, responsive, and reduced-motion rules
- `script.js` — translations, guarded persistence, audio, session state, timer/input lifecycle, Task Engine v2, all mechanics, modifiers, and rules
- `tests/task-engine.test.js` — dependency-free deterministic fake-browser harness
- `manifest.webmanifest`, `sw.js`, `offline.html` — installability and offline application shell
- `assets/` — lightweight local brand/favicons
- `_headers`, `404.html`, `robots.txt` — static deployment policy and fallback files
- `bg.png` — optimized responsive background

## Known limitations

- Records and discoveries are device/browser-profile local and can be cleared by the player.
- There is no server validation, account sync, multiplayer, or online leaderboard.
- Google Fonts is an optional network dependency. If unavailable, the game falls back to monospace system fonts; the service worker intentionally does not cache it.
- SVG manifest icons work in modern browsers, but install UI and icon rendering still vary by browser/platform.
- The automated harness simulates browser APIs; final release QA still requires real mouse, touch, keyboard, audio, install, offline, and viewport testing.

## Developer

Tunahan Kara
