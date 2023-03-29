import websocket from "./websocket";
export default {
  data() {
    return {};
  },
  methods: {
    init_notify() {
      websocket.init(this);
    },
    handle_notify_message(msg) {
      console.log(msg);
    },
  },
};
