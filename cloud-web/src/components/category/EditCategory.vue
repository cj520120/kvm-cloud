<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form ref="create_project_service_ref" :model="modify" label-position="right" label-width="80px">
      <el-form-item label="名称" prop="categoryName">
        <el-input v-model="modify.categoryName"></el-input>
      </el-form-item>
      <el-form-item label="磁盘驱动" prop="diskDriver">
        <el-select v-model="modify.diskDriver">
          <el-option value="virtio" label="virtio"></el-option>
          <el-option value="ide" label="ide"></el-option>
          <el-option value="sata" label="sata"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="网卡驱动" prop="networkDriver">
        <el-select v-model="modify.networkDriver">
          <el-option value="virtio" label="virtio"></el-option>
          <el-option value="e1000" label="e1000"></el-option>
          <el-option value="rtl8139" label="rtl8139"></el-option>
        </el-select>
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
  name: "EditCategory",
  data(){
    return {
      modify: {
        id: 0,
        categoryName: "",
        diskDriver: "",
        networkDriver: ""
      },
      dialog_visible: false,
      title: ""
    }
  },
  methods:{
    init_data(category){
      if(category){
        this.title="编辑系统分类"
        this.modify.id=category.id
        this.modify.categoryName=category.categoryName
        this.modify.diskDriver=category.diskDriver
        this.modify.networkDriver=category.networkDriver
      }else{
        this.title="创建系统分类"
        this.modify.id=0
        this.modify.categoryName=""
        this.modify.diskDriver="virtio"
        this.modify.networkDriver="virtio"
      }
      this.dialog_visible=true
    },
    modify_group(){
      if (this.modify.id === 0) {
        this.post_data(`/management/os/category/create`, this.modify).then(res => {
          if (res.data.code === 0) {
            this.$emit("on_create", res.data.data)
            this.dialog_visible = false
          }
        })
      }else {
        this.post_data(`/management/os/category/modify`, this.modify).then(res => {
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