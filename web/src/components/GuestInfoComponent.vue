<template>
	<div>
		<el-card class="box-card" v-show="this.show_type === 0" v-loading="guest_loading">
			<el-row slot="header">
				<el-page-header @back="on_back_click" content="虚拟机详情"></el-page-header>
			</el-row>
			<el-row style="text-align: left; margin: 20px 0">
				<el-button @click="show_start_guest_click(show_guest_info.current_guest)" type="primary" size="mini" :disabled="show_guest_info.current_guest.status !== 4">启动虚拟机</el-button>
				<el-button @click="show_stop_guest_click(show_guest_info.current_guest)" type="primary" size="mini" :disabled="show_guest_info.current_guest.status > 3">停止虚拟机</el-button>
				<el-button @click="reboot_guest_click(show_guest_info.current_guest)" type="primary" size="mini" :disabled="show_guest_info.current_guest.status !== 2" v-show="show_guest_info.current_guest.type !== 0">重启虚拟机</el-button>
				<el-button @click="show_reinstall_guest_click(show_guest_info.current_guest)" type="primary" size="mini" :disabled="show_guest_info.current_guest.status !== 4" v-show="show_guest_info.current_guest.type !== 0">重装系统</el-button>

				<el-button @click="show_modify_guest_click(show_guest_info.current_guest)" type="primary" size="mini" :disabled="show_guest_info.current_guest.status !== 4" v-show="show_guest_info.current_guest.type !== 0">修改配置</el-button>
				<el-button @click="vnc_click(show_guest_info.current_guest)" type="primary" size="mini" :disabled="show_guest_info.current_guest.status !== 2">远程桌面</el-button>
				<el-button @click="show_attach_cd_room_click(show_guest_info.current_guest)" type="primary" size="mini" :disabled="show_guest_info.current_guest.cdRoom !== 0" v-show="show_guest_info.current_guest.type !== 0">挂载光驱</el-button>
				<el-button @click="detach_guest_cd_room_click(show_guest_info.current_guest)" type="primary" size="mini" :disabled="show_guest_info.current_guest.cdRoom === 0" v-show="show_guest_info.current_guest.type !== 0">卸载光驱</el-button>
				<el-button @click="show_attach_network_click(show_guest_info.current_guest)" type="primary" size="mini" v-show="show_guest_info.current_guest.type !== 0">添加网卡</el-button>
				<el-button @click="show_attach_volume_click(show_guest_info.current_guest)" type="primary" size="mini" v-show="show_guest_info.current_guest.type !== 0">挂载磁盘</el-button>
				<el-button @click="destroy_guest(show_guest_info.current_guest)" type="danger" size="mini" :disabled="show_guest_info.current_guest.status < 3">销毁虚拟机</el-button>
			</el-row>
			<el-row>
				<el-descriptions :column="2" size="medium" border>
					<el-descriptions-item label="ID">{{ show_guest_info.current_guest.guestId }}</el-descriptions-item>
					<el-descriptions-item label="实例名">{{ show_guest_info.current_guest.name }}</el-descriptions-item>
					<el-descriptions-item label="标签">{{ show_guest_info.current_guest.description }}</el-descriptions-item>
					<el-descriptions-item label="总线类型">{{ show_guest_info.current_guest.busType }}</el-descriptions-item>
					<el-descriptions-item label="CPU">{{ show_guest_info.current_guest.cpu }}核</el-descriptions-item>
					<el-descriptions-item label="内存">{{ get_memory_desplay_size(show_guest_info.current_guest.memory) }}</el-descriptions-item>
					<el-descriptions-item label="配额">{{ show_guest_info.current_guest.speed }}</el-descriptions-item>
					<el-descriptions-item label="光盘">{{ show_guest_info.template.name }}</el-descriptions-item>
					<el-descriptions-item label="运行主机">
						<el-button @click="show_host_info(show_guest_info.host.hostId)" type="text" v-show="show_guest_info.host.hostId !== 0" :underline="false">{{ show_guest_info.host.displayName }}</el-button>
						<span v-show="show_guest_info.host.hostId === 0" :underline="false">{{ show_guest_info.host.displayName }}</span>
					</el-descriptions-item>
					<el-descriptions-item label="架构方案">
						<el-button @click="show_scheme_info(show_guest_info.scheme.schemeId)" type="text" v-show="show_guest_info.scheme.schemeId !== 0" :underline="false">{{ show_guest_info.scheme.name }}</el-button>
						<span v-show="show_guest_info.scheme.schemeId === 0" :underline="false">{{ show_guest_info.scheme.name }}</span>
					</el-descriptions-item>
					<el-descriptions-item label="虚拟机类型">
						<el-tag>{{ show_guest_info.current_guest.type === 0 ? '系统' : '用户' }}</el-tag>
					</el-descriptions-item>
					<el-descriptions-item label="虚拟机IP">{{ show_guest_info.current_guest.guestIp }}</el-descriptions-item>
					<el-descriptions-item label="状态">
						<el-tag :type="show_guest_info.current_guest.status === 2 ? 'success' : 'danger'">{{ get_guest_status(show_guest_info.current_guest) }}</el-tag>
					</el-descriptions-item>
				</el-descriptions>
			</el-row>
			<br />
			<el-row>
				<el-tabs type="border-card">
					<el-tab-pane label="磁盘">
						<el-table :v-loading="show_guest_info.volume_loading" :data="show_guest_info.volumes" style="width: 100%">
							<el-table-column label="ID" prop="volumeId" width="80" />
							<el-table-column label="描述" prop="description" width="200" />
							<el-table-column label="容量" prop="capacity" width="150">
								<template #default="scope">
									{{ get_volume_desplay_size(scope.row.capacity) }}
								</template>
							</el-table-column>
							<el-table-column label="已使用" prop="allocation" width="150">
								<template #default="scope">
									{{ get_volume_desplay_size(scope.row.allocation) }}
								</template>
							</el-table-column>
							<el-table-column label="路径" prop="path" show-overflow-tooltip />
							<el-table-column label="操作" width="180">
								<template #default="scope">
									<el-button type="text" @click="show_volume_info(scope.row.volumeId)">详情</el-button>
									<el-button style="margin-left: 10px" type="text" @click="detach_volume_click(scope.row)" :disabled="scope.row.attach.deviceId === 0">卸载磁盘</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-tab-pane>
					<el-tab-pane label="网卡">
						<el-table :v-loading="show_guest_info.network_loading" :data="show_guest_info.networks" style="width: 100%">
							<el-table-column label="MAC" prop="mac" width="200" />
							<el-table-column label="IP" prop="ip" width="200" />
							<el-table-column label="驱动类型" prop="driveType" width="150" />
							<el-table-column label="操作">
								<template #default="scope">
									<el-button type="text" @click="detach_network_click(scope.row)" :disabled="scope.row.deviceId === 0">卸载网卡</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-tab-pane>
				</el-tabs>
			</el-row>
		</el-card>
		<ReInstallComponentVue ref="ReInstallComponentVueRef" @back="show_type = 0" @finish="on_finish_reinstall" v-show="show_type === 1" />
		<HostInfoComponent ref="HostInfoComponentRef" v-show="this.show_type === 2" @back="show_host_return" />
		<SchemeInfoComponent ref="SchemeInfoComponentRef" v-show="this.show_type === 3" @back="show_scheme_return" />
		<VolumeInfoComponent ref="VolumeInfoComponentRef" v-if="this.show_type === 4" @back="show_volume_return" />
		<AttachDiskComponent ref="AttachDiskComponentRef" @onVoumeAttachCallBack="on_volume_attach_callback" />
		<AttachCdRoomComponent ref="AttachCdRoomComponentRef" @onGuestUpdate="on_notify_update_guest_info" />
		<AttachNetworkComponent ref="AttachNetworkComponentRef" @onGuestAttachCallback="on_network_attach_callback" />
		<ModifyGuestComponent ref="ModifyGuestComponentRef" @onGuestUpdate="on_notify_update_guest_info" />
		<StartGuestComponent ref="StartGuestComponentRef" @onGuestUpdate="on_notify_update_guest_info" />
		<StopGuestComponent ref="StopGuestComponentRef" @onGuestUpdate="on_notify_update_guest_info" />
	</div>
