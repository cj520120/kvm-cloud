<template>
  <div class="main">
    <Top/>
    <div class="container" style="display: flex">
      <Menu :index="menuIndex"/>
      <el-main>
        <el-tabs>
          <el-tab-pane label="实例管理">
            <el-row>
              <el-col :span="20">
                <div class="grid-content bg-purple-light" style="padding-top: 5px;padding-bottom: 5px">
                  <Search ref="SearchRef" @on_search="on_search_callback"/>
                  <el-button icon="el-icon-plus" plain type="primary" size="small" @click="on_create_instance_click">创建实例</el-button>&nbsp;
                  <el-button :loading="batch_start_loading" :disabled="!select_instance.length" plain size="small" type="primary" @click="on_batch_start_click">批量启动</el-button>&nbsp;
                  <el-button :disabled="!select_instance.length" plain size="small" type="primary" @click="on_batch_reboot_click">批量重启</el-button>&nbsp;
                  <el-button :disabled="!select_instance.length" plain size="small" type="danger" @click="on_batch_stop_click">批量停止</el-button>&nbsp;

                </div>
              </el-col>
              <el-col :span="4">
                <div class="grid-content bg-purple-light" style="padding-top: 5px;padding-bottom: 5px">
                  <el-input v-model="filter.key" placeholder="请输入关键字" @input="on_filter_key_change"></el-input>
                </div>
              </el-col>
            </el-row>
            <el-divider></el-divider>

            <div  >
              <el-table ref="filterTable" v-loading="loading" :data="table_instance" :expand-row-keys="expand_instance" :row-key="get_instance_id"
                  style="width: 100%" @selection-change="on_instance_selection_change">
                <el-table-column
                    type="selection"
                    width="55">
                </el-table-column>
                <el-table-column
                    label="ID"
                    prop="id"
                    width="80">
                </el-table-column>
                <el-table-column
                    label="集群"
                    prop="clusterId">
                  <template slot-scope="scope">
                    <span>{{ get_cluster_name_by_id(scope.row.clusterId) }}</span>
                  </template>
                </el-table-column>
                <el-table-column
                    label="名称"
                    prop="name">
                </el-table-column>
                <el-table-column
                    label="备注"
                    prop="description">
                </el-table-column>
                <el-table-column
                    label="IP"
                    prop="ip">
                </el-table-column>
                <el-table-column
                    label="类型"
                    prop="type">
                  <template slot-scope="scope">
                    <el-tag>{{ scope.row.type }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column
                    label="状态"
                    prop="status">
                  <template slot-scope="scope">
                    <el-tag :type="get_instance_status_tag_type(scope.row.status)">{{ scope.row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column
                    label="创建时间"
                    prop="createTime"
                    width="200">
                  <template slot-scope="scope">
                    <span>{{ parse_date(scope.row.createTime) }}</span>
                  </template>
                </el-table-column>
                <el-table-column
                    label="操作"
                    width="200">
                  <template slot-scope="scope">
                    <el-dropdown placement="bottom" size="small" trigger="click" :disabled="true" @command="on_instance_dropdown_select">
                      <span class="el-dropdown-link">
                        <el-button icon="el-icon-more" plain size="mini" type="primary" ></el-button>
                      </span>
                      <el-dropdown-menu slot="dropdown" :instance-id="scope.row.id" >
                        <el-dropdown-item :command="bind_instance_dropdown('detail',scope.row)">
                          <i class="layui-icon layui-icon-form"></i> 实例详情
                        </el-dropdown-item>
                        <el-dropdown-item  :command="bind_instance_dropdown('attach-cd',scope.row)" v-show="scope.row.status !== 'Destroy'" :disabled="!(scope.row.type === 'Guest' && !scope.row.iso)">
                          <i class="layui-icon layui-icon-link"></i> 挂载光盘
                        </el-dropdown-item>
                        <el-dropdown-item :command="bind_instance_dropdown('detach-cd',scope.row)" v-show="scope.row.status !== 'Destroy'" :disabled="!(scope.row.type === 'Guest' && scope.row.iso)">
                          <i class="layui-icon layui-icon-unlink"></i> 卸载光盘
                        </el-dropdown-item>
                        <el-dropdown-item :command="bind_instance_dropdown('vnc',scope.row)" v-show="scope.row.status !== 'Destroy'" :disabled="scope.row.status !== 'Running'">
                          <i class="layui-icon layui-icon-layer"></i> 远程桌面
                        </el-dropdown-item>
                        <el-dropdown-item :command="bind_instance_dropdown('modify',scope.row)" v-show="scope.row.status !== 'Destroy'" :disabled="!(scope.row.type === 'Guest' && scope.row.status !== 'Running')">
                          <i class="layui-icon layui-icon-set"></i> 修改实例
                        </el-dropdown-item>
                        <el-dropdown-item :command="bind_instance_dropdown('start',scope.row)" v-show="scope.row.status !== 'Destroy'" :disabled="scope.row.status !== 'Stopped'">
                          <i class="layui-icon layui-icon-play"></i> 启动实例
                        </el-dropdown-item>
                        <el-dropdown-item :command="bind_instance_dropdown('reboot',scope.row)" v-show="scope.row.status !== 'Destroy'" :disabled="scope.row.status !== 'Running'">
                          <i class="layui-icon layui-icon-refresh-3"></i> 重启实例
                        </el-dropdown-item>
                        <el-dropdown-item :command="bind_instance_dropdown('stop',scope.row)" v-show="scope.row.status  !== 'Destroy'" :disabled="!(scope.row.status === 'Running' || scope.row.status === 'Starting')">
                          <i class="layui-icon layui-icon-logout"></i> 停止实例
                        </el-dropdown-item>
                        <el-dropdown-item :command="bind_instance_dropdown('template',scope.row)" v-show="scope.row.status !== 'Destroy'" :disabled="!(scope.row.status === 'Stopped'&&scope.row.type === 'Guest')">
                          <i class="layui-icon layui-icon-export"></i> 创建模版
                        </el-dropdown-item>
                        <el-dropdown-item :command="bind_instance_dropdown('re-install',scope.row)" v-show="scope.row.status !== 'Destroy'&&scope.row.type === 'Guest'" >
                          <i class="layui-icon layui-icon-transfer"></i> 重装系统
                        </el-dropdown-item>
                        <el-dropdown-item :command="bind_instance_dropdown('metric',scope.row)" v-show="scope.row.status !== 'Destroy'">
                          <i class="layui-icon layui-icon-chart"></i> 指标监控
                        </el-dropdown-item>
                        <el-dropdown-item :command="bind_instance_dropdown('attach-network',scope.row)" v-show="scope.row.status !== 'Destroy'">
                          <i class="layui-icon layui-icon-website"></i> 附加网络
                        </el-dropdown-item>
                        <el-dropdown-item :command="bind_instance_dropdown('destroy',scope.row)" v-show="scope.row.status !== 'Destroy'">
                          <i class="layui-icon layui-icon-delete"></i> 销毁实例
                        </el-dropdown-item>
                        <el-dropdown-item :command="bind_instance_dropdown('resume',scope.row)" v-show="scope.row.status === 'Destroy'&& scope.row.type === 'Guest'">
                          <i class="layui-icon layui-icon-time"></i> 恢复实例
                        </el-dropdown-item>

                      </el-dropdown-menu>
                    </el-dropdown>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div class="block">
              <el-pagination
                  :current-page="current_page"
                  :page-size="page_size"
                  :page-sizes="[10,20,50,100, 200]"
                  :total="total_size"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="page_size_change"
                  @current-change="current_page_change">
              </el-pagination>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </div>
    <InstanceDetail ref="InstanceDetailRef"/>
    <AttachCdRoom ref="AttachCdRoomRef" @on_modify="on_instance_modify"/>
    <ModifyInstance ref="ModifyInstanceRef" @on_modify="on_instance_modify"/>
    <StartInstance ref="StartInstanceRef" @on_modify="on_instance_modify"/>
    <RebootInstance ref="RebootInstanceRef" @on_modify="on_instance_modify"/>
    <StopInstance ref="StopInstanceRef" @on_modify="on_instance_modify"/>
    <CreateTemplate ref="CreateTemplateRef" @on_modify="on_instance_modify"/>
    <ReInstall ref="ReInstallRef" @on_modify="on_instance_modify"/>
    <Metric ref="MetricRef"/>
    <AttachNetwork ref="AttachNetworkRef" @on_modify="on_instance_modify"/>
    <CreateInstance ref="CreateInstanceRef" @on_create="on_instance_create"/>
    <BatchRebootInstance ref="BatchRebootInstanceRef" @on_modify="on_instance_modify"/>
    <BatchStopInstance ref="BatchStopInstanceRef" @on_modify="on_instance_modify"/>
  </div>
</template>

<script>
import Top from "../common/Top";
import Menu from "../common/Menu"
import Search from "./Search";
import InstanceDetail from "./InstanceDetail";
import AttachCdRoom from "./AttachCdRoom"
import ModifyInstance from "./ModifyInstance";
import StartInstance from "./StartInstance";
import RebootInstance from "./RebootInstance";
import StopInstance from "./StopInstance";
import CreateTemplate from "./CreateTemplate";
import ReInstall from "./ReInstall";
import Metric from "./Metric";
import AttachNetwork from "./AttachNetwork";
import CreateInstance from "./CreateInstance"
import BatchRebootInstance from "./BatchRebootInstance";
import BatchStopInstance from "./BatchStopInstance";
import {Notification} from "element-ui";
export default {
  name: "Instance.vue",
  components: {
    Top, Menu, Search,InstanceDetail,
    AttachCdRoom,ModifyInstance,StartInstance,
    RebootInstance,StopInstance,CreateTemplate,
    ReInstall,Metric,AttachNetwork,CreateInstance,
    BatchRebootInstance,BatchStopInstance
  },
  data() {
    return {
      filter: {
        key: ""
      },
      menuIndex: "1",
      current_page: 1,
      page_size: 10,
      total_size: 0,
      expand_instance: [],
      select_instance: [],
      loading: true,
      batch_start_loading:false
    }
  },
  mounted() {
    this.load_cluster().then(() => this.load_host().then(() => this.load_groups().then(()=>this.load_template().then(()=>this.load_scheme().then(()=>this.load_network().then(()=>this.load_storage().then(()=>{
      this.$refs.SearchRef.init_data(this.all_groups, this.all_cluster, this.all_host)
      this.on_search_callback()
    })))))))
  },
  computed: {
    table_instance() {
      return this.all_instance.filter(v => {
        return v.isShow === undefined || v.isShow
      })
    },
  },
  methods: {
    get_instance_id(instance) {
      return instance.id
    },
    on_create_instance_click() {
      this.$refs.CreateInstanceRef.init_data(this.all_cluster,this.all_groups,this.all_scheme,this.all_template,this.all_storage,this.all_network)
    },
    on_batch_start_click(){
      const modify={
        ids:this.select_instance.map(v => v.id),
        hostId:0
      }
      this.batch_start_loading=true
      this.post_data(`/management/vm/batch/start`, modify).then(res => {
        this.batch_start_loading=false
        if (res.data.code === 0) {
          res.data.data.forEach((data,idx)=>{
            if(data.code===0){
              this.on_instance_modify(data.data)
            }else{
              let id=this.modify.ids[idx]
              let instance=this.all_instance.find(v=>v.id===id)
              if(instance){
                Notification.error({
                  title: '错误',
                  duration: 5000,
                  message: `[${instance.description}]启动失败:${data.message}`,
                });
              }
            }
          })
        }
      })
    },
    on_batch_reboot_click(){
      this.$refs.BatchRebootInstanceRef.init_data(this.select_instance)
    },
    on_batch_stop_click(){
      this.$refs.BatchStopInstanceRef.init_data(this.select_instance)
    },
    on_search_callback(search_data) {
      if (!search_data) {
        search_data = this.$refs.SearchRef.search
      }
      this.current_page=1
      this.load_cluster().then(() => this.load_host().then(() => this.load_groups().then(() => this.load_template().then(()=>this.load_scheme().then(()=>this.load_network().then(()=>{
        this.search_instance(search_data)
      }))))))
    },
    search_instance(search_data) {
      this.all_instance = []
      this.expand_instance = []
      this.loading = true
      return this.axios_get(`/management/vm/search?clusterId=${search_data.clusterId}&groupId=${search_data.groupId}&hostId=${search_data.hostId}&type=${search_data.type}&status=${search_data.status}`).then(res => {
        if (res.data.code === 0 && res.data.data) {
          this.all_instance = res.data.data
          this.on_filter_key_change()
          this.loading = false
        }
      })
    },
    get_instance_status_tag_type(status) {
      return status === 'Running' ? '' : 'danger'
    },
    current_page_change(current_page) {
      this.current_page = current_page
      this.on_filter_key_change()
    },
    page_size_change(page_size) {
      this.page_size = page_size
      this.on_filter_key_change()
    },
    on_filter_key_change() {
      let filter = this.filter.key.trim().toLowerCase()
      let nCount = 0
      this.all_instance.forEach((item, index) => {
        if (filter === '') {
          item.isShow = true
        } else {
          const clusterName = this.get_cluster_name_by_id(item.clusterId).toLowerCase()
          item.isShow = clusterName.indexOf(filter) >= 0
              || item.name.toLowerCase().indexOf(filter) >= 0
              || item.ip.indexOf(filter) >= 0
              || item.description.toLowerCase().indexOf(filter) >= 0

        }
        if (item.isShow) {
          nCount++
        }
        if (nCount <= this.page_size * (this.current_page - 1)
            || nCount > this.page_size * this.current_page) {
          item.isShow = false
        }
        this.$set(this.all_instance, index, item)
      })
      this.total_size = nCount
    },
    on_instance_selection_change(rows) {
      this.select_instance = rows
    },
    bind_instance_dropdown(command,instance){
      return {
        command:command,
        instance:instance
      }
    },
    on_instance_modify(instance){
      let findIndex = this.all_instance.findIndex(item => item.id === instance.id)
      this.$set(this.all_instance, findIndex,{...instance,isShow:true})
    },
    on_instance_create(instance){
      this.all_instance.push({...instance,isShow:true})
    },
    on_instance_dropdown_select(data){
      if(data.command==='detail'){
        this.$refs.InstanceDetailRef.init_data(data.instance,this.all_cluster,this.all_groups,this.all_host,this.all_scheme,this.all_template)
      }else if(data.command==="attach-cd"){
        this.$refs.AttachCdRoomRef.init_data(data.instance,this.all_template)
      }else if(data.command==='detach-cd'){
        this.post_data(`/management/vm/detach/cdroom`, {id:data.instance.id}).then(res => {
          if (res.data.code === 0) {
             this.on_instance_modify(res.data.data)
          }
        })
      }else if(data.command==='vnc'){
        let {href} =this.$router.resolve({path:"/Vnc",query:{id:data.instance.id,description:data.instance.description}})
        window.open(href, '_blank');
      }else if(data.command==="modify"){
        this.$refs.ModifyInstanceRef.init_data(data.instance,this.all_scheme,this.all_groups)
      }else if(data.command==="start"){
        this.$refs.StartInstanceRef.init_data(data.instance,this.all_host)
      }else if(data.command==="reboot"){
        this.$refs.RebootInstanceRef.init_data(data.instance)
      }else if(data.command==="stop"){
        this.$refs.StopInstanceRef.init_data(data.instance)
      }else if(data.command==="template"){
        this.$refs.CreateTemplateRef.init_data(data.instance,this.all_template)
      }else if(data.command==="re-install"){
        this.$refs.ReInstallRef.init_data(data.instance,this.all_template)
      }else if(data.command==="metric"){
        this.$refs.MetricRef.init_data(data.instance)
      }else if(data.command==="attach-network"){
        this.$refs.AttachNetworkRef.init_data(data.instance,this.all_network)
      }else if(data.command==='destroy'){
        this.$confirm(`确认销毁${data.instance.description}, 是否继续?`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          distinguishCancelAndClose: true,
        }).then(() => {
          this.post_data(`/management/vm/destroy`, {id:data.instance.id}).then(res => {
            if (res.data.code === 0) {
              if(res.data.data){
                this.on_instance_modify(res.data.data)
              }else{
                let findIndex = this.all_instance.findIndex(item => item.id === data.instance.id)
                this.$delete(this.all_instance,findIndex)
              }
            }else if(res.data.code===1000001){
              let findIndex = this.all_instance.findIndex(item => item.id === data.instance.id)
              this.$delete(this.all_instance,findIndex)
            }
          })
        })
      }else if(data.command==='resume'){
        this.post_data(`/management/vm/resume`, {id:data.instance.id}).then(res => {
          if (res.data.code === 0) {
              this.on_instance_modify(res.data.data)
          }
        })
      }
    }
  }
}
</script>

<style scoped>
@import "../../assets/css/iconfont.css";

.main{
  height: 100%;
}
.container{
  height: calc(100%  - 60px);;
}.demo-table-expand {
   font-size: 0;
 }
.demo-table-expand label {
  width: 90px;
  color: #99a9bf;
}
.demo-table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 30%;
}
</style>