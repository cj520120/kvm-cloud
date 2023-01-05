<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="go_back()" content="上传磁盘" style="color: #409eff"></el-page-header>
		</el-row>
		<el-row>
			<el-form ref="uploadForm" :model="upload_volume" label-width="100px" class="demo-ruleForm">
				<el-form-item label="名称" prop="description">
					<el-input v-model="upload_volume.description"></el-input>
				</el-form-item>
				<el-form-item label="存储池" prop="storageId">
					<el-select v-model="upload_volume.storageId" style="width: 100%">
						<el-option label="随机" :value="0"></el-option>
						<el-option v-for="item in this.storages" :key="item.storageId" :label="item.description" :value="item.storageId" />
					</el-select>
				</el-form-item>
				<el-form-item label="目标磁盘类型" prop="volumeType">
					<el-select v-model="upload_volume.volumeType" style="width: 100%">
						<el-option label="raw" value="raw"></el-option>
						<el-option label="qcow" value="qcow"></el-option>
						<el-option label="qcow2" value="qcow2"></el-option>
						<el-option label="vdi" value="vdi"></el-option>
						<el-option label="vmdk" value="vmdk"></el-option>
						<el-option label="vpc" value="vpc"></el-option>
					</el-select>
				</el-form-item>
				<el-form-item>
					<el-upload style="max-width: 300px" ref="upload" class="upload-demo" :action="get_upload_uri()" :limit="1" :on-success="on_upload_success" :on-error="on_upload_error" name="volume" :data="upload_volume" :file-list="upload_file_list" :auto-upload="false" :show-file-list="true">
						<el-button slot="trigger" size="small" type="primary">选择磁盘</el-button>
					</el-upload>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="upload_volume_click" :loading="uploading">导入</el-button>
					<el-button @click="go_back">取消</el-button>
				</el-form-item>
			</el-form>
		</el-row>
	</el-card>
</template>
<script>
import { getStorageList } from '@/api/api'
export default {
	data() {
		return {
			uploading: false,
			storages: [],
			upload_file_list: [],
			upload_volume: {
				description: '',
				storageId: 0,
				volumeType: 'qcow2'
			}
		}
	},
	methods: {
		go_back() {
			this.$emit('back')
		},
		get_upload_uri() {
			return process.env.NODE_ENV === 'production' ? './api/volume/upload' : 'http://192.168.2.107:8080/api/volume/upload'
		},
		notify_volume_update(volume) {
			this.$emit('onVolumeUpdate', volume)
		},
		async init() {
			if (this.$refs['uploadForm']) {
				this.$refs['uploadForm'].resetFields()
			}
			this.uploading = false
			this.upload_file_list = []
			this.uploading = false
			await getStorageList().then((res) => {
				if (res.code == 0) {
					this.storages = res.data
				}
			})
		},
		upload_volume_click() {
			this.uploading = true
			this.$refs.upload.submit()
		},
		on_upload_success(response) {
			this.uploading = false
			if (response.code === 0) {
				this.update_volume_info(response.data)
				this.show_type = 0
				this.upload_file_list = []
			} else {
				this.upload_file_list = []
				this.$notify.error({
					title: '错误',
					message: `上传磁盘失败:${response.message}`
				})
			}
		},
		on_upload_error() {
			this.uploading = false
			this.upload_file_list = []
			this.$notify.error({
				title: '错误',
				message: `磁盘上传失败。`
			})
		}
	}
}
</script>