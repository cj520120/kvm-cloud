<template>
  <div id="main" class="login-container">
    <el-form
        ref="ruleForm2"
        class="demo-ruleForm login-page"
        label-position="left"
        label-width="0px"
        status-icon>
      <h3 class="title">系统登录</h3>
      <el-form-item prop="username">
        <el-input v-model="loginData.loginName"
            auto-complete="off"
            autofocus="true"
            placeholder="用户名"
            prefix-icon="el-icon-s-custom"
            type="text"
        ></el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="loginData.password"
            :show-password="true"
            auto-complete="off"
            placeholder="密码"
            prefix-icon="el-icon-lock"
            type="password"
            @keyup.enter.native="login"
        ></el-input>
      </el-form-item>
      <el-form-item style="width:100%;">
        <el-button :disabled="loginData.loginName==''|| loginData.password==''" :loading="loading" style="width:100%;" type="primary" @click="login">登录</el-button>

      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      loginData: {

        loginName: "",
        password: ""
      },
      loading: false,
    }
  },
  methods: {
    login() {
      this.loading = true
      this.axios_get(`/management/login/signature?loginName=${this.loginData.loginName}`).then(res=>{
        if(res.data.code===0){
          let pwd=window.sha256_digest(window.sha256_digest(this.loginData.password + ":" + res.data.data.signature) + ":" + res.data.data.nonce)
          this.post_data(`/management/login`, {loginName:this.loginData.loginName,password: pwd,nonce:res.data.data.nonce}).then(res => {
              this.loading = false
              if (res.data.code === 0) {
                this.setToken(res.data.data.token)
                this.$router.push({path: "/Group"})
              }
          })
        }else{
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100%;
  background-image: url('../assets/image/login_bg.png');
  background-size: cover
}

.login-page {
  -webkit-border-radius: 5px;
  border-radius: 10px;
  margin: 0 auto;
  transform: translateY(180px);
  width: 350px;
  padding: 35px 35px 15px;
  background: #fff;
  border: 1px solid #eaeaea;
  box-shadow: 0 0 25px #cac6c6;
}

#wjPwd {
  text-decoration: none;
  color: #409EFF;
}

#wjPwd:hover {
  color: orange;
}
</style>