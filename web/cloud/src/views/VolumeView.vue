<template>
	<div>
		<el-container>
			<el-main>
				<el-card class="box-card" v-show="this.show_type === 0">
					<el-row slot="header" class="clearfix" style="height: 20px">
						<el-col :span="12">
							<el-link type="primary" @click="show_create_volume">创建磁盘</el-link>
							<el-link style="padding: 0 10px" :disabled="!select_volumes.length" type="danger" @click="batch_destroy_volume_click">批量删除</el-link>
						</el-col>
						<el-col :span="12">
							<el-input style="float: right; width: 300px; margin-bottom: 10px" placeholder="请输入搜索关键字" v-model="keyword" @input="update_show_page"></el-input>
						</el-col>
					</el-row>
					<el-row>
						<el-table :v-loading="data_loading" :data="show_table_volumes" style="width: 100%" @selection-change="handleSelectionChange">
							<el-table-column type="selection" width="55"></el-table-column>
							<el-table-column label="ID" prop="volumeId" width="80" />
							<el-table-column label="名称" prop="description" />
							<el-table-column label="磁盘类型" prop="type" width="100">
								<template #default="scope">
									<el-tag>{{ scope.row.type }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="磁盘空间" prop="capacity" width="100">
								<template #default="scope">
									{{ get_volume_desplay_size(scope.row.capacity) }}
								</template>
							</el-table-column>
							<el-table-column label="物理空间" prop="allocation" width="150">
								<template #default="scope">
									{{ get_volume_desplay_size(scope.row.allocation) }}
								</template>
							</el-table-column>
							<el-table-column label="挂载主机" prop="allocation" width="200">
								<template #default="scope">
									<el-link type="primary" :href="`/#/Guest?id=${scope.row.attach.guestId}`" v-if="scope.row.attach" :underline="false">{{ scope.row.attach ? scope.row.attach.description : '-' }}</el-link>
									<span v-if="!scope.row.attach">{{ scope.row.attach ? scope.row.attach.description : '-' }}</span>
								</template>
							</el-table-column>
							<el-table-column label="状态" prop="status" width="100">
								<template #default="scope">
									<el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ get_volume_status(scope.row) }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="操作" width="200">
								<template #default="scope">
									<el-dropdown size="small" @click="show_volume_info(scope.row)" split-button placement="bottom-end" type="primary" @command="menu_command_click">
										磁盘管理
										<el-dropdown-menu slot="dropdown">
											<el-dropdown-item :command="{ volume: scope.row, command: 'info' }">磁盘详情</el-dropdown-item>
											<el-dropdown-item :command="{ volume: scope.row, command: 'resize' }" divided>扩容磁盘</el-dropdown-item>
											<el-dropdown-item :command="{ volume: scope.row, command: 'clone' }">克隆磁盘</el-dropdown-item>
											<el-dropdown-item :command="{ volume: scope.row, command: 'migrate' }">迁移磁盘</el-dropdown-item>
											<el-dropdown-item :command="{ volume: scope.row, command: 'snapshote' }" divided>创建快照</el-dropdown-item>
											<el-dropdown-item :command="{ volume: scope.row, command: 'template' }">创建模版</el-dropdown-item>
											<el-dropdown-item :command="{ volume: scope.row, command: 'destroy' }" divided>销毁磁盘</el-dropdown-item>
										</el-dropdown-menu>
									</el-dropdown>
								</template>
							</el-table-column>
						</el-table>
						<el-pagination :current-page="current_page" :page-size="page_size" :page-sizes="[5, 10, 20, 50, 100, 200]" :total="total_size" layout="total, sizes, prev, pager, next, jumper" @size-change="on_page_size_change" @current-change="on_current_page_change"></el-pagination>
					</el-row>
				</el-card>
				<el-card class="box-card" v-show="this.show_type === 1">
					<el-row slot="header">
						<el-page-header @back="show_volume_list" content="磁盘详情"></el-page-header>
					</el-row>
					<el-row style="text-align: left; margin: 20px 0">
						<el-button @click="show_create_volume()" type="primary" size="mini">创建磁盘</el-button>
						<el-button @click="show_resize_volume_click(show_volume)" type="primary" size="mini">扩容磁盘</el-button>
						<el-button @click="show_clone_volume_click(show_volume)" type="primary" size="mini">克隆磁盘</el-button>
						<el-button @click="show_migrate_volume_click(show_volume)" type="primary" size="mini">迁移磁盘</el-button>
						<el-button @click="show_create_volume_snapshot_click(show_volume)" type="primary" size="mini">创建快照</el-button>
						<el-button @click="show_create_volume_template_click(show_volume)" type="primary" size="mini">创建模版</el-button>
						<el-button @click="destroy_volume(show_volume)" type="danger" size="mini">销毁磁盘</el-button>
					</el-row>
					<el-row>
						<el-descriptions :column="2" size="medium" border>
							<el-descriptions-item label="ID">{{ show_volume.volumeId }}</el-descriptions-item>
							<el-descriptions-item label="磁盘名">{{ show_volume.description }}</el-descriptions-item>
							<el-descriptions-item label="磁盘类型">{{ show_volume.type }}</el-descriptions-item>
							<el-descriptions-item label="磁盘路径">{{ show_volume.path }}</el-descriptions-item>
							<el-descriptions-item label="磁盘模版">{{ show_volume.template ? show_volume.template.name : '-' }}</el-descriptions-item>
							<el-descriptions-item label="磁盘存储池">
								<el-link type="primary" :href="`/#/Storage?id=${show_volume.storageId}`" :underline="false">
									{{ get_storage_name(show_volume.storageId) }}
								</el-link>
							</el-descriptions-item>
							<el-descriptions-item label="挂载主机">
								<el-link type="primary" :href="`/#/Guest?id=${show_volume.attach.guestId}`" :underline="false" v-if="show_volume.attach">
									{{ show_volume.attach.description }}
								</el-link>
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
				<el-card class="box-card" v-show="this.show_type === 2">
					<el-row slot="header">
						<el-page-header @back="show_volume_list()" content="创建磁盘" style="color: #409eff"></el-page-header>
					</el-row>
					<el-row>
						<el-form ref="createForm" :model="create_volume" label-width="100px" class="demo-ruleForm">
							<el-form-item label="名称" prop="description">
								<el-input v-model="create_volume.description"></el-input>
							</el-form-item>
							<el-form-item label="存储池" prop="type">
								<el-select v-model="create_volume.storageId" style="width: 100%">
									<el-option label="随机" :value="0"></el-option>
									<el-option v-for="item in this.storages" :key="item.storageId" :label="item.name" :value="item.storageId" />
								</el-select>
							</el-form-item>
							<el-form-item label="磁盘类型" prop="volumeType">
								<el-select v-model="create_volume.volumeType" style="width: 100%">
									<el-option label="raw" value="raw"></el-option>
									<el-option label="qcow" value="qcow"></el-option>
									<el-option label="qcow2" value="qcow2"></el-option>
									<el-option label="vdi" value="vdi"></el-option>
									<el-option label="vmdk" value="vmdk"></el-option>
									<el-option label="vpc" value="vpc"></el-option>
								</el-select>
							</el-form-item>
							<el-form-item label="磁盘大小(GB)" prop="volumeSize">
								<el-input v-model="create_volume.volumeSize"></el-input>
							</el-form-item>
							<el-form-item>
								<el-button type="primary" @click="create_volume_click">立即创建</el-button>
								<el-button @click="show_volume_list">取消</el-button>
							</el-form-item>
						</el-form>
					</el-row>
				</el-card>
				<el-card class="box-card" v-show="this.show_type === 3">
					<el-row slot="header">
						<el-page-header @back="show_volume_list()" content="克隆磁盘" style="color: #409eff"></el-page-header>
					</el-row>
					<el-row>
						<el-form ref="cloneForm" :model="clone_volume" label-width="100px" class="demo-ruleForm">
							<el-form-item label="名称" prop="description">
								<el-input v-model="clone_volume.description"></el-input>
							</el-form-item>
							<el-form-item label="存储池" prop="storageId">
								<el-select v-model="clone_volume.storageId" style="width: 100%">
									<el-option label="随机" :value="0"></el-option>
									<el-option v-for="item in this.storages" :key="item.storageId" :label="item.name" :value="item.storageId" />
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
								<el-button @click="show_volume_list">取消</el-button>
							</el-form-item>
						</el-form>
					</el-row>
				</el-card>

				<el-card class="box-card" v-show="this.show_type === 4">
					<el-row slot="header">
						<el-page-header @back="show_volume_list()" content="迁移磁盘" style="color: #409eff"></el-page-header>
					</el-row>
					<el-row>
						<el-form :model="migrate_volume" label-width="100px" class="demo-ruleForm">
							<el-form-item label="存储池" prop="type">
								<el-select v-model="migrate_volume.storageId" style="width: 100%">
									<el-option label="随机" :value="0"></el-option>
									<el-option v-for="item in this.storages" :key="item.storageId" :label="item.name" :value="item.storageId" />
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
								<el-button @click="show_volume_list">取消</el-button>
							</el-form-item>
						</el-form>
					</el-row>
				</el-card>

				<el-dialog title="磁盘扩容" :visible.sync="resize_dialog_visiable" width="300px">
					<el-form :model="resize_volume" label-width="100px">
						<el-form-item label="磁盘大小(GB)">
							<el-input v-model="resize_volume.size" placeholder="请输入磁盘大小(GB)"></el-input>
						</el-form-item>
					</el-form>
					<span slot="footer" class="dialog-footer">
						<el-button @click="resize_dialog_visiable = false">取 消</el-button>
						<el-button type="primary" @click="resize_volume_click">确 定</el-button>
					</span>
				</el-dialog>
				<el-dialog title="创建模版" :visible.sync="template_dialog_visiable" width="400px">
					<el-form :model="resize_volume" label-width="100px">
						<el-form-item label="模版名称">
							<el-input v-model="template_volume.name" placeholder="请输入模版名称"></el-input>
						</el-form-item>
					</el-form>
					<span slot="footer" class="dialog-footer">
						<el-button @click="template_dialog_visiable = false">取 消</el-button>
						<el-button type="primary" @click="create_volume_template_click">确 定</el-button>
					</span>
				</el-dialog>
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
			</el-main>
		</el-container>
	</div>
