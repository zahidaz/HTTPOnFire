package com.azzahid.hof.features.http.utils

object DashboardHtmlBuilder {

    fun build(baseUrl: String): String {
        cardIndex = 0
        return buildString {
            append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">")
            append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
            append("<title>HTTP on Fire - Dashboard</title>")
            appendStyles()
            append("</head><body>")
            appendHeader()
            appendCards(baseUrl)
            appendScripts(baseUrl)
            append("</body></html>")
        }
    }

    private fun StringBuilder.appendStyles() {
        append("""<style>
*{margin:0;padding:0;box-sizing:border-box}
body{background:#0D0D0F;color:#E8E8ED;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;min-height:100vh}
.header{padding:24px;border-bottom:1px solid #2E2E33}
.header h1{font-size:22px;font-weight:600;color:#F5F5F7}
.header p{font-size:13px;color:#6B6B76;margin-top:4px}
.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(320px,1fr));gap:16px;padding:24px}
.section-title{grid-column:1/-1;font-size:14px;font-weight:600;color:#6B6B76;text-transform:uppercase;letter-spacing:1px;padding:8px 0 0}
.card{background:#1A1A1E;border:1px solid #2E2E33;border-radius:12px;padding:20px;display:flex;flex-direction:column;gap:12px}
.card-head{display:flex;justify-content:space-between;align-items:center}
.card-title{font-size:15px;font-weight:600;color:#F5F5F7}
.badge{font-size:11px;font-weight:600;padding:2px 8px;border-radius:4px;border:1px solid}
.badge-get{color:#00D4AA;border-color:#00D4AA;background:rgba(0,212,170,.1)}
.badge-post{color:#A78BFA;border-color:#A78BFA;background:rgba(167,139,250,.1)}
.card-desc{font-size:13px;color:#6B6B76;line-height:1.4}
.card-path{font-size:12px;color:#6B6B76;font-family:monospace;background:#252529;padding:4px 8px;border-radius:4px;display:inline-block}
.card-input{display:flex;gap:8px;flex-wrap:wrap;align-items:end}
.card-input input,.card-input select,.card-input textarea{background:#252529;border:1px solid #2E2E33;border-radius:6px;padding:8px 10px;color:#E8E8ED;font-size:13px;outline:none;flex:1;min-width:100px}
.card-input input:focus,.card-input textarea:focus{border-color:#00D4AA}
.card-input textarea{min-height:60px;resize:vertical;font-family:inherit}
.card-input label{font-size:11px;color:#6B6B76;display:block;margin-bottom:4px}
.field{display:flex;flex-direction:column;flex:1;min-width:100px}
.btn{background:#0D2E26;border:1px solid #00D4AA;color:#00D4AA;padding:8px 16px;border-radius:6px;cursor:pointer;font-size:13px;font-weight:500;white-space:nowrap;transition:background .15s}
.btn:hover{background:#00D4AA;color:#0D0D0F}
.btn:disabled{opacity:.4;cursor:not-allowed}
.btn-warn{background:rgba(239,68,68,.1);border-color:#EF4444;color:#EF4444}
.btn-warn:hover{background:#EF4444;color:#0D0D0F}
.result{background:#252529;border:1px solid #2E2E33;border-radius:8px;padding:12px;font-size:12px;font-family:monospace;white-space:pre-wrap;word-break:break-all;max-height:300px;overflow-y:auto;display:none;color:#E8E8ED;line-height:1.5}
.result.show{display:block}
.result.error{border-color:#EF4444;color:#EF4444}
.result img{max-width:100%;border-radius:4px;margin-top:4px}
.warn{font-size:11px;color:#F59E0B;margin-top:4px}
.spinner{display:inline-block;width:14px;height:14px;border:2px solid #2E2E33;border-top-color:#00D4AA;border-radius:50%;animation:spin .6s linear infinite}
@keyframes spin{to{transform:rotate(360deg)}}
@media(max-width:600px){.grid{grid-template-columns:1fr;padding:16px}.header{padding:16px}}
</style>""")
    }

    private fun StringBuilder.appendHeader() {
        append("""<div class="header">""")
        append("""<h1>Device Dashboard</h1>""")
        append("""<p>Control and monitor your device remotely</p>""")
        append("</div>")
    }

    private fun StringBuilder.appendCards(baseUrl: String) {
        append("""<div class="grid">""")

        appendSectionTitle("Info")
        appendGetCard("Device Info", "Device model, OS, memory, and storage", "/api/device")
        appendGetCard("Battery", "Battery level, charging status, and health", "/api/battery")
        appendGetCard("WiFi", "WiFi connection details and signal info", "/api/wifi")
        appendGetCard("Installed Apps", "List all installed apps on the device", "/api/apps")

        appendSectionTitle("Actions")
        appendPostCard(
            "Vibrate", "Vibrate the device", "/api/vibrate",
            inputs = listOf(InputField("duration", "Duration (ms)", "500", "number"))
        )
        appendPostCard(
            "Flashlight", "Toggle the device flashlight", "/api/flashlight",
            inputs = listOf(InputField("action", "Action", "toggle", "select", listOf("on", "off", "toggle")))
        )
        appendPostCard("Ring", "Play alarm sound to find the device", "/api/ring")
        appendPostCard(
            "Text to Speech", "Speak text aloud", "/api/speak",
            inputs = listOf(InputField("text", "Text to speak", "Hello from HTTP on Fire", "textarea"))
        )
        appendGetCard("Volume", "Read device volume levels", "/api/volume")
        appendPostCard(
            "Set Volume", "Set device volume", "/api/volume",
            inputs = listOf(
                InputField("stream", "Stream", "media", "select", listOf("media", "ring", "alarm", "notification")),
                InputField("volume", "Level", "5", "number")
            ),
            bodyBuilder = true
        )
        appendGetCard("Clipboard", "Read clipboard contents", "/api/clipboard")
        appendPostCard(
            "Set Clipboard", "Write text to clipboard", "/api/clipboard",
            inputs = listOf(InputField("text", "Text", "Copied from dashboard", "text"))
        )

        appendSectionTitle("Media")
        appendGetCard(
            "QR Code", "Generate a QR code from text", "/api/qr",
            inputs = listOf(InputField("data", "Text or URL", baseUrl, "text")),
            isImage = true
        )
        appendPostCard("Camera Capture", "Take a photo from the camera", "/api/camera", isImage = true)

        appendSectionTitle("Sensitive")
        appendGetCard("Location", "Get device GPS coordinates", "/api/location", warn = "Requires location permission")
        appendGetCard("Contacts", "Read device contacts", "/api/contacts", warn = "Requires contacts permission")

        appendSectionTitle("Debug")
        appendGetCard("Echo", "Mirror back your request details", "/api/echo")

        append("</div>")
    }

    private data class InputField(
        val name: String,
        val label: String,
        val default: String = "",
        val type: String = "text",
        val options: List<String> = emptyList()
    )

    private var cardIndex = 0

    private fun StringBuilder.appendGetCard(
        title: String,
        desc: String,
        path: String,
        inputs: List<InputField> = emptyList(),
        isImage: Boolean = false,
        warn: String? = null
    ) {
        val id = "card${cardIndex++}"
        append("""<div class="card">""")
        append("""<div class="card-head"><span class="card-title">""")
        appendHtmlEscaped(title)
        append("""</span><span class="badge badge-get">GET</span></div>""")
        append("""<div class="card-desc">""")
        appendHtmlEscaped(desc)
        append("</div>")
        append("""<div class="card-path">""")
        appendHtmlEscaped(path)
        append("</div>")

        if (inputs.isNotEmpty()) {
            append("""<div class="card-input">""")
            inputs.forEach { appendInputField(it, id) }
            append("</div>")
        }

        warn?.let {
            append("""<div class="warn">""")
            appendHtmlEscaped(it)
            append("</div>")
        }

        append("""<button class="btn" id="${id}Btn" onclick="doGet('$id','$path',${isImage}""")
        if (inputs.isNotEmpty()) {
            append(",[")
            inputs.forEachIndexed { i, field ->
                if (i > 0) append(",")
                append("'${field.name}'")
            }
            append("]")
        }
        append(""")">Try it</button>""")
        append("""<div class="result" id="${id}Res"></div>""")
        append("</div>")
    }

    private fun StringBuilder.appendPostCard(
        title: String,
        desc: String,
        path: String,
        inputs: List<InputField> = emptyList(),
        isImage: Boolean = false,
        warn: String? = null,
        bodyBuilder: Boolean = false
    ) {
        val id = "card${cardIndex++}"
        append("""<div class="card">""")
        append("""<div class="card-head"><span class="card-title">""")
        appendHtmlEscaped(title)
        append("""</span><span class="badge badge-post">POST</span></div>""")
        append("""<div class="card-desc">""")
        appendHtmlEscaped(desc)
        append("</div>")
        append("""<div class="card-path">""")
        appendHtmlEscaped(path)
        append("</div>")

        if (inputs.isNotEmpty()) {
            append("""<div class="card-input">""")
            inputs.forEach { appendInputField(it, id) }
            append("</div>")
        }

        warn?.let {
            append("""<div class="warn">""")
            appendHtmlEscaped(it)
            append("</div>")
        }

        append("""<button class="btn" id="${id}Btn" onclick="doPost('$id','$path',${isImage}""")
        if (inputs.isNotEmpty()) {
            append(",[")
            inputs.forEachIndexed { i, field ->
                if (i > 0) append(",")
                append("'${field.name}'")
            }
            append("]")
            if (bodyBuilder) append(",true")
        }
        append(""")">Try it</button>""")
        append("""<div class="result" id="${id}Res"></div>""")
        append("</div>")
    }

    private fun StringBuilder.appendInputField(field: InputField, cardId: String) {
        append("""<div class="field">""")
        append("""<label>""")
        appendHtmlEscaped(field.label)
        append("</label>")

        when (field.type) {
            "select" -> {
                append("""<select id="${cardId}_${field.name}">""")
                field.options.forEach { opt ->
                    val selected = if (opt == field.default) " selected" else ""
                    append("""<option value="$opt"$selected>$opt</option>""")
                }
                append("</select>")
            }
            "textarea" -> {
                append("""<textarea id="${cardId}_${field.name}" placeholder="">""")
                appendHtmlEscaped(field.default)
                append("</textarea>")
            }
            else -> {
                append("""<input id="${cardId}_${field.name}" type="${field.type}" value=""")
                append("\"")
                appendHtmlAttrEscaped(field.default)
                append("\"")
                append(">")
            }
        }
        append("</div>")
    }

    private fun StringBuilder.appendSectionTitle(title: String) {
        append("""<div class="section-title">""")
        appendHtmlEscaped(title)
        append("</div>")
    }

    private fun StringBuilder.appendScripts(baseUrl: String) {
        append("""<script>
var BASE='""")
        appendJsStringEscaped(baseUrl.trimEnd('/'))
        append("""';
function showLoading(id){
var b=document.getElementById(id+'Btn');
var r=document.getElementById(id+'Res');
b.disabled=true;b.innerHTML='<span class="spinner"></span>';
r.className='result';r.innerHTML=''}
function showResult(id,data,isErr){
var b=document.getElementById(id+'Btn');
var r=document.getElementById(id+'Res');
b.disabled=false;b.textContent='Try it';
r.className='result show'+(isErr?' error':'');
r.innerHTML=data}
function getInputs(id,names){
var p={};
if(!names)return p;
names.forEach(function(n){
var el=document.getElementById(id+'_'+n);
if(el)p[n]=el.value});
return p}
function doGet(id,path,isImage,names){
showLoading(id);
var params=getInputs(id,names);
var qs=Object.keys(params).map(function(k){return k+'='+encodeURIComponent(params[k])}).join('&');
var url=BASE+path+(qs?'?'+qs:'');
if(isImage){
var img=new Image();
img.onload=function(){showResult(id,'<img src="'+url+'">')};
img.onerror=function(){showResult(id,'Failed to load image',true)};
img.src=url;return}
fetch(url).then(function(r){return r.text()}).then(function(t){
try{showResult(id,JSON.stringify(JSON.parse(t),null,2))}
catch(e){showResult(id,t)}
}).catch(function(e){showResult(id,'Error: '+e.message,true)})}
function doPost(id,path,isImage,names,bodyBuild){
showLoading(id);
var params=getInputs(id,names);
var url=BASE+path;
var opts={method:'POST',headers:{}};
if(bodyBuild){
opts.headers['Content-Type']='application/json';
opts.body=JSON.stringify(params)}
else if(names&&names.length===1&&names[0]==='text'){
opts.body=params.text||''}
else if(names&&names.length>0){
var qs=Object.keys(params).map(function(k){return k+'='+encodeURIComponent(params[k])}).join('&');
url=url+'?'+qs}
if(isImage){
fetch(url,opts).then(function(r){return r.blob()}).then(function(b){
var u=URL.createObjectURL(b);
showResult(id,'<img src="'+u+'">')
}).catch(function(e){showResult(id,'Error: '+e.message,true)});return}
fetch(url,opts).then(function(r){return r.text()}).then(function(t){
try{showResult(id,JSON.stringify(JSON.parse(t),null,2))}
catch(e){showResult(id,t)}
}).catch(function(e){showResult(id,'Error: '+e.message,true)})}
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

    private fun StringBuilder.appendHtmlAttrEscaped(text: String) {
        for (c in text) {
            when (c) {
                '&' -> append("&amp;")
                '"' -> append("&quot;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
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
