<template>
	<div>
		<HeadViewVue />
		<el-container>
			<el-aside width="200px">
				<NavViewVue current="Scheme" />
			</el-aside>
			<el-main>
				<el-card class="box-card" v-if="this.show_type === 0">
					<el-row slot="header" class="clearfix" style="height: 20px">
						<el-button style="float: left; padding: 3px 0" type="text" @click="show_create_scheme">创建计算方案</el-button>
					</el-row>
					<el-row>
						<el-table :v-loading="data_loading" :data="schemes" style="width: 100%">
							<el-table-column label="ID" prop="schemeId" width="80" />
							<el-table-column label="名称" prop="name" width="300" />
							<el-table-column label="CPU" prop="cpu" width="120" />
							<el-table-column label="内存" prop="memory" width="200">
								<template #default="scope">
									{{ get_memory_desplay(scope.row.memory) }}
								</template>
							</el-table-column>
							<el-table-column label="操作" min-width="380">
								<template #default="scope">
									<el-button @click="show_scheme_info(scope.row)" type="" size="mini">详情</el-button>
									<el-button @click="show_modify_scheme(scope.row)" type="" size="mini">修改</el-button>
									<el-button @click="destroy_scheme(scope.row)" type="danger" size="mini">删除</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-row>
				</el-card>
				<el-card class="box-card" v-if="this.show_type === 1">
					<el-row slot="header">
						<el-page-header @back="show_scheme_list" content="计算方案详情"></el-page-header>
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
							<el-descriptions-item label="内存">{{ get_memory_desplay(show_scheme.memory) }}</el-descriptions-item>
							<el-descriptions-item label="Cores">{{ show_scheme.cores }}</el-descriptions-item>
							<el-descriptions-item label="Sockets">{{ show_scheme.sockets }}</el-descriptions-item>
							<el-descriptions-item label="Threads">{{ show_scheme.threads }}</el-descriptions-item>
						</el-descriptions>
					</el-row>
				</el-card>
				<el-card class="box-card" v-if="this.show_type === 2">
					<el-row slot="header">
						<el-page-header @back="show_scheme_list()" content="创建计算方案" style="color: #409eff"></el-page-header>
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
								<el-button @click="show_scheme_list">取消</el-button>
							</el-form-item>
						</el-form>
					</el-row>
				</el-card>
				<el-card class="box-card" v-if="this.show_type === 3">
					<el-row slot="header">
						<el-page-header @back="show_scheme_list()" content="修改计算方案" style="color: #409eff"></el-page-header>
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

							<el-form-item label="配额" prop="speed">
								<el-input v-model="modify_scheme.speed"></el-input>
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
								<el-button @click="show_type = 0">取消</el-button>
							</el-form-item>
						</el-form>
					</el-row>
				</el-card>
			</el-main>
		</el-container>
	</div>
</template>
<script>
import { getSchemeList, destroyScheme, createScheme, moidfyScheme } from '@/api/api'

import NavViewVue from './NavView.vue'
import HeadViewVue from './HeadView.vue'
export default {
	name: 'schemeView',
	components: {
		NavViewVue,
		HeadViewVue
	},
	data() {
		return {
			data_loading: false,
			show_type: 0,
			show_scheme: {},
			create_scheme: {
				name: '',
				cpu: 1,
				memory: 512,
				speed: 0,
				sockets: 0,
				cores: 0,
				threads: 0
			},
			modify_scheme: {
				schemeId: 0,
				name: '',
				cpu: 1,
				memory: 512,
				speed: 0,
				sockets: 0,
				cores: 0,
				threads: 0
			},
			schemes: []
		}
	},
	created() {
		this.init_view()
	},
	methods: {
		async init_view() {
			this.data_loading = true
			await getSchemeList()
				.then((res) => {
					if (res.code == 0) {
						this.schemes = res.data
					}
				})
				.finally(() => {
					this.data_loading = false
				})
		},
		show_scheme_list() {
			this.show_type = 0
		},
		show_create_scheme() {
			this.show_type = 2
		},
		show_modify_scheme(scheme) {
			this.modify_scheme.schemeId = scheme.schemeId
			this.modify_scheme.name = scheme.name
			this.modify_scheme.cpu = scheme.cpu
			this.modify_scheme.memory = scheme.memory / 1024
			this.modify_scheme.speed = scheme.speed
			this.modify_scheme.sockets = scheme.sockets
			this.modify_scheme.cores = scheme.cores
			this.modify_scheme.threads = scheme.threads
			this.show_type = 3
		},
		show_scheme_info(scheme) {
			this.show_scheme = scheme
			this.show_type = 1
		},
		update_scheme_info(scheme) {
			let findIndex = this.schemes.findIndex((item) => item.schemeId === scheme.schemeId)
			if (findIndex >= 0) {
				this.$set(this.schemes, findIndex, scheme)
			} else {
				this.schemes.push(scheme)
			}
			if (this.show_scheme && this.show_scheme.schemeId === scheme.schemeId) {
				this.show_scheme = scheme
			}
		},
		create_scheme_click() {
			createScheme(this.create_scheme).then((res) => {
				if (res.code === 0) {
					this.update_scheme_info(res.data)
					this.show_type = 0
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建计算方案失败:${res.message}`
					})
				}
			})
		},
		modify_scheme_click() {
			moidfyScheme(this.modify_scheme).then((res) => {
				if (res.code === 0) {
					this.update_scheme_info(res.data)
					this.show_type = 0
				} else {
					this.$notify.error({
						title: '错误',
						message: `修改计算方案失败:${res.message}`
					})
				}
			})
		},
		get_memory_desplay(memory) {
			if (memory > 1024 * 1024) {
				return parseInt(memory / (1024 * 1024)) + ' GB'
			} else if (memory > 1024) {
				return parseInt(memory / 1024) + '  MB'
			}
		},
		destroy_scheme(scheme) {
			destroyScheme({ schemeId: scheme.schemeId }).then((res) => {
				if (res.code === 0) {
					let findIndex = this.schemes.findIndex((item) => item.schemeId === scheme.schemeId)
					if (findIndex >= 0) {
						this.schemes.splice(findIndex, 1)
					}
				} else {
					this.$notify.error({
						title: '错误',
						message: `删除计算方案失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>
<style lang="postcss" scoped>
.table_action button {
	margin: 0.1em;
}
</style>