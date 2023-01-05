<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="on_back_click()" content="创建计算方案" style="color: #409eff"></el-page-header>
		</el-row>
		<el-row>
			<el-form ref="createForm" :model="create_scheme" label-width="100px" class="demo-ruleForm">
				<el-form-item label="名称" prop="name">
					<el-input v-model="create_scheme.name"></el-input>
				</el-form-item>
				<el-form-item label="CPU" prop="cpu">
					<el-input v-model="create_scheme.cpu"></el-input>
				</el-form-item>
				<el-form-item label="内存(MB)" prop="memory">
					<el-input v-model="create_scheme.memory"></el-input>
				</el-form-item>

				<el-form-item label="配额" prop="speed">
					<el-input v-model="create_scheme.speed"></el-input>
				</el-form-item>
				<el-form-item label="Cores" prop="cores">
					<el-input v-model="create_scheme.cores"></el-input>
				</el-form-item>
				<el-form-item label="Sockets" prop="sockets">
					<el-input v-model="create_scheme.sockets"></el-input>
				</el-form-item>
				<el-form-item label="Threads" prop="threads">
					<el-input v-model="create_scheme.threads"></el-input>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="create_scheme_click">立即创建</el-button>
					<el-button @click="on_back_click">取消</el-button>
				</el-form-item>
			</el-form>
		</el-row>
	</el-card>
</template>
<script>
import { createScheme } from '@/api/api'
export default {
	data() {
		return {
			create_scheme: {
				name: '',
				cpu: 1,
				memory: 512,
				speed: 0,
				sockets: 0,
				cores: 0,
				threads: 0
			}
		}
	},
	methods: {
		on_back_click() {
			this.$emit('back')
		},
		on_notify_update_scheme(host) {
			this.$emit('onSchemeUpdate', host)
		},
		init() {
			if (this.$refs['createForm']) {
				this.$refs['createForm'].resetFields()
			}
		},
		create_scheme_click() {
			createScheme(this.create_scheme).then((res) => {
				if (res.code === 0) {
					this.on_notify_update_scheme(res.data)
					this.on_back_click()
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建计算方案失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>