package com.azzahid.hof.features.http.utils

object FileExplorerHtmlBuilder {

    data class FileEntry(
        val name: String,
        val isDirectory: Boolean,
        val size: Long,
        val lastModified: Long
    )

    fun build(
        files: List<FileEntry>,
        currentPath: String,
        basePath: String,
        relativePath: String,
        allowUpload: Boolean
    ): String = buildString {
        append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">")
        append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
        append("<title>")
        appendHtmlEscaped(currentPath)
        append("</title>")
        appendStyles()
        append("</head><body>")
        appendHeader(currentPath, basePath, relativePath)
        appendToolbar(allowUpload)
        if (allowUpload) appendUploadZone()
        appendFileList(files, basePath, relativePath)
        appendScripts(allowUpload, currentPath)
        append("</body></html>")
    }

    private fun StringBuilder.appendStyles() {
        append("""<style>
*{margin:0;padding:0;box-sizing:border-box}
body{background:#0D0D0F;color:#E8E8ED;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;min-height:100vh}
.header{padding:20px 24px 0}
.breadcrumb{display:flex;flex-wrap:wrap;align-items:center;gap:4px;font-size:14px;color:#6B6B76}
.breadcrumb a{color:#00D4AA;text-decoration:none}
.breadcrumb a:hover{text-decoration:underline}
.breadcrumb .sep{margin:0 2px}
.toolbar{display:flex;align-items:center;gap:12px;padding:16px 24px;flex-wrap:wrap}
.search-box{flex:1;min-width:200px;background:#1A1A1E;border:1px solid #2E2E33;border-radius:8px;padding:8px 12px;color:#E8E8ED;font-size:14px;outline:none}
.search-box:focus{border-color:#00D4AA}
.search-box::placeholder{color:#6B6B76}
.btn-group{display:flex;gap:4px}
.btn{background:#1A1A1E;border:1px solid #2E2E33;color:#E8E8ED;padding:6px 10px;border-radius:6px;cursor:pointer;font-size:13px;display:flex;align-items:center;gap:4px}
.btn:hover{border-color:#00D4AA;color:#00D4AA}
.btn.active{background:#0D2E26;border-color:#00D4AA;color:#00D4AA}
select.btn{appearance:none;padding-right:24px;background-image:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' fill='%236B6B76'%3E%3Cpath d='M2 4l4 4 4-4'/%3E%3C/svg%3E");background-repeat:no-repeat;background-position:right 8px center}
.file-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:12px;padding:0 24px 24px}
.file-list{display:flex;flex-direction:column;gap:2px;padding:0 24px 24px}
.file-item{background:#1A1A1E;border:1px solid #2E2E33;border-radius:10px;text-decoration:none;color:#E8E8ED;transition:border-color .15s,background .15s}
.file-item:hover{border-color:#00D4AA;background:#252529}
.grid .file-item{padding:16px;display:flex;flex-direction:column;align-items:center;gap:8px;text-align:center}
.grid .file-icon{display:flex;align-items:center;justify-content:center}
.grid .file-icon svg{width:32px;height:32px}
.grid .file-name{font-size:13px;word-break:break-word;line-height:1.3}
.grid .file-meta{font-size:11px;color:#6B6B76}
.list .file-item{padding:10px 16px;display:flex;align-items:center;gap:12px;border-radius:8px}
.list .file-icon{width:28px;text-align:center;flex-shrink:0;display:flex;align-items:center;justify-content:center}
.list .file-icon svg{width:20px;height:20px}
.list .file-name{flex:1;font-size:14px;min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.list .file-meta{font-size:12px;color:#6B6B76;white-space:nowrap}
.list .file-date{font-size:12px;color:#6B6B76;white-space:nowrap;min-width:140px;text-align:right}
.dir-name{color:#00D4AA}
.empty{text-align:center;padding:48px 24px;color:#6B6B76;font-size:14px}
.upload-zone{margin:0 24px 16px;border:2px dashed #2E2E33;border-radius:12px;padding:32px;text-align:center;cursor:pointer;transition:border-color .2s,background .2s}
.upload-zone:hover,.upload-zone.drag-over{border-color:#00D4AA;background:#0D2E26}
.upload-zone .uz-icon{margin:0 auto 8px;width:36px;height:36px}
.upload-zone .uz-icon svg{width:36px;height:36px}
.upload-zone .label{color:#E8E8ED;font-size:14px}
.upload-zone .hint{color:#6B6B76;font-size:12px;margin-top:4px}
.upload-progress{margin:0 24px 16px;display:none}
.progress-bar{background:#1A1A1E;border-radius:6px;overflow:hidden;height:6px}
.progress-fill{height:100%;background:#00D4AA;border-radius:6px;transition:width .2s}
.progress-text{font-size:12px;color:#6B6B76;margin-top:6px;text-align:center}
.btn svg{width:14px;height:14px}
@media(max-width:600px){.file-grid{grid-template-columns:repeat(auto-fill,minmax(140px,1fr));gap:8px;padding:0 16px 16px}.header{padding:16px 16px 0}.toolbar{padding:12px 16px}.file-list{padding:0 16px 16px}.upload-zone{margin:0 16px 12px}.upload-progress{margin:0 16px 12px}.list .file-date{display:none}}
</style>""")
    }

    private fun StringBuilder.appendHeader(
        currentPath: String,
        basePath: String,
        relativePath: String
    ) {
        append("<div class=\"header\"><nav class=\"breadcrumb\">")
        val baseSegments = basePath.trim('/').split('/').filter { it.isNotEmpty() }
        val relSegments = relativePath.split('/').filter { it.isNotEmpty() }
        val allSegments = baseSegments + relSegments

        append("<a href=\"$basePath/\">~</a>")
        allSegments.forEachIndexed { index, segment ->
            append("<span class=\"sep\">/</span>")
            if (index < allSegments.size - 1) {
                val href = "/" + allSegments.subList(0, index + 1).joinToString("/")
                append("<a href=\"$href/\">")
                appendHtmlEscaped(segment)
                append("</a>")
            } else {
                append("<span>")
                appendHtmlEscaped(segment)
                append("</span>")
            }
        }
        append("</nav></div>")
    }

    private fun StringBuilder.appendToolbar(allowUpload: Boolean) {
        append("<div class=\"toolbar\">")
        append("<input class=\"search-box\" id=\"search\" type=\"text\" placeholder=\"Filter files…\">")
        append("<div class=\"btn-group\">")
        append("<button class=\"btn active\" id=\"gridBtn\" onclick=\"setView('grid')\" title=\"Grid\">")
        append(svgIcon(SVG_GRID))
        append("</button>")
        append("<button class=\"btn\" id=\"listBtn\" onclick=\"setView('list')\" title=\"List\">")
        append(svgIcon(SVG_LIST))
        append("</button>")
        append("</div>")
        append("<select class=\"btn\" id=\"sortSelect\" onchange=\"sortFiles(this.value)\">")
        append("<option value=\"name\">Name</option>")
        append("<option value=\"size\">Size</option>")
        append("<option value=\"date\">Date</option>")
        append("</select>")
        if (allowUpload) {
            append("<button class=\"btn\" onclick=\"document.getElementById('uploadInput').click()\">")
            append(svgIcon(SVG_UPLOAD))
            append(" Upload</button>")
        }
        append("</div>")
    }

    private fun StringBuilder.appendUploadZone() {
        append("<div class=\"upload-zone\" id=\"uploadZone\">")
        append("<div class=\"uz-icon\">")
        append(svgIcon(SVG_UPLOAD, "#6B6B76"))
        append("</div>")
        append("<div class=\"label\">Drop files here or click to upload</div>")
        append("<div class=\"hint\">Files will be saved to this folder</div>")
        append("<input type=\"file\" id=\"uploadInput\" multiple style=\"display:none\">")
        append("</div>")
        append("<div class=\"upload-progress\" id=\"uploadProgress\">")
        append("<div class=\"progress-bar\"><div class=\"progress-fill\" id=\"progressFill\"></div></div>")
        append("<div class=\"progress-text\" id=\"progressText\"></div>")
        append("</div>")
    }

    private fun StringBuilder.appendFileList(
        files: List<FileEntry>,
        basePath: String,
        relativePath: String
    ) {
        append("<div class=\"file-grid grid\" id=\"fileContainer\">")

        if (relativePath.isNotEmpty()) {
            val parentPath = relativePath.substringBeforeLast("/", "")
            val parentUrl = if (parentPath.isEmpty()) basePath else "$basePath/$parentPath"
            append("<a class=\"file-item\" href=\"$parentUrl/\" data-name=\"..\" data-size=\"0\" data-date=\"0\" data-dir=\"true\">")
            append("<span class=\"file-icon\">")
            append(svgIcon(SVG_ARROW_UP, "#00D4AA"))
            append("</span>")
            append("<span class=\"file-name dir-name\">..</span>")
            append("<span class=\"file-meta\">Parent folder</span>")
            append("</a>")
        }

        files.forEach { file ->
            val fileUrl = if (relativePath.isEmpty()) {
                "$basePath/${file.name}"
            } else {
                "$basePath/$relativePath/${file.name}"
            }
            val href = if (file.isDirectory) "$fileUrl/" else fileUrl
            val icon = fileIcon(file.name, file.isDirectory)
            val nameClass = if (file.isDirectory) "file-name dir-name" else "file-name"
            val displaySize = if (file.isDirectory) "—" else formatFileSize(file.size)
            val displayDate = formatDate(file.lastModified)

            append("<a class=\"file-item\" href=\"")
            appendHtmlEscaped(href)
            append("\" data-name=\"")
            appendHtmlAttrEscaped(file.name.lowercase())
            append("\" data-size=\"${file.size}\" data-date=\"${file.lastModified}\" data-dir=\"${file.isDirectory}\">")
            append("<span class=\"file-icon\">$icon</span>")
            append("<span class=\"$nameClass\">")
            appendHtmlEscaped(if (file.isDirectory) "${file.name}/" else file.name)
            append("</span>")
            append("<span class=\"file-meta\">$displaySize</span>")
            append("<span class=\"file-date\">$displayDate</span>")
            append("</a>")
        }

        if (files.isEmpty() && relativePath.isEmpty()) {
            append("<div class=\"empty\">This folder is empty</div>")
        }

        append("</div>")
    }

    private fun StringBuilder.appendScripts(allowUpload: Boolean, currentPath: String) {
        append("""<script>
function setView(mode){
var c=document.getElementById('fileContainer');
var gb=document.getElementById('gridBtn');
var lb=document.getElementById('listBtn');
if(mode==='list'){c.className='file-list list';gb.classList.remove('active');lb.classList.add('active')}
else{c.className='file-grid grid';gb.classList.add('active');lb.classList.remove('active')}
localStorage.setItem('viewMode',mode)}
function sortFiles(by){
var c=document.getElementById('fileContainer');
var items=Array.from(c.querySelectorAll('.file-item'));
items.sort(function(a,b){
var ad=a.dataset.dir==='true',bd=b.dataset.dir==='true';
if(a.dataset.name==='..')return -1;if(b.dataset.name==='..')return 1;
if(ad!==bd)return ad?-1:1;
if(by==='size')return parseInt(a.dataset.size)-parseInt(b.dataset.size);
if(by==='date')return parseInt(b.dataset.date)-parseInt(a.dataset.date);
return a.dataset.name.localeCompare(b.dataset.name)});
items.forEach(function(i){c.appendChild(i)})}
document.getElementById('search').addEventListener('input',function(e){
var q=e.target.value.toLowerCase();
document.querySelectorAll('.file-item').forEach(function(i){
if(i.dataset.name==='..')return;
i.style.display=i.dataset.name.includes(q)?'':'none'})});
var saved=localStorage.getItem('viewMode');if(saved)setView(saved);
""")
        if (allowUpload) {
            append("""
var zone=document.getElementById('uploadZone');
var input=document.getElementById('uploadInput');
var prog=document.getElementById('uploadProgress');
var fill=document.getElementById('progressFill');
var ptext=document.getElementById('progressText');
zone.addEventListener('click',function(){input.click()});
zone.addEventListener('dragover',function(e){e.preventDefault();zone.classList.add('drag-over')});
zone.addEventListener('dragleave',function(){zone.classList.remove('drag-over')});
zone.addEventListener('drop',function(e){e.preventDefault();zone.classList.remove('drag-over');uploadFiles(e.dataTransfer.files)});
input.addEventListener('change',function(){if(input.files.length)uploadFiles(input.files)});
function uploadFiles(fileList){
var fd=new FormData();
for(var i=0;i<fileList.length;i++)fd.append('files',fileList[i],fileList[i].name);
var xhr=new XMLHttpRequest();
prog.style.display='block';fill.style.width='0%';ptext.textContent='Uploading…';
xhr.upload.addEventListener('progress',function(e){if(e.lengthComputable){var p=Math.round(e.loaded/e.total*100);fill.style.width=p+'%';ptext.textContent='Uploading… '+p+'%'}});
xhr.addEventListener('load',function(){if(xhr.status>=200&&xhr.status<300){ptext.textContent='Upload complete!';setTimeout(function(){location.reload()},800)}else{ptext.textContent='Upload failed: '+xhr.statusText;fill.style.width='100%';fill.style.background='#EF4444'}});
xhr.addEventListener('error',function(){ptext.textContent='Upload failed';fill.style.background='#EF4444'});
xhr.open('POST','""")
            appendJsStringEscaped(currentPath)
            append("""',true);xhr.send(fd)}
""")
        }
        append("</script>")
    }

    private fun fileIcon(name: String, isDirectory: Boolean): String {
        if (isDirectory) return svgIcon(SVG_FOLDER, "#00D4AA")
        val color = "#6B6B76"
        return when (name.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp", "ico" -> svgIcon(SVG_IMAGE, "#A78BFA")
            "mp4", "avi", "mov", "mkv", "webm" -> svgIcon(SVG_VIDEO, "#A78BFA")
            "mp3", "wav", "flac", "aac", "ogg" -> svgIcon(SVG_AUDIO, "#A78BFA")
            "pdf" -> svgIcon(SVG_FILE, "#EF4444")
            "doc", "docx", "odt", "rtf", "txt" -> svgIcon(SVG_FILE_TEXT, "#00D4AA")
            "xls", "xlsx", "csv", "ods" -> svgIcon(SVG_FILE, "#00D4AA")
            "ppt", "pptx", "odp" -> svgIcon(SVG_FILE, "#F59E0B")
            "zip", "rar", "7z", "tar", "gz", "bz2" -> svgIcon(SVG_ARCHIVE, "#F59E0B")
            "html", "htm", "xml", "xhtml" -> svgIcon(SVG_CODE, "#00D4AA")
            "css", "scss", "less" -> svgIcon(SVG_CODE, "#A78BFA")
            "js", "ts", "jsx", "tsx" -> svgIcon(SVG_CODE, "#F59E0B")
            "kt", "java", "py", "rb", "go", "rs", "c", "cpp", "h", "swift" -> svgIcon(SVG_CODE, "#00D4AA")
            "json", "yaml", "yml", "toml", "ini", "conf" -> svgIcon(SVG_CONFIG, color)
            "md", "markdown" -> svgIcon(SVG_FILE_TEXT, color)
            "apk", "exe", "dmg", "msi", "deb" -> svgIcon(SVG_ARCHIVE, color)
            "sql", "db", "sqlite" -> svgIcon(SVG_CONFIG, color)
            "sh", "bash", "zsh", "bat", "ps1" -> svgIcon(SVG_CODE, color)
            else -> svgIcon(SVG_FILE, color)
        }
    }

    private fun svgIcon(path: String, color: String = "currentColor"): String =
        "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"$color\" stroke-width=\"1.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\">$path</svg>"

    private const val SVG_FOLDER = "<path d=\"M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z\"/>"
    private const val SVG_FILE = "<path d=\"M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z\"/><polyline points=\"14 2 14 8 20 8\"/>"
    private const val SVG_FILE_TEXT = "<path d=\"M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z\"/><polyline points=\"14 2 14 8 20 8\"/><line x1=\"16\" y1=\"13\" x2=\"8\" y2=\"13\"/><line x1=\"16\" y1=\"17\" x2=\"8\" y2=\"17\"/>"
    private const val SVG_IMAGE = "<rect x=\"3\" y=\"3\" width=\"18\" height=\"18\" rx=\"2\" ry=\"2\"/><circle cx=\"8.5\" cy=\"8.5\" r=\"1.5\"/><polyline points=\"21 15 16 10 5 21\"/>"
    private const val SVG_VIDEO = "<polygon points=\"23 7 16 12 23 17 23 7\"/><rect x=\"1\" y=\"5\" width=\"15\" height=\"14\" rx=\"2\" ry=\"2\"/>"
    private const val SVG_AUDIO = "<path d=\"M9 18V5l12-2v13\"/><circle cx=\"6\" cy=\"18\" r=\"3\"/><circle cx=\"18\" cy=\"16\" r=\"3\"/>"
    private const val SVG_CODE = "<polyline points=\"16 18 22 12 16 6\"/><polyline points=\"8 6 2 12 8 18\"/>"
    private const val SVG_ARCHIVE = "<path d=\"M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z\"/>"
    private const val SVG_CONFIG = "<circle cx=\"12\" cy=\"12\" r=\"3\"/><path d=\"M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z\"/>"
    private const val SVG_UPLOAD = "<path d=\"M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4\"/><polyline points=\"17 8 12 3 7 8\"/><line x1=\"12\" y1=\"3\" x2=\"12\" y2=\"15\"/>"
    private const val SVG_ARROW_UP = "<line x1=\"12\" y1=\"19\" x2=\"12\" y2=\"5\"/><polyline points=\"5 12 12 5 19 12\"/>"
    private const val SVG_GRID = "<rect x=\"3\" y=\"3\" width=\"7\" height=\"7\"/><rect x=\"14\" y=\"3\" width=\"7\" height=\"7\"/><rect x=\"3\" y=\"14\" width=\"7\" height=\"7\"/><rect x=\"14\" y=\"14\" width=\"7\" height=\"7\"/>"
    private const val SVG_LIST = "<line x1=\"8\" y1=\"6\" x2=\"21\" y2=\"6\"/><line x1=\"8\" y1=\"12\" x2=\"21\" y2=\"12\"/><line x1=\"8\" y1=\"18\" x2=\"21\" y2=\"18\"/><line x1=\"3\" y1=\"6\" x2=\"3.01\" y2=\"6\"/><line x1=\"3\" y1=\"12\" x2=\"3.01\" y2=\"12\"/><line x1=\"3\" y1=\"18\" x2=\"3.01\" y2=\"18\"/>"

    private fun formatFileSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "—"
        val sdf = java.text.SimpleDateFormat("MMM d, yyyy HH:mm", java.util.Locale.US)
        return sdf.format(java.util.Date(timestamp))
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
