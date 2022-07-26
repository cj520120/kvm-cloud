<template>
	<div id="main" class="login-container">
		<el-form ref="ruleForm2" class="demo-ruleForm login-page" label-position="left" label-width="0px" status-icon v-if="show_type === 'Login' && !this.oauth.enalbe">
			<h3 class="title">Roamblue KVM Cloud Login</h3>
			<el-form-item prop="username">
				<el-input v-model="loginData.loginName" auto-complete="off" autofocus="true" placeholder="用户名" prefix-icon="el-icon-s-custom" type="text"></el-input>
			</el-form-item>
			<el-form-item prop="password">
				<el-input v-model="loginData.password" :show-password="true" auto-complete="off" placeholder="密码" prefix-icon="el-icon-lock" type="password" @keyup.enter.native="login"></el-input>
			</el-form-item>
			<el-form-item style="width: 100%">
				<el-button :disabled="loginData.loginName == '' || loginData.password == ''" :loading="loading" style="width: 100%" type="primary" @click="login">登录</el-button>
			</el-form-item>
		</el-form>

		<el-form class="login-page" label-position="left" label-width="0px" status-icon v-if="show_type === 'Loading'">
			<div class="title">{{ this.oauth.title }}</div>
			<div style="height: 80px; font-size: 30px; margin-bottom: 10px" v-loading="true" element-loading-spinner="el-icon-loading" element-loading-text="正在登录,请稍后..."></div>
		</el-form>
		<el-form class="login-page" label-position="left" label-width="0px" status-icon v-if="show_type === 'Error'">
			<div class="title">{{ this.oauth.title }}</div>
			<div>
				<strong style="color: red">{{ this.errorMessage }}</strong>
			</div>
			<div class="register"><el-link type="primary" @click="go_login()">重新登录</el-link></div>
		</el-form>
	</div>
</template>

<script>
export default {
	name: 'Login',
	data() {
		return {
			loginData: {
				loginName: '',
				password: ''
			},
			oauth: {
				enable: false,
				title: ' Oauth2 登录'
			},
			show_type: '',
			errorMessage: '',
			loading: false
		}
	},
	mounted() {
		this.axios_get('/management/config').then((res) => {
			if (res.data.code === 0) {
				if (res.data.data.oauth) {
					this.oauth.title = res.data.data.oauth.title
					this.oauth.enable = res.data.data.oauth.enable
				}
				if (this.oauth.enable) {
					let code = this.$route.query.code
					if (code && code !== '') {
						this.post_data(`/login`, { code: code }).then((res) => {
							if (res.data.code !== 0) {
								this.errorMessage = `登录失败,服务器返回如下:\r\n${res.data.message}`
								this.show_type = 'Error'
							} else {
								this.setToken(res.data.data.token)
								this.$router.push({ path: '/' })
							}
						})
					} else {
						this.go_login()
					}
				} else {
					this.show_type = 'Login'
				}
			}
		})
	},
	methods: {
		login() {
			this.loading = true
			this.axios_get(`/management/login/signature?loginName=${this.loginData.loginName}`).then((res) => {
				if (res.data.code === 0) {
					let pwd = window.sha256_digest(window.sha256_digest(this.loginData.password + ':' + res.data.data.signature) + ':' + res.data.data.nonce)
					this.post_data(`/management/login`, { loginName: this.loginData.loginName, password: pwd, nonce: res.data.data.nonce }).then((res) => {
						this.loading = false
						if (res.data.code === 0) {
							this.setToken(res.data.data.token)
							this.$router.push({ path: '/' })
						}
					})
				} else {
					this.loading = false
				}
			})
		},
		go_login() {
			window.location.href = process.env.NODE_ENV === 'production' ? '/login' : 'http://localhost:8080/login'
		},
		show_login() {
			this.show_type = 'Login'
		}
	}
}
</script>

<style scoped>
.login-container {
	width: 100%;
	height: 100%;
	background-image: url('../assets/image/login_bg.png');
	background-size: cover;
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
	color: #409eff;
}

#wjPwd:hover {
	color: orange;
}
</style>