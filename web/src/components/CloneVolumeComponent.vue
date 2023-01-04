<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="go_back()" content="克隆磁盘" style="color: #409eff"></el-page-header>
		</el-row>
		<el-row>
			<el-form ref="cloneForm" :model="clone_volume" label-width="100px" class="demo-ruleForm">
				<el-form-item label="名称" prop="description">
					<el-input v-model="clone_volume.description"></el-input>
				</el-form-item>
				<el-form-item label="存储池" prop="storageId">
					<el-select v-model="clone_volume.storageId" style="width: 100%">
						<el-option label="随机" :value="0"></el-option>
						<el-option v-for="item in this.storages" :key="item.storageId" :label="item.description" :value="item.storageId" />
					</el-select>
				</el-form-item>
				<el-form-item label="磁盘类型" prop="volumeType">
					<el-select v-model="clone_volume.volumeType" style="width: 100%">
						<el-option label="raw" value="raw"></el-option>
						<el-option label="qcow" value="qcow"></el-option>
						<el-option label="qcow2" value="qcow2"></el-option>
						<el-option label="vdi" value="vdi"></el-option>
						<el-option label="vmdk" value="vmdk"></el-option>
						<el-option label="vpc" value="vpc"></el-option>
					</el-select>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="clone_volume_click">克隆</el-button>
					<el-button @click="go_back">取消</el-button>
				</el-form-item>
			</el-form>
		</el-row>
	</el-card>
</template>
<script>
import { cloneVolume, getStorageList } from '@/api/api'
export default {
	data() {
		return {
			clone_volume: {
				sourceVolumeId: 0,
				description: '',
				storageId: 0,
				volumeType: 'qcow2'
			},
			storages: []
		}
	},
	methods: {
		go_back() {
			this.$emit('back')
		},
		notify_volume_update(volume) {
			this.$emit('onVolumeUpdate', volume)
		},
		async init(volume) {
			this.clone_volume.sourceVolumeId = volume.volumeId
			this.clone_volume.volumeType = volume.type
			this.clone_volume.description = 'Clone-' + volume.description
			this.clone_volume.storageId = volume.storageId
			await getStorageList().then((res) => {
				if (res.code == 0) {
					this.storages = res.data
				}
			})
		},

		clone_volume_click() {
			this.$confirm('克隆磁盘, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					cloneVolume(this.clone_volume).then((res) => {
						if (res.code === 0) {
							this.notify_volume_update(res.data.clone)
							this.notify_volume_update(res.data.source)
							this.go_back()
						} else {
							this.$notify.error({
								title: '错误',
								message: `克隆磁盘失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>