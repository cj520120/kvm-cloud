<template>
	<el-dialog title="下载磁盘" :visible.sync="download_dialog_visiable" width="400px">
		<el-form :model="download_volume" label-width="100px">
			<el-form-item label="磁盘类型" prop="volumeType">
				<el-select v-model="download_volume.volumeType" style="width: 100%">
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
			<el-button @click="download_dialog_visiable = false">取 消</el-button>
			<el-button type="primary" @click="download_volume_click">下载</el-button>
		</span>
	</el-dialog>
</template>
<script>
export default {
	data() {
		return {
			download_dialog_visiable: false,
			download_volume: {
				volumeType: 'qcow2',
				volumeId: 0
			}
		}
	},
	methods: {
		init(volume) {
			this.download_volume.volumeId = volume.volumeId
			this.download_volume.volumeType = volume.type
			this.download_dialog_visiable = true
		},
		download_volume_click() {
			let uri = process.env.NODE_ENV === 'production' ? `./api/volume/download?volumeId=${this.download_volume.volumeId}&volumeType=${this.download_volume.volumeType}` : `http://192.168.2.107:8080/api/volume/download?volumeId=${this.download_volume.volumeId}&volumeType=${this.download_volume.volumeType}`
			window.open(uri, '_blank')
			this.download_dialog_visiable = false
		}
	}
}
</script>