import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";

import ElementUI from "element-ui";
import "element-ui/lib/theme-chalk/index.css";
import "@/assets/css/style.css";
Vue.use(ElementUI);
Vue.config.productionTip = false;
Vue.prototype.$websocket = undefined;
new Vue({
  router,
  store,
  render: (h) => h(App),
}).$mount("#app");
