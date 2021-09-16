import Vue from "vue";
import './global.js'

Vue.mixin({
    data(){
      return {
          all_groups:[],
          all_category:[],
          all_scheme:[],
          all_volume:[],
          all_instance:[],
          all_storage:[],
          all_cluster:[],
          all_network:[],
          all_template:[],
          all_host:[],
          all_users:[],
          all_rules:[],
          all_permission:[],
          all_permission_category:[],
      }
    },
    methods: {
        load_groups(){
            return this.axios_get('/management/group').then(res=>{
                if(res.data.code===0&&res.data.data){
                    this.all_groups=res.data.data
                }
            })
        },
        load_category(){
            return this.axios_get('/management/os/category').then(res=>{
                if(res.data.code===0&&res.data.data){
                    this.all_category=res.data.data
                }
            })
        },
        load_scheme(){
            return this.axios_get('/management/calculation/scheme').then(res=>{
                if(res.data.code===0&&res.data.data){
                    this.all_scheme=res.data.data
                }
            })
        },
        load_volume(){
            return this.axios_get('/management/volume').then(res=>{
                if(res.data.code===0&&res.data.data){
                    this.all_volume=res.data.data
                }
            })
        },
        load_instance(){
            return this.axios_get('/management/vm').then(res=>{
                if(res.data.code===0&&res.data.data){
                    this.all_instance=res.data.data
                }
            })
        },
        load_storage(){
            return this.axios_get('/management/storage').then(res=>{
                if(res.data.code===0&&res.data.data){
                    this.all_storage=res.data.data
                }
            })
        },
        load_cluster(){
            return this.axios_get('/management/cluster').then(res=>{
                if(res.data.code===0&&res.data.data){
                    this.all_cluster=res.data.data
                }
            })
        },
        load_network(){
            return this.axios_get('/management/network').then(res=>{
                if(res.data.code===0&&res.data.data){
                    this.all_network=res.data.data
                }
            })
        },
        load_template(){
            return  this.axios_get('/management/template').then(res=>{
                if(res.data.code===0&&res.data.data){
                    this.all_template=res.data.data
                }
            })
        },
        load_host(){
            return this.axios_get('/management/host').then(res=>{
                if(res.data.code===0&&res.data.data){
                    this.all_host=res.data.data
                }
            })
        },
        load_users(){
            return this.axios_get('/management/user/list').then(res=>{
                if(res.data.code===0&&res.data.data){
                    this.all_users=res.data.data
                }
            })
        },
        load_rules(){
            return this.axios_get('/management/rules').then(res=>{
                if(res.data.code===0&&res.data.data){
                    this.all_rules=res.data.data
                }
            })
        },
        load_permission(){
            return this.axios_get('/management/rules/permission').then(res=>{
                if(res.data.code===0&&res.data.data){
                    this.all_permission=res.data.data
                }
            })
        },
        load_permission_category(){
            return this.axios_get('/management/rules/category').then(res=>{
                if(res.data.code===0&&res.data.data){
                    this.all_permission_category=res.data.data
                }
            })
        },
        get_host_name_by_id(hostId){
            let host = this.all_host;
            for (let idx in host) {
                if (host[idx].id === hostId) {
                    return host[idx].name
                }
            }
            return "--"
        },
        get_category_name_by_id(categoryId){
            let category = this.all_category;
            for (let idx in category) {
                if (category[idx].id === categoryId) {
                    return category[idx].categoryName
                }
            }
            return "--"
        },
        get_cluster_name_by_id(clusterId){
            let cluster = this.all_cluster;
            for (let idx in cluster) {
                if (cluster[idx].id === clusterId) {
                    return cluster[idx].name
                }
            }
            return "--"
        },
        get_group_name_by_id(groupId){
            let groups = this.all_groups;
            for (let idx in groups) {
                if (groups[idx].id === groupId) {
                    return groups[idx].name
                }
            }
            return "默认"
        },
        get_storage_name_by_id(storageId){
            let storage = this.all_storage;
            for (let idx in storage) {
                if (storage[idx].id === storageId) {
                    return storage[idx].name
                }
            }
            return "--"
        },
        get_volume_path(volume){
            let storage = this.all_storage;
            for (let idx in storage) {
                if (storage[idx].id === volume.storageId) {
                    return `/mnt/${storage[idx].target}/${volume.target}`
                }
            }
            return "--"
        },
        get_template_name_by_id(templateId){
            let template = this.all_template;
            for (let idx in template) {
                if (template[idx].id === templateId) {
                    return template[idx].name
                }
            }
            return "--"
        },
        get_scheme_name_by_id(schemeId){
            let scheme = this.all_scheme;
            for (let idx in scheme) {
                if (scheme[idx].id === schemeId) {
                    return scheme[idx].name
                }
            }
            return "--"
        },
        get_instance_name_by_id(instanceId){
            let instance = this.all_instance;
            for (let idx in instance) {
                if (instance[idx].id === instanceId) {
                    return instance[idx].description
                }
            }
            return "--"
        },
        get_rule_name_by_id(ruleId){
            let rules = this.all_rules;
            for (let idx in rules) {
                if (rules[idx].id === ruleId) {
                    return rules[idx].name
                }
            }
            return "--"
        }
    }
})