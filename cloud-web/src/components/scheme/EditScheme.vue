<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form ref="create_scheme_service_ref" :model="modify" label-position="right" label-width="80px">
      <el-form-item label="名称" prop="serviceName">
        <el-input v-model="modify.name"></el-input>
      </el-form-item>
      <el-form-item label="内存" prop="memory">
        <el-input v-model="modify.memory">
          <template slot="append">MB</template>
        </el-input>

      </el-form-item>
      <el-form-item label="内核数" prop="cpu">
        <el-input-number v-model="modify.cpu" :max="100" :min="1"></el-input-number>
      </el-form-item>
      <el-form-item label="主频" prop="memory">
        <el-input v-model="modify.speed">
          <template slot="append">MHZ</template>
        </el-input>
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
  name: "EditScheme",
  data(){
    return {
      modify: {
        id: 0,
        name: "",
        memory: "",
        cpu: 1,
        speed: 0,
      },
      dialog_visible: false,
      title: ""
    }
  },
  methods:{
    init_data(scheme){
      if(scheme){
        this.title="编辑计算方案"
        this.modify.id=scheme.id
        this.modify.name=scheme.name
        this.modify.memory=scheme.memory
        this.modify.cpu=scheme.cpu
        this.modify.speed=scheme.speed
      }else{
        this.title="创建计算方案"
        this.modify.id=0
        this.modify.name=""
        this.modify.memory="1024"
        this.modify.cpu=1
        this.modify.speed=0
      }
      this.dialog_visible=true
    },
    modify_group(){
      if (this.modify.id === 0) {
        this.post_data(`/management/calculation/scheme/create`, this.modify).then(res => {
          if (res.data.code === 0) {
            this.$emit("on_create", res.data.data)
            this.dialog_visible = false
          }
        })
      }else {
        this.post_data(`/management/calculation/scheme/modify`, this.modify).then(res => {
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