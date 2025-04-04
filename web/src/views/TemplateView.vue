<template>
	<div>
		<el-container>
			<el-main>
				<el-card class="box-card" v-show="this.show_type === 0">
					<el-row slot="header" class="clearfix" style="height: 20px">
						<el-select v-model="selectTemplateType" placeholder="请选择" size="mini">
							<el-option v-for="item in templatesFilterType" :key="item.value" :label="item.label" :value="item.value"></el-option>
						</el-select>
						<el-button size="mini" type="primary" icon="el-icon-circle-plus-outline" @click="show_create_template">创建模版</el-button>
					</el-row>
					<el-row>
						<el-table :v-loading="data_loading" :data="show_table_templates" style="width: 100%" border>
							<el-table-column label="ID" prop="templateId" width="80" />
							<el-table-column label="名称" prop="name" min-width="200" show-overflow-tooltip />
							<el-table-column label="类型" prop="type" width="100">
								<template #default="scope">
									<el-tag>{{ get_template_type(scope.row) }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="状态" prop="status" width="120">
								<template #default="scope">
									<el-tag :type="scope.row.status === 2 ? 'success' : 'danger'">{{ get_template_status(scope.row) }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="操作" width="400">
								<template #default="scope">
									<el-button @click="show_template_script(scope.row)" type="" size="mini" v-if="scope.row.templateType === 2">初始化脚本</el-button>
									<el-button @click="show_template_info(scope.row)" type="" size="mini">模版详情</el-button>
									<el-button @click="download_template(scope.row)" type="warning" size="mini" v-if="scope.row.uri.indexOf('http://') === 0 || scope.row.uri.indexOf('https://') === 0">重新下载</el-button>
									<el-button @click="destroy_template(scope.row)" type="danger" size="mini">销毁模版</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-row>
				</el-card>
				<el-card class="box-card" v-show="this.show_type === 1">
					<el-row slot="header">
						<el-page-header @back="show_template_list" content="模版详情"></el-page-header>
					</el-row>
					<el-row style="text-align: left; margin: 20px 0">
						<el-button @click="download_template(show_template)" type="warning" size="mini" v-if="show_template.uri && (show_template.uri.indexOf('http://') === 0 || show_template.uri.indexOf('https://') === 0)">重新下载</el-button>
						<el-button @click="destroy_template(show_template)" type="danger" size="mini">销毁模版</el-button>
					</el-row>
					<el-row>
						<el-descriptions :column="2" size="medium" border>
							<el-descriptions-item label="ID">{{ show_template.templateId }}</el-descriptions-item>
							<el-descriptions-item label="模版名">{{ show_template.name }}</el-descriptions-item>
							<el-descriptions-item label="下载地址" v-if="show_template.uri && show_template.uri.indexOf('http') === 0">{{ show_template.uri }}</el-descriptions-item>
							<el-descriptions-item label="MD5" v-if="show_template.uri && show_template.uri.indexOf('http') === 0">{{ show_template.md5 }}</el-descriptions-item>
							<el-descriptions-item label="模版类型">
								<el-tag>{{ get_template_type(show_template) }}</el-tag>
							</el-descriptions-item>
							<el-descriptions-item label="状态">
								<el-tag :type="show_template.status === 2 ? 'success' : 'danger'">{{ get_template_status(show_template) }}</el-tag>
							</el-descriptions-item>
						</el-descriptions>
					</el-row>
				</el-card>
				<el-card class="box-card" v-show="this.show_type === 2">
					<el-row slot="header">
						<el-page-header @back="show_template_list()" content="创建模版" style="color: #409eff"></el-page-header>
					</el-row>
					<el-row>
						<el-form ref="createForm" :model="create_template" label-width="100px" class="demo-ruleForm">
							<el-form-item label="名称" prop="name">
								<el-input v-model="create_template.name"></el-input>
							</el-form-item>
							<el-form-item label="模版类型" prop="templateType">
								<el-select v-model="create_template.templateType" style="width: 100%">
									<el-option label="ISO" :value="0"></el-option>
									<el-option label="系统模版" :value="1"></el-option>
									<el-option label="用户模版" :value="2"></el-option>
								</el-select>
							</el-form-item>
							<el-form-item label=" 下载地址" prop="uri">
								<el-input v-model="create_template.uri"></el-input>
							</el-form-item>
							<el-form-item label="文件MD5" prop="uri">
								<el-input v-model="create_template.md5"></el-input>
							</el-form-item>
							<el-form-item label="初始化脚本" v-if="create_template.templateType === 2">
								<el-input v-model="create_template.script" :rows="10" type="textarea"></el-input>
							</el-form-item>
							<el-form-item>
								<el-button type="primary" @click="create_template_click">立即创建</el-button>
								<el-button @click="show_template_list">取消</el-button>
							</el-form-item>
						</el-form>
					</el-row>
				</el-card>
			</el-main>
		</el-container>
		<ShowTemplateScript ref="ShowTemplateScriptComponentRef" />
	</div>
</template>
<script>
import { getTemplateList, downloadTemplate, destroyTemplate, createTemplate } from '@/api/api'
import Notify from '@/api/notify'
import util from '@/api/util'
import ShowTemplateScript from '@/components/ShowTemplateScript.vue'
export default {
	name: 'templateView',
	components: { ShowTemplateScript },
	data() {
		return {
			data_loading: false,
			show_type: 0,
			show_template: {},
			create_template: {
				name: '',
				templateType: 0,
				uri: '',
				md5: '',
				script: '#cloud-config\n'
			},
			templates: [],
			templatesFilterType: [
				{
					value: -1,
					label: '全部'
				},
				{
					value: 0,
					label: 'ISO镜像文件'
				},
				{
					value: 1,
					label: '系统模版'
				},
				{
					value: 2,
					label: '用户模版'
				}
			],
			selectTemplateType: -1
		}
	},
	mixins: [Notify, util],
	mounted() {
		this.init_view()
	},
	created() {
		this.subscribe_notify(this.$options.name, this.dispatch_notify_message)
		this.subscribe_connect_notify(this.$options.name, this.reload_page)
		this.init_notify()
	},
	beforeDestroy() {
		this.unsubscribe_notify(this.$options.name)
		this.unsubscribe_connect_notify(this.$options.name)
	},
	computed: {
		show_table_templates() {
			return this.templates.filter((v) => {
				return this.selectTemplateType === -1 || v.templateType === this.selectTemplateType
			})
		}
	},
	methods: {
		async reload_page() {
			this.init_view()
		},
		async init_view() {
			this.data_loading = true
			await getTemplateList()
				.then((res) => {
					if (res.code == 0) {
						this.templates = res.data
					}
				})
				.finally(() => {
					this.data_loading = false
				})
		},
		show_template_list() {
			this.show_type = 0
		},
		show_create_template() {
			if (this.$refs['createForm']) {
				this.$refs['createForm'].resetFields()
			}
			this.show_type = 2
		},
		show_template_info(template) {
			this.show_template = template
			this.show_type = 1
		},
		show_template_script(template) {
			this.$refs.ShowTemplateScriptComponentRef.init(template)
		},
		update_template_info(template) {
			let findIndex = this.templates.findIndex((item) => item.templateId === template.templateId)
			if (findIndex >= 0) {
				this.$set(this.templates, findIndex, template)
			} else {
				this.templates.push(template)
			}
			if (this.show_template && this.show_template.templateId === template.templateId) {
				this.show_template = template
			}
		},
		dispatch_notify_message(notify) {
			if (notify.type === 5) {
				let res = notify.data
				if (res.code == 0) {
					this.update_template_info(res.data)
				} else if (res.code == 5000001) {
					let findIndex = this.templates.findIndex((v) => v.templateId === notify.id)
					if (findIndex >= 0) {
						this.templates.splice(findIndex, 1)
					}
				}
			}
		},
		create_template_click() {
			let data = {
				name: this.create_template.name,
				templateType: this.create_template.templateType,
				uri: this.create_template.uri,
				md5: this.create_template.md5,
				script: this.create_template.script
			}
			createTemplate(data).then((res) => {
				if (res.code === 0) {
					this.update_template_info(res.data)
					this.show_type = 0
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建模版失败:${res.message}`
					})
				}
			})
		},
		download_template(template) {
			this.$confirm('重新下载模版, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					downloadTemplate({ templateId: template.templateId }).then((res) => {
						if (res.code === 0) {
							this.update_template_info(res.data)
						} else {
							this.$notify.error({
								title: '错误',
								message: `重新下载模版失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		destroy_template(template) {
			this.$confirm('删除模版, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					destroyTemplate({ templateId: template.templateId }).then((res) => {
						if (res.code === 0) {
							this.update_template_info(res.data)
							this.show_type = 0
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除模版失败:${res.message}`
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