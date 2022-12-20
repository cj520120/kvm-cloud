import Vue from "vue";
import VueRouter from "vue-router";
import NetworkView from "../views/NetworkView.vue";
import HostView from "../views/HostView.vue";
import StorageView from "../views/StorageView.vue";
import TemplateView from "../views/TemplateView.vue";
import SnapshotView from "../views/SnapshotView.vue";
import VolumeView from "../views/VolumeView.vue";
import SchemeView from "../views/SchemeView.vue";
Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "Network",
    component: NetworkView,
  },
  {
    path: "/Network",
    name: "Network",
    component: NetworkView,
  },
  {
    path: "/Host",
    name: "Host",
    component: HostView,
  },
  {
    path: "/Storage",
    name: "Storage",
    component: StorageView,
  },
  {
    path: "/Template",
    name: "Template",
    component: TemplateView,
  },
  {
    path: "/Snapshot",
    name: "Snapshot",
    component: SnapshotView,
  },
  {
    path: "/Volume",
    name: "Volume",
    component: VolumeView,
  },
  {
    path: "/Scheme",
    name: "Scheme",
    component: SchemeView,
  },
];

const router = new VueRouter({
  routes,
});

export default router;
