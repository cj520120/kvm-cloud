<template>
	<div>
		<el-card class="box-card">
			<el-row slot="header" class="clearfix" style="height: 20px">
				<el-form :inline="true" class="demo-form-inline">
					<el-form-item>
						<el-button size="mini" type="primary" icon="el-icon-circle-plus-outline" @click="show_create_dialog">创建配置</el-button>
					</el-form-item>
					<el-form-item>
						<el-input size="mini" style="float: right; max-width: 300px; margin-bottom: 10px" placeholder="请输入搜索关键字" v-model="keyword"></el-input>
					</el-form-item>
				</el-form>
			</el-row>
			<el-row>
				<el-table :v-loading="data_loading_visable" :data="show_filter_config_list" style="width: 100%" border>
					<el-table-column label="名称" prop="key" min-width="200" show-overflow-tooltip />
					<el-table-column label="描述" prop="description" min-width="200" show-overflow-tooltip />
					<el-table-column label="值" prop="value" min-width="200" show-overflow-tooltip />
					<el-table-column label="操作" width="300">
						<template #default="scope">
							<el-button @click="show_config_info_dialog(scope.row)" size="mini">查看</el-button>
							<el-button @click="show_edit_dialog(scope.row)" type="primary" size="mini">编辑</el-button>
							<el-button @click="delete_config(scope.row)" size="mini" type="danger" v-if="scope.row.id > 0 && !scope.row.defaultParam">删除</el-button>
							<el-button @click="delete_config(scope.row)" size="mini" type="danger" v-if="scope.row.id > 0 && scope.row.defaultParam">恢复默认</el-button>
						</template>
					</el-table-column>
				</el-table>
			</el-row>
		</el-card>

		<el-dialog title="查看配置项" :visible.sync="info_dialog_visable" width="50%">
			<el-form ref="create_dialog_form_ref" :model="show_config" label-width="50px" class="demo-ruleForm">
				<el-form-item label="名称" prop="key">
					<strong>{{ show_config.key }}</strong>
				</el-form-item>
				<el-form-item label="值" prop="value">
					<el-input type="textarea" :rows="20" readonly v-model="show_config.value" v-if="show_config.valueType === 1" style="font-family: monospace; white-space: pre; overflow-x: auto"></el-input>
					<strong v-if="show_config.valueType !== 1 && show_config.valueType !== 5">{{ show_config.value }}</strong>
				</el-form-item>
			</el-form>
			<span slot="footer" class="dialog-footer">
				<el-button @click="info_dialog_visable = false">取 消</el-button>
			</span>
		</el-dialog>
		<el-dialog title="创建配置项" :visible.sync="create_dialog_visable" width="50%">
			<el-form ref="create_dialog_form_ref" :model="create_config" label-width="50px" class="demo-ruleForm">
				<el-form-item label="名称" prop="key">
					<el-input v-model="create_config.key"></el-input>
				</el-form-item>
				<el-form-item label="值" prop="value">
					<el-input v-model="create_config.value" type="textarea" :rows="20"></el-input>
				</el-form-item>
			</el-form>
			<span slot="footer" class="dialog-footer">
				<el-button @click="create_dialog_visable = false">取 消</el-button>
				<el-button type="primary" @click="create_config_click" :disabled="create_config.key === ''">确 定</el-button>
			</span>
		</el-dialog>
		<el-dialog title="编辑配置项" :visible.sync="modify_dialog_visable" width="50%">
			<el-form :model="modify_config" label-width="50px" class="demo-ruleForm">
				<el-form-item label="名称" prop="key">
					<strong>{{ modify_config.key }}</strong>
				</el-form-item>
				<el-form-item label="值" prop="value">
					<el-input v-model="modify_config.value" v-if="modify_config.valueType === 0"></el-input>
					<el-input v-model="modify_config.value" type="textarea" :rows="20" v-if="modify_config.valueType === 1"></el-input>
					<el-input-number v-model="modify_config.value" v-if="modify_config.valueType === 2"></el-input-number>
					<el-input-number v-model="modify_config.value" :precision="2" v-if="modify_config.valueType === 3"></el-input-number>
					<el-select v-model="modify_config.value" placeholder="请选择" v-if="modify_config.valueType === 4">
						<el-option v-for="item in modify_config.valueOptions" :key="item" :label="item" :value="item"></el-option>
					</el-select>
				</el-form-item>
			</el-form>
			<span slot="footer" class="dialog-footer">
				<el-button @click="modify_dialog_visable = false">取 消</el-button>
				<el-button type="primary" @click="modify_config_click">确 定</el-button>
			</span>
		</el-dialog>
	</div>
