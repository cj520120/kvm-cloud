<template>
  <div class="main">
    <Top/>
    <div style="display: flex"  class="container">
      <Menu :index="menuIndex"/>
      <el-main>
        <el-tabs>
          <el-tab-pane label="存储池管理">
            <el-row>
              <el-col :span="24">
                <div class="grid-content bg-purple-light" style="padding-top: 5px;padding-bottom: 5px">
                  <el-button icon="el-icon-plus" plain type="primary" @click="on_storage_click">创建存储池</el-button>&nbsp;
                  <el-button icon="el-icon-refresh" plain type="primary" @click="on_refresh">刷新存储池</el-button>
                </div>
              </el-col>
            </el-row>
            <el-divider></el-divider>
            <div style="display: flex">
              <el-table v-loading="loading" ref="filterTable" :data="all_storage" style="width: 100%">
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
                        <el-form-item label="集群">
                          <span>{{ get_cluster_name_by_id(props.row.clusterId) }}</span>
                        </el-form-item>
                        <el-form-item label="挂载路径">
                          <el-tag>/mnt/{{props.row.target}}</el-tag>
                        </el-form-item>
                        <el-form-item label="地址">
                          <el-tag>nfs://{{props.row.host}}{{props.row.source}}</el-tag>
                        </el-form-item>
                        <el-form-item label="磁盘容量">
                          <span>{{ parse_disk_capacity(props.row.capacity) }}</span>
                        </el-form-item>
                        <el-form-item label="可用容量">
                          <span>{{ parse_disk_capacity(props.row.available) }}</span>
                        </el-form-item>
                        <el-form-item label="申请容量">
                          <span>{{ parse_disk_capacity(props.row.allocation) }}</span>
                        </el-form-item>
                        <el-form-item label="存储状态">
                          <el-tag  :type="get_volume_status_tag_type(props.row.status)">{{ props.row.status }}</el-tag>
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
                    width="80">
                </el-table-column>
                <el-table-column
                    label="名称"
                    prop="name"
                    width="150">
                </el-table-column>
                <el-table-column
                    label="集群"
                    prop="clusterId"
                    width="150">
                  <template slot-scope="scope">
                    <span>{{ get_cluster_name_by_id(scope.row.clusterId)}}</span>
                  </template>
                </el-table-column>
                <el-table-column
                    label="容量"
                    prop="name"
                    width="300">
                  <template slot-scope="scope">
                    <el-progress :percentage="get_storage_percentage(scope.row)" color="#f56c6c"></el-progress>
                  </template>
                </el-table-column>
                <el-table-column
                    label="状态"
                    prop="status"
                    width="150">
                  <template slot-scope="scope">
                    <el-tag :type="get_volume_status_tag_type(scope.row.status)">{{ scope.row.status }}</el-tag>
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
                      <el-popconfirm title="确认删除当前存储池？"  @confirm="on_delete_storage_click(scope.row)">
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
    <EditStorage ref="EditStorageRef" @on_create="on_storage_create_callback"/>
  </div>
</template>

<script>
import Top from "../common/Top";
import Menu from "../common/Menu"
import EditStorage from "./EditStorage";
export default {
  name: "Storage.vue",
  components:{Top,Menu,EditStorage},
  data(){
    return {
      menuIndex:"6",
      loading:true
    }
  },
  created() {
    this.on_refresh()
  },
  methods:{
    on_refresh(){
      this.all_cluster=[]
      this.all_storage=[]
      this.loading=true
      this.load_cluster().then(()=>this.load_storage().then(()=>this.loading=false))
    },
    on_storage_click(){
      this.$refs.EditStorageRef.init_data(this.all_cluster)
    },
    on_delete_storage_click(storage){
      this.post_data(`/management/storage/destroy`, {id:storage.id}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_storage.findIndex(item => item.id === storage.id)
          this.$delete(this.all_storage, findIndex)
        }
      })
    },
    get_volume_status_tag_type(status){
      return status ==='Ready'?'':'danger'
    },
    on_storage_create_callback(storage){
      this.all_storage.push(storage)
    },
    get_storage_percentage(storage){
      const storage_percent = storage.capacity === 0 ? 0 : parseInt(storage.allocation * 100 / storage.capacity);
      return storage_percent
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