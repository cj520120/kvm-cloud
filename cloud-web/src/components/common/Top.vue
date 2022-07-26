<template>
	<el-header class="header">
		<div class="box">
			<img src="../../assets/image/kl_logo.png" class="logo" />
			<span class="title">Roamblue Cloud</span>
		</div>
		<div style="display: inline">
			<el-dropdown @command="menu_change">
				<img class="user-menu" src="../../assets/image/icon.png" />
				<el-dropdown-menu slot="dropdown">
					<el-dropdown-item command="password" v-show="!this.oauth.enable">修改密码</el-dropdown-item>
					<el-dropdown-item command="logout">退出登录</el-dropdown-item>
				</el-dropdown-menu>
			</el-dropdown>
			<ChangePassword ref="ChangePasswordRef" />
		</div>
	</el-header>
</template>

<script>
import ChangePassword from './ChangePassword'
export default {
	name: 'Top.vue',
	components: { ChangePassword },
	mounted() {
		this.axios_get('/management/config').then((res) => {
			if (res.data.data.oauth) {
				this.oauth.enable = res.data.data.oauth.enable
			}
		})
		this.refresh_token()
		setInterval(this.refresh_token, 1000 * 60 * 60)
	},
	data() {
		return {
			oauth: {
				enable: true
			}
		}
	},
	methods: {
		menu_change(command) {
			if (command === 'logout') {
				localStorage.setItem('X-Token', '')
				this.$router.push({ path: '/login' })
			} else if (command == 'password') {
				this.$refs.ChangePasswordRef.dialog_visible = true
			}
		},
		refresh_token() {
			this.post_data(`/management/token/refresh`, {}).then((res) => {
				this.loading = false
				if (res.data.code === 0) {
					this.setToken(res.data.data.token)
				}
			})
		}
	}
}
</script>

<style scoped >
.header {
	color: white;
	font-weight: bold;
	width: 100%;
	height: 100%;
	background-color: #34495e;
	display: flex;
	align-items: center;
	justify-content: space-between;
}
.box {
	display: flex;
	align-items: center;
}
.logo {
	width: 40px;
	height: 40px;
}
.title {
	color: white;
	margin-left: 20px;
	font-size: 20px;
	font-weight: bold;
}
.user-menu {
	width: 40px;
	height: 40px;
}
</style>