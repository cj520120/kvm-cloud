class NotifyWebsocket {
  static instance = undefined;
  init(pThis) {
    let pThat = this;
    if (!NotifyWebsocket.instance) {
      let wsUri =
        process.env.NODE_ENV === "production"
          ? `ws://${window.location.host}/api/ws/`
          : `ws://192.168.2.107:8080/api/ws/`;
      console.log(new Date(), "开始连接通信服务器...");
      NotifyWebsocket.instance = new WebSocket(wsUri);
      NotifyWebsocket.instance.onopen = function () {
        console.log(new Date(), "通信服务器连接成功");
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
    }
    NotifyWebsocket.instance.onmessage = function (event) {
      if (event.data) {
        pThis.handle_notify_message(JSON.parse(event.data));
      }
    };
  }
}
export default new NotifyWebsocket();
