<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form ref="create_project_service_ref" :model="modify" label-position="right" label-width="80px">
      <el-form-item label="名称" prop="serviceName">
        <el-input v-model="modify.name"></el-input>
      </el-form-item>
      <el-form-item label="CPU超配" prop="overCpu">
        <el-input v-model="modify.overCpu"></el-input>
      </el-form-item>
      <el-form-item label="内存超配" prop="overMemory">
        <el-input v-model="modify.overMemory"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="modify.name==''" @click="modify_cluster">确 定</el-button>
        <el-button @click="dialog_visible = false" >取 消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
export default {
  name: "EditCluster",
  data(){
    return {
      modify: {
        id: 0,
        name: "",
        overCpu: "",
        overMemory: "",
      },
      dialog_visible: false,
      title: ""
    }
  },
  methods:{
    init_data(cluster){
      if(cluster){
        this.title="编辑群组"
        this.modify.id=cluster.id
        this.modify.name=cluster.name
        this.modify.overCpu=cluster.overCpu.toFixed(2)
        this.modify.overMemory=cluster.overMemory.toFixed(2)
      }else{
        this.title="创建群组"
        this.modify.id=0
        this.modify.name=""
        this.modify.overCpu="1.00"
        this.modify.overMemory="1.00"
      }
      this.dialog_visible=true
    },
    modify_cluster(){
      if (this.modify.id === 0) {
        this.post_data(`/management/cluster/create`, this.modify).then(res => {
          if (res.data.code === 0) {
            this.$emit("on_create", res.data.data)
            this.dialog_visible = false
          }
        })
      }else {
        this.post_data(`/management/cluster/modify`, this.modify).then(res => {
          if (res.data.code === 0) {
            this.$emit("on_modify", res.data.data)
            this.dialog_visible = false
          }
        })
      }
    }
  }
}
</script>

<style scoped>

</style>