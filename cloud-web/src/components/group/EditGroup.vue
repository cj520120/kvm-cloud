<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form ref="create_project_service_ref" :model="modify" label-position="right" label-width="80px">
      <el-form-item label="名称" prop="serviceName">
        <el-input v-model="modify.name"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="modify.name==''" @click="modify_group">确 定</el-button>
        <el-button @click="dialog_visible = false" >取 消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
export default {
  name: "EditGroup",
  data(){
    return {
      modify: {
        id: 0,
        name: ""
      },
      dialog_visible: false,
      title: ""
    }
  },
  methods:{
    init_data(group){
      if(group){
        this.title="编辑群组"
        this.modify.id=group.id
        this.modify.name=group.name
      }else{
        this.title="创建群组"
        this.modify.id=0
        this.modify.name=""
      }
      this.dialog_visible=true
    },
    modify_group(){
      if (this.modify.id === 0) {
        this.post_data(`/management/group/create`, this.modify).then(res => {
          if (res.data.code === 0) {
            this.$emit("on_create", res.data.data)
            this.dialog_visible = false
          }
        })
      }else {
        this.post_data(`/management/group/modify`, this.modify).then(res => {
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