import axios from "axios";
import Qs from "qs";
import Route from "../router/index";
const configs = require("./config");
class HttpRequest {
  constructor() {
    this.baseUrl = "";
  }

  getInsideConfig() {
    const config = {
      baseURL: configs.BASEURL,
      headers: {
        "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
        "X-Token": localStorage.getItem("X-Token"),
      },
    };
    return config;
  }

  destroy(url) {
    delete this.queue[url];
  }

  interceptors(instance, url) {
    // 请求拦截
    instance.interceptors.request.use(
      (config) => {
        if (config.method.toLowerCase() !== "get") {
          config.data = Qs.stringify(config.data);
        } else {
          config.data = true;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );
    // 响应拦截
    instance.interceptors.response.use(
      (res) => {
        const { data } = res;
        if (data.code === 401) {
          let hrefHash = window.location.hash.toLowerCase();
          if (hrefHash && !hrefHash.startsWith("#/login")) {
            localStorage.setItem("X-Back", window.location.href);
          }
          Route.push({ path: "/login" });
          return res.data;
        } else {
          return res.data;
        }
      },
      (error) => {
        this.destroy(url);
        const errorInfo = error.response;
        if (!errorInfo) {
          return Promise.reject(error);
        } else {
          const { status } = errorInfo;
          let errorMessage = null;
          switch (status) {
            case 400:
              errorMessage = "请求参数错误";
              break;
            case 401:
              errorMessage = "未授权或token过期，请登录";
              break;
            case 413:
              errorMessage = "资源大小超过限制";
              break;
            case 403:
              errorMessage = "跨域访问已被拒绝";
              break;
            case 404:
              errorMessage = `请求地址出错: ${errorInfo.config.url}`;
              break;
            case 408:
              errorMessage = "请求超时";
              break;
            case 405:
              errorMessage = "请求方法不正确";
              break;
            case 500:
              errorMessage = "服务器内部错误";
              break;
            case 501:
              errorMessage = "服务器未实现";
              break;
            case 502:
              errorMessage = "网关错误";
              break;
            case 503:
              errorMessage = "该服务不可用";
              break;
            case 504:
              errorMessage = "网关超时";
              break;
            case 505:
              errorMessage = "HTTP版本不受支持";
              break;
            default:
              errorMessage = `未知错误status:${status}`;
          }
          //   Notification({
          //     title: "错误通知",
          //     type: "error",
          //     message: errorMessage,
          //   });
          console.log(errorMessage);
          return Promise.reject(error);
        }
      }
    );
  }

  request(options) {
    // this.options = options
    const instance = axios.create();
    const _options = Object.assign({}, options, this.getInsideConfig());
    this.interceptors(instance, _options.url);
    return instance(_options);
  }
}
export default new HttpRequest();
