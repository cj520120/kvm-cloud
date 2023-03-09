<template>
	<el-header>
		<div style="float: left; background-color: #34495e; height: 100%">
			<img src="../assets/logo.png" style="width: 32px; height: 32px; margin-left: 10px; margin-top: 15px" />
		</div>
		<div style="float: left; background-color: #34495e; height: 100%">
			<h5 style="font-size: 18px; margin: 20px 0px 20px 10px; color: white">KVM Cloud</h5>
		</div>
		<el-menu class="headerMenu" mode="horizontal" @select="handleSelect" background-color="#34495e" text-color="#fff" active-text-color="#ffd04b">
			<el-menu-item index="quit">退出</el-menu-item>
			<el-menu-item index="password">修改密码</el-menu-item>
		</el-menu>
		<el-dialog title="修改密码" :visible.sync="update_password_dialog_visible" center width="400px" :close-on-click-modal="false" :close-on-press-escape="false">
			<el-form :model="update_password" label-position="right" label-width="80px">
				<el-form-item label="旧密码" prop="oldPassword">
					<el-input v-model="update_password.oldPassword" type="password" :show-password="true"></el-input>
				</el-form-item>
				<el-form-item label="新密码" prop="newPassword">
					<el-input v-model="update_password.newPassword" type="password" :show-password="true"></el-input>
				</el-form-item>
				<el-form-item label="确认密码" prop="confirmPassword">
					<el-input v-model="update_password.confirmPassword" type="password" :show-password="true"></el-input>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="update_user_password_click">确 定</el-button>
					<el-button @click="update_password_dialog_visible = false">取 消</el-button>
				</el-form-item>
			</el-form>
		</el-dialog>
	</el-header>
</template>
  
<script>
import { getCurrentLoginSignature, modifyUserPassword } from '@/api/api'
export default {
	data() {
		return {
			update_password_dialog_visible: false,
			update_password: {
				oldPassword: '',
				newPassword: '',
				confirmPassword: '',
				nonce: ''
			}
		}
	},
	methods: {
		handleSelect(data) {
			switch (data) {
				case 'password':
					this.update_password.oldPassword = ''
					this.update_password.newPassword = ''
					this.update_password.confirmPassword = ''
					this.update_password.nonce = ''
					this.update_password_dialog_visible = true
					break
				case 'quit':
					this.$confirm(`退出登录, 是否继续?`, '提示', {
						confirmButtonText: '确定',
						cancelButtonText: '取消',
						type: 'warning'
					})
						.then(() => {
							localStorage.removeItem('X-Token')
							this.$router.push({ path: '/Login' })
						})
						.catch(() => {})
					break
			}
		},
		update_user_password_click() {
			if (this.update_password.newPassword != this.update_password.confirmPassword) {
				this.$notify.error({
					title: '错误',
					duration: 3000,
					message: `新密码与旧密码不一致`
				})
				return
			}
			getCurrentLoginSignature().then((res) => {
				if (res.code === 0) {
					let oldPassword = window.sha256_digest(window.sha256_digest(this.update_password.oldPassword + ':' + res.data.signature) + ':' + res.data.nonce)
					let nonce = res.data.nonce
					let newPassword = window.sha256_digest(this.update_password.newPassword + ':' + res.data.signature)
					let update_request = {
						oldPassword: oldPassword,
						newPassword: newPassword,
						nonce: nonce
					}
					modifyUserPassword(update_request).then((res) => {
						if (res.code !== 0) {
							this.$notify.error({
								title: '错误',
								message: `修改密码失败:${res.message}`
							})
						} else {
							this.update_password_dialog_visible = false
						}
					})
				} else {
					this.$notify.error({
						title: '错误',
						message: `修改密码失败:${res.message}`
					})
				}
			})
		}
	}
}
</script> 
<style scoped>
.el-header {
	padding: 0;
	width: 100%;
}
.headerMenu {
	display: flex;
	flex-direction: row-reverse;
}
</style>