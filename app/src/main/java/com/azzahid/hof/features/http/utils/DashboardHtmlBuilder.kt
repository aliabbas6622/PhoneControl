package com.azzahid.hof.features.http.utils

object DashboardHtmlBuilder {

    fun build(baseUrl: String): String {
        return buildString {
            append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">")
            append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
            append("<title>HTTP on Fire</title>")
            appendStyles()
            append("</head><body>")
            appendLayout()
            appendScripts(baseUrl)
            append("</body></html>")
        }
    }

    private fun StringBuilder.appendStyles() {
        append("""<style>
:root{--bg:#0a0b0d;--surface:#14171c;--surface-2:#1b1f26;--surface-3:#222732;--border:#272d37;--border-strong:#39414d;--text:#eef1f6;--muted:#9aa3b2;--faint:#6f7888;--accent:#00d4aa;--accent-bright:#5cffd9;--accent-soft:rgba(0,212,170,.12);--accent-line:rgba(0,212,170,.35);--violet:#b39dff;--violet-soft:rgba(179,157,255,.12);--amber:#ffb020;--amber-soft:rgba(255,176,32,.12);--red:#ff6b6b;--red-soft:rgba(255,107,107,.12);--radius:16px;--radius-sm:10px;--shadow:0 1px 0 rgba(255,255,255,.02) inset,0 10px 30px rgba(0,0,0,.22)}
*{margin:0;padding:0;box-sizing:border-box}
html{scroll-behavior:smooth}
body{background:radial-gradient(1100px 520px at 50% -10%,rgba(0,212,170,.10),transparent 60%),var(--bg);color:var(--text);font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;font-size:15px;line-height:1.5;min-height:100vh;-webkit-font-smoothing:antialiased}
a{color:var(--accent)}
:focus-visible{outline:2px solid var(--accent);outline-offset:2px;border-radius:6px}
.skip{position:absolute;left:-9999px;top:8px;background:var(--surface-2);color:var(--text);padding:10px 14px;border-radius:8px;z-index:100}
.skip:focus{left:12px}
header{position:sticky;top:0;z-index:50;display:flex;align-items:center;gap:10px;flex-wrap:wrap;padding:12px 24px;border-bottom:1px solid var(--border);background:rgba(10,11,13,.82);backdrop-filter:blur(14px)}
.brand{display:flex;align-items:center;gap:10px;margin-right:auto;min-width:0}
.brand svg{width:26px;height:26px;flex-shrink:0}
.brand h1{font-size:16px;font-weight:650;letter-spacing:-.01em;color:var(--text);white-space:nowrap}
.device-name{font-size:12.5px;color:var(--muted);background:var(--surface);border:1px solid var(--border);padding:4px 10px;border-radius:999px;max-width:220px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.live{display:inline-flex;align-items:center;gap:6px;font-size:12px;color:var(--accent);background:var(--accent-soft);border:1px solid var(--accent-line);padding:4px 10px;border-radius:999px;white-space:nowrap}
.live-dot{width:7px;height:7px;border-radius:50%;background:var(--accent);box-shadow:0 0 8px var(--accent);animation:pulse 2s ease-in-out infinite}
.hdr-actions{display:flex;align-items:center;gap:8px}
.hdr-btn{display:inline-flex;align-items:center;min-height:36px;padding:0 12px;border-radius:9px;border:1px solid var(--border);background:var(--surface);color:var(--muted);font-size:12.5px;font-weight:550;text-decoration:none;transition:background .15s,color .15s,border-color .15s}
.hdr-btn:hover{color:var(--text);border-color:var(--border-strong);background:var(--surface-2)}
.section-nav{max-width:1200px;margin:0 auto;padding:16px 24px 0;display:flex;gap:8px;overflow-x:auto;scrollbar-width:none}
.section-nav::-webkit-scrollbar{display:none}
.section-nav a{flex:0 0 auto;font-size:12.5px;font-weight:550;color:var(--muted);text-decoration:none;padding:8px 13px;border-radius:999px;border:1px solid var(--border);background:var(--surface);transition:background .15s,color .15s,border-color .15s}
.section-nav a:hover{color:var(--text);border-color:var(--border-strong);background:var(--surface-2)}
main{max-width:1200px;margin:0 auto;padding:20px 24px 80px}
.section{margin-bottom:36px;scroll-margin-top:84px}
.section-title{display:flex;align-items:center;gap:10px;font-size:11.5px;font-weight:650;color:var(--muted);text-transform:uppercase;letter-spacing:.14em;margin-bottom:14px}
.section-title::after{content:'';height:1px;flex:1;background:linear-gradient(90deg,var(--border),transparent)}
.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:14px}
.actions-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(300px,1fr));gap:14px}
.card{background:linear-gradient(180deg,rgba(255,255,255,.025),transparent 40%),var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:18px;box-shadow:var(--shadow)}
.card h3,.card-head{display:flex;align-items:center;gap:8px;font-size:13.5px;font-weight:650;color:var(--text);letter-spacing:.01em}
.card h3{margin-bottom:14px}
.card h3::before,.card-head::before{content:'';width:6px;height:6px;border-radius:50%;background:var(--accent);box-shadow:0 0 8px var(--accent-line);flex-shrink:0}
.card-head{width:100%;background:none;border:none;text-align:left;font-family:inherit;cursor:pointer;min-height:32px;padding:0}
.card-head .chev{margin-left:auto;font-size:11px;color:var(--muted);transition:transform .2s}
.card-head .perm-badge ~ .chev{margin-left:8px}
.collapsible.open .card-head .chev{transform:rotate(90deg)}
.gauge-wrap{display:flex;align-items:center;gap:20px}
.gauge{position:relative;width:104px;height:104px;flex-shrink:0}
.gauge svg{transform:rotate(-90deg);display:block}
.gauge-text{position:absolute;inset:0;display:flex;align-items:center;justify-content:center;font-size:23px;font-weight:650;color:var(--text)}
.gauge-text span{font-size:12px;color:var(--muted);font-weight:500;margin-left:1px}
.info-rows{display:flex;flex-direction:column;gap:7px;flex:1;min-width:0}
.info-row{display:flex;justify-content:space-between;gap:12px;font-size:13.5px}
.info-row .label{color:var(--muted);white-space:nowrap}
.info-row .value{color:var(--text);font-weight:550;text-align:right;overflow-wrap:anywhere}
.bar-wrap{margin-top:10px}
.bar-label{display:flex;justify-content:space-between;gap:8px;font-size:12px;margin-bottom:5px}
.bar-label .bl{color:var(--muted)}
.bar-label .bv{color:var(--text)}
.bar{height:8px;background:var(--surface-3);border-radius:999px;overflow:hidden}
.bar-fill{height:100%;border-radius:999px;transition:width .5s ease}
.toggle{width:50px;height:30px;border-radius:999px;background:var(--surface-3);border:1px solid var(--border-strong);cursor:pointer;position:relative;transition:background .2s,border-color .2s;flex-shrink:0;padding:0;appearance:none}
.toggle.on,.toggle[aria-pressed="true"]{background:var(--accent-soft);border-color:var(--accent)}
.toggle::after{content:'';position:absolute;top:3px;left:3px;width:22px;height:22px;border-radius:50%;background:var(--muted);transition:transform .2s,background .2s}
.toggle.on::after,.toggle[aria-pressed="true"]::after{transform:translateX(20px);background:var(--accent)}
.toggle-row{display:flex;align-items:center;justify-content:space-between;gap:12px}
.slider-group{display:flex;flex-direction:column;gap:14px}
.slider-row{display:flex;align-items:center;gap:12px}
.slider-row .sl{font-size:12px;color:var(--muted);width:80px;flex-shrink:0}
.slider-row input[type=range]{flex:1;-webkit-appearance:none;appearance:none;height:6px;background:var(--surface-3);border-radius:999px;outline:none;cursor:pointer}
.slider-row input[type=range]::-webkit-slider-thumb{-webkit-appearance:none;width:20px;height:20px;border-radius:50%;background:var(--accent);border:2px solid var(--bg);cursor:pointer}
.slider-row .sv{font-size:12px;color:var(--text);width:28px;text-align:right;flex-shrink:0}
.btn{display:inline-flex;align-items:center;justify-content:center;gap:8px;min-height:44px;padding:0 16px;border-radius:10px;background:var(--accent-soft);border:1px solid var(--accent-line);color:var(--accent-bright);font-size:13.5px;font-weight:600;font-family:inherit;cursor:pointer;text-decoration:none;transition:background .15s,color .15s,border-color .15s}
.btn:hover{background:var(--accent);color:var(--bg);border-color:var(--accent)}
.btn:disabled{opacity:.45;cursor:not-allowed}
.btn-violet{background:var(--violet-soft);border-color:rgba(179,157,255,.4);color:var(--violet)}
.btn-violet:hover{background:var(--violet);color:var(--bg)}
.btn-amber{background:var(--amber-soft);border-color:rgba(255,176,32,.4);color:var(--amber)}
.btn-amber:hover{background:var(--amber);color:var(--bg)}
.btn-red{background:var(--red-soft);border-color:rgba(255,107,107,.4);color:var(--red)}
.btn-red:hover{background:var(--red);color:var(--bg)}
.btn-xs{min-height:34px;padding:0 10px;font-size:12px;border-radius:8px}
.inline-input{display:flex;gap:8px;align-items:center;margin-top:10px;flex-wrap:wrap}
.inline-input input,.inline-input textarea,.inline-input select{background:var(--surface-2);border:1px solid var(--border);border-radius:9px;padding:10px 12px;color:var(--text);font-size:13.5px;font-family:inherit;min-height:44px;min-width:0;outline:none;transition:border-color .15s}
.inline-input input:focus,.inline-input textarea:focus,.inline-input select:focus{border-color:var(--accent)}
.inline-input input[type=number]{width:88px;flex:0 0 auto}
.text-area-wrap textarea{width:100%;min-height:76px;resize:vertical;background:var(--surface-2);border:1px solid var(--border);border-radius:9px;padding:10px 12px;color:var(--text);font-size:13.5px;font-family:inherit;outline:none;margin-bottom:10px;transition:border-color .15s}
.text-area-wrap textarea:focus{border-color:var(--accent)}
.clip-current{font-size:13px;color:var(--muted);background:var(--surface-2);border-radius:9px;padding:10px 12px;margin-bottom:10px;word-break:break-all;max-height:76px;overflow-y:auto;border:1px solid var(--border)}
.qr-preview{text-align:center;margin-top:12px}
.qr-preview img{border-radius:10px;background:#fff;padding:8px;max-width:100%;height:auto}
.cam-preview{margin-top:12px;text-align:center}
.cam-preview img{max-width:100%;border-radius:10px}
.cam-select{display:flex;gap:8px;flex-wrap:wrap;align-items:center}
.cam-select select{background:var(--surface-2);border:1px solid var(--border);border-radius:9px;padding:10px 12px;color:var(--text);font-size:13.5px;font-family:inherit;min-height:44px;outline:none}
.collapsible .col-body{display:none;margin-top:14px}
.collapsible.open .col-body{display:block}
.search-input{width:100%;background:var(--surface-2);border:1px solid var(--border);border-radius:9px;padding:10px 12px;color:var(--text);font-size:13.5px;font-family:inherit;min-height:44px;outline:none;margin-bottom:10px;transition:border-color .15s}
.search-input:focus{border-color:var(--accent)}
.data-table{width:100%;border-collapse:collapse;font-size:13px}
.data-table th{text-align:left;color:var(--muted);font-weight:600;padding:8px;border-bottom:1px solid var(--border)}
.data-table td{padding:8px;border-bottom:1px solid var(--border);color:var(--text)}
.data-list{max-height:320px;overflow-y:auto;overflow-x:hidden}
.data-list .item{padding:10px 4px;border-bottom:1px solid var(--border);font-size:13px;overflow:hidden}
.data-list .item:last-child{border-bottom:none}
.data-list .item>div{min-width:0}
.data-list .item .name{color:var(--text);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.data-list .item .pkg{color:var(--muted);font-size:11.5px;margin-top:2px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.data-list .item .btn{flex-shrink:0}
.upload-zone{border:1.5px dashed var(--border-strong);border-radius:14px;padding:26px 18px;text-align:center;cursor:pointer;background:var(--surface-2);transition:border-color .15s,background .15s}
.upload-zone:hover,.upload-zone.drag-over{border-color:var(--accent);background:var(--accent-soft)}
.upload-zone .label{font-size:13.5px;font-weight:600;color:var(--text)}
.upload-zone .hint{font-size:12px;color:var(--muted);margin-top:4px}
.upload-progress{display:none;margin-top:14px}
.progress-bar{height:6px;background:var(--surface-3);border-radius:999px;overflow:hidden}
.progress-fill{height:100%;width:0;border-radius:999px;background:linear-gradient(90deg,var(--accent),var(--accent-bright));transition:width .2s}
.progress-text{font-size:12px;color:var(--muted);margin-top:6px}
input[type=file]{color:var(--muted);font-size:12px;max-width:100%}
input[type=file]::file-selector-button,input[type=file]::-webkit-file-upload-button{background:var(--surface-2);border:1px solid var(--border);border-radius:8px;padding:8px 12px;color:var(--text);margin-right:8px;cursor:pointer;font-family:inherit;font-size:12px;min-height:38px}
.perm-badge{font-size:10.5px;font-weight:600;background:var(--amber-soft);color:var(--amber);padding:2px 8px;border-radius:999px;border:1px solid rgba(255,176,32,.3);margin-left:auto}
.loc-info{display:grid;grid-template-columns:1fr 1fr;gap:10px}
.loc-info .li{font-size:13.5px;color:var(--text)}
.loc-info .li .ll{color:var(--muted);font-size:11.5px;margin-bottom:2px}
.toast-container{position:fixed;bottom:20px;right:20px;display:flex;flex-direction:column;gap:8px;z-index:1000;pointer-events:none}
.toast{padding:12px 16px;border-radius:10px;font-size:13px;pointer-events:auto;animation:slideIn .3s ease;max-width:360px;box-shadow:var(--shadow)}
.toast-ok{background:var(--accent-soft);border:1px solid var(--accent-line);color:var(--accent-bright)}
.toast-err{background:var(--red-soft);border:1px solid rgba(255,107,107,.4);color:var(--red)}
@keyframes slideIn{from{opacity:0;transform:translateY(10px)}to{opacity:1;transform:translateY(0)}}
.loading-dot{display:inline-block;width:8px;height:8px;border-radius:50%;background:var(--muted);animation:pulse 1s infinite}
@keyframes pulse{0%,100%{opacity:.3}50%{opacity:1}}
@media(max-width:700px){header{padding:10px 14px}.brand h1{font-size:15px}.device-name{display:none}.section-nav{padding:12px 14px 0}main{padding:16px 14px 64px}.grid,.actions-grid{grid-template-columns:1fr}.loc-info{grid-template-columns:1fr}.toast-container{left:14px;right:14px;bottom:14px}.toast{max-width:none}}
@media(prefers-reduced-motion:reduce){html{scroll-behavior:auto}*{animation-duration:.001ms!important;animation-iteration-count:1!important;transition-duration:.001ms!important}}
</style>""")
    }

    private fun StringBuilder.appendLayout() {
        append("""
<a class="skip" href="#status">Skip to status</a>
<header>
<div class="brand">
<svg viewBox="0 0 32 32" aria-hidden="true"><defs><linearGradient id="flame" x1="0" y1="1" x2="0" y2="0"><stop offset="0" stop-color="#ff7a18"/><stop offset=".55" stop-color="#ffb020"/><stop offset="1" stop-color="#ffe066"/></linearGradient></defs><path d="M16 2c4 6 9 9 9 16a9 9 0 0 1-18 0c0-3 1.5-5.5 3.5-7.5C11 14 12 15.5 13.5 16c-.5-4 0-9 2.5-14z" fill="url(#flame)"/><path d="M16 17c2 2.2 4 4 4 6.6A4 4 0 0 1 12 23c0-1.8 1-3.2 2.2-4.4.4 1 .9 1.6 1.3 1.9.2-1.2.3-2.4.5-3.5z" fill="#5cffd9" opacity=".9"/></svg>
<h1>HTTP on Fire</h1>
</div>
<span class="device-name" id="deviceLabel"></span>
<span class="live" role="status"><span class="live-dot"></span>Live</span>
<div class="hdr-actions">
<a class="hdr-btn" href="/account">Change password</a>
<a class="hdr-btn" href="/logout">Sign out</a>
</div>
</header>
<nav class="section-nav" aria-label="Page sections">
<a href="#status">Status</a><a href="#files">Files</a><a href="#actions">Actions</a><a href="#tools">Tools</a><a href="#data">Data</a>
</nav>
<main>
<section class="section" id="status">
<div class="section-title">Status</div>
<div class="grid">
<div class="card" id="batteryCard"><h3>Battery</h3>
<div class="gauge-wrap">
<div class="gauge"><svg viewBox="0 0 100 100" width="104" height="104"><circle cx="50" cy="50" r="44" fill="none" stroke="#222732" stroke-width="8"/><circle id="battGauge" cx="50" cy="50" r="44" fill="none" stroke="#00D4AA" stroke-width="8" stroke-linecap="round" stroke-dasharray="276.46" stroke-dashoffset="276.46"/></svg><div class="gauge-text" id="battPct">--</div></div>
<div class="info-rows" id="battInfo"></div>
</div></div>
<div class="card" id="wifiCard"><h3>Wi-Fi</h3><div class="info-rows" id="wifiInfo"><div class="loading-dot"></div></div></div>
<div class="card" id="deviceCard"><h3>Device</h3><div id="deviceInfo"><div class="loading-dot"></div></div></div>
</div></section>

<section class="section" id="files">
<div class="section-title">File Drop</div>
<div class="card"><h3>Shared Inbox<a class="btn btn-xs" style="margin-left:auto;text-decoration:none" href="/files/" target="_blank" rel="noopener">Open files</a></h3>
<div class="upload-zone" id="dropZone" role="button" tabindex="0" aria-label="Send files to your phone" onclick="document.getElementById('dropInput').click()" onkeydown="if(event.key==='Enter'||event.key===' '){event.preventDefault();document.getElementById('dropInput').click()}">
<div class="label">Drop files here, or click to browse</div>
<div class="hint">Files are saved to the File Drop folder on your phone</div>
<input type="file" id="dropInput" multiple style="display:none">
</div>
<div class="upload-progress" id="dropProgress">
<div class="progress-bar"><div class="progress-fill" id="dropFill"></div></div>
<div class="progress-text" id="dropText"></div>
</div>
<div class="data-list" id="dropList"><div class="loading-dot"></div></div>
</div></section>

<section class="section" id="actions">
<div class="section-title">Quick Actions</div><div class="actions-grid">
<div class="card"><h3>Flashlight</h3>
<div class="toggle-row"><span id="flashLabel" style="font-size:13px;color:var(--muted)">OFF</span><button class="toggle" id="flashToggle" type="button" aria-pressed="false" aria-label="Flashlight" onclick="toggleFlash()"></button></div></div>

<div class="card"><h3>Volume</h3>
<div class="slider-group" id="volumeSliders"><div class="loading-dot"></div></div></div>

<div class="card"><h3>Vibrate</h3>
<div class="inline-input"><input type="number" id="vibDur" value="500" min="1" max="10000" placeholder="ms" aria-label="Vibration duration in milliseconds"><button class="btn" onclick="doVibrate()">Vibrate</button></div></div>

<div class="card"><h3>Find My Phone</h3>
<div class="inline-input"><select id="ringDur" aria-label="Ring duration"><option value="3000">3 sec</option><option value="5000" selected>5 sec</option><option value="10000">10 sec</option><option value="15000">15 sec</option></select><button class="btn btn-amber" onclick="doRing()">Ring</button></div></div>

<div class="card"><h3>Text to Speech</h3>
<div class="text-area-wrap"><textarea id="ttsText" placeholder="Type something to speak..." aria-label="Text to speak"></textarea><button class="btn btn-violet" onclick="doSpeak()">Speak</button></div></div>

<div class="card"><h3>Clipboard</h3>
<div class="clip-current" id="clipText">Loading...</div>
<div class="inline-input"><input type="text" id="clipInput" placeholder="New clipboard text" aria-label="New clipboard text" style="flex:1"><button class="btn" onclick="setClipboard()">Set</button></div></div>

<div class="card"><h3>Microphone<span class="perm-badge">Permission Required</span></h3>
<div class="inline-input"><button class="btn btn-red" id="micBtn" onclick="toggleMic()">Listen</button></div>
<audio id="micAudio" style="width:100%;margin-top:12px;display:none" controls></audio></div>

<div class="card"><h3>Notification</h3>
<div class="inline-input" style="margin-top:0"><input type="text" id="notifTitle" placeholder="Title" aria-label="Notification title" style="flex:1"></div>
<div class="text-area-wrap" style="margin-top:8px"><textarea id="notifBody" placeholder="Message body..." aria-label="Notification message"></textarea></div>
<div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
<select id="notifPriority" aria-label="Notification priority" style="background:var(--surface-2);border:1px solid var(--border);border-radius:9px;padding:10px 12px;color:var(--text);font-size:13.5px;min-height:44px"><option value="DEFAULT">Default</option><option value="LOW">Low</option><option value="HIGH">High</option><option value="MAX">Max</option></select>
<button class="btn btn-amber" onclick="doNotify()">Send</button></div></div>
</div></section>

<section class="section" id="tools">
<div class="section-title">Tools</div>
<div class="grid">
<div class="card"><h3>QR Code</h3>
<div class="inline-input"><input type="text" id="qrInput" placeholder="Text or URL" aria-label="QR code content" style="flex:1"></div>
<div class="qr-preview" id="qrPreview"></div></div>

<div class="card"><h3>Camera</h3>
<div class="cam-select"><select id="camFacing" aria-label="Camera facing"><option value="back">Back Camera</option><option value="front">Front Camera</option></select><button class="btn btn-violet" onclick="doCapture()">Capture</button></div>
<div class="cam-preview" id="camPreview"></div></div>

<div class="card"><h3>Brightness</h3>
<div class="slider-row"><span class="sl">Screen</span><input type="range" id="brightSlider" min="1" max="255" value="128" aria-label="Screen brightness" oninput="updateBrightness(this)"><span class="sv" id="brightVal">—</span></div>
<div class="info-row" style="margin-top:10px"><span class="label">Mode</span><span class="value" id="brightMode">Loading…</span></div></div>

<div class="card"><h3>Toast</h3>
<div class="inline-input" style="margin-top:0"><input type="text" id="toastText" placeholder="Message to show on device" aria-label="Toast message" style="flex:1"><button class="btn" onclick="doToast()">Show</button></div></div>

<div class="card"><h3>Wallpaper</h3>
<div class="inline-input" style="margin-top:0"><input type="file" id="wallpaperInput" accept="image/*" aria-label="Wallpaper image" style="flex:1;color:var(--text);font-size:12px"></div>
<div style="display:flex;gap:8px;align-items:center;margin-top:10px;flex-wrap:wrap">
<select id="wallpaperTarget" aria-label="Wallpaper target" style="background:var(--surface-2);border:1px solid var(--border);border-radius:9px;padding:10px 12px;color:var(--text);font-size:13.5px;min-height:44px"><option value="both" selected>Home &amp; Lock</option><option value="home">Home</option><option value="lock">Lock</option></select>
<button class="btn" onclick="setWallpaper()">Set</button></div></div>

<div class="card collapsible" id="sensorsSection"><button class="card-head" type="button" aria-expanded="false" aria-controls="sensorsList" onclick="toggleSection('sensorsSection')">Sensors<span class="chev">▸</span></button>
<div class="col-body"><div class="data-list" id="sensorsList"><div class="loading-dot"></div></div></div></div>

<div class="card collapsible" id="systemSection"><button class="card-head" type="button" aria-expanded="false" aria-controls="systemInfo" onclick="toggleSection('systemSection')">System<span class="chev">▸</span></button>
<div class="col-body"><div class="info-rows" id="systemInfo"><div class="loading-dot"></div></div></div></div>
</div></section>

<section class="section" id="data">
<div class="section-title">Data</div>
<div class="grid">
<div class="card collapsible" id="locSection"><button class="card-head" type="button" aria-expanded="false" aria-controls="locInfo" onclick="toggleSection('locSection')">Location<span class="perm-badge">Permission Required</span><span class="chev">▸</span></button>
<div class="col-body"><div class="loc-info" id="locInfo"></div></div></div>

<div class="card collapsible" id="contactsSection"><button class="card-head" type="button" aria-expanded="false" aria-controls="contactsList" onclick="toggleSection('contactsSection')">Contacts<span class="perm-badge">Permission Required</span><span class="chev">▸</span></button>
<div class="col-body"><input class="search-input" placeholder="Search contacts..." aria-label="Search contacts" oninput="filterContacts(this.value)"><div class="data-list" id="contactsList"></div></div></div>

<div class="card collapsible" id="appsSection"><button class="card-head" type="button" aria-expanded="false" aria-controls="appsList" onclick="toggleSection('appsSection')">Installed Apps<span class="chev">▸</span></button>
<div class="col-body"><input class="search-input" placeholder="Search apps..." aria-label="Search apps" oninput="filterApps(this.value)"><div class="data-list" id="appsList"></div></div></div>
</div></section>
</main>
<div class="toast-container" id="toasts" aria-live="polite"></div>
""")
    }

    private fun StringBuilder.appendScripts(baseUrl: String) {
        append("""<script>
var B='""")
        appendJsStringEscaped(baseUrl.trimEnd('/'))
        append("""';
var flashOn=false,allContacts=[],allApps=[];

function toast(msg,ok){
var c=document.getElementById('toasts');
var d=document.createElement('div');
d.className='toast '+(ok?'toast-ok':'toast-err');
d.textContent=msg;
c.appendChild(d);
setTimeout(function(){d.remove()},3000)}

function api(path,opts){
return fetch(B+path,opts||{}).catch(function(){throw new Error('Server unreachable — is HTTP on Fire running?')}).then(function(r){
if(!r.ok)return r.json().then(function(j){throw new Error(j.error||'Request failed')});
var ct=r.headers.get('content-type')||'';
if(ct.indexOf('image/')!==-1)return r.blob().then(function(b){return{_blob:b}});
return r.json()}).then(function(j){
if(j&&j._blob)return j;
if(j.success===false)throw new Error(j.error||'Unknown error');
return j.data!=null?j.data:j})}

function loadBattery(){
api('/api/battery').then(function(d){
var pct=d.level;
var circ=document.getElementById('battGauge');
var offset=276.46-(276.46*Math.max(0,pct)/100);
circ.style.strokeDashoffset=offset;
circ.style.stroke=pct<=20?'var(--red)':pct<=50?'var(--amber)':'var(--accent)';
document.getElementById('battPct').innerHTML=pct+'<span>%</span>';
var info='';
info+='<div class="info-row"><span class="label">Status</span><span class="value">'+(d.isCharging?'Charging ('+d.chargingSource+')':'Discharging')+'</span></div>';
info+='<div class="info-row"><span class="label">Health</span><span class="value">'+d.health+'</span></div>';
info+='<div class="info-row"><span class="label">Temperature</span><span class="value">'+d.temperature+' °C</span></div>';
document.getElementById('battInfo').innerHTML=info;
}).catch(function(e){document.getElementById('battInfo').innerHTML='<span style="color:var(--red);font-size:13px">'+e.message+'</span>'})}

function loadWifi(){
api('/api/wifi').then(function(d){
var info='';
info+='<div class="info-row"><span class="label">SSID</span><span class="value">'+(d.ssid||'Unknown')+'</span></div>';
info+='<div class="info-row"><span class="label">IP Address</span><span class="value">'+(d.ip||'N/A')+'</span></div>';
info+='<div class="info-row"><span class="label">Signal</span><span class="value">'+d.rssi+' dBm</span></div>';
info+='<div class="info-row"><span class="label">Speed</span><span class="value">'+d.linkSpeed+' '+d.linkSpeedUnits+'</span></div>';
if(d.frequency)info+='<div class="info-row"><span class="label">Frequency</span><span class="value">'+d.frequency+' '+d.frequencyUnits+'</span></div>';
document.getElementById('wifiInfo').innerHTML=info;
}).catch(function(e){document.getElementById('wifiInfo').innerHTML='<span style="color:var(--red);font-size:13px">'+e.message+'</span>'})}

function fmtBytes(b){
if(b>=1073741824)return(b/1073741824).toFixed(1)+' GB';
if(b>=1048576)return(b/1048576).toFixed(0)+' MB';
return(b/1024).toFixed(0)+' KB'}

function loadDevice(){
api('/api/device').then(function(d){
document.getElementById('deviceLabel').textContent=d.manufacturer+' '+d.model;
var h='';
h+='<div class="info-rows">';
h+='<div class="info-row"><span class="label">Model</span><span class="value">'+d.manufacturer+' '+d.model+'</span></div>';
h+='<div class="info-row"><span class="label">Android</span><span class="value">'+d.osVersion+' (SDK '+d.sdkVersion+')</span></div>';
h+='</div>';
var memPct=d.totalMemory>0?Math.round((d.totalMemory-d.availableMemory)/d.totalMemory*100):0;
var storagePct=d.totalStorage>0?Math.round((d.totalStorage-d.availableStorage)/d.totalStorage*100):0;
h+='<div class="bar-wrap"><div class="bar-label"><span class="bl">Memory</span><span class="bv">'+fmtBytes(d.totalMemory-d.availableMemory)+' / '+fmtBytes(d.totalMemory)+'</span></div><div class="bar"><div class="bar-fill" style="width:'+memPct+'%;background:var(--accent)"></div></div></div>';
h+='<div class="bar-wrap"><div class="bar-label"><span class="bl">Storage</span><span class="bv">'+fmtBytes(d.totalStorage-d.availableStorage)+' / '+fmtBytes(d.totalStorage)+'</span></div><div class="bar"><div class="bar-fill" style="width:'+storagePct+'%;background:var(--violet)"></div></div></div>';
document.getElementById('deviceInfo').innerHTML=h;
}).catch(function(e){document.getElementById('deviceInfo').innerHTML='<span style="color:var(--red);font-size:13px">'+e.message+'</span>'})}

function loadVolume(){
api('/api/volume').then(function(d){
var streams=['media','ring','alarm','notification'];
var labels=['Media','Ring','Alarm','Notification'];
var h='';
for(var i=0;i<streams.length;i++){
var s=streams[i],cur=d[s].current,mx=d[s].max;
h+='<div class="slider-row"><span class="sl">'+labels[i]+'</span><input type="range" min="0" max="'+mx+'" value="'+cur+'" data-stream="'+s+'" oninput="updateVolSlider(this)"><span class="sv" id="vol_'+s+'">'+cur+'</span></div>'}
document.getElementById('volumeSliders').innerHTML=h;
}).catch(function(e){document.getElementById('volumeSliders').innerHTML='<span style="color:var(--red);font-size:13px">'+e.message+'</span>'})}

var volTimer={};
function updateVolSlider(el){
var s=el.dataset.stream,v=parseInt(el.value);
document.getElementById('vol_'+s).textContent=v;
clearTimeout(volTimer[s]);
volTimer[s]=setTimeout(function(){
api('/api/volume',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({stream:s,volume:v})}).catch(function(e){toast(e.message,false)})},300)}

function loadClipboard(){
api('/api/clipboard').then(function(d){
document.getElementById('clipText').textContent=d.text||'(empty)';
}).catch(function(e){document.getElementById('clipText').textContent='Unable to read'})}

function toggleFlash(){
flashOn=!flashOn;
var tog=document.getElementById('flashToggle');
tog.classList.toggle('on',flashOn);
tog.setAttribute('aria-pressed',flashOn?'true':'false');
document.getElementById('flashLabel').textContent=flashOn?'ON':'OFF';
api('/api/flashlight?enable='+flashOn,{method:'POST'}).then(function(){
toast('Flashlight '+(flashOn?'on':'off'),true)}).catch(function(e){
flashOn=!flashOn;tog.classList.toggle('on',flashOn);
tog.setAttribute('aria-pressed',flashOn?'true':'false');
document.getElementById('flashLabel').textContent=flashOn?'ON':'OFF';
toast(e.message,false)})}

function doVibrate(){
var dur=parseInt(document.getElementById('vibDur').value)||500;
api('/api/vibrate?duration='+dur,{method:'POST'}).then(function(){
toast('Vibrating for '+dur+'ms',true)}).catch(function(e){toast(e.message,false)})}

function doRing(){
var dur=document.getElementById('ringDur').value;
api('/api/ring?duration='+dur,{method:'POST'}).then(function(){
toast('Ringing device',true)}).catch(function(e){toast(e.message,false)})}

function doSpeak(){
var text=document.getElementById('ttsText').value.trim();
if(!text){toast('Enter text to speak',false);return}
api('/api/speak',{method:'POST',body:text}).then(function(){
toast('Speaking text',true)}).catch(function(e){toast(e.message,false)})}

function setClipboard(){
var text=document.getElementById('clipInput').value;
if(!text){toast('Enter text first',false);return}
api('/api/clipboard',{method:'POST',body:text}).then(function(){
document.getElementById('clipText').textContent=text;
document.getElementById('clipInput').value='';
toast('Clipboard updated',true)}).catch(function(e){toast(e.message,false)})}

var qrDebounce;
function setupQr(){
var inp=document.getElementById('qrInput');
inp.value=B;
updateQr();
inp.addEventListener('input',function(){clearTimeout(qrDebounce);qrDebounce=setTimeout(updateQr,400)})}
function updateQr(){
var data=document.getElementById('qrInput').value.trim();
if(!data){document.getElementById('qrPreview').innerHTML='';return}
document.getElementById('qrPreview').innerHTML='<img src="'+B+'/api/qr?data='+encodeURIComponent(data)+'" width="180" height="180" alt="QR Code">'}

function doCapture(){
var facing=document.getElementById('camFacing').value;
var preview=document.getElementById('camPreview');
preview.innerHTML='<div class="loading-dot"></div>';
fetch(B+'/api/camera?facing='+facing,{method:'POST'}).then(function(r){
if(!r.ok)return r.json().then(function(j){throw new Error(j.error||'Capture failed')});
return r.blob()}).then(function(blob){
var url=URL.createObjectURL(blob);
preview.innerHTML='<img src="'+url+'">'}).catch(function(e){
preview.innerHTML='';toast(e.message,false)})}

function toggleSection(id){
var el=document.getElementById(id);
el.classList.toggle('open');
var open=el.classList.contains('open');
var btn=el.querySelector('.card-head');
if(btn)btn.setAttribute('aria-expanded',open?'true':'false');
if(open&&!el.dataset.loaded){el.dataset.loaded='1';
if(id==='locSection')loadLocation();
if(id==='contactsSection')loadContacts();
if(id==='appsSection')loadApps();
if(id==='sensorsSection')loadSensors();
if(id==='systemSection')loadSystem()}}

function loadLocation(){
var el=document.getElementById('locInfo');
el.innerHTML='<div class="loading-dot"></div>';
api('/api/location').then(function(d){
el.innerHTML='<div class="li"><div class="ll">Latitude</div>'+d.latitude+'</div>'
+'<div class="li"><div class="ll">Longitude</div>'+d.longitude+'</div>'
+'<div class="li"><div class="ll">Altitude</div>'+(d.altitude||'N/A')+'</div>'
+'<div class="li"><div class="ll">Accuracy</div>'+d.accuracy+' m</div>'
+'<div class="li"><div class="ll">Provider</div>'+d.provider+'</div>';
}).catch(function(e){el.innerHTML='<span style="color:var(--red);font-size:13px">'+e.message+'</span>'})}

function loadContacts(){
var el=document.getElementById('contactsList');
el.innerHTML='<div class="loading-dot"></div>';
api('/api/contacts').then(function(d){
allContacts=d.contacts||[];
renderContacts(allContacts)}).catch(function(e){el.innerHTML='<span style="color:var(--red);font-size:13px">'+e.message+'</span>'})}
function renderContacts(list){
if(!list.length){document.getElementById('contactsList').innerHTML='<div class="item" style="color:var(--muted)">No contacts found</div>';return}
var h='<table class="data-table"><thead><tr><th>Name</th><th>Phone</th></tr></thead><tbody>';
for(var i=0;i<list.length;i++){h+='<tr><td>'+esc(list[i].name)+'</td><td>'+esc(list[i].phone)+'</td></tr>'}
h+='</tbody></table>';
document.getElementById('contactsList').innerHTML=h}
function filterContacts(q){
q=q.toLowerCase();
renderContacts(allContacts.filter(function(c){return c.name.toLowerCase().indexOf(q)!==-1||c.phone.indexOf(q)!==-1}))}

function loadApps(){
var el=document.getElementById('appsList');
el.innerHTML='<div class="loading-dot"></div>';
api('/api/apps').then(function(d){
allApps=d.apps||[];
renderApps(allApps)}).catch(function(e){el.innerHTML='<span style="color:var(--red);font-size:13px">'+e.message+'</span>'})}
function renderApps(list){
if(!list.length){document.getElementById('appsList').innerHTML='<div class="item" style="color:var(--muted)">No apps found</div>';return}
var h='';
for(var i=0;i<list.length;i++){h+='<div class="item" style="display:flex;align-items:center;gap:8px"><div style="flex:1"><div class="name">'+esc(list[i].name)+'</div><div class="pkg">'+esc(list[i].packageName)+'</div></div><button class="btn" style="padding:4px 8px;font-size:11px" data-pkg="'+esc(list[i].packageName)+'" onclick="launchApp(this.dataset.pkg)">Launch</button><button class="btn btn-red" style="padding:4px 8px;font-size:11px" data-pkg="'+esc(list[i].packageName)+'" onclick="stopApp(this.dataset.pkg)">Stop</button></div>'}
document.getElementById('appsList').innerHTML=h}
function filterApps(q){
q=q.toLowerCase();
renderApps(allApps.filter(function(a){return a.name.toLowerCase().indexOf(q)!==-1||a.packageName.toLowerCase().indexOf(q)!==-1}))}

function esc(s){if(!s)return'';return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;')}

var micActive=false;
function toggleMic(){
var btn=document.getElementById('micBtn');
var audio=document.getElementById('micAudio');
if(!micActive){
audio.src=B+'/api/mic/stream';
audio.style.display='block';
audio.play().catch(function(e){toast('Playback failed: '+e.message,false)});
btn.textContent='Stop';btn.classList.add('btn-amber');btn.classList.remove('btn-red');
micActive=true;
}else{
audio.pause();audio.removeAttribute('src');audio.load();
audio.style.display='none';
btn.textContent='Listen';btn.classList.remove('btn-amber');btn.classList.add('btn-red');
micActive=false}}

function launchApp(pkg){
api('/api/apps/launch?package='+encodeURIComponent(pkg),{method:'POST'}).then(function(){
toast('Launched '+pkg,true)}).catch(function(e){toast(e.message,false)})}

function stopApp(pkg){
api('/api/apps/stop?package='+encodeURIComponent(pkg),{method:'POST'}).then(function(d){
toast('Stopped '+pkg+(d&&d.note?' ('+d.note+')':''),true)}).catch(function(e){toast(e.message,false)})}

function doNotify(){
var title=document.getElementById('notifTitle').value.trim();
var body=document.getElementById('notifBody').value.trim();
if(!title||!body){toast('Title and message are required',false);return}
var priority=document.getElementById('notifPriority').value;
api('/api/notify',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({title:title,body:body,priority:priority})}).then(function(){
document.getElementById('notifTitle').value='';
document.getElementById('notifBody').value='';
toast('Notification sent',true)}).catch(function(e){toast(e.message,false)})}

function loadBrightness(){
api('/api/brightness').then(function(d){
var s=document.getElementById('brightSlider');
s.max=d.maxBrightness||255;s.value=d.brightness;
document.getElementById('brightVal').textContent=d.brightness;
document.getElementById('brightMode').textContent=d.mode+(d.canWrite?'':' (read-only)')}
).catch(function(e){document.getElementById('brightMode').textContent=e.message})}

var brightTimer;
function updateBrightness(el){
var v=parseInt(el.value);
document.getElementById('brightVal').textContent=v;
clearTimeout(brightTimer);
brightTimer=setTimeout(function(){
api('/api/brightness',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({brightness:v})}
).catch(function(e){toast(e.message,false)})},250)}

function setWallpaper(){
var input=document.getElementById('wallpaperInput');
if(!input.files||!input.files.length){toast('Choose an image first',false);return}
var target=document.getElementById('wallpaperTarget').value;
api('/api/wallpaper?target='+target,{method:'POST',headers:{'Content-Type':input.files[0].type||'application/octet-stream'},body:input.files[0]}).then(function(d){
toast('Wallpaper set ('+d.width+'×'+d.height+')',true)}).catch(function(e){toast(e.message,false)})}

function doToast(){
var t=document.getElementById('toastText').value.trim();
if(!t){toast('Enter a message',false);return}
api('/api/toast',{method:'POST',body:t}).then(function(){
document.getElementById('toastText').value='';toast('Toast shown',true)}).catch(function(e){toast(e.message,false)})}

function loadSensors(){
var el=document.getElementById('sensorsList');
el.innerHTML='<div class="loading-dot"></div>';
api('/api/sensors').then(function(d){
var names=Object.keys(d.sensors||{});
if(!names.length){el.innerHTML='<div class="item" style="color:var(--muted)">No sensors available</div>';return}
var h='<table class="data-table"><thead><tr><th>Sensor</th><th>Values</th></tr></thead><tbody>';
for(var i=0;i<names.length;i++){
var s=d.sensors[names[i]];
var values=(s.values||[]).map(function(v){return Number(v).toFixed(3)}).join(', ')||'—';
h+='<tr><td>'+esc(names[i])+'</td><td>'+esc(values)+'</td></tr>'}
el.innerHTML=h+'</tbody></table>'}
).catch(function(e){el.innerHTML='<span style="color:var(--red);font-size:13px">'+e.message+'</span>'})}

function loadSystem(){
var el=document.getElementById('systemInfo');
el.innerHTML='<div class="loading-dot"></div>';
api('/api/system').then(function(d){
var h='';
h+='<div class="info-row"><span class="label">Uptime</span><span class="value">'+esc(d.uptimeFormatted)+'</span></div>';
h+='<div class="info-row"><span class="label">CPU cores</span><span class="value">'+d.cpuCores+'</span></div>';
h+='<div class="info-row"><span class="label">ABI</span><span class="value">'+esc(d.abi)+'</span></div>';
h+='<div class="info-row"><span class="label">Security patch</span><span class="value">'+esc(d.securityPatch)+'</span></div>';
h+='<div class="info-row"><span class="label">App version</span><span class="value">'+esc(d.appVersion)+'</span></div>';
el.innerHTML=h}
).catch(function(e){el.innerHTML='<span style="color:var(--red);font-size:13px">'+e.message+'</span>'})}

var dropZone=document.getElementById('dropZone');
var dropInput=document.getElementById('dropInput');
var dropProgress=document.getElementById('dropProgress');
var dropFill=document.getElementById('dropFill');
var dropText=document.getElementById('dropText');
dropZone.addEventListener('dragover',function(e){e.preventDefault();dropZone.classList.add('drag-over')});
dropZone.addEventListener('dragleave',function(){dropZone.classList.remove('drag-over')});
dropZone.addEventListener('drop',function(e){e.preventDefault();dropZone.classList.remove('drag-over');if(e.dataTransfer.files.length)sendDropFiles(e.dataTransfer.files)});
dropInput.addEventListener('change',function(){if(dropInput.files.length)sendDropFiles(dropInput.files)});

function sendDropFiles(list){
var fd=new FormData();
for(var i=0;i<list.length;i++)fd.append('files',list[i],list[i].name);
dropProgress.style.display='block';dropFill.style.width='0%';dropText.textContent='Uploading…';
var xhr=new XMLHttpRequest();
xhr.upload.addEventListener('progress',function(e){if(e.lengthComputable){var p=Math.round(e.loaded/e.total*100);dropFill.style.width=p+'%';dropText.textContent='Uploading… '+p+'%'}});
xhr.onload=function(){
dropProgress.style.display='none';dropFill.style.width='0%';dropInput.value='';
if(xhr.status>=200&&xhr.status<300){dropText.textContent='';toast('Files sent to device',true);loadFileDrop()}
else{toast('Upload failed',false)}};
xhr.onerror=function(){dropProgress.style.display='none';toast('Upload failed',false)};
xhr.open('POST','/files/');xhr.send(fd)}

function loadFileDrop(){
fetch(B+'/files/?json=1').then(function(r){return r.json()}).then(function(d){
var el=document.getElementById('dropList');
var list=d.files||[];
if(!list.length){el.innerHTML='<div class="item" style="color:var(--muted)">No files yet</div>';return}
var h='';
for(var i=0;i<list.length;i++){
var f=list[i];
h+='<div class="item"><a href="'+B+encodeURI(f.url)+'" target="_blank" style="color:var(--accent);text-decoration:none">'+esc(f.name)+'</a><div class="pkg">'+(f.isDirectory?'folder':fmtBytes(f.size))+'</div></div>'}
el.innerHTML=h}).catch(function(){document.getElementById('dropList').innerHTML='<div class="item" style="color:var(--muted)">File Drop unavailable</div>'})}

function loadAll(){loadBattery();loadWifi();loadDevice();loadVolume();loadClipboard();loadBrightness();loadFileDrop()}
loadAll();
setupQr();
setInterval(function(){loadBattery();loadWifi();loadDevice()},30000);
</script>""")
    }

    private fun StringBuilder.appendHtmlEscaped(text: String) {
        for (c in text) {
            when (c) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&#39;")
                else -> append(c)
            }
        }
    }

    private fun StringBuilder.appendJsStringEscaped(text: String) {
        for (c in text) {
            when (c) {
                '\\' -> append("\\\\")
                '\'' -> append("\\'")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(c)
            }
        }
    }
}
