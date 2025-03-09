<template>
	<el-card class="box-card" v-loading="storage_loading">
		<el-row slot="header">
			<el-page-header @back="go_back" content="存储池详情"></el-page-header>
		</el-row>
		<el-row style="text-align: left; margin: 20px 0">
			<el-button @click="register_storage(show_storage)" type="success" size="mini">重新注册</el-button>
			<el-button @click="show_modify_storage_support_category_dialog(show_storage)" type="success" size="mini" v-if="show_storage.type !== 'local'">修改支持范围</el-button>
			<el-button @click="pasue_storage(show_storage)" type="warning" size="mini" v-if="show_storage.status !== 3">开始维护</el-button>
			<el-button @click="migrate_dialog_visable = true" type="danger" size="mini">迁移存储池</el-button>
			<el-button @click="destroy_storage(show_storage)" type="danger" size="mini">销毁存储池</el-button>
		</el-row>
		<el-row>
			<el-descriptions :column="2" size="medium" border>
				<el-descriptions-item label="ID">{{ show_storage.storageId }}</el-descriptions-item>
				<el-descriptions-item label="存储池名">{{ show_storage.description }}</el-descriptions-item>
				<el-descriptions-item label="存储池类型">{{ show_storage.type }}</el-descriptions-item>
				<el-descriptions-item label="允许范围"><div v-html="get_support_category_html(show_storage.supportCategory)" /></el-descriptions-item>
				<el-descriptions-item label="本地路径" v-if="show_storage.type === 'local'">{{ JSON.parse(show_storage.param).path }}</el-descriptions-item>
				<el-descriptions-item label="挂载路径" v-if="show_storage.type === 'nfs'">{{ show_storage.mountPath }}</el-descriptions-item>
				<el-descriptions-item label="NFS路径" v-if="show_storage.type === 'nfs'">{{ JSON.parse(show_storage.param).path }}</el-descriptions-item>
				<el-descriptions-item label="NFS地址" v-if="show_storage.type === 'nfs'">{{ JSON.parse(show_storage.param).uri }}</el-descriptions-item>

				<el-descriptions-item label="Glusterfs磁盘" v-if="show_storage.type === 'glusterfs'">{{ JSON.parse(show_storage.param).volume }}</el-descriptions-item>
				<el-descriptions-item label="Glusterfs地址" v-if="show_storage.type === 'glusterfs'">{{ JSON.parse(show_storage.param).uri }}</el-descriptions-item>

				<el-descriptions-item label="Ceph地址" v-if="show_storage.type === 'ceph-rbd'">{{ JSON.parse(show_storage.param).uri }}</el-descriptions-item>
				<el-descriptions-item label="Ceph存储池名称" v-if="show_storage.type === 'ceph-rbd'">{{ JSON.parse(show_storage.param).pool }}</el-descriptions-item>
				<el-descriptions-item label="Ceph用户" v-if="show_storage.type === 'ceph-rbd'">{{ JSON.parse(show_storage.param).username }}</el-descriptions-item>

				<el-descriptions-item label="容量">{{ get_volume_display_size(show_storage.capacity) }}</el-descriptions-item>
				<el-descriptions-item label="可用">{{ get_volume_display_size(show_storage.available) }}</el-descriptions-item>
				<el-descriptions-item label="已申请">{{ get_volume_display_size(show_storage.allocation) }}</el-descriptions-item>
				<el-descriptions-item label="状态">
					<el-tag :type="show_storage.status === 1 ? 'success' : 'danger'">{{ get_storage_status(show_storage) }}</el-tag>
				</el-descriptions-item>
			</el-descriptions>
		</el-row>

		<el-row>
			<el-tabs>
				<el-tab-pane label="系统配置">
					<ConfigComponent ref="ConfigComponentRef" />
				</el-tab-pane>
			</el-tabs>
		</el-row>
		<el-dialog title="修改存储支持范围" :visible.sync="update_dialog_visable" width="30%">
			<el-form label-width="100px" class="demo-ruleForm">
				<el-form-item label="可选范围" prop="value">
					<el-checkbox-group v-model="storage_support_category_select">
						<el-checkbox :label="1">模版</el-checkbox>
						<el-checkbox :label="2">磁盘</el-checkbox>
					</el-checkbox-group>
				</el-form-item>
			</el-form>
			<span slot="footer" class="dialog-footer">
				<el-button @click="update_dialog_visable = false">取 消</el-button>
				<el-button type="primary" @click="modify_storage_support_category_click">确 定</el-button>
			</span>
		</el-dialog>
		<el-dialog title="迁移存储池" :visible.sync="migrate_dialog_visable" width="30%">
			<el-form ref="migrate_dialog_form_ref" label-width="100px" class="demo-ruleForm">
				<el-form-item label="目标存储池" prop="value">
					<el-select v-model="migrate_storage_id" style="width: 100%" placeholder="请选择目标存储池">
						<el-option v-for="item in this.storage_list.filter((v) => v.status === 1 && v.storageId !== show_storage_id)" :key="item.storageId" :label="item.description" :value="item.storageId" />
					</el-select>
				</el-form-item>
			</el-form>
			<span slot="footer" class="dialog-footer">
				<span slot="footer" class="dialog-footer">
					<el-button @click="migrate_dialog_visable = false">取 消</el-button>
					<el-button type="primary" @click="migrate_storage_click" :disabled="migrate_storage_id === ''">确 定</el-button>
				</span>
			</span>
		</el-dialog>
	</el-card>
