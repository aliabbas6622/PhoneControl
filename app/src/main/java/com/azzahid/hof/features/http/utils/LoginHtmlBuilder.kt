package com.azzahid.hof.features.http.utils

internal object LoginHtmlBuilder {

    fun build(nextPath: String, showError: Boolean): String {
        val errorBlock = if (showError) {
            """<div class="error" role="alert">Wrong password. Check Settings &rarr; Remote Access in the app and try again.</div>"""
        } else {
            ""
        }
        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<meta name="theme-color" content="#0a0b0d">
<title>HTTP on Fire — Sign in</title>
<style>
:root{--bg:#0a0b0d;--surface:#14171c;--surface-2:#1b1f26;--border:#272d37;--border-strong:#39414d;--text:#eef1f6;--muted:#9aa3b2;--accent:#00d4aa;--accent-bright:#5cffd9;--accent-soft:rgba(0,212,170,.12);--accent-line:rgba(0,212,170,.35);--red:#ff6b6b;--red-soft:rgba(255,107,107,.12)}
*{box-sizing:border-box}
html,body{height:100%}
body{margin:0;min-height:100vh;display:flex;align-items:center;justify-content:center;background:radial-gradient(900px 500px at 50% -10%,rgba(0,212,170,.14),transparent 60%),var(--bg);color:var(--text);font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif;font-size:15px;line-height:1.5;padding:24px;-webkit-font-smoothing:antialiased}
:focus-visible{outline:2px solid var(--accent);outline-offset:2px;border-radius:6px}
.card{width:100%;max-width:400px}
.brand{display:flex;align-items:center;gap:10px;margin-bottom:20px}
.brand svg{width:30px;height:30px;flex-shrink:0}
.brand strong{font-size:16px;font-weight:650;letter-spacing:-.01em}
.panel{background:linear-gradient(180deg,rgba(255,255,255,.025),transparent 40%),var(--surface);border:1px solid var(--border);border-radius:18px;padding:28px;box-shadow:0 18px 50px rgba(0,0,0,.35)}
.badge{display:inline-flex;align-items:center;gap:6px;font-size:11px;font-weight:600;letter-spacing:.04em;text-transform:uppercase;color:var(--accent);background:var(--accent-soft);border:1px solid var(--accent-line);padding:4px 9px;border-radius:999px;margin-bottom:14px}
.badge::before{content:'';width:6px;height:6px;border-radius:50%;background:var(--accent);box-shadow:0 0 8px var(--accent)}
h1{font-size:24px;line-height:1.25;font-weight:650;letter-spacing:-.02em;margin:0 0 8px}
p.lead{color:var(--muted);font-size:13.5px;margin:0 0 22px}
label{display:block;font-size:12px;font-weight:600;letter-spacing:.04em;text-transform:uppercase;color:var(--muted);margin-bottom:8px}
.field{display:flex;align-items:stretch;background:var(--surface-2);border:1px solid var(--border);border-radius:11px;overflow:hidden;transition:border-color .15s}
.field:focus-within{border-color:var(--accent)}
.field input{flex:1;min-width:0;border:none;background:transparent;color:var(--text);font-size:15px;font-family:inherit;padding:13px 14px;outline:none}
.pw-toggle{border:none;border-left:1px solid var(--border);background:transparent;color:var(--muted);font-size:12.5px;font-weight:600;font-family:inherit;padding:0 14px;cursor:pointer;min-width:64px}
.pw-toggle:hover{color:var(--text);background:var(--surface-3,var(--surface-2))}
button[type=submit]{width:100%;min-height:48px;margin-top:16px;border:none;border-radius:11px;background:var(--accent);color:#04110d;font-size:14.5px;font-weight:650;font-family:inherit;cursor:pointer;transition:background .15s,transform .1s}
button[type=submit]:hover{background:var(--accent-bright)}
button[type=submit]:active{transform:translateY(1px)}
.error{margin-top:16px;font-size:13px;color:var(--red);background:var(--red-soft);border:1px solid rgba(255,107,107,.4);border-radius:11px;padding:11px 13px}
.hint{margin:18px 0 0;font-size:12px;color:var(--muted)}
.hint a{color:var(--accent);text-decoration:none}
.hint a:hover{text-decoration:underline}
.foot{display:flex;justify-content:center;gap:8px;margin-top:18px;font-size:11.5px;color:var(--faint,#6f7888)}
.foot span{opacity:.6}
@media (max-width:420px){.panel{padding:22px}body{padding:16px}}
@media (prefers-reduced-motion:reduce){*{transition-duration:.001ms!important;animation-duration:.001ms!important}}
</style>
</head>
<body>
<main class="card">
<div class="brand">
<svg viewBox="0 0 32 32" aria-hidden="true"><defs><linearGradient id="flame" x1="0" y1="1" x2="0" y2="0"><stop offset="0" stop-color="#ff7a18"/><stop offset=".55" stop-color="#ffb020"/><stop offset="1" stop-color="#ffe066"/></linearGradient></defs><path d="M16 2c4 6 9 9 9 16a9 9 0 0 1-18 0c0-3 1.5-5.5 3.5-7.5C11 14 12 15.5 13.5 16c-.5-4 0-9 2.5-14z" fill="url(#flame)"/><path d="M16 17c2 2.2 4 4 4 6.6A4 4 0 0 1 12 23c0-1.8 1-3.2 2.2-4.4.4 1 .9 1.6 1.3 1.9.2-1.2.3-2.4.5-3.5z" fill="#5cffd9" opacity=".9"/></svg>
<strong>HTTP on Fire</strong>
</div>
<form class="panel" method="post" action="/login">
<span class="badge">Protected server</span>
<h1>Sign in to your phone</h1>
<p class="lead">Enter the access password shown in the HTTP on Fire app under Settings &rarr; Remote Access.</p>
<label for="token">Access password</label>
<div class="field">
<input id="token" name="token" type="password" autocomplete="current-password" autofocus required spellcheck="false" autocapitalize="off">
<button class="pw-toggle" id="pwBtn" type="button" aria-pressed="false" aria-label="Show password" onclick="togglePw()">Show</button>
</div>
<input type="hidden" name="next" value="${escapeAttribute(nextPath)}">
<button type="submit">Unlock</button>
$errorBlock
<p class="hint">The browser stays signed in for 30 days. <a href="/logout">Sign out</a> at any time.</p>
</form>
<div class="foot"><span>Local server</span><span>•</span><span>Encrypted when served over HTTPS</span></div>
</main>
<script>
function togglePw(){
var input=document.getElementById('token');
var btn=document.getElementById('pwBtn');
var show=input.type==='password';
input.type=show?'text':'password';
btn.textContent=show?'Hide':'Show';
btn.setAttribute('aria-pressed',show?'true':'false');
btn.setAttribute('aria-label',show?'Hide password':'Show password');
input.focus()}
</script>
</body>
</html>
""".trimIndent()
    }

    private fun escapeAttribute(value: String): String = value
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}
