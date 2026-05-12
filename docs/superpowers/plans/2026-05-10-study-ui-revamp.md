# Study UI Revamp Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rewrite `study.html` chrome (CSS + HTML + JS) into a Focus/Anki-style layout with top tab navigation, VS Code Dark+ syntax-highlighted code blocks, and a bottom stats bar — preserving all card data.

**Architecture:** Single self-contained HTML file. No CDN, no build tools. New CSS replaces sidebar layout with centered-card stage. New JS replaces old renderSB/renderCard with renderTabs/renderCard. Syntax highlighter uses a placeholder-token approach so comments/strings are safe from keyword coloring. All `addTopic()` data calls below `<!-- CARD DATA injected below -->` are untouched.

**Tech Stack:** Vanilla HTML/CSS/JS, localStorage, Fira Code (system font stack fallback)

---

## File Map

| File | Change |
|------|--------|
| `study.html` lines 1–158 | Full replacement — new CSS, new DOM, new JS |
| `study.html` line 159 onward | **Untouched** — all `addTopic()` card data |

---

### Task 1: Capture card data + write new study.html

**Files:**
- Modify: `C:\Users\mayan\OneDrive\Documents\Interview\uber\study.html`

- [ ] **Step 1: Read the current file — note the card data block**

  Open `study.html`. Everything from the line `<!-- CARD DATA injected below -->` to the end of the file is the card data. You will need to preserve it exactly. Do NOT modify any `addTopic({...})` call.

- [ ] **Step 2: Replace everything above the card data marker**

  Use the Edit tool to replace the old_string from `<!DOCTYPE html>` through the closing `</script>` of the core JS block (ending just before `<!-- CARD DATA injected below -->`).

  The new content to put in its place (exact replacement):

