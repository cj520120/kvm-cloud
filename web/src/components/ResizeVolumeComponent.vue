<template>
	<el-dialog title="磁盘扩容" :visible.sync="resize_dialog_visiable" width="300px">
		<el-form :model="resize_volume" label-width="100px">
			<el-form-item label="磁盘大小(GB)">
				<el-input v-model="resize_volume.size" placeholder="请输入磁盘大小(GB)"></el-input>
			</el-form-item>
		</el-form>
		<span slot="footer" class="dialog-footer">
			<el-button @click="resize_dialog_visiable = false">取 消</el-button>
			<el-button type="primary" @click="resize_volume_click">确 定</el-button>
		</span>
	</el-dialog>
</template>
<script>
import { resizeVolume } from '@/api/api'
export default {
	data() {
		return {
			resize_dialog_visiable: false,
			resize_volume: {
				volumeId: 0,
				size: 100
			}
		}
	},
	methods: {
		init(volume) {
			this.resize_volume.volumeId = volume.volumeId
			this.resize_volume.size = 100
			this.resize_dialog_visiable = true
		},
		resize_volume_click() {
			this.$confirm('扩容磁盘, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					resizeVolume(this.resize_volume).then((res) => {
						if (res.code === 0) {
							// this.update_volume_info(res.data)
							this.$emit('onVolumeUpdate', res.data)
							this.resize_dialog_visiable = false
						} else {
							this.$notify.error({
								title: '错误',
								message: `扩容磁盘失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>