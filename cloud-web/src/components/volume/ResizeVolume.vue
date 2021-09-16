<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form ref="create_scheme_service_ref" :model="modify" label-position="right" label-width="80px">
      <el-form-item label="新增大小" prop="size">
        <el-input-number v-model="modify.size" :max="10000" :min="1"></el-input-number>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="modify.size===0" @click="modify_size">确 定</el-button>
        <el-button @click="dialog_visible = false" >取 消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
export default {
  name: "ResizeVolume",
  data(){
    return {
      modify: {
        id: 0,
        size: 0
      },
      dialog_visible: false,
      title: ""
    }
  },
  methods:{
    init_data(id){
      this.modify.id=id
      this.title="扩容磁盘"
      this.dialog_visible=true
    },
    modify_size(){
        this.post_data(`/management/volume/resize`, this.modify).then(res => {
          if (res.data.code === 0) {
            this.$emit("on_resize", res.data.data)
            this.dialog_visible = false
          }
        })
    }
  }
}
</script>

<style scoped>

</style>