```html
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>Interview Prep</title>
<style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:'Inter',-apple-system,BlinkMacSystemFont,sans-serif;background:#0a0a0a;color:#e0e0e0;height:100vh;display:flex;flex-direction:column;overflow:hidden}
.tabbar{display:flex;align-items:stretch;background:#111;border-bottom:1px solid #1f1f1f;flex-shrink:0;overflow-x:auto;scrollbar-width:none}
.tabbar::-webkit-scrollbar{display:none}
.tab{display:flex;align-items:center;gap:6px;padding:10px 16px;font-size:12px;cursor:pointer;white-space:nowrap;color:#555;border-bottom:2px solid transparent;transition:color .15s,border-color .15s;user-select:none;flex-shrink:0}
.tab:hover{color:#aaa;background:#161616}
.tab.active{color:#fff;border-bottom-color:#fff}
.tab .tc{font-size:10px;color:#444}
.tab.active .tc{color:#666}
.pwrap{height:2px;background:#1a1a1a;flex-shrink:0}
#pf{height:100%;width:0%;background:#fff;transition:width .4s}
.stage{flex:1;display:flex;align-items:flex-start;justify-content:center;padding:28px 20px;overflow-y:auto;scrollbar-width:thin;scrollbar-color:#1f1f1f #0a0a0a}
.stage::-webkit-scrollbar{width:6px}
.stage::-webkit-scrollbar-track{background:#0a0a0a}
.stage::-webkit-scrollbar-thumb{background:#1f1f1f;border-radius:3px}
.card-wrap{width:100%;max-width:700px}
.card{background:#111;border:1px solid #1f1f1f;border-radius:12px;overflow:hidden}
.card-meta{display:flex;align-items:center;justify-content:space-between;padding:14px 18px 0}
.card-tag{font-size:11px;color:#555;text-transform:uppercase;letter-spacing:.8px}
.card-num{font-size:11px;color:#444}
.card-title{padding:8px 18px 0;font-size:17px;font-weight:600;color:#f0f0f0;line-height:1.4}
.card-q{padding:8px 18px 16px;font-size:13px;color:#777;line-height:1.75;white-space:pre-wrap}
.loci-box{margin:0 18px 12px;padding:8px 12px;background:#0a1a0a;border:1px solid #1a3a1a;border-radius:6px;font-size:11px;color:#3fb950}
.hint-box{margin:0 18px 12px;padding:10px 14px;background:#161208;border:1px solid #2a2008;border-radius:8px;font-size:12px;color:#c9a227;line-height:1.6}
.ans-section{padding:0 18px 16px;border-top:1px solid #1a1a1a}
.ans-text{font-size:13px;color:#b0b0b0;line-height:1.8;white-space:pre-wrap;padding:14px 0 12px}
.code-block{background:#1e1e1e;border-radius:8px;overflow:hidden;border:1px solid #2d2d2d;margin-bottom:4px}
.code-hdr{background:#2d2d2d;padding:6px 12px;display:flex;align-items:center;justify-content:space-between;border-bottom:1px solid #3a3a3a}
.code-lang{background:#3c3c3c;color:#ccc;padding:2px 8px;border-radius:3px;font-size:9px;font-family:'Fira Code',monospace;letter-spacing:.5px}
.code-copy{background:transparent;border:none;color:#666;cursor:pointer;font-size:11px;padding:2px 6px;border-radius:3px;font-family:inherit;transition:all .15s}
.code-copy:hover{color:#aaa;background:#3c3c3c}
.code-body{display:flex;overflow-x:auto}
.line-nums{background:#1e1e1e;padding:12px 8px 12px 10px;text-align:right;color:#404040;font-size:11px;line-height:1.75;min-width:36px;border-right:1px solid #2d2d2d;font-family:'Fira Code',monospace;user-select:none;flex-shrink:0;white-space:pre}
.code-pre{padding:12px 14px;font-family:'Fira Code',monospace;font-size:11px;line-height:1.75;flex:1;color:#d4d4d4;margin:0;white-space:pre}
.kw{color:#c586c0}.ty{color:#4ec9b0}.fn{color:#dcdcaa}.st{color:#ce9178}.cm{color:#6a9955;font-style:italic}.nu{color:#b5cea8}
.card-foot{display:flex;align-items:center;gap:7px;padding:12px 18px;border-top:1px solid #1a1a1a;flex-wrap:wrap}
.btn{padding:6px 14px;border-radius:7px;border:1px solid;font-size:11px;cursor:pointer;font-family:inherit;transition:all .15s;white-space:nowrap}
.btn-hint{background:transparent;border-color:#2a2a2a;color:#666}
.btn-hint:hover{border-color:#444;color:#aaa}
.btn-reveal{background:#fff;border-color:#fff;color:#000;font-weight:600}
.btn-got{background:transparent;border-color:#1a3a1a;color:#3fb950}
.btn-got:hover{background:#0d2a0d;border-color:#238636}
.btn-rev{background:transparent;border-color:#3a1a1a;color:#f85149}
.btn-rev:hover{background:#2a0d0d;border-color:#b91c1c}
.btn-nav{background:transparent;border-color:#222;color:#555}
.btn-nav:hover{border-color:#444;color:#aaa}
#bn{margin-left:auto}
.statsbar{display:flex;align-items:center;gap:16px;padding:7px 20px;background:#0d0d0d;border-top:1px solid #1a1a1a;flex-shrink:0;font-size:11px;color:#444}
.s-known{color:#3fb950}.s-rev{color:#f85149}
.kbds{margin-left:auto;display:flex;align-items:center;gap:5px;color:#383838}
kbd{background:#1a1a1a;border:1px solid #2a2a2a;border-radius:3px;padding:1px 5px;font-size:10px;font-family:'Fira Code',monospace;color:#555}
</style>
</head>
<body>
<div class="tabbar" id="tb"></div>
<div class="pwrap"><div id="pf"></div></div>
<div class="stage">
  <div class="card-wrap">
    <div class="card">
      <div class="card-meta">
        <span id="ct" class="card-tag"></span>
        <span id="cn" class="card-num"></span>
      </div>
      <div id="ctitle" class="card-title"></div>
      <div id="cq" class="card-q"></div>
      <div id="loci" class="loci-box" style="display:none"></div>
      <div id="hint" class="hint-box" style="display:none"></div>
      <div id="ans" class="ans-section" style="display:none">
        <div id="at" class="ans-text"></div>
        <div id="cd" class="code-block" style="display:none">
          <div class="code-hdr">
            <span id="clang" class="code-lang"></span>
            <button class="code-copy" onclick="copyCode()">⎘ copy</button>
          </div>
          <div class="code-body">
            <div id="clns" class="line-nums"></div>
            <pre id="cp" class="code-pre"></pre>
          </div>
        </div>
      </div>
      <div class="card-foot">
        <button id="bh" class="btn btn-hint" onclick="showHint()">💡 Hint</button>
        <button id="br" class="btn btn-reveal" onclick="reveal()">Reveal ↓</button>
        <button id="bg" class="btn btn-got" style="display:none" onclick="markGot()">✓ Got it</button>
        <button id="bv" class="btn btn-rev" style="display:none" onclick="markRev()">↩ Review</button>
        <button id="bp" class="btn btn-nav" onclick="prev()">← Prev</button>
        <button id="bn" class="btn btn-nav" onclick="next()">Next →</button>
      </div>
    </div>
  </div>
</div>
<div class="statsbar">
  <span id="ks" class="s-known"></span>
  <span id="rs" class="s-rev"></span>
  <div class="kbds">
    <kbd>space</kbd>reveal &nbsp;
    <kbd>g</kbd>got it &nbsp;
    <kbd>r</kbd>review &nbsp;
    <kbd>h</kbd>hint &nbsp;
    <kbd>←→</kbd>nav &nbsp;
    <kbd>1-7</kbd>topic
  </div>
</div>
<script>
const TOPICS=[];
let ti=0,ci=0,revealed=false;
const gs=id=>localStorage.getItem('c_'+id)||'new';
const ss=(id,s)=>localStorage.setItem('c_'+id,s);
const all=()=>TOPICS.flatMap(t=>t.cards);
const known=()=>all().filter(c=>gs(c.id)==='known').length;
const reviewed=()=>all().filter(c=>gs(c.id)==='review').length;

function renderTabs(){
  document.getElementById('tb').innerHTML=TOPICS.map((t,i)=>{
    const k=t.cards.filter(c=>gs(c.id)==='known').length;
    return`<div class="tab${i===ti?' active':''}" onclick="selT(${i})">${t.icon} ${t.name} <span class="tc">${k}/${t.cards.length}</span></div>`;
  }).join('');
}

function updP(){
  const t=all().length,k=known();
  document.getElementById('pf').style.width=(t?Math.round(k/t*100):0)+'%';
  document.getElementById('ks').textContent=k+' / '+t+' known';
  document.getElementById('rs').textContent=reviewed()+' to review';
  renderTabs();
}

function renderCard(){
  const topic=TOPICS[ti],card=topic.cards[ci];
  revealed=false;
  document.getElementById('ct').textContent=topic.icon+' '+topic.name;
  document.getElementById('cn').textContent=(ci+1)+' / '+topic.cards.length;
  document.getElementById('ctitle').textContent=card.title;
  document.getElementById('cq').textContent=card.q;
  const lb=document.getElementById('loci');
  if(card.loci){lb.textContent='🏛 '+card.loci;lb.style.display='block';}else lb.style.display='none';
  document.getElementById('hint').style.display='none';
  document.getElementById('ans').style.display='none';
  document.getElementById('cd').style.display='none';
  document.getElementById('br').style.display='';
  document.getElementById('bg').style.display='none';
  document.getElementById('bv').style.display='none';
  updP();
  document.querySelector('.stage').scrollTop=0;
}

function reveal(){
  if(revealed)return;revealed=true;
  const card=TOPICS[ti].cards[ci];
  document.getElementById('at').textContent=card.answer;
  document.getElementById('ans').style.display='block';
  if(card.code){
    document.getElementById('clang').textContent=detectLang(card.code);
    const lines=card.code.split('\n');
    document.getElementById('clns').textContent=lines.map((_,i)=>i+1).join('\n');
    document.getElementById('cp').innerHTML=highlight(card.code);
    document.getElementById('cd').style.display='block';
  }
  document.getElementById('br').style.display='none';
  document.getElementById('bg').style.display='';
  document.getElementById('bv').style.display='';
}

function showHint(){
  const h=document.getElementById('hint');
  h.textContent='💡 '+TOPICS[ti].cards[ci].hint;
  h.style.display='block';
}

function selT(i){ti=i;ci=0;renderCard();}
function next(){if(ci<TOPICS[ti].cards.length-1){ci++;renderCard();}}
function prev(){if(ci>0){ci--;renderCard();}}
function markGot(){ss(TOPICS[ti].cards[ci].id,'known');updP();next();}
function markRev(){ss(TOPICS[ti].cards[ci].id,'review');updP();next();}

function copyCode(){
  navigator.clipboard.writeText(TOPICS[ti].cards[ci].code).then(()=>{
    const b=document.querySelector('.code-copy');
    b.textContent='✓ copied';
    setTimeout(()=>b.textContent='⎘ copy',1500);
  });
}

function detectLang(code){
  if(/#include|std::|cout|nullptr|vector</.test(code))return'C++';
  if(/public\s+class|import\s+java|ArrayList|HashMap/.test(code))return'Java';
  if(/\bdef\s+\w+\s*\(/.test(code))return'Python';
  if(/\bfunc\s+\w+/.test(code))return'Go';
  if(/const\s+\w+=|let\s+\w+=|=>/.test(code))return'JS';
  return'Code';
}

function highlight(code){
  const saved=[];
  const save=html=>{saved.push(html);return'\x00'+(saved.length-1)+'\x00';};
  let s=code.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
  // save comments and strings as opaque tokens so keywords don't color inside them
  s=s.replace(/(\/\/[^\n]*)/g,m=>save('<span class="cm">'+m+'</span>'));
  s=s.replace(/(\/\*[\s\S]*?\*\/)/g,m=>save('<span class="cm">'+m+'</span>'));
  s=s.replace(/("(?:[^"\\]|\\.)*")/g,m=>save('<span class="st">'+m+'</span>'));
  s=s.replace(/('(?:[^'\\]|\\.)*')/g,m=>save('<span class="st">'+m+'</span>'));
  // keywords
  const kws='if|else|return|while|for|break|continue|switch|case|default|new|this|super|null|nullptr|true|false|void|int|long|double|float|bool|char|var|let|const|auto|static|final|abstract|class|interface|enum|struct|public|private|protected|extends|implements|override|virtual|typename|import|package|def';
  s=s.replace(new RegExp('\\b('+kws+')\\b','g'),'<span class="kw">$1</span>');
  // function calls (lowercase identifier before '(')
  s=s.replace(/\b([a-z_][a-zA-Z0-9_]*)(?=\s*\()/g,'<span class="fn">$1</span>');
  // types (PascalCase)
  s=s.replace(/\b([A-Z][a-zA-Z0-9_]*)\b/g,'<span class="ty">$1</span>');
  // numbers
  s=s.replace(/\b(\d+(?:\.\d+)?)\b/g,'<span class="nu">$1</span>');
  // restore saved tokens
  s=s.replace(/\x00(\d+)\x00/g,(_,i)=>saved[+i]);
  return s;
}

document.addEventListener('keydown',e=>{
  if(['INPUT','TEXTAREA'].includes(document.activeElement.tagName))return;
  if(e.key===' '||e.key==='Enter'){e.preventDefault();reveal();}
  else if(e.key==='ArrowRight')next();
  else if(e.key==='ArrowLeft')prev();
  else if(e.key==='g')markGot();
  else if(e.key==='r')markRev();
  else if(e.key==='h')showHint();
  else if(e.key>='1'&&e.key<='9'){const i=parseInt(e.key)-1;if(i<TOPICS.length)selT(i);}
});

function addTopic(t){TOPICS.push(t);if(TOPICS.length===1){renderTabs();renderCard();}else renderTabs();}
</script>
```

  **How to apply this edit:**
  The `old_string` for the Edit tool is the entire file content from `<!DOCTYPE html>` up to and including the line `function addTopic(t){TOPICS.push(t);if(TOPICS.length===1){renderSB();renderCard();}else renderSB();}` and the closing `</script>` tag — everything before `<!-- CARD DATA injected below -->`.

  The `new_string` is the complete block shown above.

