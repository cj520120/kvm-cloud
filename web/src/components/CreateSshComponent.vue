<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="on_back_click()" content="创建密钥" style="color: #409eff"></el-page-header>
		</el-row>
		<el-row>
			<el-form ref="createForm" :model="create_ssh" label-width="100px" class="demo-ruleForm">
				<el-form-item label="名称" prop="name">
					<el-input v-model="create_ssh.name"></el-input>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="create_ssh_click">立即创建</el-button>
					<el-button @click="on_back_click">取消</el-button>
				</el-form-item>
			</el-form>
		</el-row>
	</el-card>
</template>
<script>
import { createSSh } from '@/api/api'
export default {
	data() {
		return {
			create_ssh: {
				name: ''
			}
		}
	},
	methods: {
		on_back_click() {
			this.$emit('back')
		},
		on_notify_update_sshAuthorized(host) {
			this.$emit('onSshUpdate', host)
		},
		init() {
			if (this.$refs['createForm']) {
				this.$refs['createForm'].resetFields()
			}
		},
		create_ssh_click() {
			createSSh(this.create_ssh).then((res) => {
				if (res.code === 0) {
					this.on_notify_update_sshAuthorized(res.data)
					this.on_back_click()
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建密钥失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>