<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">

    <el-form ref="create_scheme_service_ref" :model="modify" label-position="right" label-width="80px">

      <el-form-item label="选择网络">
        <el-select v-model="modify.networkId">
          <el-option
              v-for="network in all_network"
              :key="network.id"
              :label="network.name"
              :value="network.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button :loading="loading"  type="primary" :disabled="modify.iso===''" @click="ok">确 定</el-button>
        <el-button @click="dialog_visible = false" >取 消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
export default {
  name: "AttachNetwork",
  data(){
    return {
      modify:{
        vmId:0,
        networkId:""
      },
      title:"",
      dialog_visible:false,
      loading:false
    }
  },
  methods:{
    init_data(instance,all_network){
      this.instance=instance
      this.title=`${instance.description}-附加网卡`
      console.log(all_network)
      this.all_network=all_network.filter(v=>v.clusterId===instance.clusterId)
      if(this.all_network.length>0){
        this.modify.networkId=this.all_network[0].id
      }
      this.modify.vmId=instance.id
      this.dialog_visible=true
      this.loading=false
    },
    ok(){
      this.loading=true
      this.post_data(`/management/vm/attach/network`, this.modify).then(res => {
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