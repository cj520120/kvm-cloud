<template>
  <div class="main">
    <Top/>
    <div style="display: flex"  class="container">
      <Menu :index="menuIndex"/>
      <el-main>
        <el-tabs>
          <el-tab-pane label="计算方案管理">
            <el-row>
              <el-col :span="24">
                <div class="grid-content bg-purple-light" style="padding-top: 5px;padding-bottom: 5px">
                  <el-button icon="el-icon-plus" plain type="primary" @click="on_scheme_click">创建计算方案</el-button>&nbsp;
                  <el-button icon="el-icon-refresh" plain type="primary" @click="on_refresh">刷新计算方案</el-button>
                </div>
              </el-col>
            </el-row>
            <el-divider></el-divider>
            <div style="display: flex">
              <el-table v-loading="loading" ref="filterTable" :data="table_scheme" style="width: 100%">
                <el-table-column
                    label="ID"
                    prop="id"
                    width="80">
                </el-table-column>
                <el-table-column
                    label="名称"
                    prop="name"
                    width="200">
                </el-table-column>
                <el-table-column
                    label="CPU"
                    prop="cpu"
                    width="100">
                </el-table-column>
                <el-table-column
                    label="主频(MHZ)"
                    prop="speed"
                    width="150">
                  <template slot-scope="scope">
                    <span style="margin-left: 10px">{{ scope.row.speed>0?scope.row.speed:"-" }}</span>
                  </template>
                </el-table-column>

                <el-table-column
                    label="套接字"
                    prop="socket"
                    width="100">
                </el-table-column>
                <el-table-column
                    label="核心"
                    prop="core"
                    width="100">
                </el-table-column>
                <el-table-column
                    label="线程"
                    prop="threads"
                    width="100">
                </el-table-column>
                <el-table-column
                    label="内存"
                    prop="memory"
                    width="150">
                  <template slot-scope="scope">
                    <span style="margin-left: 10px">{{ parse_memory_capacity(scope.row.memory) }}</span>
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
                    <el-popconfirm title="确认删除当前方案？" style="margin-left: 10px" @confirm="on_delete_scheme_click(scope.row)">
                      <el-button slot="reference" plain size="mini" type="danger">删除</el-button>
                    </el-popconfirm>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div class="block">
              <el-pagination
                  @size-change="page_size_change"
                  @current-change="current_page_change"
                  :current-page="current_page"
                  :page-sizes="[10,20,50,100, 200]"
                  :page-size="page_size"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="total_size">
              </el-pagination>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </div>
    <EditScheme ref="EditSchemeRef" @on_create="on_scheme_create_callback"  @on_modify="on_scheme_modify_callback"/>
  </div>
</template>

<script>
import Top from "../common/Top";
import Menu from "../common/Menu"
import EditScheme from "./EditScheme";
export default {
  name: "Scheme.vue",
  components:{Top,Menu,EditScheme},
  data(){
    return {
      menuIndex:"8",
      current_page:1,
      page_size:10,
      total_size:0,
    }
  },
  mounted() {
    this.on_refresh()
  },
  computed:{
    table_scheme(){
      return this.all_scheme.filter(v=>v.id>0&&(v.isShow===undefined || v.isShow))
    }
  },
  methods:{
    on_refresh(){
      this.all_scheme=[]
      this.loading=true
      this.load_scheme().then(()=>{
        this.current_page=1
        this.refresh_data()
        this.loading=false
      })

    },
    on_scheme_click(){
      this.$refs.EditSchemeRef.init_data()
    },
    on_delete_scheme_click(scheme){
      this.post_data(`/management/calculation/scheme/destroy`, {id:scheme.id}).then(res => {
        if (res.data.code === 0) {
          let findIndex = this.all_scheme.findIndex(item => item.id === scheme.id)
          this.$delete(this.all_scheme, findIndex)
        }
      })
    },
    on_scheme_create_callback(scheme){
      this.all_scheme.push(scheme)
    },
    on_scheme_modify_callback(scheme){
      let findIndex = this.all_scheme.findIndex(item => item.id === scheme.id)
      this.$set(this.all_scheme, findIndex,scheme)

    },
    current_page_change(current_page){
      this.current_page=current_page
      this.refresh_data()
    },
    page_size_change(page_size){
      this.page_size=page_size
      this.refresh_data()
    },
    refresh_data: function () {
      let nCount = 0;
      this.all_scheme.forEach((item, index) => {
        nCount++;
        item.isShow = true
        if (nCount <= this.page_size * (this.current_page - 1)
            || nCount > this.page_size * this.current_page) {
          item.isShow = false
        }
        this.$set(this.all_scheme, index, item)
      })
      this.total_size = nCount
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