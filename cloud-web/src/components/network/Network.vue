<template>
  <div class="main">
    <Top/>
    <div style="display: flex"  class="container">
      <Menu :index="menuIndex"/>
      <el-main>
        <el-tabs>
          <el-tab-pane label="网络管理">
            <el-row>
              <el-col :span="24">
                <div class="grid-content bg-purple-light" style="padding-top: 5px;padding-bottom: 5px">
                  <el-button icon="el-icon-plus" plain type="primary" @click="on_network_click">创建网络</el-button>&nbsp;
                  <el-button icon="el-icon-refresh" plain type="primary" @click="on_refresh">刷新网络</el-button>
                </div>
              </el-col>
            </el-row>
            <el-divider></el-divider>
            <div style="display: flex">
              <el-table v-loading="loading" ref="filterTable" :data="all_network" style="width: 100%">
                <el-table-column type="expand">
                  <template slot-scope="props">
                    <div>
                      <el-form label-position="left" inline class="demo-table-expand">
                        <el-form-item label="编号">
                          <span>{{ props.row.id }}</span>
                        </el-form-item>
                        <el-form-item label="网卡">
                          <span>{{ props.row.card }}</span>
                        </el-form-item>
                        <el-form-item label="名称">
                          <span>{{ props.row.name }}</span>
                        </el-form-item>
                        <el-form-item label="集群">
                          <span>{{ get_cluster_name_by_id(props.row.clusterId) }}</span>
                        </el-form-item>
                        <el-form-item label="管理起始IP">
                          <span>{{ props.row.managerStartIp }}</span>
                        </el-form-item>
                        <el-form-item label="管理结束IP">
                          <span>{{ props.row.managerEndIp }}</span>
                        </el-form-item>
                        <el-form-item label="实例开始IP">
                          <span>{{ props.row.guestStartIp }}</span>
                        </el-form-item>
                        <el-form-item label="实例结束IP">
                          <span>{{ props.row.guestEndIp }}</span>
                        </el-form-item>


                        <el-form-item label="子网地址">
                          <span>{{ props.row.subnet }}</span>
                        </el-form-item>
                        <el-form-item label="DNS信息">
                          <span>{{ props.row.dns }}</span>
                        </el-form-item>


                        <el-form-item label="网络状态">
                          <el-tag  :type="get_network_status_tag_type(props.row.status)">{{ props.row.status }}</el-tag>
                        </el-form-item>
                        <el-form-item label="网络类型">
                          <span> {{ props.row.type }}</span>
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
                    label="状态"
                    prop="status"
                    width="120">
                  <template slot-scope="scope">
                    <el-tag :type="get_network_status_tag_type(scope.row.status)">{{ scope.row.status }}</el-tag>
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
                    <el-button plain size="small" type="primary" @click="start_network_click(scope.row)" v-show="scope.row.status==='Pause'">启用</el-button>
                    <el-popconfirm title="确认暂停网络？"  @confirm="stop_network_click(scope.row)" v-show="scope.row.status==='Ready'">
                      <el-button slot="reference" plain size="mini" type="danger">暂停</el-button>
                    </el-popconfirm>
                    <el-popconfirm title="确认删除当前网络？" style="margin-left: 10px"  @confirm="on_delete_network_click(scope.row)">
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
    <EditNetwork ref="EditNetworkRef" @on_create="on_network_create_callback"/>
  </div>
</template>

<script>
import Top from "../common/Top";
import Menu from "../common/Menu"
import EditNetwork from "./EditNetwork";
export default {
  name: "Network.vue",
  components:{Top,Menu,EditNetwork},
  data(){
    return {
      menuIndex:"3",
      loading:false
    }
  },
  created() {
    this.on_refresh()
  },
  methods:{
    on_refresh(){
      this.all_cluster=[]
      this.all_network=[]
      this.load_cluster().then(()=>this.load_network()).then(()=>this.loading=false)
    },
    on_network_click(){
      this.$refs.EditNetworkRef.init_data(this.all_cluster)
    },
    on_delete_network_click(network){
      this.post_data(`/management/network/destroy`, {id:network.id}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_network.findIndex(item => item.id === network.id)
          this.$delete(this.all_network, findIndex)
        }
      })
    },

    get_network_status_tag_type(status){
      return status ==='Ready'?'':'danger'
    },
    on_network_create_callback(network){
      this.all_network.push(network)
    },
    start_network_click(network){
      this.post_data(`/management/network/start`, {id:network.id}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_network.findIndex(item => item.id === network.id)
          this.$set(this.all_network, findIndex,res.data.data)
        }
      })
    },
    stop_network_click(network){
      this.post_data(`/management/network/pause`, {id:network.id}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_network.findIndex(item => item.id === network.id)
          this.$set(this.all_network, findIndex,res.data.data)
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