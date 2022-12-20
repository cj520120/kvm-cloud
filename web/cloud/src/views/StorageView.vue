<template>
	<div>
		<NavViewVue current="Storage" />
		<el-card class="box-card" v-if="this.show_type === 0">
			<el-row slot="header" class="clearfix" style="height: 20px">
				<el-button style="float: left; padding: 3px 0" type="text" @click="show_create_storage">创建存储池</el-button>
			</el-row>
			<el-row>
				<el-table :v-loading="data_loading" :data="storages" style="width: 100%">
					<el-table-column label="ID" prop="storageId" width="80" />
					<el-table-column label="名称" prop="name" width="120" />
					<el-table-column label="类型" prop="type" width="120" />
					<el-table-column label="挂载路径" prop="mountPath" width="350" />
					<el-table-column label="容量" prop="capacity" width="180">
						<template #default="scope">
							<el-tooltip class="item" effect="dark" :content="'已使用:' + get_storage_desplay(scope.row.available) + ' / 总共:' + get_storage_desplay(scope.row.capacity)" placement="top">
								<el-progress color="#67C23A" :percentage="parseInt((scope.row.available * 100) / scope.row.capacity)"></el-progress>
							</el-tooltip>
						</template>
					</el-table-column>
					<el-table-column label="状态" prop="status" width="100">
						<template #default="scope">
							<el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ get_storage_status(scope.row) }}</el-tag>
						</template>
					</el-table-column>
					<el-table-column label="操作" min-width="380">
						<template #default="scope">
							<el-button @click="show_storage_info(scope.row)" type="" size="mini">存储池详情</el-button>
							<el-button @click="register_storage(scope.row)" type="success" size="mini">重新注册</el-button>
							<el-button @click="pasue_storage(scope.row)" type="warning" size="mini" v-if="scope.row.status !== 2">开始维护</el-button>
							<el-button @click="destroy_storage(scope.row)" type="danger" size="mini">销毁存储池</el-button>
						</template>
					</el-table-column>
				</el-table>
			</el-row>
		</el-card>
		<el-card class="box-card" v-if="this.show_type === 1">
			<el-row slot="header">
				<el-page-header @back="show_storage_list" content="存储池详情"></el-page-header>
			</el-row>
			<el-row style="text-align: left; margin: 20px 0">
				<el-button @click="register_storage(show_storage)" type="success" size="mini">重新注册</el-button>
				<el-button @click="pasue_storage(show_storage)" type="warning" size="mini" v-if="show_storage.status !== 3">开始维护</el-button>
				<el-button @click="destroy_storage(show_storage)" type="danger" size="mini">销毁存储池</el-button>
			</el-row>
			<el-row>
				<el-descriptions :column="2" size="medium" border>
					<el-descriptions-item label="ID">{{ show_storage.storageId }}</el-descriptions-item>
					<el-descriptions-item label="存储池名">{{ show_storage.name }}</el-descriptions-item>
					<el-descriptions-item label="存储池类型">{{ show_storage.type }}</el-descriptions-item>
					<el-descriptions-item label="挂载路径">{{ show_storage.mountPath }}</el-descriptions-item>
					<el-descriptions-item label="NFS路径" v-if="show_storage.type === 'nfs'">{{ JSON.parse(show_storage.param).path }}</el-descriptions-item>
					<el-descriptions-item label="NFS地址" v-if="show_storage.type === 'nfs'">{{ JSON.parse(show_storage.param).uri }}</el-descriptions-item>
					<el-descriptions-item label="容量">{{ get_storage_desplay(show_storage.capacity) }}</el-descriptions-item>
					<el-descriptions-item label="可用">{{ get_storage_desplay(show_storage.available) }}</el-descriptions-item>
					<el-descriptions-item label="已申请">{{ get_storage_desplay(show_storage.allocation) }}</el-descriptions-item>
					<el-descriptions-item label="状态">
						<el-tag :type="show_storage.status === 1 ? 'success' : 'danger'">{{ get_storage_status(show_storage) }}</el-tag>
					</el-descriptions-item>
				</el-descriptions>
			</el-row>
		</el-card>
		<el-card class="box-card" v-if="this.show_type === 2">
			<el-row slot="header">
				<el-page-header @back="show_storage_list()" content="创建存储池" style="color: #409eff"></el-page-header>
			</el-row>
			<el-row>
				<el-form ref="createForm" :model="create_storage" label-width="100px" class="demo-ruleForm">
					<el-form-item label="名称" prop="name">
						<el-input v-model="create_storage.name"></el-input>
					</el-form-item>
					<el-form-item label="存储池类型" prop="type">
						<el-select v-model="create_storage.type" style="width: 100%">
							<el-option label="NFS" value="nfs"></el-option>
						</el-select>
					</el-form-item>
					<el-form-item label="NFS路径" prop="path" v-if="create_storage.type === 'nfs'">
						<el-input v-model="create_storage.path"></el-input>
					</el-form-item>
					<el-form-item label="NFS地址" prop="uri" v-if="create_storage.type === 'nfs'">
						<el-input v-model="create_storage.uri"></el-input>
					</el-form-item>
					<el-form-item>
						<el-button type="primary" @click="create_storage_click">立即创建</el-button>
						<el-button @click="show_storage_list">取消</el-button>
					</el-form-item>
				</el-form>
			</el-row>
		</el-card>
	</div>
