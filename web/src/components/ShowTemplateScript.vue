<template>
	<el-dialog :title="title" :close-on-click-modal="false" :visible.sync="dialog_visiable" width="600px">
		<el-form :model="template">
			<el-form-item label="">
				<el-input v-model="template.script" :rows="10" type="textarea"></el-input>
			</el-form-item>
		</el-form>
		<span slot="footer" class="dialog-footer">
			<el-button @click="dialog_visiable = false">取 消</el-button>
			<el-button type="primary" @click="save_template_click">更 新</el-button>
		</span>
	</el-dialog>
</template>
<script>
import { updateTemplateScript } from '@/api/api'
export default {
	data() {
		return {
			dialog_visiable: false,
			title: '',
			template: {
				templateId: 0,
				script: ''
			}
		}
	},
	methods: {
		async init(data) {
			this.template.templateId = data.templateId
			this.template.script = data.script
			this.title = `初始化脚本[${data.name}]`
			this.dialog_visiable = true
		},
		save_template_click() {
			updateTemplateScript(this.template).then((res) => {
				if (res.code === 0) {
					this.$emit('onTemplateUpdate', res.data)
					this.dialog_visiable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `更新初始化脚本失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>