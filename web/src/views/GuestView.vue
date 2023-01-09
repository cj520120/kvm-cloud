<template>
	<div>
		<el-container>
			<el-main>
				<el-card class="box-card" v-show="this.show_type === 0">
					<el-row slot="header" class="clearfix" style="height: 30px">
						<div style="float: left">
							<el-form :inline="true" class="demo-form-inline">
								<el-form-item>
									<el-button type="primary" size="mini" @click="show_create_guest_click">创建虚拟机</el-button>
								</el-form-item>
								<el-form-item><el-button :disabled="!select_guests.length" type="primary" size="mini" @click="batch_start_guest_click">批量启动</el-button></el-form-item>
								<el-form-item><el-button :disabled="!select_guests.length" type="danger" size="mini" @click="batch_stop_guest_click">批量停止</el-button></el-form-item>
								<el-form-item label="运行主机">
									<el-select v-model="select_host_id" style="width: 100%" @change="update_guest_show_page">
										<el-option label="全部" :value="0"></el-option>
										<el-option v-for="item in this.hosts" :key="item.hostId" :label="item.displayName" :value="item.hostId" />
									</el-select>
								</el-form-item>
							</el-form>
						</div>
						<div>
							<el-input style="float: right; width: 300px; margin-bottom: 10px" placeholder="请输入搜索关键字" v-model="keyword" @input="on_key_word_change"></el-input>
						</div>
					</el-row>
					<el-row>
						<el-table ref="guestTable" :v-loading="data_loading" :data="show_table_guests" style="width: 100%" @selection-change="handleSelectionChange">
							<el-table-column type="selection" width="55"></el-table-column>
							<el-table-column label="ID" prop="guestId" width="80" />
							<el-table-column label="实例名" prop="name" width="180" show-overflow-tooltip />
							<el-table-column label="标签" prop="description" width="180" />
							<el-table-column label="IP地址" prop="guestIp" width="150" />
							<el-table-column label="配置" prop="cpu" width="150">
								<template #default="scope">{{ scope.row.cpu }}核/{{ get_memory_desplay_size(scope.row.memory) }}</template>
							</el-table-column>
							<el-table-column label="类型" width="100">
								<template #default="scope">
									<el-tag>{{ scope.row.type === 0 ? '系统主机' : '用户主机' }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="状态" prop="status" width="100">
								<template #default="scope">
									<el-tag :type="scope.row.status === 2 ? 'success' : 'danger'">{{ get_guest_status(scope.row) }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="操作">
								<template #default="scope">
									<el-dropdown size="small" @click="show_guest_info_click(scope.row)" split-button placement="bottom-end" type="primary" @command="menu_command_click">
										虚拟机管理
										<el-dropdown-menu slot="dropdown">
											<el-dropdown-item :command="{ guest: scope.row, command: 'info' }">虚拟机详情</el-dropdown-item>
											<el-dropdown-item :command="{ guest: scope.row, command: 'start' }" divided :disabled="scope.row.status !== 4">启动虚拟机</el-dropdown-item>
											<el-dropdown-item :command="{ guest: scope.row, command: 'stop' }" :disabled="scope.row.status !== 2">停止虚拟机</el-dropdown-item>
											<el-dropdown-item :command="{ guest: scope.row, command: 'vnc' }" :disabled="scope.row.status !== 2">远程桌面</el-dropdown-item>
											<el-dropdown-item :command="{ guest: scope.row, command: 'reboot' }" :disabled="scope.row.status !== 2">重启虚拟机</el-dropdown-item>
											<el-dropdown-item :command="{ guest: scope.row, command: 'attach_cd' }" :disabled="scope.row.cdRoom !== 0" v-if="scope.row.type !== 0">挂载光驱</el-dropdown-item>
											<el-dropdown-item :command="{ guest: scope.row, command: 'detach_cd' }" :disabled="scope.row.cdRoom === 0" v-if="scope.row.type !== 0">卸载光驱</el-dropdown-item>
											<el-dropdown-item :command="{ guest: scope.row, command: 'destroy' }" divided>销毁虚拟机</el-dropdown-item>
										</el-dropdown-menu>
									</el-dropdown>
								</template>
							</el-table-column>
						</el-table>
						<el-pagination :current-page="current_page" :page-size="page_size" :page-sizes="[1, 2, 5, 10, 20, 50, 100, 200]" :total="total_size" layout="total, sizes, prev, pager, next, jumper" @size-change="on_page_size_change" @current-change="on_current_page_change"></el-pagination>
					</el-row>
				</el-card>
				<GuestInfoComponent ref="GuestInfoComponentRef" @back="show_guest_list_page" @onGuestUpdate="update_guest_info" v-show="this.show_type === 1" />
				<CreateGuestComponent ref="CreateGuestComponentRef" @back="show_guest_list_page" @onGuestUpdate="update_guest_info" v-show="this.show_type === 2" />
			</el-main>
		</el-container>

		<AttachCdRoomComponent ref="AttachCdRoomComponentRef" @onGuestUpdate="update_guest_info" />
		<StartGuestComponent ref="StartGuestComponentRef" @onGuestUpdate="update_guest_info" />
		<StopGuestComponent ref="StopGuestComponentRef" @onGuestUpdate="update_guest_info" />
	</div>
</template>
<script>
import { getGuestInfo, destroyGuest, rebootGuest, getHostList, detachGuestCdRoom, getUserGuestList, batchStoptGuest, batchStartGuest, getHostInfo, getSchemeInfo } from '@/api/api'
import Notify from '@/api/notify'
import StartGuestComponent from '@/components/StartGuestComponent'
import StopGuestComponent from '@/components/StopGuestComponent.vue'
import GuestInfoComponent from '@/components/GuestInfoComponent'
import CreateGuestComponent from '@/components/CreateGuestComponent'
import AttachCdRoomComponent from '@/components/AttachCdRoomComponent'
import util from '@/api/util'
export default {
	name: 'guestView',
	components: {
		StartGuestComponent,
		StopGuestComponent,
		GuestInfoComponent,
		CreateGuestComponent,
		AttachCdRoomComponent
	},
	data() {
		return {
			data_loading: false,
			select_host_id: 0,
			show_type: -1,
			keyword: '',
			guests: [],
			storages: [],
			hosts: [],
			select_guests: [],
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
		show_table_guests() {
			return this.guests.filter((v) => {
				return v.isShow === undefined || v.isShow
			})
		}
	},
	methods: {
		handleSelectionChange(val) {
			this.select_guests = val
		},
		async init_view() {
			this.data_loading = true
			await getUserGuestList()
				.then((res) => {
					if (res.code == 0) {
						this.guests = res.data
						this.update_guest_show_page()
					}
				})
				.finally(() => {
					this.data_loading = false
				})
			await this.load_all_host()
		},
		async load_all_host() {
			await getHostList().then((res) => {
				if (res.code === 0) {
					this.hosts = res.data.filter((v) => v.status == 1)
				}
			})
		},
		on_key_word_change() {
			this.current_page = 1
			this.update_guest_show_page()
		},
		on_current_page_change(current_page) {
			this.current_page = current_page
			this.update_guest_show_page()
		},
		on_page_size_change(page_size) {
			this.page_size = page_size
			this.update_guest_show_page()
		},
		update_guest_show_page() {
			let nCount = 0
			let nStart = this.page_size * (this.current_page - 1)
			let nEnd = this.page_size * this.current_page
			this.guests.forEach((item, index) => {
				let hasKeyword = true
				let searchKeyword = this.keyword.trim().toLowerCase()
				if (searchKeyword !== '') {
					hasKeyword = '' + item.guestId === searchKeyword || item.description.toLowerCase().indexOf(searchKeyword) >= 0 || item.name.toLowerCase().indexOf(searchKeyword) >= 0 || item.guestIp.toLowerCase().indexOf(searchKeyword) >= 0
				}
				let isHost = true
				if (this.select_host_id > 0) {
					isHost = item.hostId === this.select_host_id
				}
				if (hasKeyword && isHost) {
					nCount++
					if (nCount <= nStart || nCount > nEnd) {
						item.isShow = false
					} else {
						item.isShow = true
					}
				} else {
					item.isShow = false
				}
				this.$set(this.guests, index, item)
			})
			this.total_size = nCount
		},
		async update_guest_info(guest) {
			let select_guest_ids = this.select_guests.map((v) => v.guestId)
			let findIndex = this.guests.findIndex((item) => item.guestId === guest.guestId)
			if (findIndex >= 0) {
				this.$set(this.guests, findIndex, guest)
			} else if (guest.type === 1) {
				//只处理用户系统
				let index = this.page_size * (this.current_page - 1)
				this.guests.splice(index, 0, guest)
			}
			this.update_guest_show_page()
			this.$nextTick(() => {
				this.guests.forEach((v) => {
					if (select_guest_ids.includes(v.guestId) && v.isShow) {
						this.$refs.guestTable.toggleRowSelection(v)
					}
				})
			})
			this.$refs.GuestInfoComponentRef.update_guest_info(guest)
		},
		handle_notify_message(notify) {
			if (notify.type === 1) {
				getGuestInfo({ guestId: notify.id }).then((res) => {
					if (res.code == 0) {
						this.update_guest_info(res.data)
					} else if (res.code == 2000001) {
						let select_guest_ids = this.select_guests.map((v) => v.guestId)
						let findIndex = this.guests.findIndex((v) => v.guestId === notify.id)
						if (findIndex >= 0) {
							this.guests.splice(findIndex, 1)
						}
						this.$nextTick(() => {
							this.guests.forEach((v) => {
								if (select_guest_ids.includes(v.guestId) && v.isShow) {
									this.$refs.guestTable.toggleRowSelection(v)
								}
							})
						})
					}
				})
			} else if (notify.type === 4) {
				if (notify.type === 4) {
					getHostInfo({ hostId: notify.id }).then((res) => {
						if (res.code == 0) {
							this.$refs.GuestInfoComponentRef.refresh_host(res.data)
						}
					})
				}
			} else if (notify.type === 8) {
				getSchemeInfo({ schemeId: notify.id }).then((res) => {
					if (res.code == 0) {
						this.$refs.GuestInfoComponentRef.refresh_scheme(res.data)
					}
				})
			}
		},
		show_guest_list_page() {
			this.show_type = 0
		},
		show_host_back() {
			this.show_type = 1
		},
		async show_create_guest_click() {
			this.$refs.CreateGuestComponentRef.init()
			this.show_type = 2
		},
		reboot_guest_click(guest) {
			this.$confirm('重启当前虚拟机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					rebootGuest({ guestId: guest.guestId }).then((res) => {
						if (res.code === 0) {
							this.update_guest_info(res.data)
						} else {
							this.$notify.error({
								title: '错误',
								message: `重启虚拟机失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		detach_guest_cd_room_click(guest) {
			detachGuestCdRoom({ guestId: guest.guestId }).then((res) => {
				if (res.code === 0) {
					this.update_guest_info(res.data)
					this.stop_dialog_visiable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `卸载虚拟机光驱失败:${res.message}`
					})
				}
			})
		},
		menu_command_click(data) {
			switch (data.command) {
				case 'info':
					this.show_guest_info_click(data.guest)
					break
				case 'start':
					this.show_start_guest_click(data.guest)
					break
				case 'reboot':
					this.reboot_guest_click(data.guest)
					break
				case 'stop':
					this.show_stop_guest_click(data.guest)
					break
				case 'detach_cd':
					this.detach_guest_cd_room_click(data.guest)
					break
				case 'attach_cd':
					this.show_attach_cd_room_click(data.guest)
					break
				case 'vnc':
					this.vnc_click(data.guest)
					break
				case 'destroy':
					this.destroy_guest(data.guest)
					break
			}
		},
		async show_guest_info_click(guest) {
			this.$refs.GuestInfoComponentRef.init(guest)
			this.show_type = 1
		},
		async show_start_guest_click(guest) {
			this.$refs.StartGuestComponentRef.init(guest)
		},
		show_stop_guest_click(guest) {
			this.$refs.StopGuestComponentRef.init(guest)
		},
		show_attach_cd_room_click(guest) {
			this.$refs.AttachCdRoomComponentRef.init(guest)
		},
		vnc_click(guest) {
			let { href } = this.$router.resolve({ path: '/Vnc', query: { id: guest.guestId, description: guest.description } })

			window.open(href, '_blank')
		},
		destroy_guest(guest) {
			this.$confirm('删除当前虚拟机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					destroyGuest({ guestId: guest.guestId }).then((res) => {
						if (res.code === 0) {
							let findIndex = this.guests.findIndex((v) => v.guestId === guest.guestId)
							if (findIndex >= 0) {
								this.guests.splice(findIndex, 1)
							}
							this.show_type = 0
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除虚拟机失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		batch_start_guest_click() {
			this.$confirm('启动所选虚拟机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					let guestIds = this.select_guests.map((v) => v.guestId).join(',')
					batchStartGuest({ guestIds: guestIds }).then((res) => {
						if (res.code === 0) {
							res.data.filter((guest) => {
								this.update_guest_info(guest)
							})
						} else {
							this.$notify.error({
								title: '错误',
								message: `批量启动虚拟机失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		batch_stop_guest_click() {
			this.$confirm('停止所选虚拟机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					let guestIds = this.select_guests.map((v) => v.guestId).join(',')
					batchStoptGuest({ guestIds: guestIds }).then((res) => {
						if (res.code === 0) {
							res.data.filter((guest) => {
								this.update_guest_info(guest)
							})
						} else {
							this.$notify.error({
								title: '错误',
								message: `批量停止虚拟机失败:${res.message}`
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