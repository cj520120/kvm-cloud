import Vue from 'vue'
import Login from "./components/Login"
import Group from "./components/group/Group";
import Category from "./components/category/Category";
import Scheme from "./components/scheme/Scheme";
import Volume from "./components/volume/Volume";
import Storage from "./components/storage/Storage";
import Template from "./components/template/Template";
import Host from "./components/host/Host";
import Cluster from "./components/cluster/Cluster";
import Network from "./components/network/Network";
import User from "./components/user/User";
import Instance from "./components/instance/Instance";
import Vnc from "./components/instance/Vnc";
import VueRouter from 'vue-router'

const locationPush = VueRouter.prototype.push
VueRouter.prototype.push = function (location) {
    return locationPush.call(this, location).catch(err => {
        return new Error(err)
    })
}
Vue.use(VueRouter)

const routes = [
    {path: '/login', component: Login},
    {path: '/Group', component: Group},
    {path: '/Category', component: Category},
    {path: '/Scheme', component: Scheme},
    {path: '/Storage', component: Storage},
    {path: '/Volume', component: Volume},
    {path: '/Template', component: Template},
    {path: '/Host', component: Host},
    {path: '/Cluster', component: Cluster},
    {path: '/Network', component: Network},
    {path: '/User', component: User},
    {path: '/Instance', component: Instance},
    {path: '/Vnc', component: Vnc},
    {path: '/', component: Instance}
]

const router = new VueRouter({
    routes
})

export default router