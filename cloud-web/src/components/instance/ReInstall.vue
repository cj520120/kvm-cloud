<template>
	<el-dialog :title="title" :visible.sync="dialog_visible" center width="400px" :close-on-click-modal="false" :close-on-press-escape="false">
		<el-form :model="modify" label-position="right" label-width="80px">
			<el-form-item label="系统模版">
				<el-select v-model="modify.templateId">
					<el-option v-for="template in all_template" :key="template.id" :label="template.name" :value="template.id"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="存储池">
				<el-select v-model="modify.storageId">
					<el-option :key="0" label="不限制" :value="0"></el-option>
					<el-option v-for="storage in all_storage" :key="storage.id" :label="storage.name" :value="storage.id"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-button :loading="loading" type="primary" :disabled="modify.iso === ''" @click="ok">确 定</el-button>
				<el-button @click="dialog_visible = false">取 消</el-button>
			</el-form-item>
		</el-form>
	</el-dialog>
</template>

<script>
export default {
	name: 'ReInstall',
	data() {
		return {
			modify: {
				id: 0,
				storageId: 0,
				templateId: ''
			},
			title: '',
			dialog_visible: false,
			loading: false
		}
	},
	methods: {
		init_data(instance, all_template, all_storage) {
			this.instance = instance
			this.title = `${instance.description}-重装系统`
			this.all_template = all_template.filter((v) => v.clusterId === instance.clusterId && (v.type === 'ISO' || v.type === 'Disk'))
			this.all_storage = all_storage.map((item) => ({ ...item, isShow: false }))
			if (this.all_template.length > 0) {
				this.modify.templateId = this.all_template[0].id
			}
			if (instance.templateId > 0) {
				this.modify.templateId = instance.templateId
			}
			this.modify.id = instance.id
			this.modify.storageId = 0
			this.dialog_visible = true
			this.loading = false
		},
		ok() {
			this.loading = true
			this.post_data(`/management/vm/reinstall`, this.modify).then((res) => {
				this.loading = false
				if (res.data.code === 0) {
					this.$emit('on_modify', res.data.data)
					this.dialog_visible = false
				}
			})
			this.dialog_visible = true
		}
	}
}
</script>

<style scoped>
</style>