window.getToken = () => {
    return localStorage.getItem("X_CLOUD_TOKEN");
}
window.dateFormat = (fmt, date) => {
    let ret;
    const opt = {
        "Y+": date.getFullYear().toString(),        // 年
        "m+": (date.getMonth() + 1).toString(),     // 月
        "d+": date.getDate().toString(),            // 日
        "H+": date.getHours().toString(),           // 时
        "M+": date.getMinutes().toString(),         // 分
        "S+": date.getSeconds().toString()          // 秒
        // 有其他格式化字符需求可以继续添加，必须转化成字符串
    };
    for (let k in opt) {
        ret = new RegExp("(" + k + ")").exec(fmt);
        if (ret) {
            fmt = fmt.replace(ret[1], (ret[1].length == 1) ? (opt[k]) : (opt[k].padStart(ret[1].length, "0")))
        }
    }
    return fmt
}

window.verify_number = (value) => {
    value = value.replace(/[^\d]/g, '').replace(/^0{1,}/g, '');
    if (value != '')
        value = parseFloat(value).toFixed(0);
    else
        value = parseFloat(0).toFixed(0);
    return value;
}

window.go_login_page = () => {
    window.parent.location.href = "../login.html";
}
window.formArray2Data = function (array = []) {
    const data = {};
    for (let i = 0; i < array.length; i++) {
        data[array[i].name] = array[i].value;
    }
    return data;
};
window.getUrlParam = (name) => {
    const reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    const r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]);
    return null;
}

