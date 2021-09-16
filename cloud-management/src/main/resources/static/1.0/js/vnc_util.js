import RFB from '../novnc/core/rfb.js';

function connectedToServer(e) {
    document.getElementById('status').textContent ="已连接到【" + window.vmName+"】";
}
function disconnectedFromServer(e) {
    document.getElementById('status').textContent ="远程连接已断开" ;
}
function credentialsAreRequired(e) {
    const password = get_vnc_password();
    if(password){
        window.rfb.sendCredentials({ password:  password});
    }

}
window.sendCtrlAltDel=()=> {
    window.rfb.sendCtrlAltDel();
    return false;
}
function get_vnc_password(){
    let data=new data_handler({uri:config_util.base_uri+`/management/vm/vnc?id=${window.vmId}`}).load_data();
    return data.password;
}

config_util.init(() => {
    window.vmId = getUrlParam("id");
    window.vmName = decodeURI(getUrlParam("name"))
    document.title = window.vmName
    document.getElementById('status').textContent = "正在连接...";
    let protocol;
    if (window.location.protocol === "https:") {
        protocol = 'wss';
    } else {
        protocol = 'ws';
    }
    let url = protocol + "://" + window.location.host + '/vnc/connect/' + window.vmId;
    window.rfb = new RFB(document.getElementById('screen'), url,{ credentials: { password: undefined} });
    window.rfb.addEventListener("connect",  connectedToServer);
    window.rfb.addEventListener("disconnect", disconnectedFromServer);
    window.rfb.addEventListener("credentialsrequired", credentialsAreRequired);
});