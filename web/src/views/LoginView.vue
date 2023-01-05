<template>
	<div id="main" class="login-container">
		<el-form ref="ruleForm2" class="demo-ruleForm login-page" label-position="left" label-width="0px" status-icon v-if="show_type === 'Login' && !this.oauth.enalbe">
			<h3 class="title">KVM Cloud Login</h3>
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
import { getLoginSignature, getSystemConfig, loginUser, oauth2Login } from '@/api/api'
export default {
	name: 'loginView',
	inject: ['check_full_screen'],
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
		this.check_full_screen(true)
		getSystemConfig().then((res) => {
			if (res.code === 0) {
				if (res.data.oauth) {
					this.oauth.title = res.data.oauth.title
					this.oauth.enable = res.data.oauth.enable
				}
				if (this.oauth.enable) {
					let code = this.$route.query.code
					if (code && code !== '') {
						oauth2Login({ code: code }).then((res) => {
							if (res.code !== 0) {
								this.errorMessage = `登录失败,服务器返回如下:\r\n${res.message}`
								this.show_type = 'Error'
							} else {
								localStorage.setItem('X-Token', res.data.token)
								this.$router.push({ path: '/' })
							}
						})
					} else {
						this.go_login()
					}
				} else {
					this.show_type = 'Login'
				}
			} else {
				this.show_type = 'Login'
			}
		})
	},
	beforeDestroy() {
		this.check_full_screen(false)
	},
	methods: {
		login() {
			this.loading = true
			getLoginSignature({ loginName: this.loginData.loginName })
				.then((res) => {
					if (res.code === 0) {
						let pwd = window.sha256_digest(window.sha256_digest(this.loginData.password + ':' + res.data.signature) + ':' + res.data.nonce)

						loginUser({ loginName: this.loginData.loginName, password: pwd, nonce: res.data.nonce })
							.then((res) => {
								if (res.code === 0) {
									localStorage.setItem('X-Token', res.data.token)
									this.$router.push({ path: '/' })
								} else {
									this.$notify.error({
										title: '错误',
										duration: 3000,
										message: `登录失败:${res.message}`
									})
								}
							})
							.finally(() => {
								this.loading = false
							})
					} else {
						this.$notify.error({
							title: '错误',
							duration: 3000,
							message: `登录失败:${res.message}`
						})
					}
				})
				.finally(() => {
					this.loading = false
				})
		},
		go_login() {
			window.location.href = process.env.NODE_ENV === 'production' ? '/login' : 'http://192.168.2.107:8080/login'
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
	background-image: url('../assets/login_bg.png');
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