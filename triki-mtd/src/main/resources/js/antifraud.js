function uuidv4() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

function setAntiFraudHeaders() {
    var hmrcGuid = localStorage.getItem('hmrc-guid');
    if(hmrcGuid == null){
        hmrcGuid = uuidv4();
        localStorage.setItem('hmrc-guid', hmrcGuid);
    }

    let antifraudHeaders = {}
    antifraudHeaders["Gov-Client-Device-ID"] = hmrcGuid
    antifraudHeaders["Gov-Client-Timezone"] = Intl.DateTimeFormat().resolvedOptions().timeZone
    antifraudHeaders["Gov-Client-Browser-JS-User-Agent"] = window.navigator.userAgent
    antifraudHeaders["Gov-Client-Browser-Plugins"] = navigator.plugins

    if(document.getElementById('hmrcHeaders') != null) {
        document.getElementById('hmrcHeaders').value = JSON.stringify(antifraudHeaders)
    }
}
