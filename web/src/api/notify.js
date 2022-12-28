import websocket from "./websocket";
export default {
  data() {
    return {};
  },
  methods: {
    init_notify() {
      websocket.init(this);
      // let that = this;
      // if ("WebSocket" in window) {
      //   console.log(this.ws, "----");
      //   if (!window.ws) {
      //     window.ws = new WebSocket(`ws://127.0.0.1:8080/api/ws/`);
      //     window.ws.onopen = function () {};
      //     window.ws.onclose = function () {
      //       window.ws = undefined;
      //       setTimeout(() => {
      //         that.init_notify();
      //       }, 3000);
      //     };
      //     window.ws.onmessage = function (event) {
      //       if (event.data) {
      //         that.handle_notify_message(JSON.parse(event.data));
      //       }
      //     };
      //   } else {
      //     window.ws.onmessage = function (event) {
      //       if (event.data) {
      //         that.handle_notify_message(JSON.parse(event.data));
      //       }
      //     };
      //   }
      // }
    },
    handle_notify_message(msg) {
      console.log(msg);
    },
  },
};
