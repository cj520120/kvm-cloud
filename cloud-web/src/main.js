import Vue from 'vue'
import Route from "./Route";
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import App from './App.vue'
import './global'
import './Data'
Vue.use(ElementUI);
Vue.config.productionTip = false
new Vue({
  router: Route,
  render: h => h(App),
}).$mount('#app')