</template>
<script>
import Notify from '@/api/notify'
import util from '@/api/util'

import ConfigComponent from '@/components/ConfigComponent.vue'
import { destroyStorage, getStorageInfo, getStorageList, migrateStorage, pauseStorage, registerStorage, updateStorageSupportCategory } from '@/api/api'
export default {
	name: 'StorageInfoComponent',
	data() {
		return {
			show_storage_id: 0,
			update_dialog_visable: false,
			migrate_dialog_visable: false,
			migrate_storage_id: '',
			storage_loading: false,
			show_storage: {},
			storage_list: [],
			storage_support_category_select: []
		}
	},
	components: { ConfigComponent },
	mixins: [Notify, util],
	created() {
		this.show_storage_id = 0
		this.subscribe_notify(this.$options.name, this.dispatch_notify_message)
		this.subscribe_connect_notify(this.$options.name, this.reload_page)
		this.init_notify()
	},
	beforeDestroy() {
		this.unsubscribe_notify(this.$options.name)
		this.unsubscribe_connect_notify(this.$options.name)
		this.show_storage_id = 0
	},
	methods: {
		go_back() {
			this.show_storage_id = 0
			this.$emit('back')
		},
		async reload_page() {
			if (this.show_storage_id > 0) {
				this.storage_loading = true
				await getStorageInfo({ storageId: this.show_storage_id })
					.then((res) => {
						if (res.code === 0) {
							this.init_storage(res.data)
						} else {
							this.$alert(`获取存储池信息失败:${res.message}`, '提示', {
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
						this.storage_loading = false
					})
			}
			await getStorageList().then((res) => {
				if (res.code == 0) {
					this.storage_list = res.data
				}
			})
		},
		async init_storage(storage, storage_list) {
			this.show_storage = storage
			this.show_storage_id = storage.storageId
			this.storage_list = storage_list
			this.migrate_storage_id = ''
			this.storage_loading = false
			this.$refs.ConfigComponentRef.init(4, this.show_storage.storageId)
		},
		async init(storageId) {
			this.show_storage_id = storageId
			this.migrate_storage_id
			this.storage_list = []
			this.migrate_storage_id = ''

			this.$refs.ConfigComponentRef.init(4, storageId)
			this.reload_page()
		},
		notify_storage_update(volume) {
			this.refresh_storage(volume)
			this.$emit('onStorageUpdate', volume)
		},
		refresh_storage(storage) {
			if (this.show_storage.storageId === storage.storageId) {
				this.show_storage = storage
			}
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
							this.notify_storage_update(res.data)
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
							this.notify_storage_update(res.data)
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
							this.notify_storage_update(res.data)
							this.go_back()
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除存储池失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		migrate_storage_click() {
			if (this.migrate_storage_id == 0) {
				return
			}
			this.$confirm('迁移存储池将根据目标存储池支持类型自动选择已经关机的磁盘以及模版磁盘进行迁移, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}).then(() => {
				migrateStorage({ sourceStorageId: this.show_storage_id, destStorageId: this.migrate_storage_id }).then((res) => {
					if (res.code === 0) {
						this.migrate_dialog_visable = false
					} else {
						this.$notify.error({
							title: '错误',
							message: `迁移存储池失败:${res.message}`
						})
					}
				})
			})
		},
		show_modify_storage_support_category_dialog(storage) {
			this.storage_support_category_select = []
			if ((storage.supportCategory & 1) == 1) {
				this.storage_support_category_select.push(1)
			}
			if ((storage.supportCategory & 2) == 2) {
				this.storage_support_category_select.push(2)
			}
			console.log(this.storage_support_category_select)
			this.update_dialog_visable = true
		},
		modify_storage_support_category_click() {
			let supportCategory = 0
			this.storage_support_category_select.forEach((val) => {
				supportCategory |= val
			})
			updateStorageSupportCategory({ storageId: this.show_storage.storageId, supportCategory: supportCategory }).then((res) => {
				if (res.code === 0) {
					this.show_storage = res.data
					this.update_dialog_visable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `修改支持范围失败:${res.message}`
					})
				}
			})
			console.log(supportCategory)
		},
		dispatch_notify_message(notify) {
			if (notify.type === 7 && this.show_storage.storageId == notify.id) {
				let res = notify.data
				if (res.code == 0) {
					this.refresh_storage(res.data)
				} else if (res.code == 2000001) {
					this.go_back()
				}
			}
		}
	}
}
</script>