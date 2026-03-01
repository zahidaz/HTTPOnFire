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
:root{--bg:#0D0D0F;--surface:#1A1A1E;--surface2:#252529;--border:#2E2E33;--muted:#6B6B76;--text:#E8E8ED;--white:#F5F5F7;--cyan:#00D4AA;--cyan-d:#00A888;--cyan-s:#0D2E26;--violet:#A78BFA;--violet-d:#7C5CBF;--violet-s:#1E1533;--amber:#F59E0B;--amber-s:#2E2008;--red:#EF4444;--red-s:#2E1010}
*{margin:0;padding:0;box-sizing:border-box}
body{background:var(--bg);color:var(--text);font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;min-height:100vh}
header{padding:20px 24px;border-bottom:1px solid var(--border);display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:12px}
.logo{display:flex;align-items:center;gap:12px}
.logo h1{font-size:20px;font-weight:600;color:var(--white)}
.logo .dot{width:8px;height:8px;border-radius:50%;background:var(--cyan);box-shadow:0 0 8px var(--cyan);flex-shrink:0}
.device-name{font-size:13px;color:var(--muted)}
main{max-width:1200px;margin:0 auto;padding:24px}
.section{margin-bottom:32px}
.section-title{font-size:12px;font-weight:600;color:var(--muted);text-transform:uppercase;letter-spacing:1.5px;margin-bottom:16px}
.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:16px}
.card{background:var(--surface);border:1px solid var(--border);border-radius:12px;padding:20px}
.card h3{font-size:14px;font-weight:600;color:var(--white);margin-bottom:12px;display:flex;align-items:center;gap:8px}
.gauge-wrap{display:flex;align-items:center;gap:20px}
.gauge{position:relative;width:100px;height:100px;flex-shrink:0}
.gauge svg{transform:rotate(-90deg)}
.gauge-text{position:absolute;inset:0;display:flex;align-items:center;justify-content:center;font-size:22px;font-weight:600;color:var(--white)}
.gauge-text span{font-size:12px;color:var(--muted);font-weight:400}
.info-rows{display:flex;flex-direction:column;gap:6px;flex:1}
.info-row{display:flex;justify-content:space-between;font-size:13px}
.info-row .label{color:var(--muted)}
.info-row .value{color:var(--text);font-weight:500}
.bar-wrap{margin-top:4px}
.bar-label{display:flex;justify-content:space-between;font-size:12px;margin-bottom:4px}
.bar-label .bl{color:var(--muted)}
.bar-label .bv{color:var(--text)}
.bar{height:6px;background:var(--surface2);border-radius:3px;overflow:hidden}
.bar-fill{height:100%;border-radius:3px;transition:width .5s ease}
.actions-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:16px}
.toggle-row{display:flex;align-items:center;justify-content:space-between}
.toggle{width:48px;height:26px;border-radius:13px;background:var(--surface2);border:1px solid var(--border);cursor:pointer;position:relative;transition:background .2s,border-color .2s}
.toggle.on{background:var(--cyan-s);border-color:var(--cyan)}
.toggle::after{content:'';position:absolute;top:2px;left:2px;width:20px;height:20px;border-radius:50%;background:var(--muted);transition:transform .2s,background .2s}
.toggle.on::after{transform:translateX(22px);background:var(--cyan)}
.slider-group{display:flex;flex-direction:column;gap:12px}
.slider-row{display:flex;align-items:center;gap:12px}
.slider-row .sl{font-size:12px;color:var(--muted);width:80px;flex-shrink:0}
.slider-row input[type=range]{flex:1;-webkit-appearance:none;height:4px;background:var(--surface2);border-radius:2px;outline:none}
.slider-row input[type=range]::-webkit-slider-thumb{-webkit-appearance:none;width:16px;height:16px;border-radius:50%;background:var(--cyan);cursor:pointer}
.slider-row .sv{font-size:12px;color:var(--text);width:24px;text-align:right;flex-shrink:0}
.btn{background:var(--cyan-s);border:1px solid var(--cyan);color:var(--cyan);padding:8px 16px;border-radius:8px;cursor:pointer;font-size:13px;font-weight:500;transition:background .15s,color .15s;outline:none}
.btn:hover{background:var(--cyan);color:var(--bg)}
.btn:disabled{opacity:.4;cursor:not-allowed}
.btn-violet{background:var(--violet-s);border-color:var(--violet);color:var(--violet)}
.btn-violet:hover{background:var(--violet);color:var(--bg)}
.btn-amber{background:var(--amber-s);border-color:var(--amber);color:var(--amber)}
.btn-amber:hover{background:var(--amber);color:var(--bg)}
.btn-red{background:var(--red-s);border-color:var(--red);color:var(--red)}
.btn-red:hover{background:var(--red);color:var(--bg)}
.inline-input{display:flex;gap:8px;align-items:center;margin-top:8px}
.inline-input input,.inline-input textarea,.inline-input select{background:var(--surface2);border:1px solid var(--border);border-radius:6px;padding:8px 10px;color:var(--text);font-size:13px;outline:none;font-family:inherit}
.inline-input input:focus,.inline-input textarea:focus{border-color:var(--cyan)}
.inline-input input[type=number]{width:80px}
.text-area-wrap textarea{width:100%;min-height:60px;resize:vertical;background:var(--surface2);border:1px solid var(--border);border-radius:6px;padding:8px 10px;color:var(--text);font-size:13px;outline:none;font-family:inherit;margin-bottom:8px}
.text-area-wrap textarea:focus{border-color:var(--cyan)}
.clip-current{font-size:13px;color:var(--muted);background:var(--surface2);border-radius:6px;padding:8px 10px;margin-bottom:8px;word-break:break-all;max-height:60px;overflow-y:auto;border:1px solid var(--border)}
.qr-preview{text-align:center;margin-top:12px}
.qr-preview img{border-radius:8px;background:#fff;padding:8px}
.cam-preview{margin-top:12px;text-align:center}
.cam-preview img{max-width:100%;border-radius:8px}
.cam-select{display:flex;gap:8px;flex-wrap:wrap;align-items:center}
.collapsible h3{cursor:pointer;user-select:none}
.collapsible h3::after{content:'▸';margin-left:auto;transition:transform .2s;font-size:12px}
.collapsible.open h3::after{transform:rotate(90deg)}
.collapsible .col-body{display:none;margin-top:12px}
.collapsible.open .col-body{display:block}
.search-input{width:100%;background:var(--surface2);border:1px solid var(--border);border-radius:6px;padding:8px 10px;color:var(--text);font-size:13px;outline:none;margin-bottom:8px}
.search-input:focus{border-color:var(--cyan)}
.data-table{width:100%;border-collapse:collapse;font-size:13px}
.data-table th{text-align:left;color:var(--muted);font-weight:500;padding:6px 8px;border-bottom:1px solid var(--border)}
.data-table td{padding:6px 8px;border-bottom:1px solid var(--border);color:var(--text)}
.data-list{max-height:300px;overflow-y:auto}
.data-list .item{padding:8px;border-bottom:1px solid var(--border);font-size:13px}
.data-list .item .name{color:var(--text)}
.data-list .item .pkg{color:var(--muted);font-size:11px;margin-top:2px}
.perm-badge{font-size:10px;background:var(--amber-s);color:var(--amber);padding:2px 6px;border-radius:4px;border:1px solid var(--amber);margin-left:8px}
.loc-info{display:grid;grid-template-columns:1fr 1fr;gap:8px}
.loc-info .li{font-size:13px}
.loc-info .li .ll{color:var(--muted);font-size:11px}
.toast-container{position:fixed;bottom:24px;right:24px;display:flex;flex-direction:column;gap:8px;z-index:1000;pointer-events:none}
.toast{padding:10px 16px;border-radius:8px;font-size:13px;pointer-events:auto;animation:slideIn .3s ease;max-width:360px}
.toast-ok{background:var(--cyan-s);border:1px solid var(--cyan);color:var(--cyan)}
.toast-err{background:var(--red-s);border:1px solid var(--red);color:var(--red)}
@keyframes slideIn{from{opacity:0;transform:translateY(10px)}to{opacity:1;transform:translateY(0)}}
.loading-dot{display:inline-block;width:6px;height:6px;border-radius:50%;background:var(--muted);animation:pulse 1s infinite}
@keyframes pulse{0%,100%{opacity:.3}50%{opacity:1}}
@media(max-width:600px){main{padding:16px}header{padding:16px}.grid,.actions-grid{grid-template-columns:1fr}.loc-info{grid-template-columns:1fr}}
</style>""")
    }

    private fun StringBuilder.appendLayout() {
        append("""
<header>
<div class="logo"><div class="dot"></div><h1>HTTP on Fire</h1></div>
<div class="device-name" id="deviceLabel"></div>
</header>
<main>
<div class="section"><div class="section-title">Status</div>
<div class="grid">
<div class="card" id="batteryCard"><h3>Battery</h3>
<div class="gauge-wrap">
<div class="gauge"><svg viewBox="0 0 100 100" width="100" height="100"><circle cx="50" cy="50" r="44" fill="none" stroke="#252529" stroke-width="8"/><circle id="battGauge" cx="50" cy="50" r="44" fill="none" stroke="#00D4AA" stroke-width="8" stroke-linecap="round" stroke-dasharray="276.46" stroke-dashoffset="276.46"/></svg><div class="gauge-text" id="battPct">--</div></div>
<div class="info-rows" id="battInfo"></div>
</div></div>
<div class="card" id="wifiCard"><h3>WiFi</h3><div class="info-rows" id="wifiInfo"><div class="loading-dot"></div></div></div>
<div class="card" id="deviceCard"><h3>Device</h3><div id="deviceInfo"><div class="loading-dot"></div></div></div>
</div></div>

<div class="section"><div class="section-title">Quick Actions</div>
<div class="actions-grid">
<div class="card"><h3>Flashlight</h3>
<div class="toggle-row"><span style="font-size:13px;color:var(--muted)" id="flashLabel">OFF</span><div class="toggle" id="flashToggle" onclick="toggleFlash()"></div></div></div>

<div class="card"><h3>Volume</h3>
<div class="slider-group" id="volumeSliders"><div class="loading-dot"></div></div></div>

<div class="card"><h3>Vibrate</h3>
<div class="inline-input"><input type="number" id="vibDur" value="500" min="1" max="10000" placeholder="ms"><button class="btn" onclick="doVibrate()">Vibrate</button></div></div>

<div class="card"><h3>Find My Phone</h3>
<div class="inline-input"><select id="ringDur"><option value="3000">3 sec</option><option value="5000" selected>5 sec</option><option value="10000">10 sec</option><option value="15000">15 sec</option></select><button class="btn btn-amber" onclick="doRing()">Ring</button></div></div>

<div class="card"><h3>Text to Speech</h3>
<div class="text-area-wrap"><textarea id="ttsText" placeholder="Type something to speak..."></textarea><button class="btn btn-violet" onclick="doSpeak()">Speak</button></div></div>

<div class="card"><h3>Clipboard</h3>
<div class="clip-current" id="clipText">Loading...</div>
<div class="inline-input"><input type="text" id="clipInput" placeholder="New clipboard text" style="flex:1"><button class="btn" onclick="setClipboard()">Set</button></div></div>
</div></div>

<div class="section"><div class="section-title">Tools</div>
<div class="grid">
<div class="card"><h3>QR Code</h3>
<div class="inline-input"><input type="text" id="qrInput" placeholder="Text or URL" style="flex:1"></div>
<div class="qr-preview" id="qrPreview"></div></div>

<div class="card"><h3>Camera</h3>
<div class="cam-select"><select id="camFacing"><option value="back">Back Camera</option><option value="front">Front Camera</option></select><button class="btn btn-violet" onclick="doCapture()">Capture</button></div>
<div class="cam-preview" id="camPreview"></div></div>
</div></div>

<div class="section"><div class="section-title">Data</div>
<div class="grid">
<div class="card collapsible" id="locSection"><h3 onclick="toggleSection('locSection')">Location<span class="perm-badge">Permission Required</span></h3>
<div class="col-body"><div class="loc-info" id="locInfo"></div></div></div>

<div class="card collapsible" id="contactsSection"><h3 onclick="toggleSection('contactsSection')">Contacts<span class="perm-badge">Permission Required</span></h3>
<div class="col-body"><input class="search-input" placeholder="Search contacts..." oninput="filterContacts(this.value)"><div class="data-list" id="contactsList"></div></div></div>

<div class="card collapsible" id="appsSection"><h3 onclick="toggleSection('appsSection')">Installed Apps</h3>
<div class="col-body"><input class="search-input" placeholder="Search apps..." oninput="filterApps(this.value)"><div class="data-list" id="appsList"></div></div></div>
</div></div>
</main>
<div class="toast-container" id="toasts"></div>
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
return fetch(B+path,opts||{}).then(function(r){
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
circ.style.stroke=pct<=20?'var(--red)':pct<=50?'var(--amber)':'var(--cyan)';
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
h+='<div class="bar-wrap"><div class="bar-label"><span class="bl">Memory</span><span class="bv">'+fmtBytes(d.totalMemory-d.availableMemory)+' / '+fmtBytes(d.totalMemory)+'</span></div><div class="bar"><div class="bar-fill" style="width:'+memPct+'%;background:var(--cyan)"></div></div></div>';
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
document.getElementById('flashLabel').textContent=flashOn?'ON':'OFF';
api('/api/flashlight?enable='+flashOn,{method:'POST'}).then(function(){
toast('Flashlight '+(flashOn?'on':'off'),true)}).catch(function(e){
flashOn=!flashOn;tog.classList.toggle('on',flashOn);
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

function toggleSection(id){document.getElementById(id).classList.toggle('open');
var el=document.getElementById(id);
if(el.classList.contains('open')&&!el.dataset.loaded){el.dataset.loaded='1';
if(id==='locSection')loadLocation();
if(id==='contactsSection')loadContacts();
if(id==='appsSection')loadApps()}}

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
for(var i=0;i<list.length;i++){h+='<div class="item"><div class="name">'+esc(list[i].name)+'</div><div class="pkg">'+esc(list[i].packageName)+'</div></div>'}
document.getElementById('appsList').innerHTML=h}
function filterApps(q){
q=q.toLowerCase();
renderApps(allApps.filter(function(a){return a.name.toLowerCase().indexOf(q)!==-1||a.packageName.toLowerCase().indexOf(q)!==-1}))}

function esc(s){if(!s)return'';return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;')}

function loadAll(){loadBattery();loadWifi();loadDevice();loadVolume();loadClipboard()}
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
