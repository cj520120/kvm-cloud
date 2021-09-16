<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="300px"  :close-on-click-modal="false" :close-on-press-escape="false">

    <el-form :model="modify" label-position="right" label-width="30px">
      <el-form-item>
        <el-checkbox v-model="modify.force">强制关机</el-checkbox>
      </el-form-item>
      <el-form-item>
        <el-button :loading="loading" type="primary" :disabled="modify.name==''" @click="ok">确 定</el-button>
        <el-button @click="dialog_visible = false" >取 消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
export default {
  name: "StopInstance",
  data(){
    return {
      modify:{
        id:0,
        force:false
      },
      title:"",
      loading:false,
      dialog_visible:false
    }
  },
  methods:{
    init_data(instance){
      this.instance=instance
      this.title=`${instance.description}-停止实例`
      this.modify.id=instance.id
      this.modify.force=false
      this.dialog_visible=true
      this.loading=false

    },
    ok(){
      this.loading=true
      this.post_data(`/management/vm/stop`, this.modify).then(res => {
        this.loading=false
        if (res.data.code === 0) {
          this.$emit("on_modify", res.data.data)
          this.dialog_visible = false
        }
      })
      this.dialog_visible=true
    }
  }
}
</script>

<style scoped>

</style>