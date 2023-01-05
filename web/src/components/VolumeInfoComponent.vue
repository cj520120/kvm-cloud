<template>
	<div>
		<el-card class="box-card" v-show="this.show_type === 0" v-loading="volume_loading">
			<el-row slot="header">
				<el-page-header @back="go_back" content="磁盘详情"></el-page-header>
			</el-row>
			<el-row style="text-align: left; margin: 20px 0">
				<el-button @click="show_resize_volume_click(show_volume)" type="primary" size="mini">扩容磁盘</el-button>
				<el-button @click="show_clone_volume_click(show_volume)" type="primary" size="mini">克隆磁盘</el-button>
				<el-button @click="show_migrate_volume_click(show_volume)" type="primary" size="mini">迁移磁盘</el-button>
				<el-button @click="show_create_volume_snapshot_click(show_volume)" type="primary" size="mini">创建快照</el-button>
				<el-button @click="show_create_volume_template_click(show_volume)" type="primary" size="mini">创建模版</el-button>
				<el-button @click="show_download_volume_click(show_volume)" type="primary" size="mini">下载磁盘</el-button>
				<el-button @click="destroy_volume(show_volume)" type="danger" size="mini">销毁磁盘</el-button>
			</el-row>
			<el-row>
				<el-descriptions :column="2" size="medium" border>
					<el-descriptions-item label="ID">{{ show_volume.volumeId }}</el-descriptions-item>
					<el-descriptions-item label="磁盘名">{{ show_volume.description }}</el-descriptions-item>
					<el-descriptions-item label="磁盘类型">{{ show_volume.type }}</el-descriptions-item>
					<el-descriptions-item label="磁盘路径">{{ show_volume.path }}</el-descriptions-item>
					<el-descriptions-item label="磁盘模版">{{ this.template ? this.template.name : '-' }}</el-descriptions-item>
					<el-descriptions-item label="磁盘存储池">
						<el-button type="text" @click="show_storage_info(show_volume.storageId)" :underline="false">
							{{ this.storage ? this.storage.name : show_volume.storageId }}
						</el-button>
					</el-descriptions-item>
					<el-descriptions-item label="挂载主机">
						<el-button type="text" @click="show_guest_info(show_volume.attach.guestId)" :underline="false" v-if="show_volume.attach">
							{{ show_volume.attach.description }}
						</el-button>
						<span v-if="!show_volume.attach">-</span>
					</el-descriptions-item>
					<el-descriptions-item label="磁盘容量">{{ get_volume_desplay_size(show_volume.capacity) }}</el-descriptions-item>
					<el-descriptions-item label="物理占用">{{ get_volume_desplay_size(show_volume.allocation) }}</el-descriptions-item>
					<el-descriptions-item label="状态">
						<el-tag :type="show_volume.status === 1 ? 'success' : 'danger'">{{ get_volume_status(show_volume) }}</el-tag>
					</el-descriptions-item>
				</el-descriptions>
			</el-row>
		</el-card>
		<CloneVolumeComponent ref="CloneVolumeComponentRef" @back="show_type = 0" @onVolumeUpdate="notify_volume_update" v-show="this.show_type === 1" />
		<MigrateVolumeComponent ref="MigrateVolumeComponentRef" @back="show_type = 0" @onVolumeUpdate="notify_volume_update" v-show="this.show_type === 2" />
		<GuestInfoComponent ref="GuestInfoComponentRef" @back="show_type = 0" v-show="this.show_type === 3" />
		<StorageInfoComponent ref="StorageInfoComponentRef" @back="show_type = 0" v-show="this.show_type === 4" />
		<DownloadVolumeComponent ref="DownloadVolumeComponentRef" />
		<ResizeVolumeComponent ref="ResizeVolumeComponentRef" @onVolumeUpdate="notify_volume_update" />
		<CreateVolumeTemplateComponent ref="CreateVolumeTemplateComponentRef" />
		<CreateVolumeSnapshotComponent ref="CreateVolumeSnapshotComponentRef" />
	</div>