</template>
<script>
import { getConfigList, modifyConfig, createConfig, destroyConfig } from '@/api/api'

export default {
	name: 'configComponent',

	data() {
		return {
			allocateType: 0,
			allocateId: 0,
			keyword: '',
			data_loading_visable: true,
			create_dialog_visable: false,
			modify_dialog_visable: false,
			info_dialog_visable: false,
			config_list: [],
			show_config: {},
			create_config: { key: '', value: '' },
			modify_config: { key: '', value: '' }
		}
	},
	computed: {
		show_filter_config_list() {
			return this.config_list.filter((v) => {
				let hasKeyword = true
				let searchKeyword = this.keyword.trim().toLowerCase()
				if (searchKeyword !== '') {
					hasKeyword = v.description.toLowerCase().indexOf(searchKeyword) >= 0 || v.key.indexOf(searchKeyword) >= 0
				}
				if (hasKeyword && v.intern) {
					console.log(v)
					hasKeyword = v.id > 0
				}
				return hasKeyword
			})
		}
	},
	methods: {
		async init(allocateType, allocateId) {
			this.allocateType = allocateType
			this.allocateId = allocateId
			this.data_loading = true
			await getConfigList({ allocateId: this.allocateId, allocateType: this.allocateType })
				.then((res) => {
					if (res.code == 0) {
						this.config_list = res.data
					}
				})
				.finally(() => {
					this.data_loading = false
				})
		},
		show_config_info_dialog(config) {
			this.show_config = config
			this.info_dialog_visable = true
		},
		show_create_dialog() {
			this.create_config.key = ''
			this.create_config.value = ''
			this.create_dialog_visable = true
		},
		show_edit_dialog(config) {
			this.modify_config = config
			this.modify_dialog_visable = true
		},
		update_config_info(config) {
			let findIndex = this.config_list.findIndex((item) => item.key === config.key && item.allocateType === config.allocateType && item.allocateId === config.allocateId)
			if (findIndex >= 0) {
				this.$set(this.config_list, findIndex, config)
			} else {
				this.config_list.push(config)
			}
		},
		create_config_click() {
			let data = {
				configKey: this.create_config.key,
				configValue: this.create_config.value,
				allocateType: this.allocateType,
				allocateId: this.allocateId
			}
			createConfig(data).then((res) => {
				if (res.code === 0) {
					this.update_config_info(res.data)
					this.create_dialog_visable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建配置失败:${res.message}`
					})
				}
			})
		},
		modify_config_click() {
			let data = {
				configKey: this.modify_config.key,
				configValue: this.modify_config.value,
				allocateType: this.allocateType,
				allocateId: this.allocateId
			}
			modifyConfig(data).then((res) => {
				if (res.code === 0) {
					this.update_config_info(res.data)
					this.modify_dialog_visable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建配置失败:${res.message}`
					})
				}
			})
		},
		delete_config(config) {
			this.$confirm('删除配置, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(() => {
				destroyConfig({ id: config.id }).then((res) => {
					if (res.code === 0) {
						let findIndex = this.config_list.findIndex((item) => item.key === config.key && item.allocateType === config.allocateType && item.allocateType === config.allocateId)
						if (res.data && this.allocateType === 0 && this.allocateType === 0) {
							this.update_config_info(res.data)
						} else {
							this.config_list.splice(findIndex, 1)
						}
					} else {
						this.$notify.error({
							title: '错误',
							message: `删除配置失败:${res.message}`
						})
					}
				})
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