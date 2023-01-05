<template>
	<el-dialog title="创建快照" :visible.sync="snapshot_dialog_visiable" width="400px">
		<el-form :model="snapshot_volume" label-width="100px">
			<el-form-item label="快照名称">
				<el-input v-model="snapshot_volume.snapshotName" placeholder="请输入快照名称"></el-input>
			</el-form-item>
			<el-form-item label="磁盘类型" prop="volumeType">
				<el-select v-model="snapshot_volume.snapshotVolumeType" style="width: 100%">
					<el-option label="raw" value="raw"></el-option>
					<el-option label="qcow" value="qcow"></el-option>
					<el-option label="qcow2" value="qcow2"></el-option>
					<el-option label="vdi" value="vdi"></el-option>
					<el-option label="vmdk" value="vmdk"></el-option>
					<el-option label="vpc" value="vpc"></el-option>
				</el-select>
			</el-form-item>
		</el-form>
		<span slot="footer" class="dialog-footer">
			<el-button @click="snapshot_dialog_visiable = false">取 消</el-button>
			<el-button type="primary" @click="create_volume_snapshot_click">确 定</el-button>
		</span>
	</el-dialog>
</template>
<script>
import { createSnapshot } from '@/api/api'
export default {
	data() {
		return {
			snapshot_volume: {
				volumeId: 0,
				snapshotName: '',
				snapshotVolumeType: 'qcow2'
			},
			snapshot_dialog_visiable: false
		}
	},
	methods: {
		init(volume) {
			this.snapshot_volume.volumeId = volume.volumeId
			this.snapshot_volume.name = ''
			this.snapshot_volume.snapshotVolumeType = volume.type
			this.snapshot_dialog_visiable = true
		},
		create_volume_snapshot_click() {
			this.$confirm('创建磁盘快照, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					createSnapshot(this.snapshot_volume).then((res) => {
						if (res.code === 0) {
							this.snapshot_dialog_visiable = false
						} else {
							this.$notify.error({
								title: '错误',
								message: `创建磁盘快照失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>