</template>
<script>
import DownloadVolumeComponent from '@/components/DownloadVolumeComponent'
import ResizeVolumeComponent from '@/components/ResizeVolumeComponent'
import CreateVolumeTemplateComponent from '@/components/CreateVolumeTemplateComponent'
import CreateVolumeSnapshotComponent from '@/components/CreateVolumeSnapshotComponent.vue'
import CloneVolumeComponent from '@/components/CloneVolumeComponent'
import MigrateVolumeComponent from '@/components/MigrateVolumeComponent.vue'
import StorageInfoComponent from '@/components/StorageInfoComponent'
import util from '@/api/util'
import { destroyVolume, getStorageInfo, getTemplateInfo, getVolumeInfo } from '@/api/api'
export default {
	data() {
		return {
			volume_loading: false,
			show_type: 0,
			show_volume: {},
			template: {},
			storage: {}
		}
	},
	components: { DownloadVolumeComponent, ResizeVolumeComponent, CreateVolumeTemplateComponent, CreateVolumeSnapshotComponent, CloneVolumeComponent, MigrateVolumeComponent, StorageInfoComponent },
	beforeCreate() {
		this.$options.components.GuestInfoComponent = require('./GuestInfoComponent.vue').default
	},
	mixins: [util],
	methods: {
		go_back() {
			this.$emit('back')
		},
		async init_volume(volume) {
			this.show_volume = volume
			this.show_type = 0
			await this.init_volume_template()
			await this.init_volume_storage()
		},
		async init(volumeId) {
			this.show_type = 0
			this.volume_loading = true
			await getVolumeInfo({ volumeId: volumeId })
				.then((res) => {
					if (res.code === 0) {
						this.init_volume(res.data)
					} else {
						this.$alert(`获取磁盘信息失败:${res.message}`, '提示', {
							dangerouslyUseHTMLString: true,
							confirmButtonText: '返回',
							type: 'error'
						})
							.then(() => {
								this.go_back()
							})
							.catch(() => {
								this.go_back()
							})
					}
				})
				.finally(() => {
					this.volume_loading = false
				})
		},
		notify_volume_update(volume) {
			this.refresh_volume(volume)
			this.$emit('onVolumeUpdate', volume)
		},
		refresh_volume(volume) {
			if (this.show_volume.volumeId === volume.volumeId) {
				this.show_volume = volume
			}
		},
		async init_volume_template() {
			if (this.show_volume.templateId === 0) {
				return
			}
			await getTemplateInfo({ templateId: this.show_volume.templateId }).then((res) => {
				if (res.code == 0) {
					this.template = res.data
				}
			})
		},
		async init_volume_storage() {
			await getStorageInfo({ storageId: this.show_volume.storageId }).then((res) => {
				if (res.code == 0) {
					this.storage = res.data
				}
			})
		},

		show_resize_volume_click(volume) {
			this.$refs.ResizeVolumeComponentRef.init(volume)
		},

		show_clone_volume_click(volume) {
			this.$refs.CloneVolumeComponentRef.init(volume)
			this.show_type = 1
		},
		show_migrate_volume_click(volume) {
			this.$refs.MigrateVolumeComponentRef.init(volume)
			this.show_type = 2
		},
		show_create_volume_template_click(volume) {
			this.$refs.CreateVolumeTemplateComponentRef.init(volume)
		},
		show_create_volume_snapshot_click(volume) {
			this.$refs.CreateVolumeSnapshotComponentRef.init(volume)
		},
		show_guest_info(guestId) {
			this.$refs.GuestInfoComponentRef.initGuestId(guestId)
			this.show_type = 3
		},
		show_storage_info(storageId) {
			this.$refs.StorageInfoComponentRef.init(storageId)
			this.show_type = 4
		},
		destroy_volume(volume) {
			this.$confirm('删除磁盘, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					console.log(volume)
					destroyVolume({ volumeId: volume.volumeId }).then((res) => {
						if (res.code === 0) {
							this.notify_volume_update(res.data)
							this.go_back()
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除磁盘失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>