- [ ] **Step 3: Verify the file still has card data**

  After the edit, confirm `<!-- CARD DATA injected below -->` still exists in the file and all `addTopic({...})` blocks are intact. Run:

  ```
  findstr /n "CARD DATA" "C:\Users\mayan\OneDrive\Documents\Interview\uber\study.html"
  ```

  Expected: one match showing the marker line still present.

- [ ] **Step 4: Open in browser and verify layout**

  Open `C:\Users\mayan\OneDrive\Documents\Interview\uber\study.html` in a browser.

  Expected:
  - Near-black background (`#0a0a0a`)
  - Top tab bar showing all 7 topics (📝 Strings, ☕ Java Algos, 🎬 DP, 🌳 Trees, 🎨 Design Patterns, 🏗️ System Design, 🔧 LLD)
  - First topic active (white text, white underline)
  - Thin white progress bar below tabs
  - Centered card (max 700px wide, rounded corners)
  - Card shows title + question of first String Algorithms card
  - Bottom stats bar with known count + keyboard shortcuts

- [ ] **Step 5: Verify tab switching**

  Click each tab. Expected: card updates to first card of that topic. Active tab highlights in white.

  Click `2` on keyboard. Expected: jumps to Java Algorithms topic.

- [ ] **Step 6: Verify card flow**

  On any card: press `Space`. Expected: answer section slides in, Reveal button hides, Got it + Review buttons appear.

  Press `g`. Expected: card advances to next, progress bar updates.

  Press `h` on a new card. Expected: amber hint box appears above answer area.

