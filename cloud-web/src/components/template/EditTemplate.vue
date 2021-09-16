<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="modify" label-position="right" label-width="90px">
      <el-form-item label="名称">
        <el-input v-model="modify.templateName"></el-input>
      </el-form-item>
      <el-form-item label="集群">
        <el-select v-model="modify.clusterId">
        <el-option
            v-for="cluster in all_cluster"
            :key="cluster.id"
            :label="cluster.name"
            :value="cluster.id">
        </el-option>
      </el-select>
      </el-form-item>
      <el-form-item label="集群">
        <el-select v-model="modify.osCategoryId">
          <el-option
              v-for="category in all_category"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="模版地址">
          <el-input v-model="modify.templateUri"></el-input>
      </el-form-item>
      <el-form-item label="模版类型">
        <el-select v-model="modify.templateType">
          <el-option value="ISO" label="ISO"></el-option>
          <el-option value="Disk" label="Disk"></el-option>
          <el-option value="Route" label="Route"></el-option>
          <el-option value="Console" label="Console"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button :loading="loading" type="primary" :disabled="modify.templateType=='' ||modify.templateUri=='' ||modify.osCategoryId=='' || modify.templateName=='' || modify.clusterId===''  " @click="create_template">确 定</el-button>
        <el-button @click="dialog_visible = false" >取 消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
export default {
  name: "EditTemplate",
  data(){
    return {
      modify: {
        templateName: "",
        clusterId: "",
        osCategoryId: "",
        templateType:"ISO",
        templateUri: ""
      },
      dialog_visible: false,
      loading:false,
      title: ""
    }
  },
  methods:{
    init_data(all_cluster,all_category){
      this.title="创建模版"
      this.modify.templateName=""
      this.modify.templateUri=""
      this.modify.clusterId=""
      this.modify.osCategoryId=""
      this.modify.templateType="ISO"
      this.loading=false
      this.all_cluster=all_cluster
      this.all_category=all_category
      this.dialog_visible=true
    },
    create_template(){
      this.loading=true
      this.post_data(`/management/template/create`, this.modify).then(res => {
        this.loading=false
          if (res.data.code === 0) {
            this.$emit("on_create", res.data.data)
            this.dialog_visible = false
          }
      })
    }
  }
}
</script>

<style scoped>

</style>