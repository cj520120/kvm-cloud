<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form ref="create_scheme_service_ref" :model="modify" label-position="right" label-width="80px">

      <el-form-item label="光盘镜像">
        <el-select v-model="modify.iso">
          <el-option
              v-for="template in all_template"
              :key="template.id"
              :label="template.name"
              :value="template.id">
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
  name: "AttachCdRoom",
  data(){
    return {
      modify:{
        id:0,
        iso:""
      },
      title:"",
      dialog_visible:false,
      loading:false
    }
  },
  methods:{
    init_data(instance,all_template){
      this.instance=instance
      this.title=`${instance.description}-挂载光驱`
      this.all_template=all_template.filter(v=>v.clusterId===instance.clusterId&&v.type==='ISO')
      if(this.all_template.length>0){
        this.modify.iso=this.all_template[0].id
      }
      if(instance.iso>0){
        this.modify.iso=instance.iso
      }
      this.modify.id=instance.id
      this.dialog_visible=true
      this.loading=false
    },
    ok(){
      this.loading=true
      this.post_data(`/management/vm/attach/cdroom`, this.modify).then(res => {
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