</template>
<script>
import { getVolumeList, getStorageList, getVolumeInfo, destroyVolume, createVolume, getTemplateInfo, cloneVolume, migrateVolume, resizeVolume, createVolumeTemplate, createSnapshot, batchDestroyVolume } from '@/api/api'
import Notify from '@/api/notify'
export default {
	name: 'volumeView',
	components: {},
	data() {
		return {
			data_loading: false,
			resize_dialog_visiable: false,
			template_dialog_visiable: false,
			snapshot_dialog_visiable: false,
			current_volume_id: 0,
			current_loading: false,
			show_type: -1,
			show_volume: {},
			create_volume: {
				description: '',
				storageId: 0,
				volumeType: 'qcow2',
				volumeSize: 100
			},
			clone_volume: {
				sourceVolumeId: 0,
				description: '',
				storageId: 0,
				volumeType: 'qcow2'
			},
			migrate_volume: {
				sourceVolumeId: 0,
				storageId: 0,
				volumeType: 'qcow2'
			},
			resize_volume: {
				volumeId: 0,
				size: 100
			},
			template_volume: {
				volumeId: 0,
				name: ''
			},
			snapshot_volume: {
				volumeId: 0,
				snapshotName: '',
				snapshotVolumeType: 'qcow2'
			},
			keyword: '',
			volumes: [],
			storages: [],
			select_volumes: [],
			current_page: 1,
			page_size: 10,
			total_size: 0
		}
	},
	mixins: [Notify],
	mounted() {
		this.current_volume_id = this.$route.query.id
		if (this.current_volume_id) {
			this.show_type == 2
			this.current_loading = true
			getVolumeInfo({ volumeId: this.current_volume_id })
				.then((res) => {
					if (res.code === 0) {
						this.show_volume_info(res.data)
					} else {
						this.$alert(`获取磁盘信息失败:${res.message}`, '提示', {
							dangerouslyUseHTMLString: true,
							confirmButtonText: '返回',
							type: 'error'
						}).then(() => {
							this.show_type = 0
						})
					}
				})
				.finally(() => {
					this.current_loading = false
				})
		} else {
			this.show_type = 0
		}
		this.init_view()
		this.init_notify()
	},
	computed: {
		show_table_volumes() {
			return this.volumes.filter((v) => {
				return v.isShow === undefined || v.isShow
			})
		}
	},
	methods: {
		handleSelectionChange(val) {
			this.select_volumes = val
		},
		async init_view() {
			this.data_loading = true
			await getStorageList().then((res) => {
				if (res.code == 0) {
					this.storages = res.data
				}
			})
			await getVolumeList()
				.then((res) => {
					if (res.code == 0) {
						this.volumes = res.data
						this.update_show_page()
					}
				})
				.finally(() => {
					this.data_loading = false
				})
		},
		on_current_page_change(current_page) {
			this.current_page = current_page
			this.update_show_page()
		},
		on_page_size_change(page_size) {
			this.page_size = page_size
			this.update_show_page()
		},
		update_show_page() {
			let nCount = 0
			this.volumes.forEach((item, index) => {
				let hasKeyword = true
				let searchKeyword = this.keyword.trim().toLowerCase()
				if (searchKeyword !== '') {
					let attachDescription = item.attach ? item.attach.description.toLowerCase() : ''
					hasKeyword = '' + item.volumeId === searchKeyword || item.description.toLowerCase().indexOf(searchKeyword) >= 0 || attachDescription.indexOf(searchKeyword) >= 0
				}
				if (hasKeyword) {
					nCount++
					if (nCount <= this.page_size * (this.current_page - 1) || nCount > this.page_size * this.current_page) {
						item.isShow = false
					} else {
						item.isShow = true
					}
				} else {
					item.isShow = false
				}
				this.$set(this.volumes, index, item)
			})
			this.total_size = nCount
		},
		get_storage_name(storageId) {
			let findStorage = this.storages.find((item) => item.storageId === storageId) || { name: '-' }
			return findStorage.name
		},
		get_volume_status(volume) {
			switch (volume.status) {
				case 0:
					return '正在创建'
				case 1:
					return '已就绪'
				case 2:
					return '正在挂载'
				case 3:
					return '正在卸载'
				case 4:
					return '正在克隆'
				case 5:
					return '创建模版'
				case 6:
					return '创建快照'
				case 7:
					return '正在迁移'
				case 8:
					return '正在扩容'
				case 9:
					return '正在销毁'
				case 10:
					return '磁盘错误'
				default:
					return `未知状态[${volume.status}]`
			}
		},
		update_volume_info(volume) {
			let findIndex = this.volumes.findIndex((item) => item.volumeId === volume.volumeId)
			if (findIndex >= 0) {
				this.$set(this.volumes, findIndex, volume)
			} else {
				let index = this.page_size * (this.current_page - 1)
				this.volumes.splice(index, 0, volume)
			}
			if (this.show_volume && this.show_volume.volumeId === volume.volumeId) {
				this.show_volume = volume
			}
			this.update_show_page()
			this.$forceUpdate()
		},
		handle_notify_message(notify) {
			if (notify.type === 2) {
				getVolumeInfo({ volumeId: notify.id }).then((res) => {
					if (res.code == 0) {
						this.update_volume_info(res.data)
					} else if (res.code == 4000001) {
						let findIndex = this.volumes.findIndex((v) => v.volumeId === notify.id)
						if (findIndex >= 0) {
							this.volumes.splice(findIndex, 1)
						}
					}
				})
			}
		},
		show_volume_list() {
			this.show_type = 0
		},
		show_create_volume() {
			if (this.$refs['createForm']) {
				this.$refs['createForm'].resetFields()
			}
			this.show_type = 2
		},
		async show_volume_info(volume) {
			this.show_volume = volume
			await this.init_volume_template(volume)
			this.show_type = 1
		},
		async init_volume_template(volume) {
			if (volume.templateId === 0) {
				return
			}
			await getTemplateInfo({ templateId: volume.templateId }).then((res) => {
				if (res.code == 0) {
					volume.template = res.data
				}
			})
		},
		create_volume_click() {
			createVolume(this.create_volume).then((res) => {
				if (res.code === 0) {
					this.update_volume_info(res.data)
					this.show_type = 0
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建磁盘失败:${res.message}`
					})
				}
			})
		},
		update_volume_status(volumeId, status) {
			let find = this.volumes.find((item) => item.volumeId === volumeId)
			if (find) {
				find.status = status
			}
		},
		menu_command_click(data) {
			switch (data.command) {
				case 'info':
					this.show_volume_info(data.volume)
					break
				case 'resize':
					this.show_resize_volume_click(data.volume)
					break
				case 'snapshote':
					this.show_create_volume_snapshot_click(data.volume)
					break
				case 'clone':
					this.show_clone_volume_click(data.volume)
					break
				case 'migrate':
					this.show_migrate_volume_click(data.volume)
					break
				case 'template':
					this.show_create_volume_template_click(data.volume)
					break
				case 'destroy':
					this.destroy_volume(data.volume)
					break
			}
		},
		show_clone_volume_click(volume) {
			this.clone_volume.sourceVolumeId = volume.volumeId
			this.clone_volume.volumeType = volume.type
			this.clone_volume.description = 'Clone-' + volume.description
			this.clone_volume.storageId = volume.storageId
			this.show_type = 3
		},
		show_migrate_volume_click(volume) {
			this.migrate_volume.sourceVolumeId = volume.volumeId
			this.migrate_volume.volumeType = volume.type
			this.migrate_volume.storageId = volume.storageId
			this.show_type = 4
		},
		show_create_volume_template_click(volume) {
			this.template_volume.volumeId = volume.volumeId
			this.template_volume.name = ''
			this.template_dialog_visiable = true
		},
		show_resize_volume_click(volume) {
			this.resize_volume.volumeId = volume.volumeId
			this.resize_volume.size = 100
			this.resize_dialog_visiable = true
		},

		show_create_volume_snapshot_click(volume) {
			this.snapshot_volume.volumeId = volume.volumeId
			this.snapshot_volume.name = ''
			this.snapshot_volume.snapshotVolumeType = volume.type
			this.snapshot_dialog_visiable = true
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
							this.update_volume_info(res.data.clone)
							this.update_volume_info(res.data.source)
							this.show_type = 0
						} else {
							this.$notify.error({
								title: '错误',
								message: `克隆磁盘失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
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
							this.update_volume_info(res.data.migrate)
							this.update_volume_info(res.data.source)
							this.show_type = 0
						} else {
							this.$notify.error({
								title: '错误',
								message: `迁移磁盘失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		resize_volume_click() {
			this.$confirm('扩容磁盘, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					resizeVolume(this.resize_volume).then((res) => {
						if (res.code === 0) {
							this.update_volume_info(res.data)
							this.resize_dialog_visiable = false
						} else {
							this.$notify.error({
								title: '错误',
								message: `扩容磁盘失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		create_volume_template_click() {
			this.$confirm('创建磁盘模版, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					createVolumeTemplate(this.template_volume).then((res) => {
						if (res.code === 0) {
							this.update_volume_status(this.template_volume.volumeId, 5)
							this.template_dialog_visiable = false
						} else {
							this.$notify.error({
								title: '错误',
								message: `创建磁盘模版失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
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
							this.update_volume_status(this.snapshot_volume.volumeId, 6)
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
		},
		get_volume_desplay_size(size) {
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
		destroy_volume(volume) {
			this.$confirm('删除磁盘, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					destroyVolume({ volumeId: volume.volumeId }).then((res) => {
						if (res.code === 0) {
							this.update_volume_info(res.data)
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除磁盘失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		batch_destroy_volume_click() {
			this.$confirm('批量删除所选磁盘, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					let volumeIds = this.select_volumes.map((v) => v.volumeId).join(',')
					batchDestroyVolume({ volumeIds: volumeIds }).then((res) => {
						if (res.code === 0) {
							res.data.filter((guest) => {
								this.update_volume_info(guest)
							})
						} else {
							this.$notify.error({
								title: '错误',
								message: `批量删除磁盘:${res.message}`
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
.el-dropdown {
	vertical-align: top;
}
.el-dropdown + .el-dropdown {
	margin-left: 15px;
}
.el-icon-arrow-down {
	font-size: 12px;
}
</style>