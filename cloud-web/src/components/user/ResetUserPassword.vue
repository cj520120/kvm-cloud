<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form ref="create_project_service_ref" :model="modify" label-position="right" label-width="80px">

      <el-form-item label="登录密码" prop="password">
        <el-input type="password" v-model="modify.password" :show-password="true"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="modify.name==''" @click="modify_user">确 定</el-button>
        <el-button @click="dialog_visible = false"  >取 消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
export default {
  name: "ResetUserPassword",
  data(){
    return {
      modify: {
        userId: 0,
        password:""
      },
      dialog_visible: false,
      title: ""
    }
  },
  methods:{
    init_data(user){
      this.title="重置密码"
      this.modify.password=""
      this.modify.userId=user.userId
      this.dialog_visible=true
    },
    modify_user(){
        this.post_data(`/management/user/reset/password`, this.modify).then(res => {
          if (res.data.code === 0) {
            this.$emit("on_modify", res.data.data)
            this.dialog_visible = false
          }
        })
    }
  }
}
</script>

<style scoped>

</style>