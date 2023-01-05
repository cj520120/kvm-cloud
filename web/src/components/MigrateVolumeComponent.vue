<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="go_back()" content="迁移磁盘" style="color: #409eff"></el-page-header>
		</el-row>
		<el-row>
			<el-form :model="migrate_volume" label-width="100px" class="demo-ruleForm">
				<el-form-item label="存储池" prop="type">
					<el-select v-model="migrate_volume.storageId" style="width: 100%">
						<el-option label="随机" :value="0"></el-option>
						<el-option v-for="item in this.storages" :key="item.storageId" :label="item.description" :value="item.storageId" />
					</el-select>
				</el-form-item>
				<el-form-item label="磁盘类型" prop="volumeType">
					<el-select v-model="migrate_volume.volumeType" style="width: 100%">
						<el-option label="raw" value="raw"></el-option>
						<el-option label="qcow" value="qcow"></el-option>
						<el-option label="qcow2" value="qcow2"></el-option>
						<el-option label="vdi" value="vdi"></el-option>
						<el-option label="vmdk" value="vmdk"></el-option>
						<el-option label="vpc" value="vpc"></el-option>
					</el-select>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="migrate_volume_click">迁移</el-button>
					<el-button @click="go_back">取消</el-button>
				</el-form-item>
			</el-form>
		</el-row>
	</el-card>
</template>
<script>
import { getStorageList, migrateVolume } from '@/api/api'
export default {
	data() {
		return {
			migrate_volume: {
				sourceVolumeId: 0,
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
			this.migrate_volume.sourceVolumeId = volume.volumeId
			this.migrate_volume.volumeType = volume.type
			this.migrate_volume.storageId = volume.storageId
			await getStorageList().then((res) => {
				if (res.code == 0) {
					this.storages = res.data
				}
			})
		},

		migrate_volume_click() {
			this.$confirm('迁移磁盘, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					migrateVolume(this.migrate_volume).then((res) => {
						if (res.code === 0) {
							this.notify_volume_update(res.data.migrate)
							this.notify_volume_update(res.data.source)
							this.go_back()
						} else {
							this.$notify.error({
								title: '错误',
								message: `迁移磁盘失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>