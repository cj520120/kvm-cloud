<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form  :model="modify" label-position="right" label-width="80px">
      <el-form-item label="挂载实例" prop="size">
        <el-select v-model="modify.id">
          <el-option
              v-for="instance in all_instance"
              :key="instance.id"
              :label="instance.description"
              :value="instance.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="this.modify.id===''" @click="modify_size">确 定</el-button>
        <el-button @click="dialog_visible = false" >取 消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
export default {
  name: "AttachVolume",
  data(){
    return {
      modify: {
        id: "",
        volume: 0
      },
      dialog_visible: false,
      title: ""
    }
  },
  methods:{
    init_data(id,all_instance){
      this.modify.volume=id
      this.title="挂载磁盘"
      this.dialog_visible=true
      this.all_instance=all_instance
    },
    modify_size(){
        this.post_data(`/management/vm/attach/disk`, this.modify).then(res => {
          if (res.data.code === 0) {
            this.$emit("on_attach", res.data.data)
            this.dialog_visible = false
          }
        })
    }
  }
}
</script>

<style scoped>

</style>