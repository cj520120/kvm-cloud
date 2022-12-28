<template>
	<div>
		<el-container>
			<el-main>
				<el-card class="box-card" v-show="this.show_type === 0">
					<el-row>
						<el-table :v-loading="data_loading" :data="snapshots" style="width: 100%">
							<el-table-column label="ID" prop="snapshotVolumeId" width="80" />
							<el-table-column label="名称" prop="name" width="300" />
							<el-table-column label="磁盘类型" prop="type" width="100" />
							<el-table-column label="磁盘空间" prop="capacity" width="100">
								<template #default="scope">
									{{ get_volume_desplay_size(scope.row.capacity) }}
								</template>
							</el-table-column>
							<el-table-column label="物理空间" prop="allocation" width="100">
								<template #default="scope">
									{{ get_volume_desplay_size(scope.row.allocation) }}
								</template>
							</el-table-column>
							<el-table-column label="状态" prop="status" width="100">
								<template #default="scope">
									<el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ get_snapshot_status(scope.row) }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="操作">
								<template #default="scope">
									<el-button @click="show_snapshot_info(scope.row)" type="" size="mini">快照详情</el-button>
									<el-button @click="destroy_snapshot(scope.row)" type="danger" size="mini">销毁快照</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-row>
				</el-card>
				<el-card class="box-card" v-show="this.show_type === 1">
					<el-row slot="header">
						<el-page-header @back="show_snapshot_list" content="快照详情"></el-page-header>
					</el-row>
					<el-row style="text-align: left; margin: 20px 0">
						<el-button @click="destroy_snapshot(show_snapshot)" type="danger" size="mini">销毁快照</el-button>
					</el-row>
					<el-row>
						<el-descriptions :column="2" size="medium" border>
							<el-descriptions-item label="ID">{{ show_snapshot.snapshotVolumeId }}</el-descriptions-item>
							<el-descriptions-item label="快照名">{{ show_snapshot.name }}</el-descriptions-item>
							<el-descriptions-item label="快照路径">{{ show_snapshot.volumePath }}</el-descriptions-item>
							<el-descriptions-item label="快照容量">{{ get_volume_desplay_size(show_snapshot.capacity) }}</el-descriptions-item>
							<el-descriptions-item label="物理占有">{{ get_volume_desplay_size(show_snapshot.allocation) }}</el-descriptions-item>
							<el-descriptions-item label="磁盘类型">{{ show_snapshot.type }}</el-descriptions-item>
							<el-descriptions-item label="存储池">{{ get_storage_name(show_snapshot.storageId) }}</el-descriptions-item>
							<el-descriptions-item label="状态">
								<el-tag :type="show_snapshot.status === 1 ? 'success' : 'danger'">{{ get_snapshot_status(show_snapshot) }}</el-tag>
							</el-descriptions-item>
						</el-descriptions>
					</el-row>
				</el-card>
			</el-main>
		</el-container>
	</div>
</template>
<script>
import { getSnapshotList, getSnapshotInfo, destroySnapshot, getStorageList } from '@/api/api'
import Notify from '@/api/notify'
export default {
	name: 'snapshotView',
	components: {},
	data() {
		return {
			data_loading: false,
			show_type: 0,
			show_snapshot: {},
			snapshots: [],
			storages: []
		}
	},
	mixins: [Notify],
	created() {
		this.init_view()
		this.init_notify()
	},
	methods: {
		async init_view() {
			this.data_loading = true
			await getStorageList().then((res) => {
				if (res.code === 0) {
					this.storages = res.data
				}
			})
			await getSnapshotList()
				.then((res) => {
					if (res.code == 0) {
						this.snapshots = res.data
					}
				})
				.finally(() => {
					this.data_loading = false
				})
		},
		get_storage_name(storageId) {
			let findStorage = this.storages.find((item) => item.storageId === storageId) || { name: '-' }
			return findStorage.name
		},
		get_volume_desplay_size(size) {
			if (size >= 1024 * 1024 * 1024 * 1024) {
				return (size / (1024 * 1024 * 1024 * 1024)).toFixed(2) + ' TB'
			} else if (size >= 1024 * 1024 * 1024) {
				return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
			} else if (size >= 1024 * 1024) {
				return (size / (1024 * 1024)).toFixed(2) + ' MB'
			} else if (size >= 1024) {
				return (size / 1024).toFixed(2) + '  KB'
			} else {
				return size + '  bytes'
			}
		},
		get_snapshot_status(snapshot) {
			switch (snapshot.status) {
				case 0:
					return '正在创建'
				case 1:
					return '已就绪'
				case 2:
					return '快照错误'
				case 3:
					return '正在删除'
				default:
					return `未知状态[${snapshot.status}]`
			}
		},
		show_snapshot_list() {
			this.show_type = 0
		},
		show_snapshot_info(snapshot) {
			this.show_snapshot = snapshot
			this.show_type = 1
		},
		update_snapshot_info(snapshot) {
			let findIndex = this.snapshots.findIndex((item) => item.snapshotVolumeId === snapshot.snapshotVolumeId)
			if (findIndex >= 0) {
				this.$set(this.snapshots, findIndex, snapshot)
			} else {
				this.snapshots.push(snapshot)
			}
			if (this.show_snapshot && this.show_snapshot.snapshotVolumeId === snapshot.snapshotVolumeId) {
				this.show_snapshot = snapshot
			}
		},
		handle_notify_message(notify) {
			if (notify.type === 6) {
				getSnapshotInfo({ snapshotVolumeId: notify.id }).then((res) => {
					if (res.code == 0) {
						this.update_snapshot_info(res.data)
					} else if (res.code == 6000001) {
						let findIndex = this.snapshots.findIndex((v) => v.snapshotVolumeId === notify.id)
						if (findIndex >= 0) {
							this.snapshots.splice(findIndex, 1)
						}
					}
				})
			}
		},
		destroy_snapshot(snapshot) {
			this.$confirm('删除快照, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					destroySnapshot({ snapshotVolumeId: snapshot.snapshotVolumeId }).then((res) => {
						if (res.code === 0) {
							let findIndex = this.snapshots.findIndex((item) => item.snapshotVolumeId === snapshot.snapshotVolumeId)
							if (findIndex >= 0) {
								this.snapshots.splice(findIndex, 1)
							}
							this.show_type = 0
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除快照失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>
<style lang="postcss" scoped>
.table_action button {
	margin: 0.1em;
}
</style>