</template>
<script>
import util from '@/api/util'
import AttachDiskComponent from '@/components/AttachDiskComponent.vue'
import AttachCdRoomComponent from '@/components/AttachCdRoomComponent.vue'
import AttachNetworkComponent from '@/components/AttachNetworkComponent'
import ModifyGuestComponent from '@/components/ModifyGuestComponent.vue'
import StartGuestComponent from '@/components/StartGuestComponent'
import StopGuestComponent from '@/components/StopGuestComponent.vue'
import ReInstallComponentVue from './ReInstallComponent.vue'
import HostInfoComponent from '@/components/HostInfoComponent.vue'
import SchemeInfoComponent from './SchemeInfoComponent.vue'
import VolumeInfoComponent from '@/components/VolumeInfoComponent'

import { destroyGuest, getTemplateInfo, getSchemeInfo, getHostInfo, getGuestVolumes, getGuestNetworks, rebootGuest, detachGuestCdRoom, detachGuestNetwork, detachGuestDisk, getGuestInfo } from '@/api/api'

export default {
	components: {
		AttachDiskComponent,
		AttachCdRoomComponent,
		AttachNetworkComponent,
		ModifyGuestComponent,
		StartGuestComponent,
		StopGuestComponent,
		ReInstallComponentVue,
		HostInfoComponent,
		SchemeInfoComponent,
		VolumeInfoComponent
	},
	mixins: [util],
	data() {
		return {
			show_type: 0,
			guest_loading: false,
			show_guest_info: {
				guestId: 0,
				volume_loading: false,
				network_loading: false,
				guest_loading: true,
				current_guest: {
					guestId: 0,
					name: '',
					description: '',
					busType: 'virtio',
					cpu: 1,
					memory: 524288,
					speed: 500,
					cdRoom: 0,
					hostId: 0,
					schemeId: 0,
					type: 0,
					networkId: 0,
					status: 1,
					guestIp: ''
				},
				host: {
					hostId: 0,
					displayName: '-'
				},
				template: {
					templateId: 0,
					name: '-'
				},
				scheme: {
					schemeId: 0,
					name: '-'
				},
				volumes: [],
				networks: []
			}
		}
	},
	methods: {
		on_volume_attach_callback(guest, volume) {
			this.update_guest_info(guest)
			this.show_guest_info.volumes.push(volume)
		},
		on_network_attach_callback(guest, network) {
			this.update_guest_info(guest)
			this.show_guest_info.networks.push(network)
		},
		show_host_return() {
			this.show_type = 0
		},
		show_scheme_return() {
			this.show_type = 0
		},
		show_volume_return() {
			this.show_type = 0
		},
		show_host_info(hostId) {
			this.show_type = 2
			this.$refs.HostInfoComponentRef.init(hostId)
		},
		show_scheme_info(schemeId) {
			this.show_type = 3
			this.$refs.SchemeInfoComponentRef.init(schemeId)
		},
		show_volume_info(volumeId) {
			this.show_type = 4
			this.$nextTick(() => {
				this.$refs.VolumeInfoComponentRef.init(volumeId)
			})
		},
		on_back_click() {
			this.$emit('back')
		},
		on_notify_update_guest_info(guest) {
			this.$emit('onGuestUpdate', guest)
		},
		show_reinstall_guest_click(guest) {
			this.$refs.ReInstallComponentVueRef.init(guest)
			this.show_type = 1
		},
		on_finish_reinstall(guest) {
			this.show_type = 0
			this.on_notify_update_guest_info(guest)
		},
		refresh_host(host) {
			this.$refs.HostInfoComponentRef.refresh_host(host)
		},
		refresh_scheme(scheme) {
			this.$refs.SchemeInfoComponentRef.refresh_scheme(scheme)
		},
		async init(guest) {
			this.show_type = 0
			this.guest_loading = false
			this.show_guest_info.guestId = guest.guestId
			this.show_guest_info.current_guest = guest
			await this.load_current_guest_template(guest)
			await this.load_current_guest_host(guest)
			await this.load_current_guest_scheme(guest)
			await this.load_current_guest_volume(guest)
			await this.load_current_guest_network(guest)
		},
		async initGuestId(guestId) {
			this.show_type = 0
			this.guest_loading = true
			await getGuestInfo({ guestId: guestId })
				.then((res) => {
					if (res.code === 0) {
						this.init(res.data)
					} else {
						this.$alert(`获取虚拟机信息失败:${res.message}`, '提示', {
							dangerouslyUseHTMLString: true,
							confirmButtonText: '返回',
							type: 'error'
						})
							.then(() => {
								this.on_back_click()
							})
							.catch(() => {
								this.on_back_click()
							})
					}
				})
				.finally(() => {
					this.guest_loading = false
				})
		},
		async load_current_guest_template(guest) {
			this.show_guest_info.template.templateId = guest.cdRoom
			this.show_guest_info.template.name = '-'
			if (this.show_guest_info.template.templateId > 0) {
				await getTemplateInfo({ templateId: guest.cdRoom }).then((res) => {
					if (res.code === 0) {
						this.show_guest_info.template.name = res.data.name
					}
				})
			}
		},
		async load_current_guest_scheme(guest) {
			this.show_guest_info.scheme.schemeId = guest.schemeId
			this.show_guest_info.scheme.name = '-'
			if (this.show_guest_info.scheme.schemeId > 0) {
				await getSchemeInfo({ schemeId: guest.schemeId }).then((res) => {
					if (res.code === 0) {
						this.show_guest_info.scheme.name = res.data.name
					}
				})
			}
		},
		async load_current_guest_host(guest) {
			this.show_guest_info.host.hostId = guest.hostId
			this.show_guest_info.host.displayName = '-'
			if (this.show_guest_info.host.hostId > 0) {
				await getHostInfo({ hostId: guest.hostId }).then((res) => {
					if (res.code === 0) {
						this.show_guest_info.host.displayName = res.data.displayName
					}
				})
			}
		},
		load_current_guest_volume(guest) {
			this.show_guest_info.volume_loading = true
			getGuestVolumes({ guestId: guest.guestId })
				.then((res) => {
					if (res.code === 0) {
						this.show_guest_info.volumes = res.data
					}
				})
				.finally(() => {
					this.show_guest_info.volume_loading = false
				})
		},
		load_current_guest_network(guest) {
			this.show_guest_info.network_loading = true
			getGuestNetworks({ guestId: guest.guestId })
				.then((res) => {
					if (res.code === 0) {
						this.show_guest_info.networks = res.data
					}
				})
				.finally(() => {
					this.show_guest_info.network_loading = false
				})
		},
		async update_guest_info(guest) {
			if (this.show_guest_info.guestId === guest.guestId) {
				this.show_guest_info.current_guest = guest
				await this.load_current_guest_template(guest)
				await this.load_current_guest_host(guest)
				await this.load_current_guest_scheme(guest)
				this.$forceUpdate()
			}
		},
		show_start_guest_click(guest) {
			this.$refs.StartGuestComponentRef.init(guest)
		},
		show_attach_network_click(guest) {
			this.$refs.AttachNetworkComponentRef.init(guest)
		},
		show_attach_volume_click(guest) {
			this.$refs.AttachDiskComponentRef.init(guest)
		},
		show_stop_guest_click(guest) {
			this.$refs.StopGuestComponentRef.init(guest)
		},
		show_attach_cd_room_click(guest) {
			this.$refs.AttachCdRoomComponentRef.init(guest)
		},
		show_modify_guest_click(guest) {
			this.$refs.ModifyGuestComponentRef.init(guest)
		},
		vnc_click(guest) {
			let { href } = this.$router.resolve({ path: '/Vnc', query: { id: guest.guestId, description: guest.description } })
			window.open(href, '_blank')
		},
		detach_guest_cd_room_click(guest) {
			detachGuestCdRoom({ guestId: guest.guestId }).then((res) => {
				if (res.code === 0) {
					this.$emit('onGuestUpdate', res.data)
					this.stop_dialog_visiable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `卸载虚拟机光驱失败:${res.message}`
					})
				}
			})
		},
		detach_network_click(guestNetwork) {
			this.$confirm('卸载当前挂载网卡, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					detachGuestNetwork({ guestId: guestNetwork.guestId, guestNetworkId: guestNetwork.guestNetworkId }).then((res) => {
						if (res.code === 0) {
							this.$emit('onGuestUpdate', res.data)
							let findIndex = this.show_guest_info.networks.findIndex((item) => item.guestNetworkId === guestNetwork.guestNetworkId)
							if (findIndex >= 0) {
								this.show_guest_info.networks.splice(findIndex, 1)
							}
						} else {
							this.$notify.error({
								title: '错误',
								message: `虚拟机附加网卡失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		detach_volume_click(guestVolume) {
			this.$confirm('卸载当前挂载磁盘, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					let attach = guestVolume.attach
					detachGuestDisk({ guestId: attach.guestId, guestDiskId: attach.guestDiskId }).then((res) => {
						if (res.code === 0) {
							let findIndex = this.show_guest_info.volumes.findIndex((v) => v.attach.guestDiskId === attach.guestDiskId)
							if (findIndex >= 0) {
								this.show_guest_info.volumes.splice(findIndex, 1)
							}
						} else {
							this.$notify.error({
								title: '错误',
								message: `卸载磁盘失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
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
							this.$emit('onGuestUpdate', res.data)
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
		destroy_guest(guest) {
			this.$confirm('删除当前虚拟机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					destroyGuest({ guestId: guest.guestId }).then((res) => {
						if (res.code === 0) {
							this.on_back_click()
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除虚拟机失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>
