import websocket from "./websocket";
export default {
  data() {
    return {};
  },
  methods: {
    init_notify() {
      websocket.init(this);
    },
    subscribe_notify(key, func) {
      websocket.notify_list.push({ k: key, n: func });
    },
    unsubscribe_notify(key) {
      let findIndex = websocket.notify_list.findIndex((v) => v.k === key);
      if (findIndex >= 0) {
        websocket.notify_list.splice(findIndex, 1);
      }
    },

    subscribe_connect_notify(key, func) {
      websocket.connect_notify_list.push({ k: key, n: func });
    },
    unsubscribe_connect_notify(key) {
      let findIndex = websocket.connect_notify_list.findIndex(
        (v) => v.k === key
      );
      if (findIndex >= 0) {
        websocket.connect_notify_list.splice(findIndex, 1);
      }
    },
    handle_notify_message(msg) {
      websocket.notify_list.forEach((obj) => {
        obj.n(msg);
      });
    },
    handle_connect() {
      websocket.connect_notify_list.forEach((obj) => {
        obj.n();
      });
    },
  },
};
