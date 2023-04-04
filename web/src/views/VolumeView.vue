<template>
	<div>
		<el-container>
			<el-main>
				<el-card class="box-card" v-show="this.show_type === 0">
					<el-row slot="header" class="clearfix" style="height: 30px">
						<div style="float: left">
							<el-form :inline="true" class="demo-form-inline">
								<el-form-item><el-button size="mini" type="primary" @click="show_create_volume">创建磁盘</el-button></el-form-item>
								<!-- <el-form-item><el-button size="mini" type="primary" @click="show_upload_volume">导入磁盘</el-button></el-form-item> -->
								<el-form-item><el-button size="mini" :disabled="!select_volumes.length" type="danger" @click="batch_destroy_volume_click">批量删除</el-button></el-form-item>

								<el-form-item label="存储池">
									<el-select v-model="select_storage_id" style="width: 100%" @change="update_show_page">
										<el-option label="全部" :value="0"></el-option>
										<el-option v-for="item in this.storages" :key="item.storageId" :label="item.description" :value="item.storageId" />
									</el-select>
								</el-form-item>
							</el-form>
						</div>
						<div>
							<el-input style="float: right; width: 300px; margin-bottom: 10px" placeholder="请输入搜索关键字" v-model="keyword" @input="on_key_word_change"></el-input>
						</div>
					</el-row>
					<el-row>
						<el-table ref="volumeTable" :v-loading="true" :data="show_table_volumes" style="width: 100%" @selection-change="handleSelectionChange">
							<el-table-column type="selection" width="55"></el-table-column>
							<el-table-column label="ID" prop="volumeId" width="80" />
							<el-table-column label="名称" prop="description" show-overflow-tooltip />
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
							<el-table-column label="挂载机器" prop="allocation" width="120" show-overflow-tooltip>
								<template #default="scope">
									<el-button type="text" @click="show_guest_info(scope.row.attach.guestId)" v-if="scope.row.attach" :underline="false">{{ scope.row.attach ? scope.row.attach.description : '-' }}</el-button>
									<span v-if="!scope.row.attach">{{ scope.row.attach ? scope.row.attach.description : '-' }}</span>
								</template>
							</el-table-column>
							<el-table-column label="状态" prop="status" width="100">
								<template #default="scope">
									<el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ get_volume_status(scope.row) }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="操作" width="250">
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
				<VolumeInfoComponent ref="VolumeInfoComponentRef" @back="show_volume_list()" @onVolumeUpdate="update_volume_info" v-show="this.show_type === 1" />

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
									<el-option v-for="item in this.storages" :key="item.storageId" :label="item.description" :value="item.storageId" />
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
								©
								<el-input v-model="create_volume.volumeSize"></el-input>
							</el-form-item>
							<el-form-item>
								<el-button type="primary" @click="create_volume_click">立即创建</el-button>
								<el-button @click="show_volume_list">取消</el-button>
							</el-form-item>
						</el-form>
					</el-row>
				</el-card>
				<CloneVolumeComponent ref="CloneVolumeComponentRef" @back="show_volume_list()" @onVolumeUpdate="update_volume_info" v-show="this.show_type === 3" />

				<MigrateVolumeComponent ref="MigrateVolumeComponentRef" @back="show_volume_list()" @onVolumeUpdate="update_volume_info" v-show="this.show_type === 4" />

				<UploadVolumeComponent ref="UploadVolumeComponentRef" @back="show_volume_list()" @onVolumeUpdate="update_volume_info" v-show="this.show_type === 5" />

				<GuestInfoComponent ref="GuestInfoComponentRef" @back="show_volume_list" v-show="this.show_type === 6" />

				<DownloadVolumeComponent ref="DownloadVolumeComponentRef" />
				<ResizeVolumeComponent ref="ResizeVolumeComponentRef" @onVolumeUpdate="update_volume_info" />
				<CreateVolumeTemplateComponent ref="CreateVolumeTemplateComponentRef" />
				<CreateVolumeSnapshotComponent ref="CreateVolumeSnapshotComponentRef" />
			</el-main>
		</el-container>
	</div>
