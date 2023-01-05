<template>
	<div>
		<el-container>
			<el-main>
				<el-card class="box-card" v-show="this.show_type === 0">
					<el-row slot="header" class="clearfix" style="height: 20px">
						<el-button style="float: left; padding: 3px 0" type="text" @click="show_create_user_click">注册用户</el-button>
					</el-row>
					<el-row>
						<el-table :v-loading="data_loading" :data="users" style="width: 100%">
							<el-table-column label="ID" prop="userId" width="80" />
							<el-table-column label="用户名" prop="loginName" width="200" />
							<el-table-column label="状态" prop="state" width="180">
								<template #default="scope">
									<el-tag :type="scope.row.state === 0 ? 'success' : 'danger'">{{ scope.row.state === 0 ? '启用' : '禁用' }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="操作">
								<template #default="scope">
									<el-button @click="show_reset_password_click(scope.row)" type="" size="mini">重置密码</el-button>
									<el-button @click="update_user_state_click(scope.row, 0)" type="success" size="mini" v-if="scope.row.state === 1">启用</el-button>
									<el-button @click="update_user_state_click(scope.row, 1)" type="danger" size="mini" v-if="scope.row.state === 0">禁用</el-button>
									<el-button @click="destroy_user_click(scope.row)" type="danger" size="mini">删除</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-row>
				</el-card>

				<el-card class="box-card" v-show="this.show_type === 1">
					<el-row slot="header">
						<el-page-header @back="show_user_list()" content="注册用户" style="color: #409eff"></el-page-header>
					</el-row>
					<el-row>
						<el-form ref="createForm" :model="create_user" label-width="100px" class="demo-ruleForm">
							<el-form-item label="登录名" prop="loginName">
								<el-input v-model="create_user.loginName"></el-input>
							</el-form-item>
							<el-form-item label="密码" prop="password"><el-input v-model="create_user.password" show-password></el-input></el-form-item>

							<el-form-item>
								<el-button type="primary" @click="on_create_user_click">注册</el-button>
								<el-button @click="show_user_list">取消</el-button>
							</el-form-item>
						</el-form>
					</el-row>
				</el-card>
				<el-dialog title="重置密码" :visible.sync="reset_passwor_dialog_visiable" width="300px">
					<el-form ref="resetPasswordForm" :model="reset_password_user" label-width="60px">
						<el-form-item label="密码">
							<el-input v-model="reset_password_user.password" placeholder="请输入密码" show-password></el-input>
						</el-form-item>
					</el-form>
					<span slot="footer" class="dialog-footer">
						<el-button @click="reset_passwor_dialog_visiable = false">取 消</el-button>
						<el-button type="primary" @click="on_reset_password_click">确 定</el-button>
					</span>
				</el-dialog>
			</el-main>
		</el-container>
	</div>
</template>
<script>
import { destroyUser, getUserList, registerUser, resetUserPassword, updateUserState } from '@/api/api'
export default {
	name: 'userView',
	components: {},
	data() {
		return {
			data_loading: false,
			current_loading: false,
			reset_passwor_dialog_visiable: false,
			show_type: 0,
			create_user: {
				loginName: '',
				password: ''
			},
			reset_password_user: {
				userId: 0,
				password: ''
			},
			users: []
		}
	},

	mounted() {
		this.init_view()
		this.show_type = 0
	},
	methods: {
		async init_view() {
			this.data_loading = true
			await getUserList()
				.then((res) => {
					if (res.code == 0) {
						this.users = res.data
					}
				})
				.finally(() => {
					this.data_loading = false
				})
		},
		show_user_list() {
			this.show_type = 0
		},
		show_create_user_click() {
			if (this.$refs['createForm']) {
				this.$refs['createForm'].resetFields()
			}
			this.show_type = 1
		},
		show_reset_password_click(user) {
			if (this.$refs['resetPasswordForm']) {
				this.$refs['resetPasswordForm'].resetFields()
			}
			this.reset_password_user.userId = user.userId
			this.reset_password_user.password = ''
			this.reset_passwor_dialog_visiable = true
		},
		update_user_info(user) {
			let findIndex = this.users.findIndex((item) => item.userId === user.userId)
			if (findIndex >= 0) {
				this.$set(this.users, findIndex, user)
			} else {
				this.users.push(user)
			}
			if (this.show_user && this.show_user.userId === user.userId) {
				this.show_user = user
			}
		},
		on_create_user_click() {
			registerUser(this.create_user).then((res) => {
				if (res.code === 0) {
					this.update_user_info(res.data)
					this.show_type = 0
				} else {
					this.$notify.error({
						title: '错误',
						message: `注册用户失败:${res.message}`
					})
				}
			})
		},
		on_reset_password_click() {
			this.$confirm('重置密码, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					resetUserPassword(this.reset_password_user).then((res) => {
						if (res.code === 0) {
							this.update_user_info(res.data)
							this.reset_passwor_dialog_visiable = false
						} else {
							this.$notify.error({
								title: '错误',
								message: `重置密码失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		update_user_state_click(user, state) {
			let message = state === 0 ? '启用用户' : '禁用用户'
			this.$confirm(`${message}, 是否继续?`, '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					updateUserState({ userId: user.userId, state: state }).then((res) => {
						if (res.code === 0) {
							this.update_user_info(res.data)
						} else {
							this.$notify.error({
								title: '错误',
								message: `${message}失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		destroy_user_click(user) {
			this.$confirm('删除用户, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					destroyUser({ userId: user.userId }).then((res) => {
						if (res.code === 0) {
							let findIndex = this.users.findIndex((item) => item.userId === user.userId)
							if (findIndex >= 0) {
								this.users.splice(findIndex, 1)
							}
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除用户失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>
<style lang="postcss" scoped>
.table_action button {
	margin: 0.1em;
}
</style>