</template>
<script>
import { getStorageList, getStorageInfo, pauseStorage, registerStorage, destroyStorage, createStorage } from '@/api/api'
import Notify from '@/api/notify'
import NavViewVue from './NavView.vue'
export default {
	name: 'storageView',
	components: {
		NavViewVue
	},
	data() {
		return {
			data_loading: false,
			show_type: 0,
			show_storage: {},
			create_storage: {
				name: '',
				type: 'nfs',
				param: '',
				path: '',
				uri: ''
			},
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
		get_storage_status(storage) {
			switch (storage.status) {
				case 0:
					return '正在创建'
				case 1:
					return '已就绪'
				case 2:
					return '正在维护'
				case 3:
					return '正在销毁'
				case 4:
					return '存储池错误'
				default:
					return `未知状态[${storage.status}]`
			}
		},
		handle_notify_message(notify) {
			if (notify.type === 7) {
				getStorageInfo({ storageId: notify.id }).then((res) => {
					if (res.code == 0) {
						let update_storage = res.data
						let findIndex = this.storages.findIndex((item) => item.storageId === update_storage.storageId)
						if (findIndex >= 0) {
							this.$set(this.storages, findIndex, update_storage)
						} else {
							this.storages.push(update_storage)
						}
					}
				})
			}
		},
		show_storage_list() {
			this.show_type = 0
		},
		show_create_storage() {
			if (this.$refs['createForm']) {
				this.$refs['createForm'].resetFields()
			}
			this.show_type = 2
		},
		show_storage_info(storage) {
			this.show_storage = storage
			this.show_type = 1
		},
		create_storage_click() {
			let data = {
				name: this.create_storage.name,
				type: this.create_storage.type,
				param: '{}'
			}
			if (this.create_storage.type === 'nfs') {
				data.param = JSON.stringify({
					path: this.create_storage.path,
					uri: this.create_storage.uri
				})
			} else {
				this.$notify.error({
					title: '错误',
					message: `不支持的存储池:${this.create_storage.type}`
				})
				return
			}
			createStorage(data).then((res) => {
				if (res.code === 0) {
					this.storages.push(res.data)
					this.show_type = 0
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建存储池失败:${res.message}`
					})
				}
			})
		},
		get_storage_desplay(size) {
			if (size > 1024 * 1024 * 1024 * 1024) {
				return (size / (1024 * 1024 * 1024 * 1024)).toFixed(2) + ' TB'
			} else if (size > 1024 * 1024 * 1024) {
				return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
			} else if (size > 1024 * 1024) {
				return (size / (1024 * 1024)).toFixed(2) + ' MB'
			} else if (size > 1024) {
				return (size / 1024).toFixed(2) + '  KB'
			} else {
				return size + '  bytes'
			}
		},
		pasue_storage(storage) {
			pauseStorage({ storageId: storage.storageId }).then((res) => {
				if (res.code === 0) {
					storage.status = 2
				} else {
					this.$notify.error({
						title: '错误',
						message: `暂停存储池失败:${res.message}`
					})
				}
			})
		},
		register_storage(storage) {
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
		},
		destroy_storage(storage) {
			destroyStorage({ storageId: storage.storageId }).then((res) => {
				if (res.code === 0) {
					storage.status = 3
				} else {
					this.$notify.error({
						title: '错误',
						message: `删除存储池失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>
<style lang="postcss" scoped>
.table_action button {
	margin: 0.1em;
}
</style>