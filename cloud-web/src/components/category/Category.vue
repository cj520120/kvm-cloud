<template>
  <div class="main">
    <Top/>
    <div style="display: flex"  class="container">
      <Menu :index="menuIndex"/>
      <el-main>
        <el-tabs>
          <el-tab-pane label="系统分类管理">
            <el-row>
              <el-col :span="24">
                <div class="grid-content bg-purple-light" style="padding-top: 5px;padding-bottom: 5px">
                  <el-button icon="el-icon-plus" plain type="primary" @click="on_category_click">创建系统分类</el-button>&nbsp;
                </div>
              </el-col>
            </el-row>
            <el-divider></el-divider>
            <div style="display: flex">
              <el-table ref="filterTable" :data="all_category" style="width: 100%">
                <el-table-column
                    label="ID"
                    prop="id"
                    width="80">
                </el-table-column>
                <el-table-column
                    label="名称"
                    prop="categoryName"
                    width="300">
                </el-table-column>
                <el-table-column
                    label="磁盘驱动"
                    prop="diskDriver"
                    width="300">
                </el-table-column>
                <el-table-column
                    label="网络驱动"
                    prop="networkDriver"
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
                    <el-button plain size="mini" type="primary" @click="on_modify_category_click(scope.row)">编辑</el-button>
                    <el-popconfirm title="确认删除当前系统分类？" style="margin-left: 10px" @confirm="on_delete_category_click(scope.row)">
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
    <EditCategory ref="EditEditCategory" @on_create="on_category_create_callback"  @on_modify="on_category_modify_callback"/>
  </div>
</template>

<script>
import Top from "../common/Top";
import Menu from "../common/Menu"
import EditCategory from "./EditCategory";
export default {
  name: "Category.vue",
  components:{Top,Menu,EditCategory},
  data(){
    return {
      menuIndex:"9"
    }
  },
  created() {
    this.load_category()
  },
  methods:{
    on_category_click(){
      this.$refs.EditEditCategory.init_data()
    },
    on_modify_category_click(category){
      this.$refs.EditEditCategory.init_data(category)
    },
    on_delete_category_click(category){
      this.post_data(`/management/os/category/destroy`, {id:category.id}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_category.findIndex(item => item.id === category.id)
          this.$delete(this.all_category, findIndex)
        }
      })
    },
    on_category_create_callback(category){
      this.all_category.push(category)
    },
    on_category_modify_callback(category){
      let findIndex = this.all_category.findIndex(item => item.id === category.id)
      this.$set(this.all_category, findIndex,category)

    }
  }
}
</script>

<style scoped>
.main{
  height: 100%;
}
.container{
  height: calc(100%  - 60px);
}
</style>