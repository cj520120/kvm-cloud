class NotifyWebsocket {
  static instance = undefined;
  init(pThis) {
    if (!NotifyWebsocket.instance) {
      let wsUri =
        process.env.NODE_ENV === "production"
          ? `ws://${window.location.host}/api/ws/`
          : `ws://192.168.2.107:8080/api/ws/`;
      NotifyWebsocket.instance = new WebSocket(wsUri);
      NotifyWebsocket.instance.onopen = function () {};
      NotifyWebsocket.instance.onclose = function () {
        NotifyWebsocket.instance = undefined;
        setTimeout(() => {
          NotifyWebsocket.init(pThis);
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
