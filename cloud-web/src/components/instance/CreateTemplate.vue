<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">

    <el-form :model="modify" label-position="right" label-width="90px">
      <el-form-item label="模版名称">
        <el-input v-model="modify.name"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="modify.name==''" @click="ok">确 定</el-button>
        <el-button @click="dialog_visible = false" >取 消</el-button>
      </el-form-item>
    </el-form>

  </el-dialog>
</template>

<script>
export default {
  name: "CreateTemplate",
  data(){
    return {
      modify:{
        id:0,
        name:""
      },
      title:"",
      dialog_visible:false
    }
  },
  methods:{
    init_data(instance){
      this.title=`${instance.description}-创建模版`
      this.dialog_visible=true
      this.modify.id=instance.id
      this.modify.description=instance.description
    },
    ok(){
      const load = this.$loading({
        lock: true,
        text: '正在创建模版，该过程需要一段时间，请耐心等待....',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });
      this.dialog_visible=false
      this.post_data(`/management/vm/template`, this.modify).then(res => {
        load.close()
        if (res.data.code === 0) {
          this.$emit("on_modify", res.data.data)
          this.dialog_visible = false
        }else{
          this.dialog_visible=true
        }
      })
    }
  }
}
</script>

<style scoped>

</style>