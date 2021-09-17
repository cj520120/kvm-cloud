<template>
  <div class="main">
    <Top/>
    <div style="display: flex"  class="container">
      <Menu :index="menuIndex"/>
      <el-main>
        <el-tabs>
          <el-tab-pane label="集群管理">
            <el-row>
              <el-col :span="24">
                <div class="grid-content bg-purple-light" style="padding-top: 5px;padding-bottom: 5px">
                  <el-button icon="el-icon-plus" plain type="primary" @click="on_cluster_click">创建集群</el-button>&nbsp;
                  <el-button icon="el-icon-refresh" plain type="primary" @click="on_refresh">刷新集群</el-button>
                </div>
              </el-col>
            </el-row>
            <el-divider></el-divider>
            <div style="display: flex">
              <el-table v-loading="loading" ref="filterTable" :data="all_cluster" style="width: 100%">
                <el-table-column
                    label="ID"
                    prop="id"
                    width="80">
                </el-table-column>
                <el-table-column
                    label="名称"
                    prop="name"
                    width="300">
                </el-table-column>
                <el-table-column
                    label="CPU超配"
                    prop="overCpu"
                    width="100">
                  <template slot-scope="scope">
                    <span>{{ scope.row.overCpu.toFixed(2)}}</span>
                  </template>
                </el-table-column>
                <el-table-column
                    label="内存超配"
                    prop="overMemory"
                    width="100">
                  <template slot-scope="scope">
                    <span>{{ scope.row.overMemory.toFixed(2)}}</span>
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
                    <el-button plain size="mini" type="primary" @click="on_modify_cluster_click(scope.row)">编辑</el-button>
                    <el-popconfirm title="确认删除当前集群？" style="margin-left: 10px" @confirm="on_delete_cluster_click(scope.row)">
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
    <EditCluster ref="EditClusterRef" @on_create="on_cluster_create_callback"  @on_modify="on_cluster_modify_callback"/>
  </div>
</template>

<script>
import Top from "../common/Top";
import Menu from "../common/Menu"
import EditCluster from "./EditCluster";
export default {
  name: "Cluster.vue",
  components:{Top,Menu,EditCluster},
  data(){
    return {
      menuIndex:"2",
      loading:false
    }
  },
  created() {
    this.on_refresh()
  },
  methods:{
    on_refresh(){
      this.all_cluster=[]
      this.loading=true
      this.load_cluster().then(()=>this.loading=false)
    },
    on_cluster_click(){
      this.$refs.EditClusterRef.init_data()
    },
    on_modify_cluster_click(cluster){
      this.$refs.EditClusterRef.init_data(cluster)
    },
    on_delete_cluster_click(cluster){
      this.post_data(`/management/cluster/destroy`, {id:cluster.id}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_cluster.findIndex(item => item.id === cluster.id)
          this.$delete(this.all_cluster, findIndex)
        }
      })
    },
    on_cluster_create_callback(cluster){
      this.all_cluster.push(cluster)
    },
    on_cluster_modify_callback(cluster){
      let findIndex = this.all_cluster.findIndex(item => item.id === cluster.id)
      this.$set(this.all_cluster, findIndex,cluster)

    }
  }
}
</script>

<style scoped>
.main{
  height: 100%;
}
.container{
  height: calc(100%  - 60px);;
}
</style>