<template>

  <el-dialog :visible.sync="dialog_visible" center title="修改密码" width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form ref="change_password_ref" :model="this" label-position="right" label-width="80px">
      <el-form-item label="新密码" prop="oldPassword">
      <el-input v-model="oldPassword" type="password" :show-password="true"></el-input>
    </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="newPassword" type="password" :show-password="true"></el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="confirmPassword" type="password" :show-password="true"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary"  :disabled="oldPassword==''||newPassword==''|| confirmPassword==''"  :loading="loading" @click="change_password">修 改</el-button>
        <el-button @click="dialog_visible = false">取 消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
export default {
  name: "ChangePassword",
  data() {
    return {
      dialog_visible: false,
      oldPassword: "",
      newPassword: "",
      confirmPassword: "",
      loading:false
    }
  },
  methods: {
    change_password() {
      if (this.newPassword != this.confirmPassword) {
        this.$notify.error({
          title: '错误',
          duration: 3000,
          message: `新密码与旧密码不一致`,
        });
        return
      }
      this.loading=true
      this.axios_get('/management/signature').then(res=>{
        if(res.data.code===0){
          let oldPassword=window.sha256_digest(window.sha256_digest(this.oldPassword + ":" + res.data.data.signature) + ":" + res.data.data.nonce)
          let nonce=res.data.data.nonce
          let netPassword=window.sha256_digest(this.newPassword + ":" + res.data.data.signature)
          let request={
            oldPassword:oldPassword,
            newPassword:netPassword,
            nonce:nonce
          }
          this.post_data(`/management/password`, request).then(res => {
            this.loading=false
            if (res.data.code === 0) {
              this.$refs.change_password_ref.resetFields()
              this.dialog_visible = false
            }
          })
        }else{
          this.loading=false
        }
      })
    }
  }
}
</script>

<style scoped>

</style>