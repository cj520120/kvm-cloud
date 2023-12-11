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
					<el-descriptions-item label="搜索域">{{ show_network.domain }}</el-descriptions-item>
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
							<el-table-column label="VIP地址" prop="componentVip" width="150" />
							<el-table-column label="类型" prop="status" width="80">
								<template #default="scope">
									<el-tag type="success">{{ get_component_type(scope.row.componentType) }}</el-tag>
								</template>
							</el-table-column>
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
					<el-tab-pane label="内部dns">
						<el-button type="primary" size="small" @click="dialog_create_network_dns_visible = true">新建内部解析</el-button>
						<el-table :data="network_dns_list" style="width: 100%">
							<el-table-column label="IP" prop="ip" width="300" />
							<el-table-column label="Domain" prop="domain" />
							<el-table-column label="操作" width="200px">
								<template #default="scope">
									<el-button type="text" size="small" @click="destroy_network_dns(scope.row.id)">删除</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-tab-pane>
				</el-tabs>
			</el-row>
		</el-card>
		<GuestInfoComponent ref="GuestInfoComponentRef" @back="show_type = 0" @onGuestUpdate="update_guest_info" v-show="this.show_type === 1" />
		<el-dialog title="创建内部解析" :visible.sync="dialog_create_network_dns_visible" width="500px">
			<el-form :model="create_network_dns" label-width="100px" class="demo-ruleForm">
				<el-form-item label="域名" prop="name">
					<el-input v-model="create_network_dns.domain"></el-input>
				</el-form-item>
				<el-form-item label="IP" prop="cpu">
					<el-input v-model="create_network_dns.ip"></el-input>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="create_network_dns_click">创建</el-button>
					<el-button @click="dialog_create_network_dns_visible = false">取消</el-button>
				</el-form-item>
			</el-form>
		</el-dialog>
	</div>
</template>
<script>
import Notify from '@/api/notify'
import { getNetworkInfo, pauseNetwork, registerNetwork, destroyNetwork, getSystemGuestList, getNetworkDnsList, destroyNetworDns, createNetworkDns } from '@/api/api'
import util from '@/api/util'
import GuestInfoComponent from '@/components/GuestInfoComponent'
export default {
	name: 'NetworkInfoComponent',
	data() {
		return {
			dialog_create_network_dns_visible: false,
			network_loading: false,
			system_guests: [],
			networks: [],
			network_dns_list: [],
			show_type: 0,
			create_network_dns: {
				networkId: 0,
				domain: '',
				ip: ''
			},
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
				domain: '',
				type: 0,
				vlanId: 100,
				basicNetworkId: ''
			},
			show_network_id: 0
		}
	},
	components: { GuestInfoComponent },
	mixins: [Notify, util],
	created() {
		this.show_network_id = 0
		this.subscribe_notify(this.$options.name, this.dispatch_notify_message)
		this.subscribe_connect_notify(this.$options.name, this.reload_page)
		this.init_notify()
	},
	beforeDestroy() {
		this.unsubscribe_notify(this.$options.name)
		this.unsubscribe_connect_notify(this.$options.name)
		this.show_network_id = 0
	},
	methods: {
		on_back_click() {
			this.show_network_id = 0
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
		async init_network(networks, show_network) {
			this.show_type = 0
			if (networks) {
				this.networks = networks
			}
			this.show_network = show_network
			this.system_guests = []
			this.network_dns_list = []
			this.create_network_dns.networkId = show_network.networkId
			this.create_network_dns.domain = ''
			this.create_network_dns.ip = ''
			this.show_network_id = show_network.networkId
			await this.load_system_guest(show_network)
			await this.load_network_dns(show_network)
		},
		async reload_page() {
			if (this.show_network_id > 0) {
				this.show_type = 0
				this.network_loading = true
				await getNetworkInfo({ networkId: this.show_network_id })
					.then((res) => {
						this.network_loading = false
						if (res.code === 0) {
							this.init_network(null, res.data)
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
						this.network_loading = false
					})
			}
		},
		async init(networkId) {
			this.show_network_id = networkId
			this.reload_page()
		},
		async load_network_dns(network) {
			this.network_dns_list = []
			await getNetworkDnsList({ networkId: network.networkId }).then((res) => {
				if (res.code === 0) {
					this.network_dns_list = res.data
				}
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
		get_parent_network(network) {
			let find = this.networks.find((v) => v.networkId === network.basicNetworkId)
			return find || { name: '-' }
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
		create_network_dns_click() {
			createNetworkDns(this.create_network_dns).then((res) => {
				if (res.code === 0) {
					this.create_network_dns.domain = ''
					this.create_network_dns.ip = ''
					let findIndex = this.network_dns_list.findIndex((v) => v.id === res.data.id)
					if (findIndex < 0) {
						this.network_dns_list.push(res.data)
					}
					this.dialog_create_network_dns_visible = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `添加内部Dns失败:${res.message}`
					})
				}
			})
		},
		destroy_network_dns(dnsId) {
			this.$confirm('删除dns记录, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					destroyNetworDns({ dnsId: dnsId }).then((res) => {
						if (res.code === 0) {
							let findIndex = this.network_dns_list.findIndex((v) => v.id === dnsId)
							if (findIndex >= 0) {
								this.network_dns_list.splice(findIndex, 1)
							}
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除网络Dns失败:${res.message}`
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
		},
		dispatch_notify_message(notify) {
			if (notify.type === 3 && this.show_network.networkId === notify.id) {
				let res = notify.data
				if (res.code == 0) {
					this.refresh_network(res.data)
				} else if (res.code == 2000001) {
					this.on_back_click()
				}
			} else if (notify.type === 1) {
				let res = notify.data
				if (res.code == 0) {
					this.update_guest_info(res.data)
				} else if (res.code == 2000001) {
					this.delete_guest(notify.id)
				}
			} else if (notify.type === 10) {
				let res = notify.data
				if (res.code == 0 && res.data.networkId == this.show_network.networkId) {
					let findIndex = this.network_dns_list.findIndex((v) => v.id === notify.id)
					if (findIndex >= 0) {
						this.$set(this.volumes, findIndex, res.data)
					} else {
						this.network_dns_list.push(res.data)
					}
				} else if (res.code == 11000001) {
					let findIndex = this.network_dns_list.findIndex((v) => v.id === notify.id)
					if (findIndex >= 0) {
						this.network_dns_list.splice(findIndex, 1)
					}
				}
			}
		}
	}
}
</script>