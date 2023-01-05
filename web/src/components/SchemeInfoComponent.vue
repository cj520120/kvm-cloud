<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="on_back_click" content="计算方案详情" v-loading="schme_loading"></el-page-header>
		</el-row>
		<el-row style="text-align: left; margin: 20px 0">
			<el-button @click="destroy_scheme(show_scheme)" type="danger" size="mini">删除</el-button>
		</el-row>
		<el-row>
			<el-descriptions :column="2" size="medium" border>
				<el-descriptions-item label="ID">{{ show_scheme.schemeId }}</el-descriptions-item>
				<el-descriptions-item label="名称">{{ show_scheme.name }}</el-descriptions-item>
				<el-descriptions-item label="配额">{{ show_scheme.speed }}</el-descriptions-item>
				<el-descriptions-item label="CPU">{{ show_scheme.cpu }}</el-descriptions-item>
				<el-descriptions-item label="内存">{{ get_memory_desplay_size(show_scheme.memory) }}</el-descriptions-item>
				<el-descriptions-item label="Cores">{{ show_scheme.cores }}</el-descriptions-item>
				<el-descriptions-item label="Sockets">{{ show_scheme.sockets }}</el-descriptions-item>
				<el-descriptions-item label="Threads">{{ show_scheme.threads }}</el-descriptions-item>
			</el-descriptions>
		</el-row>
	</el-card>
</template>
<script>
import { destroyScheme, getSchemeInfo } from '@/api/api'
import util from '@/api/util'
export default {
	data() {
		return {
			schme_loading: false,
			show_scheme: { schemeId: 0, name: '-', cpu: 0, memory: 0, speed: 0, sockets: 0, cores: 0, threads: 0 }
		}
	},
	mixins: [util],
	methods: {
		on_back_click() {
			this.$emit('back')
		},
		on_notify_update_scheme_info(host) {
			this.refresh_scheme(host)
			this.$emit('onSchemeUpdate', host)
		},
		refresh_scheme(scheme) {
			if (this.show_scheme.schemeId === scheme.schemeId) {
				this.show_scheme = scheme
			}
		},
		init_scheme(scheme) {
			this.show_scheme = scheme
			this.schme_loading = false
		},
		async init(schemeId) {
			this.schme_loading = true
			await getSchemeInfo({ schemeId: schemeId })
				.then((res) => {
					if (res.code === 0) {
						this.init_scheme(res.data)
					} else {
						this.$alert(`获取计算方案信息失败:${res.message}`, '提示', {
							dangerouslyUseHTMLString: true,
							confirmButtonText: '返回',
							type: 'error'
						})
							.then(() => {
								this.on_back_click()
							})
							.catch(() => {
								this.on_back_click()
							})
					}
				})
				.finally(() => {
					this.host_loading = false
				})
		},
		destroy_scheme(scheme) {
			this.$confirm('删除计算方案, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					destroyScheme({ schemeId: scheme.schemeId }).then((res) => {
						if (res.code === 0) {
							this.on_back_click()
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除计算方案失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>