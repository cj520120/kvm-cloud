export default {
  data() {
    return {
      notify_web_socket: undefined,
    };
  },
  methods: {
    init_notify() {
      if ("WebSocket" in window) {
        let that = this;
        if (!this.notify_web_socket) {
          this.notify_web_socket = new WebSocket(`ws://127.0.0.1:8080/api/ws/`);
          this.notify_web_socket.onopen = function () {};
          this.notify_web_socket.onclose = function () {
            that.notify_web_socket = undefined;
            that.init_notify();
          };
          this.notify_web_socket.onmessage = function (event) {
            if (event.data) {
              that.handle_notify_message(JSON.parse(event.data));
            }
          };
        }
      }
    },
    handle_notify_message(msg) {
      console.log(msg);
    },
  },
};