</template>
<script>
import { getVolumeList, getStorageList, getVolumeInfo, destroyVolume, createVolume, batchDestroyVolume } from '@/api/api'
import Notify from '@/api/notify'
import util from '@/api/util'
import GuestInfoComponent from '@/components/GuestInfoComponent'
import DownloadVolumeComponent from '@/components/DownloadVolumeComponent'
import ResizeVolumeComponent from '@/components/ResizeVolumeComponent'
import CreateVolumeTemplateComponent from '@/components/CreateVolumeTemplateComponent'
import CreateVolumeSnapshotComponent from '@/components/CreateVolumeSnapshotComponent.vue'
import CloneVolumeComponent from '@/components/CloneVolumeComponent'
import MigrateVolumeComponent from '@/components/MigrateVolumeComponent.vue'
import UploadVolumeComponent from '@/components/UploadVolumeComponent'
import VolumeInfoComponent from '@/components/VolumeInfoComponent'
export default {
	name: 'volumeView',
	components: { GuestInfoComponent, DownloadVolumeComponent, ResizeVolumeComponent, CreateVolumeTemplateComponent, CreateVolumeSnapshotComponent, CloneVolumeComponent, MigrateVolumeComponent, UploadVolumeComponent, VolumeInfoComponent },
	data() {
		return {
			data_loading: false,
			uploading: false,
			show_type: -1,
			select_storage_id: 0,
			create_volume: {
				description: '',
				storageId: 0,
				volumeType: 'qcow2',
				volumeSize: 100
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
	mixins: [Notify, util],
	mounted() {
		this.show_type = 0

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
		show_guest_info(guestId) {
			this.$refs.GuestInfoComponentRef.initGuestId(guestId)
			this.show_type = 6
		},
		on_key_word_change() {
			this.current_page = 1
			this.update_show_page()
		},
		update_show_page() {
			let nCount = 0
			let nStart = this.page_size * (this.current_page - 1)
			let nEnd = this.page_size * this.current_page
			this.volumes.forEach((item, index) => {
				let hasKeyword = true
				let searchKeyword = this.keyword.trim().toLowerCase()
				if (searchKeyword !== '') {
					let attachDescription = item.attach ? item.attach.description.toLowerCase() : ''
					hasKeyword = '' + item.volumeId === searchKeyword || item.description.toLowerCase().indexOf(searchKeyword) >= 0 || attachDescription.indexOf(searchKeyword) >= 0
				}
				let isStorage = true
				if (this.select_storage_id > 0) {
					isStorage = item.storageId === this.select_storage_id
				}
				if (hasKeyword && isStorage) {
					nCount++
					if (nCount <= nStart || nCount > nEnd) {
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
			let findStorage = this.storages.find((item) => item.storageId === storageId) || { description: '-' }
			return findStorage.description
		},
		update_volume_info(volume) {
			let select_volume_ids = this.select_volumes.map((v) => v.volumeId)
			let findIndex = this.volumes.findIndex((item) => item.volumeId === volume.volumeId)
			if (findIndex >= 0) {
				this.$set(this.volumes, findIndex, volume)
			} else {
				let index = this.page_size * (this.current_page - 1)
				this.volumes.splice(index, 0, volume)
			}
			this.update_show_page()
			this.$nextTick(() => {
				this.volumes.forEach((v) => {
					if (select_volume_ids.includes(v.volumeId) && v.isShow) {
						this.$refs.volumeTable.toggleRowSelection(v)
					}
				})
			})
			this.$refs.VolumeInfoComponentRef.refresh_volume(volume)
		},
		handle_notify_message(notify) {
			if (notify.type === 2) {
				getVolumeInfo({ volumeId: notify.id }).then((res) => {
					if (res.code == 0) {
						this.update_volume_info(res.data)
					} else if (res.code == 4000001) {
						let select_volume_ids = this.select_volumes.map((v) => v.volumeId)
						let findIndex = this.volumes.findIndex((v) => v.volumeId === notify.id)
						if (findIndex >= 0) {
							this.volumes.splice(findIndex, 1)
						}
						this.$nextTick(() => {
							this.volumes.forEach((v) => {
								if (select_volume_ids.includes(v.volumeId) && v.isShow) {
									this.$refs.volumeTable.toggleRowSelection(v)
								}
							})
						})
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
		show_upload_volume() {
			this.$refs.UploadVolumeComponentRef.init()
			this.show_type = 5
		},
		async show_volume_info(volume) {
			this.$refs.VolumeInfoComponentRef.init_volume(volume)
			this.show_type = 1
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
		show_download_volume_click(volume) {
			this.$refs.DownloadVolumeComponentRef.init(volume)
		},
		show_clone_volume_click(volume) {
			this.$refs.CloneVolumeComponentRef.init(volume)
			this.show_type = 3
		},
		show_migrate_volume_click(volume) {
			this.$refs.MigrateVolumeComponentRef.init(volume)
			this.show_type = 4
		},
		show_create_volume_template_click(volume) {
			this.$refs.CreateVolumeTemplateComponentRef.init(volume)
		},
		show_resize_volume_click(volume) {
			this.$refs.ResizeVolumeComponentRef.init(volume)
		},

		show_create_volume_snapshot_click(volume) {
			this.$refs.CreateVolumeSnapshotComponentRef.init(volume)
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
							this.update_volume_info(res.data)
							this.show_type = 0
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