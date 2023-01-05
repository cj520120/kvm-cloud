<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="on_back_click()" content="创建主机" style="color: #409eff"></el-page-header>
		</el-row>
		<el-row>
			<el-form ref="createForm" :model="create_host" label-width="100px" class="demo-ruleForm">
				<el-form-item label="显示名称" prop="displayName">
					<el-input v-model="create_host.displayName"></el-input>
				</el-form-item>
				<el-form-item label="主机IP" prop="hostIp"><el-input v-model="create_host.hostIp"></el-input></el-form-item>

				<el-form-item label="网卡名称" prop="nic"><el-input v-model="create_host.nic"></el-input></el-form-item>

				<el-form-item label="通信地址" prop="uri"><el-input v-model="create_host.uri"></el-input></el-form-item>

				<el-form-item>
					<el-button type="primary" @click="create_host_click">立即创建</el-button>
					<el-button @click="on_back_click">取消</el-button>
				</el-form-item>
			</el-form>
		</el-row>
	</el-card>
</template>
<script>
import { createHost } from '@/api/api'
export default {
	data() {
		return {
			create_host: {
				displayName: '',
				hostIp: '',
				nic: '',
				uri: ''
			}
		}
	},
	methods: {
		on_back_click() {
			this.$emit('back')
		},
		on_notify_update_host_info(host) {
			this.$emit('onHostUpdate', host)
		},
		init() {
			if (this.$refs['createForm']) {
				this.$refs['createForm'].resetFields()
			}
		},
		create_host_click() {
			createHost(this.create_host).then((res) => {
				if (res.code === 0) {
					this.on_notify_update_host_info(res.data)
					this.on_back_click()
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建主机失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>