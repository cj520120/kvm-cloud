<template>
  <div class="main">
    <Top/>
    <div style="display: flex"  class="container">
      <Menu :index="menuIndex"/>
      <el-main>
        <el-tabs>
          <el-tab-pane label="主机管理">
            <el-row>
              <el-col :span="24">
                <div class="grid-content bg-purple-light" style="padding-top: 5px;padding-bottom: 5px">
                  <el-button icon="el-icon-plus" plain type="primary" @click="on_host_click">创建主机</el-button>&nbsp;
                </div>
              </el-col>
            </el-row>
            <el-divider></el-divider>
            <div style="display: flex">
              <el-table ref="filterTable" :data="all_host" style="width: 100%">
                <el-table-column type="expand">
                  <template slot-scope="props">
                    <div>
                      <el-form label-position="left" inline class="demo-table-expand">
                        <el-form-item label="编号">
                          <span>{{ props.row.id }}</span>
                        </el-form-item>
                        <el-form-item label="名称">
                          <span>{{ props.row.name }}</span>
                        </el-form-item>
                        <el-form-item label="主机IP">
                          <span>{{ props.row.ip }}</span>
                        </el-form-item>
                        <el-form-item label="通信地址">
                          <span>{{ props.row.uri }}</span>
                        </el-form-item>
                        <el-form-item label="主机状态">
                          <el-tag  :type="get_host_status_tag_type(props.row.status)">{{ props.row.status }}</el-tag>
                        </el-form-item>
                        <el-form-item label="集群">
                          <span>{{ get_cluster_name_by_id(props.row.clusterId) }}</span>
                        </el-form-item>
                        <el-form-item label="已用内核">
                          <span>{{ props.row.allocationCpu }}</span>
                        </el-form-item>
                        <el-form-item label="总内核">
                          <span>{{ props.row.cpu }}</span>
                        </el-form-item>
                        <el-form-item label="已用内存">
                          <span>{{ parse_memory_capacity(props.row.allocationMemory)}}</span>
                        </el-form-item>
                        <el-form-item label="总内存">
                          <span> {{ parse_memory_capacity(props.row.memory)}}</span>
                        </el-form-item>

                        <el-form-item label="创建时间">
                          <span>{{ parse_date(props.row.createTime) }}</span>
                        </el-form-item>
                      </el-form>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column
                    label="ID"
                    prop="id"
                    width="50">
                </el-table-column>
                <el-table-column
                    label="名称"
                    prop="name"
                    width="150">
                </el-table-column>
                <el-table-column
                    label="集群"
                    prop="clusterId"
                    width="100">
                  <template slot-scope="scope">
                    <span>{{ get_cluster_name_by_id(scope.row.clusterId)}}</span>
                  </template>
                </el-table-column>
                <el-table-column
                    label="Cpu使用率"
                    prop="cpu"
                    width="150">
                  <template slot-scope="scope">
                    <el-progress :percentage="get_host_cpu_percentage(scope.row)" color="#f56c6c"></el-progress>
                  </template>
                </el-table-column>
                <el-table-column
                    label="内存使用率"
                    prop="name"
                    width="150">
                  <template slot-scope="scope">
                    <el-progress :percentage="get_host_memory_percentage(scope.row)" color="#f56c6c"></el-progress>
                  </template>
                </el-table-column>
                <el-table-column
                    label="状态"
                    prop="status"
                    width="120">
                  <template slot-scope="scope">
                    <el-tag :type="get_host_status_tag_type(scope.row.status)">{{ scope.row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column
                    label="创建时间"
                    prop="createTime"
                    width="200">
                  <template slot-scope="scope">
                    <span style="margin-left: 10px">{{ parse_date(scope.row.createTime) }}</span>
                  </template>
                </el-table-column>

                <el-table-column label="操作">
                  <template slot-scope="scope">
                    <el-button plain size="small" type="primary" @click="start_host_click(scope.row)" v-show="scope.row.status==='Maintenance'">启用</el-button>
                    <el-popconfirm title="维护主机会导致该主机所有实例停止，是否继续？"  @confirm="stop_host_click(scope.row)" v-show="scope.row.status==='Ready'">
                      <el-button slot="reference" plain size="mini" type="danger">维护</el-button>
                    </el-popconfirm>
                    <el-popconfirm title="确认删除当前主机？" style="margin-left: 10px"  @confirm="on_delete_host_click(scope.row)">
                      <el-button slot="reference" plain size="mini" type="danger">删除</el-button>
                    </el-popconfirm>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </div>
    <EditHost ref="EditHostRef" @on_create="on_host_create_callback"/>
  </div>
</template>

<script>
import Top from "../common/Top";
import Menu from "../common/Menu"
import EditHost from "./EditHost";
export default {
  name: "Host.vue",
  components:{Top,Menu,EditHost},
  data(){
    return {
      menuIndex:"4"
    }
  },
  created() {
    this.load_cluster().then(()=>this.load_host())
  },
  methods:{
    on_host_click(){
      this.$refs.EditHostRef.init_data(this.all_cluster)
    },
    on_delete_host_click(host){
      this.post_data(`/management/host/destroy`, {id:host.id}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_host.findIndex(item => item.id === host.id)
          this.$delete(this.all_host, findIndex)
        }
      })
    },

    get_host_status_tag_type(status){
      return status ==='Ready'?'':'danger'
    },
    on_host_create_callback(host){
      this.all_host.push(host)
    },
    get_host_cpu_percentage(host){
      const host_percent = host.cpu === 0 ? 0 : parseInt(host.allocationCpu * 100 / host.cpu);
      return host_percent
    },
    get_host_memory_percentage(host){
      const host_percent = host.memory === 0 ? 0 : parseInt(host.allocationMemory * 100 / host.memory);
      return host_percent
    },
    start_host_click(host){
      this.post_data(`/management/host/status`, {id:host.id,status:"Ready"}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_host.findIndex(item => item.id === host.id)
          this.$set(this.all_host, findIndex,res.data.data)
        }
      })
    },
    stop_host_click(host){
      this.post_data(`/management/host/status`, {id:host.id,status:"Maintenance"}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_host.findIndex(item => item.id === host.id)
          this.$set(this.all_host, findIndex,res.data.data)
        }
      })
    },
  }
}
</script>

<style scoped>
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
  width: 50%;
}
</style>