- [ ] **Step 7: Verify code block**

  Navigate to a card that has code (e.g. Strings topic → card 1 "Reverse a String"). Reveal it.

  Expected:
  - Code block visible below answer text
  - Language badge shows `C++` or `Java`
  - Line numbers on left, separated by a dark border
  - Keywords in purple (`#c586c0`), types in teal (`#4ec9b0`), function names in yellow (`#dcdcaa`)
  - Click `⎘ copy` — button changes to `✓ copied` for 1.5s

- [ ] **Step 8: Commit**

  ```bash
  git -C "C:\Users\mayan\OneDrive\Documents\Interview\uber" add study.html
  git -C "C:\Users\mayan\OneDrive\Documents\Interview\uber" commit -m "feat: revamp study UI — Focus layout, VS Code code blocks, tab nav"
  ```

  Expected: 1 file changed.

---

## Self-Review

**Spec coverage:**
- Top tab bar ✓ (renderTabs, selT, `.tab.active`)
- Thin white progress bar ✓ (`#pf` width transition)
- Centered card, max 700px, rounded ✓ (`.card-wrap`, `.card`)
- Card meta, title, question, loci, hint, answer ✓ (all DOM ids present)
- Footer buttons: hint, reveal, got it, review, prev, next ✓
- VS Code Dark+ code block: line nums, lang badge, copy button ✓
- Token colors: kw/ty/fn/st/cm/nu ✓
- Placeholder-token highlighter (safe comments/strings) ✓
- detectLang: C++, Java, Python, Go, JS ✓
- Bottom stats bar: known count, review count, kbd hints ✓
- Keyboard shortcuts: space, g, r, h, ←→, 1-9 ✓
- localStorage schema unchanged ✓
- addTopic() and TOPICS unchanged ✓

**Placeholder scan:** No TBDs. All code complete.

**Consistency:** `renderTabs` uses `.tab.active` class — matches CSS. `updP` writes to `#ks` and `#rs` — match DOM ids. `reveal` writes to `#at`, `#cd`, `#clang`, `#clns`, `#cp` — all present in HTML. `highlight` saves tokens with `\x00N\x00` and restores — self-consistent.
