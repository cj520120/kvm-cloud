import Route from "../router/index";
import { getSystemConfig } from "./api";
class NotifyWebsocket {
  static instance = undefined;
  notify_list = [];
  connect_notify_list = [];
  static base_ws_url = undefined;
  static is_init = false;
  async init(pThis, is_success_callback) {
    let pThat = this;
    if (!is_success_callback) {
      if (NotifyWebsocket.is_init) {
        return;
      }
      NotifyWebsocket.is_init = true;
      if (!NotifyWebsocket.base_ws_url) {
        console.log(new Date(), "等待获取服务器基础地址...");
        getSystemConfig()
          .then((response) => {
            if (response.data.baseUri) {
              NotifyWebsocket.base_ws_url = response.data.baseUri;
            } else {
              NotifyWebsocket.base_ws_url = "/";
            }
          })
          .finally(() => {
            if (NotifyWebsocket.base_ws_url) {
              pThat.init(pThis, true);
            } else {
              NotifyWebsocket.is_init = false;
              setTimeout(() => {
                NotifyWebsocket.is_init = false;
                pThat.init(pThis);
              }, 3000);
            }
          });
        return;
      }
    }
    if (!NotifyWebsocket.instance) {
      let protocol;
      if (window.location.protocol === "https:") {
        protocol = "wss";
      } else {
        protocol = "ws";
      }
      let wsUri;
      if (
        NotifyWebsocket.base_ws_url.startsWith("ws://") ||
        NotifyWebsocket.base_ws_url.startsWith("wss://")
      ) {
        wsUri = `${NotifyWebsocket.base_ws_url}api/ws/`;
      } else {
        wsUri =
          process.env.NODE_ENV === "production"
            ? `${protocol}://${window.location.host}${NotifyWebsocket.base_ws_url}api/ws/`
            : `${protocol}://localhost:8080/api/ws/`;
      }
      console.log(new Date(), "开始连接通信服务器...", wsUri);
      NotifyWebsocket.instance = new WebSocket(wsUri);
      NotifyWebsocket.instance.onopen = function () {
        console.log(new Date(), "通信服务器连接成功,发送认证信息");
        let connect_data = {
          command: 100,
          data: {
            token: localStorage.getItem("X-Token"),
          },
        };
        NotifyWebsocket.instance.send(JSON.stringify(connect_data));
      };
      NotifyWebsocket.instance.onerror = function () {
        console.log(new Date(), "出现错误，断开链接，重新开始连接...");
        NotifyWebsocket.instance.close();
        NotifyWebsocket.instance = undefined;
        setTimeout(() => {
          pThat.init(pThis);
        }, 3000);
      };
      NotifyWebsocket.instance.onclose = function () {
        console.log(new Date(), "断开链接,等待链接...");
        NotifyWebsocket.instance = undefined;
        setTimeout(() => {
          pThat.init(pThis);
        }, 3000);
      };
      NotifyWebsocket.instance.onmessage = function (event) {
        if (event.data) {
          let wsMessage = JSON.parse(event.data);
          if (wsMessage.command == 101) {
            console.log(new Date(), "WebSocket 登录认证成功.");
            pThis.handle_connect();
          } else if (wsMessage.command == 102) {
            console.log(new Date(), "WebSocket 登录认证Token错误.");
            let hrefHash = window.location.hash.toLowerCase();
            if (hrefHash && !hrefHash.startsWith("#/login")) {
              localStorage.setItem("X-Back", window.location.href);
            }
            Route.push({ path: "/login" });
          } else if (wsMessage.command == 103) {
            pThis.handle_notify_message(wsMessage.data);
          }
        }
      };
    }
  }
}
export default new NotifyWebsocket();
