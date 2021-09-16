<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="modify" label-position="right" label-width="90px">
      <el-form-item label="名称">
        <el-input v-model="modify.name"></el-input>
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
      <el-form-item label="Nfs地址">
          <el-input v-model="modify.uri"></el-input>
      </el-form-item>
      <el-form-item label="Nfs路径">
          <el-input v-model="modify.source"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button :loading="loading" type="primary" :disabled="modify.uri=='' ||modify.source=='' || modify.name=='' || modify.clusterId===''  " @click="create_storage">确 定</el-button>
        <el-button @click="dialog_visible = false" >取 消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
export default {
  name: "EditStorage",
  data(){
    return {
      modify: {
        name: "",
        clusterId: "",
        uri: "",
        source: "",
      },
      dialog_visible: false,
      loading:false,
      title: ""
    }
  },
  methods:{
    init_data(all_cluster){
      this.title="创建存储池"
      this.modify.name=""
      this.modify.uri=""
      this.modify.source=""
      this.loading=false
      this.all_cluster=all_cluster
      this.dialog_visible=true
    },
    create_storage(){
      this.loading=true
      this.post_data(`/management/storage/create`, this.modify).then(res => {
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