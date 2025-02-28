import Vue from "vue";
import VueRouter from "vue-router";
import NetworkView from "../views/NetworkView.vue";
import HostView from "../views/HostView.vue";
import StorageView from "../views/StorageView.vue";
import TemplateView from "../views/TemplateView.vue";
import VolumeView from "../views/VolumeView.vue";
import SchemeView from "../views/SchemeView.vue";
import GroupView from "../views/GroupView.vue";
import GuestView from "../views/GuestView.vue";
import VncView from "../views/VncView.vue";
import HomeView from "../views/HomeView.vue";
import UserView from "../views/UserView.vue";
import LoginView from "../views/LoginView.vue";
import SshView from "@/views/SshView.vue";
import ConfigView from "@/views/ConfigView.vue";
Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "Home",
    component: HomeView,
  },
  {
    path: "/Vnc",
    name: "Vnc",
    component: VncView,
  },
  {
    path: "/Guest",
    name: "Guest",
    component: GuestView,
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
    path: "/Volume",
    name: "Volume",
    component: VolumeView,
  },
  {
    path: "/Ssh",
    name: "Ssh",
    component: SshView,
  },
  {
    path: "/Scheme",
    name: "Scheme",
    component: SchemeView,
  },
  {
    path: "/Group",
    name: "Group",
    component: GroupView,
  },
  {
    path: "/User",
    name: "User",
    component: UserView,
  },
  {
    path: "/Login",
    name: "Login",
    component: LoginView,
  },
  {
    path: "/Config",
    name: "Config",
    component: ConfigView,
  },
];

const router = new VueRouter({
  routes,
});

export default router;
