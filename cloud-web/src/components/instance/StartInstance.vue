<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">

    <el-form :model="modify" label-position="right" label-width="90px">

      <el-form-item label="主机">
        <el-select v-model="modify.hostId" size="small" style="width: 200px">
          <el-option :key="0" :value="0" label="无限制"/>
          <el-option
              v-for="host in all_host"
              :key="host.id"
              :label="host.name"
              :value="host.id">
          </el-option>
        </el-select>
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
  name: "StartInstance",
  data(){
    return {
      modify:{
          id:0,
          hostId:0
      },
      title:"",
      loading:false,
      dialog_visible:false
    }
  },
  methods:{
    init_data(instance,all_host){
      this.instance=instance
      this.title=`${instance.description}-启动实例`
      this.all_host=all_host.filter(v=>v.clusterId===instance.clusterId)
      this.modify.id=instance.id
      this.modify.hostId=0
      this.dialog_visible=true
      this.loading=false
    },
    ok(){
      this.loading=true
      this.post_data(`/management/vm/start`, this.modify).then(res => {
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