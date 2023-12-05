<template>
	<el-dialog title="创建模版" :close-on-click-modal="false" :visible.sync="template_dialog_visiable" width="400px">
		<el-form :model="template_volume" label-width="100px">
			<el-form-item label="模版名称">
				<el-input v-model="template_volume.name" placeholder="请输入模版名称"></el-input>
			</el-form-item>
		</el-form>
		<span slot="footer" class="dialog-footer">
			<el-button @click="template_dialog_visiable = false">取 消</el-button>
			<el-button type="primary" @click="create_volume_template_click">确 定</el-button>
		</span>
	</el-dialog>
</template>
<script>
import { createVolumeTemplate } from '@/api/api'
export default {
	data() {
		return {
			template_dialog_visiable: false,
			template_volume: {
				volumeId: 0,
				name: ''
			}
		}
	},
	methods: {
		init(volume) {
			this.template_volume.volumeId = volume.volumeId
			this.template_volume.name = ''
			this.template_dialog_visiable = true
		},

		create_volume_template_click() {
			this.$confirm('创建磁盘模版, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					createVolumeTemplate(this.template_volume).then((res) => {
						if (res.code === 0) {
							this.template_dialog_visiable = false
						} else {
							this.$notify.error({
								title: '错误',
								message: `创建磁盘模版失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>