import Vue from "vue";
import Route from "./Route";
import axios from "axios";
import { Notification } from "element-ui";

axios.interceptors.request.use(
  function(config) {
    config.headers = {
      ...config.headers,
      "X-CLOUD-TOKEN": localStorage.getItem("X-CLOUD-TOKEN"),
    };
    return config;
  },
  function(error) {
    // 对请求错误做些什么
    return Promise.reject(error);
  }
);
axios.interceptors.response.use(
  (response) => {
    if (response.status === 200) {
      if (response.data.code === 401) {
        Route.push({ path: "/login" });
      } else if (response.data.code != 0 && response.data.code != 1000001) {
        Notification.error({
          title: "错误",
          duration: 3000,
          message: `请求出错[${response.data.code}]:${response.data.message}`,
        });
      }
    }
    return response;
  },
  function(error) {
    Notification.error({
      title: "错误",
      duration: 3000,
      message: `请求出现未知错误`,
    });
    return Promise.reject(error);
  }
);
Vue.mixin({
  created() {
    axios.defaults.headers.post["Content-Type"] =
      "application/x-www-form-urlencoded";
  },
  data() {
    return {
      base_api_uri:
        process.env.NODE_ENV === "production" ? "." : "//127.0.0.1:8080",
      base_image_uri: "",
    };
  },
  methods: {
    axios_get(uri) {
      return axios.get(`${this.base_api_uri}${uri}`);
    },
    post_data(uri, data) {
      return axios({
        url: `${this.base_api_uri}${uri}`,
        method: "POST",
        data: data,
        transformRequest: [
          function(data) {
            let ret = "";
            for (let it in data) {
              // 如果要发送中文 编码
              ret +=
                encodeURIComponent(it) +
                "=" +
                encodeURIComponent(data[it]) +
                "&";
            }
            return ret;
          },
        ],
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
      });
    },
    setToken(token) {
      localStorage.setItem("X-CLOUD-TOKEN", token);
    },
    getToken() {
      return localStorage.getItem("X-CLOUD-TOKEN");
    },
    parse_date(date, fmt = "yyyy-MM-dd hh:mm:ss") {
      if (!date) {
        return "";
      }
      date = new Date(Number(date));
      let o = {
        "M+": date.getMonth() + 1, // 月份
        "d+": date.getDate(), // 日
        "h+": date.getHours(), // 小时
        "m+": date.getMinutes(), // 分
        "s+": date.getSeconds(), // 秒
        "q+": Math.floor((date.getMonth() + 3) / 3), // 季度
        S: date.getMilliseconds(), // 毫秒
      };
      if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(
          RegExp.$1,
          (date.getFullYear() + "").substr(4 - RegExp.$1.length)
        );
      }
      for (let k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
          fmt = fmt.replace(
            RegExp.$1,
            RegExp.$1.length === 1
              ? o[k]
              : ("00" + o[k]).substr(("" + o[k]).length)
          );
        }
      }
      return fmt;
    },
    parse_memory_capacity(memory) {
      if (memory >= 1024 * 1024) {
        return (memory / 1024 / 1024).toFixed(0) + "GB";
      } else if (memory >= 1024) {
        return (memory / 1024).toFixed(0) + "MB";
      } else {
        return memory + "KBytes";
      }
    },
    parse_disk_capacity(disk) {
      if (disk > 1024 * 1024 * 1024 * 1024) {
        return (disk / 1024 / 1024 / 1024 / 1024).toFixed(2) + "TB";
      } else if (disk > 1024 * 1024 * 1024) {
        return (disk / 1024 / 1024 / 1024).toFixed(2) + "GB";
      } else if (disk > 1024 * 1024) {
        return (disk / 1024 / 1024).toFixed(2) + "MB";
      } else if (disk > 1024) {
        return (disk / 1024).toFixed(2) + "KB";
      } else {
        return disk + "B";
      }
    },
  },
});
