# Study UI Revamp — Design Spec

## Goal

Rewrite `study.html` from the current sidebar-based dark IDE aesthetic to a focused, Anki-style single-page app with VS Code Dark+ code blocks and a top tab navigation.

## Architecture

Single self-contained HTML file. No build tools, no CDN dependencies, no frameworks. All CSS and JS inline. Card data injected via `addTopic()` calls at the bottom — unchanged. localStorage for progress persistence — unchanged.

## Visual Design

**Palette**
- Page background: `#0a0a0a`
- Card background: `#111`
- Card border: `#1f1f1f`
- Active tab / reveal button: `#ffffff`
- Text primary: `#f0f0f0`
- Text secondary: `#888`
- Got-it accent: `#3fb950`
- Review accent: `#f85149`
- Hint accent: `#c9a227` on `#161208`
- Code editor bg: `#1e1e1e`
- Code header bg: `#2d2d2d`

**Typography**
- UI chrome: `Inter, -apple-system, BlinkMacSystemFont, sans-serif`
- Code blocks: `'Fira Code', monospace`

## Layout

```
┌─────────────────────────────────────────────────┐
│  [Strings 5/19] [Java 3/10] [DP 0/14] [Trees ●] │  ← tab bar
├─────────────────────────────────────────────────┤
│ ▓▓▓▓▓░░░░░░░░░░░░░░░░░░░░░░░░  (progress bar)   │
│                                                  │
│         ┌───────────────────────┐                │
│         │ 🌳 Trees        5/13  │                │
│         │ Title                 │                │
│         │ Question text...      │                │
│         │ [hint box - optional] │                │
│         │ ─────────────────     │                │
│         │ Answer text...        │                │
│         │ ┌─ code block ──────┐ │                │
│         │ │ C++        ⎘ copy │ │                │
│         │ │ 1 │ code...       │ │                │
│         │ └───────────────────┘ │                │
│         │ [Hint][Got it][Review]│                │
│         │             [←][Next→]│                │
│         └───────────────────────┘                │
│                                                  │
├─────────────────────────────────────────────────┤
│ 20/83 known  ■20 known  ■7 review   space g r h ←→│
└─────────────────────────────────────────────────┘
```

## Components

### Top Tab Bar
- One tab per topic, horizontally scrollable on small screens
- Each tab: `icon name count/total`
- Active tab: `color:#fff; border-bottom: 2px solid #fff`
- Inactive: `color:#555`
- Clicking a tab calls `selT(i)` — same as current sidebar

### Progress Bar
- 2px white bar directly below the tab bar
- Width = `(known / total) * 100%`
- Smooth CSS transition on update

### Card
- Max-width `700px`, centered, `border-radius: 12px`
- Sections in order:
  1. **Meta row**: topic tag (left) + `N / total` (right)
  2. **Title**: `font-size: 17px`, `font-weight: 600`
  3. **Question**: `font-size: 13px`, `color: #888`, `white-space: pre-wrap`
  4. **Loci** (if present): green badge, hidden otherwise
  5. **Hint box** (hidden until Hint clicked): amber border + bg
  6. **Answer section** (hidden until Reveal clicked):
     - Answer text: `font-size: 13px`, `color: #c0c0c0`
     - Code block (if present): VS Code Dark+ (see below)
  7. **Footer**: action buttons

### Code Block — VS Code Dark+
```
┌─ header ────────────────────────────────┐
│  [lang badge]                  [⎘ copy] │
├─ body ──────────────────────────────────┤
│ 1 │                                     │
│ 2 │  actual code here                   │
│ 3 │                                     │
└─────────────────────────────────────────┘
```
- Line numbers: `color:#404040`, right-aligned, `border-right: 1px solid #2d2d2d`
- Code area scrollable horizontally
- Syntax color classes (applied via JS token regex at render time):
  - `.kw` keywords → `#c586c0`
  - `.ty` types/classes → `#4ec9b0`
  - `.fn` function names → `#dcdcaa`
  - `.st` strings → `#ce9178`
  - `.cm` comments → `#6a9955`
  - `.nm` variables → `#9cdcfe`
  - `.nu` numbers → `#b5cea8`
- Copy button: copies raw code text to clipboard via `navigator.clipboard`
- Language detected from first token of code string (e.g. `// Java` comment → "Java", `class ` → "Java", `#include` → "C++", `def ` → "Python")

### Footer Buttons
| Button | Style | Condition |
|--------|-------|-----------|
| Hint | outline `#2a2a2a` | always visible |
| Reveal ↓ | white filled | hidden after reveal |
| ✓ Got it | outline green | visible after reveal |
| ↩ Review | outline red | visible after reveal |
| ← Prev | outline grey | always |
| Next → | outline grey | always |

### Bottom Stats Bar
- `background: #0d0d0d`, `border-top: 1px solid #1a1a1a`
- Left: `N / total known`, green known count, red review count
- Right: keyboard shortcut hints with `<kbd>`-style badges

## Keyboard Shortcuts
- `Space` / `Enter` → reveal
- `g` → got it
- `r` → review
- `h` → hint
- `←` → prev
- `→` → next
- `1`–`7` → jump to topic N

## Syntax Highlighting Implementation

No CDN. Pure JS regex applied at render time inside `renderCard()`:

```js
function highlight(code) {
  // escape HTML first
  // then apply regex replacements in safe order:
  // 1. comments (// ... and /* ... */)
  // 2. strings (" " and ' ')
  // 3. keywords (if/else/return/class/etc)
  // 4. types (capitalized identifiers)
  // 5. function calls (word followed by '(')
  // 6. numbers
}
```

Applied only to the `<pre>` content, not to answer text.

Line numbers generated dynamically by counting `\n` in code string.

## State & Data

- `TOPICS` array and `addTopic()` function: unchanged
- `localStorage` schema: `c_{id}` → `'new' | 'known' | 'review'` — unchanged
- `ti` (topic index), `ci` (card index), `revealed` — unchanged

## Files Changed

| File | Change |
|------|--------|
| `study.html` | Full rewrite of `<style>`, `<header>`→tabs, `<nav>`→removed, `<main>`→centered stage, script logic updated for new DOM, syntax highlighter added. Card data `addTopic()` calls at bottom: **untouched**. |
