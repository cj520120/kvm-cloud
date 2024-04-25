<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="on_back_click()" content="导入密钥" style="color: #409eff"></el-page-header>
		</el-row>
		<el-row>
			<el-form ref="createForm" :model="import_ssh" label-width="100px" class="demo-ruleForm">
				<el-form-item label="名称" prop="name">
					<el-input v-model="import_ssh.name"></el-input>
				</el-form-item>
				<el-form-item label="公钥内容" prop="key">
					<el-input v-model="import_ssh.publciKey" type="textarea" :autosize="{ minRows: 10, maxRows: 1000 }"></el-input>
				</el-form-item>
				<el-form-item label="私钥内容" prop="key">
					<el-input v-model="import_ssh.private" type="textarea" :autosize="{ minRows: 10, maxRows: 1000 }"></el-input>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="import_ssh_click">导入</el-button>
					<el-button @click="on_back_click">取消</el-button>
				</el-form-item>
			</el-form>
		</el-row>
	</el-card>
</template>
<script>
import { importSSh } from '@/api/api'
export default {
	data() {
		return {
			import_ssh: {
				name: '',
				publciKey: '',
				privateKey: ''
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
		import_ssh_click() {
			importSSh(this.import_ssh).then((res) => {
				if (res.code === 0) {
					this.on_notify_update_sshAuthorized(res.data)
					this.on_back_click()
				} else {
					this.$notify.error({
						title: '错误',
						message: `导入密钥失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>