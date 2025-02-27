<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="on_back_click()" content="修改计算方案" style="color: #409eff"></el-page-header>
		</el-row>
		<el-row>
			<el-form :model="modify_scheme" label-width="100px" class="demo-ruleForm">
				<el-form-item label="名称" prop="name">
					<el-input v-model="modify_scheme.name"></el-input>
				</el-form-item>
				<el-form-item label="CPU" prop="cpu">
					<el-input v-model="modify_scheme.cpu"></el-input>
				</el-form-item>
				<el-form-item label="内存(MB)" prop="memory">
					<el-input v-model="modify_scheme.memory"></el-input>
				</el-form-item>

				<el-form-item label="配额(Share)" prop="share">
					<el-input v-model="modify_scheme.share"></el-input>
				</el-form-item>
				<el-form-item label="Cores" prop="cores">
					<el-input v-model="modify_scheme.cores"></el-input>
				</el-form-item>
				<el-form-item label="Sockets" prop="sockets">
					<el-input v-model="modify_scheme.sockets"></el-input>
				</el-form-item>
				<el-form-item label="Threads" prop="threads">
					<el-input v-model="modify_scheme.threads"></el-input>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="modify_scheme_click">修改</el-button>
					<el-button @click="on_back_click">取消</el-button>
				</el-form-item>
			</el-form>
		</el-row>
	</el-card>
</template>
<script>
import { moidfyScheme } from '@/api/api'
export default {
	data() {
		return {
			modify_scheme: {
				schemeId: 0,
				name: '',
				cpu: 1,
				memory: 512,
				share: 0,
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
		init(scheme) {
			this.modify_scheme.schemeId = scheme.schemeId
			this.modify_scheme.name = scheme.name
			this.modify_scheme.cpu = scheme.cpu
			this.modify_scheme.memory = scheme.memory / 1024
			this.modify_scheme.speed = scheme.speed
			this.modify_scheme.sockets = scheme.sockets
			this.modify_scheme.cores = scheme.cores
			this.modify_scheme.threads = scheme.threads
		},

		modify_scheme_click() {
			this.$confirm('修改计算方案, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					moidfyScheme(this.modify_scheme).then((res) => {
						if (res.code === 0) {
							this.on_notify_update_scheme(res.data)
							this.on_back_click()
						} else {
							this.$notify.error({
								title: '错误',
								message: `修改计算方案失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>