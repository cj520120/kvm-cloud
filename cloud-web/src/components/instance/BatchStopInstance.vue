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
import {Notification} from "element-ui";

export default {
  name: "BatchStopInstance",
  data(){
    return {
      modify:{
        ids:"",
        force:false
      },
      title:"",
      loading:false,
      dialog_visible:false
    }
  },
  methods:{
    init_data(instance_list){
      this.title=`批量停止实例`
      this.modify.ids=instance_list.map(v => v.id)
      this.all_instance=instance_list
      this.modify.force=false
      this.dialog_visible=true
      this.loading=false

    },
    ok(){
      this.loading=true
      this.post_data(`/management/vm/batch/stop`, this.modify).then(res => {
        this.loading=false
        if (res.data.code === 0) {
          res.data.data.forEach((data,idx)=>{
            if(data.code===0){
              this.$emit("on_modify", data.data)
            }else{
              let id=this.modify.ids[idx]
              let instance=this.all_instance.find(v=>v.id===id)
              if(instance){
                Notification.error({
                  title: '错误',
                  duration: 5000,
                  message: `[${instance.description}]停止失败:${data.message}`,
                });
              }
            }
          })
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