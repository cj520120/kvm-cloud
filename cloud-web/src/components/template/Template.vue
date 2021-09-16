<template>
  <div class="main">
    <Top/>
    <div style="display: flex"  class="container">
      <Menu :index="menuIndex"/>
      <el-main>
        <el-tabs>
          <el-tab-pane label="模版管理">
            <el-row>
              <el-col :span="24">
                <div class="grid-content bg-purple-light" style="padding-top: 5px;padding-bottom: 5px">
                  <el-button icon="el-icon-plus" plain type="primary" @click="on_template_click">创建模版</el-button>&nbsp;
                </div>
              </el-col>
            </el-row>
            <el-divider></el-divider>
            <div style="display: flex">
              <el-table ref="filterTable" :data="all_template" style="width: 100%">
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
                        <el-form-item label="模版地址" v-show="props.row.uri">
                          <el-tag>{{props.row.uri}}</el-tag>
                        </el-form-item>
                        <el-form-item label="模版类型">
                          <span>{{ props.row.type }}</span>
                        </el-form-item>
                        <el-form-item label="系统类型">
                          <el-tag>{{ get_category_name_by_id(props.row.osCategoryId) }}</el-tag>
                        </el-form-item>
                        <el-form-item label="模版状态">
                          <el-tag  :type="get_template_status_tag_type(props.row.status)">{{ props.row.status }}</el-tag>
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
                    width="400">
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
                    label="状态"
                    prop="status"
                    width="150">
                  <template slot-scope="scope">
                    <el-tag :type="get_template_status_tag_type(scope.row.status)">{{ scope.row.status }}</el-tag>
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
                      <el-popconfirm title="确认删除当前模版？"  @confirm="on_delete_template_click(scope.row)">
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
    <EditTemplate ref="EditTemplateRef" @on_create="on_template_create_callback"/>
  </div>
</template>

<script>
import Top from "../common/Top";
import Menu from "../common/Menu"
import EditTemplate from "./EditTemplate";
export default {
  name: "Template.vue",
  components:{Top,Menu,EditTemplate},
  data(){
    return {
      menuIndex:"5"
    }
  },
  created() {
    this.load_cluster().then(()=>this.load_category().then(()=>this.load_template()))
  },
  methods:{
    on_template_click(){
      this.$refs.EditTemplateRef.init_data(this.all_cluster,this.all_category)
    },
    on_delete_template_click(template){
      this.post_data(`/management/template/destroy`, {id:template.id}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_template.findIndex(item => item.id === template.id)
          this.$delete(this.all_template, findIndex)
        }
      })
    },
    get_template_status_tag_type(status){
      return status ==='Ready'?'':'danger'
    },
    on_template_create_callback(template){
      this.all_template.push(template)
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
  width: 100%;
}
</style>