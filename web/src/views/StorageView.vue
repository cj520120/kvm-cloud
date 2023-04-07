<template>
	<div>
		<el-container>
			<el-main>
				<el-card class="box-card" v-show="this.show_type === 0">
					<el-row slot="header" class="clearfix" style="height: 20px">
						<el-button style="float: left; padding: 3px 0" type="text" @click="show_create_storage">创建存储池</el-button>
					</el-row>
					<el-row>
						<el-table :v-loading="data_loading" :data="storages" style="width: 100%">
							<el-table-column label="ID" prop="storageId" width="80" />
							<el-table-column label="名称" prop="description" show-overflow-tooltip />
							<el-table-column label="类型" prop="type" width="120" />
							<el-table-column label="容量" prop="capacity" width="120">
								<template #default="scope">
									<el-tooltip class="item" effect="dark" :content="'已用:' + get_volume_display_size(scope.row.allocation) + ' / 总共:' + get_volume_display_size(scope.row.capacity)" placement="top">
										<el-progress color="#67C23A" :percentage="scope.row.capacity <= 0 ? 0 : Math.floor((scope.row.allocation * 100) / scope.row.capacity)"></el-progress>
									</el-tooltip>
								</template>
							</el-table-column>
							<el-table-column label="状态" prop="status" width="110">
								<template #default="scope">
									<el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ get_storage_status(scope.row) }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="操作" width="400">
								<template #default="scope">
									<el-button @click="show_storage_info_click(scope.row)" type="" size="mini">存储池详情</el-button>
									<el-button @click="register_storage(scope.row)" type="success" size="mini">重新注册</el-button>
									<el-button @click="pasue_storage(scope.row)" type="warning" size="mini" v-if="scope.row.status !== 2">开始维护</el-button>
									<el-button @click="destroy_storage(scope.row)" type="danger" size="mini">销毁存储池</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-row>
				</el-card>
				<StorageInfoComponent ref="StorageInfoComponentRef" @back="show_storage_list()" @onStorageUpdate="update_storate_info" v-show="this.show_type === 1" />
				<CreateStorageComponent ref="CreateStorageComponentRef" @back="show_storage_list()" @onStorageUpdate="update_storate_info" v-show="this.show_type === 2" />
			</el-main>
		</el-container>
	</div>
</template>
<script>
import { getStorageList, getStorageInfo, pauseStorage, registerStorage, destroyStorage } from '@/api/api'
import StorageInfoComponent from '@/components/StorageInfoComponent'
import CreateStorageComponent from '@/components/CreateStorageComponent'
import Notify from '@/api/notify'
import util from '@/api/util'
export default {
	name: 'storageView',
	components: { StorageInfoComponent, CreateStorageComponent },
	data() {
		return {
			data_loading: false,
			show_type: -1,
			show_storage: {},
			storages: []
		}
	},
	mixins: [Notify, util],
	mounted() {
		this.show_type = 0
		this.init_view()
	},
	created() {
		this.subscribe_notify(this.$options.name, this.dispatch_notify_message)
		this.init_notify()
	},
	beforeDestroy() {
		this.unsubscribe_notify(this.$options.name)
	},
	methods: {
		async init_view() {
			this.data_loading = true
			await getStorageList()
				.then((res) => {
					if (res.code == 0) {
						this.storages = res.data
					}
				})
				.finally(() => {
					this.data_loading = false
				})
		},
		update_storate_info(storage) {
			let findIndex = this.storages.findIndex((item) => item.storageId === storage.storageId)
			if (findIndex >= 0) {
				this.$set(this.storages, findIndex, storage)
			} else {
				this.storages.push(storage)
			}
		},
		dispatch_notify_message(notify) {
			if (notify.type === 7) {
				getStorageInfo({ storageId: notify.id }).then((res) => {
					if (res.code == 0) {
						this.update_storate_info(res.data)
					} else if (res.code == 3000001) {
						let findIndex = this.storages.findIndex((v) => v.storageId === notify.id)
						if (findIndex >= 0) {
							this.storages.splice(findIndex, 1)
						}
					}
				})
			}
		},
		show_storage_list() {
			this.show_type = 0
		},
		show_create_storage() {
			this.$refs.CreateStorageComponentRef.init()
			this.show_type = 2
		},
		show_storage_info_click(storage) {
			this.$refs.StorageInfoComponentRef.init_storage(storage)
			this.show_type = 1
		},
		pasue_storage(storage) {
			this.$confirm('暂停存储池, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					pauseStorage({ storageId: storage.storageId }).then((res) => {
						if (res.code === 0) {
							this.update_storate_info(res.data)
						} else {
							this.$notify.error({
								title: '错误',
								message: `暂停存储池失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		register_storage(storage) {
			this.$confirm('重新注册存储池, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					registerStorage({ storageId: storage.storageId }).then((res) => {
						if (res.code === 0) {
							storage.status = 0
						} else {
							this.$notify.error({
								title: '错误',
								message: `注册存储池失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		destroy_storage(storage) {
			this.$confirm('删除存储池, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					destroyStorage({ storageId: storage.storageId }).then((res) => {
						if (res.code === 0) {
							this.update_storate_info(res.data)
							this.show_type = 0
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除存储池失败:${res.message}`
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