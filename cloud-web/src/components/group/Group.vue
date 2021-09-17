<template>
  <div class="main">
    <Top/>
    <div style="display: flex"  class="container">
      <Menu :index="menuIndex"/>
      <el-main>
        <el-tabs>
          <el-tab-pane label="分组管理">
            <el-row>
              <el-col :span="24">
                <div class="grid-content bg-purple-light" style="padding-top: 5px;padding-bottom: 5px">
                  <el-button icon="el-icon-plus" plain type="primary" @click="on_group_click">创建群组</el-button>&nbsp;
                  <el-button icon="el-icon-refresh" plain type="primary" @click="on_refresh">刷新群组</el-button>
                </div>
              </el-col>
            </el-row>
            <el-divider></el-divider>
            <div style="display: flex">
              <el-table v-loading="loading" ref="filterTable" :data="all_groups" style="width: 100%">
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
                    label="创建时间"
                    prop="createTime"
                    width="200">
                  <template slot-scope="scope">
                    <span style="margin-left: 10px">{{ parse_date(scope.row.createTime) }}</span>
                  </template>
                </el-table-column>

                <el-table-column label="操作">
                  <template slot-scope="scope">
                    <el-button plain size="mini" type="primary" @click="on_modify_group_click(scope.row)">编辑</el-button>
                    <el-popconfirm title="确认删除当前群组？" style="margin-left: 10px" @confirm="on_delete_group_click(scope.row)">
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
    <EditGroup ref="EditGroupRef" @on_create="on_group_create_callback"  @on_modify="on_group_modify_callback"/>
  </div>
</template>

<script>
import Top from "../common/Top";
import Menu from "../common/Menu"
import EditGroup from "./EditGroup";
export default {
  name: "Group.vue",
  components:{Top,Menu,EditGroup},
  data(){
    return {
      menuIndex:"10",
      loading:false
    }
  },
  created() {
    this.on_refresh()
  },
  methods:{
    on_refresh(){
      this.all_groups=[]
      this.loading=true
      this.load_groups().then(()=>this.loading=false)
    },
    on_group_click(){
      this.$refs.EditGroupRef.init_data()
    },
    on_modify_group_click(group){
      this.$refs.EditGroupRef.init_data(group)
    },
    on_delete_group_click(group){
      this.post_data(`/management/group/destroy`, {id:group.id}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_groups.findIndex(item => item.id === group.id)
          this.$delete(this.all_groups, findIndex)
        }
      })
    },
    on_group_create_callback(group){
      this.all_groups.push(group)
    },
    on_group_modify_callback(group){
      let findIndex = this.all_groups.findIndex(item => item.id === group.id)
      this.$set(this.all_groups, findIndex,group)

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