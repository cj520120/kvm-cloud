<template>
  <el-dialog :title="title" :visible.sync="dialog_visible" center width="400px"  :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form ref="create_project_service_ref" :model="modify" label-position="right" label-width="80px">
      <el-form-item label="登录名称" prop="loginName">
        <el-input v-model="modify.loginName"></el-input>
      </el-form-item>
      <el-form-item label="登录密码" prop="password">
        <el-input type="password" v-model="modify.password" :show-password="true"></el-input>
      </el-form-item>
      <el-form-item label="用户权限">
        <el-select v-model="modify.rule" style="width: 200px">
          <el-option
              v-for="rule in all_rules"
              :key="rule.id"
              :label="rule.name"
              :value="rule.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="modify.name==''" @click="modify_user">确 定</el-button>
        <el-button @click="dialog_visible = false" >取 消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
export default {
  name: "RegisterUser",
  data(){
    return {
      modify: {
        loginName: "",
        password:"",
        rule:""
      },
      dialog_visible: false,
      title: ""
    }
  },
  methods:{
    init_data(all_rules){
      this.title="注册用户"
      this.modify.loginName=""
      this.modify.password=""
      this.modify.rule=""
      this.all_rules=all_rules
      if(this.all_rules.length>0){
        this.modify.rule=this.all_rules[0].id
      }
      this.dialog_visible=true
    },
    modify_user(){
        this.post_data(`/management/user/register`, this.modify).then(res => {
          if (res.data.code === 0) {
            this.$emit("on_create", res.data.data)
            this.dialog_visible = false
          }
        })
    }
  }
}
</script>

<style scoped>

</style>