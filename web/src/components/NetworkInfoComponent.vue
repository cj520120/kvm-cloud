<template>
	<div>
		<el-card class="box-card" v-show="this.show_type === 0" v-loading="network_loading">
			<el-row slot="header">
				<el-page-header @back="on_back_click" content="网络详情"></el-page-header>
			</el-row>
			<el-row style="text-align: left; margin: 20px 0">
				<el-button @click="register_network(show_network)" type="success" size="mini">重新注册</el-button>
				<el-button @click="pasue_network(show_network)" type="warning" size="mini" v-if="show_network.status !== 3">开始维护</el-button>
				<el-button @click="destroy_network(show_network)" type="danger" size="mini">销毁网络</el-button>
			</el-row>
			<el-row>
				<el-descriptions :column="2" size="medium" border>
					<el-descriptions-item label="ID">{{ show_network.networkId }}</el-descriptions-item>
					<el-descriptions-item label="名称">{{ show_network.name }}</el-descriptions-item>
					<el-descriptions-item label="起始IP">{{ show_network.startIp }}</el-descriptions-item>
					<el-descriptions-item label="结束IP">{{ show_network.endIp }}</el-descriptions-item>
					<el-descriptions-item label="网关地址">{{ show_network.gateway }}</el-descriptions-item>
					<el-descriptions-item label="子网掩码">{{ show_network.mask }}</el-descriptions-item>
					<el-descriptions-item label="子网地址">{{ show_network.subnet }}</el-descriptions-item>
					<el-descriptions-item label="广播地址">{{ show_network.broadcast }}</el-descriptions-item>
					<el-descriptions-item label="DNS">{{ show_network.dns }}</el-descriptions-item>
					<el-descriptions-item label="桥接网卡">{{ show_network.bridge }}</el-descriptions-item>
					<el-descriptions-item label="网络类型">{{ get_network_type(show_network) }}</el-descriptions-item>
					<el-descriptions-item label="VLAN ID" v-if="show_network.type === 1">{{ show_network.vlanId }}</el-descriptions-item>
					<el-descriptions-item label="基础网络" v-if="show_network.type === 1">
						<el-button type="text">{{ get_parent_network(show_network).name }}</el-button>
					</el-descriptions-item>
					<el-descriptions-item label="网络状态">
						<el-tag :type="show_network.status === 2 ? 'success' : 'danger'">{{ get_network_status(show_network) }}</el-tag>
					</el-descriptions-item>
				</el-descriptions>

				<br />
				<el-tabs type="border-card">
					<el-tab-pane label="系统组件">
						<el-table :data="system_guests" style="width: 100%">
							<el-table-column label="ID" prop="guestId" width="80" />
							<el-table-column label="实例名" prop="name" width="200" />
							<el-table-column label="标签" prop="description" width="200" />
							<el-table-column label="IP地址" prop="guestIp" width="150" />

							<el-table-column label="状态" prop="status" width="100">
								<template #default="scope">
									<el-tag :type="scope.row.status === 2 ? 'success' : 'danger'">{{ get_guest_status(scope.row) }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="操作">
								<template #default="scope">
									<el-button type="text" @click="go_guest_info(scope.row.guestId)">详情</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-tab-pane>
				</el-tabs>
			</el-row>
		</el-card>
		<GuestInfoComponent ref="GuestInfoComponentRef" @back="show_type = 0" @onGuestUpdate="update_guest_info" v-show="this.show_type === 1" />
	</div>
</template>
<script>
import { getNetworkInfo, pauseNetwork, registerNetwork, destroyNetwork, getSystemGuestList } from '@/api/api'
import util from '@/api/util'
import GuestInfoComponent from '@/components/GuestInfoComponent'
export default {
	data() {
		return {
			network_loading: false,
			system_guests: [],
			show_type: 0,
			show_network: {
				networkId: 0,
				name: '',
				startIp: '',
				endIp: '',
				gateway: '',
				mask: '',
				subnet: '',
				broadcast: '',
				bridge: '',
				dns: '',
				type: 0,
				vlanId: 100,
				basicNetworkId: ''
			}
		}
	},
	components: { GuestInfoComponent },
	mixins: [util],
	methods: {
		on_back_click() {
			this.$emit('back')
		},
		on_notify_update_networkt_info(network) {
			this.refresh_network(network)
			this.$emit('onNetworkUpdate', network)
		},
		refresh_network(network) {
			if (this.show_network.networkId == network.networkId) {
				this.show_network = network
			}
		},
		go_guest_info(guestId) {
			this.show_type = 1
			this.$refs.GuestInfoComponentRef.initGuestId(guestId)
		},
		async init_network(network) {
			this.show_type = 0
			this.show_network = network
			this.system_guests = []
			await this.load_system_guest(network)
		},
		async init(networkId) {
			this.show_type = 0
			this.network_loading = true
			await getNetworkInfo({ networkId: networkId })
				.then((res) => {
					console.log(res)
					if (res.code === 0) {
						this.init_network(res.data)
					} else {
						this.$alert(`获取网络信息失败:${res.message}`, '提示', {
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
					this.host_loading = false
				})
		},
		async load_system_guest(network) {
			this.system_guests = []
			this.network_loading = true
			await getSystemGuestList({ networkId: network.networkId })
				.then((res) => {
					if (res.code === 0) {
						this.system_guests = res.data
					}
				})
				.finally(() => {
					this.network_loading = false
				})
		},
		delete_guest(guestId) {
			let findIndex = this.system_guests.findIndex((item) => item.guestId === guestId)
			if (findIndex >= 0) {
				this.system_guests.splice(findIndex, 1)
			}
		},
		update_guest_info(guest) {
			if (guest.type === 0 && guest.networkId === this.show_network.networkId) {
				let findIndex = this.system_guests.findIndex((item) => item.guestId === guest.guestId)
				if (findIndex >= 0) {
					this.$set(this.system_guests, findIndex, guest)
				} else {
					this.system_guests.push(guest)
				}
				this.$forceUpdate()
			}
			this.$refs.GuestInfoComponentRef.update_guest_info(guest)
		},
		pasue_network(network) {
			this.$confirm('维护网络, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					pauseNetwork({ networkId: network.networkId }).then((res) => {
						if (res.code === 0) {
							this.on_notify_update_networkt_info(res.data)
						} else {
							this.$notify.error({
								title: '错误',
								message: `暂停网络失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		register_network(network) {
			this.$confirm('重新注册网络, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					registerNetwork({ networkId: network.networkId }).then((res) => {
						if (res.code === 0) {
							this.on_notify_update_networkt_info(res.data)
						} else {
							this.$notify.error({
								title: '错误',
								message: `注册网络失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		destroy_network(network) {
			this.$confirm('销毁网络, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					destroyNetwork({ networkId: network.networkId }).then((res) => {
						if (res.code === 0) {
							this.on_back_click()
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除网络失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>