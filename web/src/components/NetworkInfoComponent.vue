<template>
	<div>
		<el-card v-show="this.show_type === 0" v-loading="network_loading">
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
					<el-descriptions-item label="桥接方式">
						<el-tag>{{ get_bridge_type(show_network) }}</el-tag>
					</el-descriptions-item>
					<el-descriptions-item label="网络类型">{{ get_network_type(show_network) }}</el-descriptions-item>
					<el-descriptions-item label="VLAN ID" v-if="show_network.type === 1">{{ show_network.vlanId }}</el-descriptions-item>

					<el-descriptions-item label="基础网络" v-if="show_network.type === 1">
						<el-button type="text">{{ get_parent_network(show_network).name }}</el-button>
					</el-descriptions-item>
					<el-descriptions-item label="Pool ID">{{ show_network.poolId }}</el-descriptions-item>
					<el-descriptions-item label="网络状态">
						<el-tag :type="show_network.status === 2 ? 'success' : 'danger'">{{ get_network_status(show_network) }}</el-tag>
					</el-descriptions-item>
				</el-descriptions>

				<br />
				<el-card>
					<div slot="header" class="clearfix">
						<span>系统组件</span>
						<el-button style="float: right; padding: 3px 0" @click="dialog_create_network_component_visible = true" type="text">添加组件</el-button>
					</div>
					<el-row class="table_tr" type="flex">
						<el-col style="width: 80px">&nbsp;</el-col>
						<el-col style="width: 150px">ID</el-col>
						<el-col style="width: 200px">类型</el-col>
						<el-col style="width: 200px">VIP</el-col>
						<el-col style="width: 200px">Base VIP</el-col>
						<el-col style="width: 200px">Slave 数量</el-col>
						<el-col style="width: 300px">状态</el-col>
						<el-col style="width: 100%">操作</el-col>
					</el-row>
					<div v-for="(component, index) in components" :key="component.componentId">
						<el-row class="table_td" type="flex">
							<el-col style="width: 80px; text-align: center"><i :class="`${component.is_show ? 'el-icon-arrow-down' : 'el-icon-arrow-right'}`" style="cursor: pointer" @click="change_componet_show(index, component)" /></el-col>
							<el-col style="width: 150px">{{ component.componentId }}</el-col>
							<el-col style="width: 200px">
								<el-tag size="small">{{ get_component_type(component.componentType) }}</el-tag>
							</el-col>
							<el-col style="width: 200px">{{ component.componentVip }}</el-col>
							<el-col style="width: 200px">{{ component.basicComponentVip }}</el-col>
							<el-col style="width: 200px">{{ component.componentSlaveNumber }}</el-col>
							<el-col style="width: 300px">
								<span v-for="guest in component.guests" :key="guest.guestId">
									<i :style="guest.status === 2 ? 'color: #67c23a' : 'color:#F56C6C'" :class="guest.status === 2 ? 'el-icon-success' : 'el-icon-error'" />
								</span>
							</el-col>
							<el-col style="width: 100%">
								<el-button size="mini" type="primary" plain @click="show_update_component_slave_number(component)">修改Slave数量</el-button>
								<el-button size="mini" type="danger" plain v-if="component.componentType != 1" @click="destroy_network_component(component)">删除</el-button>
							</el-col>
						</el-row>
						<div class="component_card" v-show="component.is_show">
							<div>
								<div class="header">
									<span>系统虚拟机</span>
									<div style="float: right; padding: 3px 0">
										<el-button size="mini" type="text" v-if="component.componentType === 2" @click="show_network_component_nat_list(component)">Nat转发管理</el-button>
										<el-button size="mini" type="text" v-if="component.componentType === 1" @click="show_network_dns_list()">Dns管理</el-button>
									</div>
								</div>
								<el-table :data="component.guests" style="width: 100%" size="small">
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
							</div>
						</div>
					</div>
				</el-card>
			</el-row>
		</el-card>
		<GuestInfoComponent ref="GuestInfoComponentRef" @back="show_type = 0" @onGuestUpdate="update_guest_info" v-show="this.show_type === 1" />
		<el-dialog title="创建内部解析" :visible.sync="dialog_create_network_dns_visible" width="500px" :close-on-click-modal="false">
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
		<el-dialog title="创建组件" :visible.sync="dialog_create_network_component_visible" width="500px" :close-on-click-modal="false">
			<el-form :model="create_network_componet" label-width="100px" class="demo-ruleForm">
				<el-form-item label="组件类型" prop="componentType">
					<el-select v-model="create_network_componet.componentType" style="width: 100%">
						<el-option label="Nat网关" :value="2"></el-option>
					</el-select>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="create_network_component">创建</el-button>
					<el-button @click="dialog_create_network_component_visible = false">取消</el-button>
				</el-form-item>
			</el-form>
		</el-dialog>
		<el-dialog title="Dns管理" :visible.sync="dialog_network_dns_visible" width="800px" :close-on-click-modal="false">
			<el-button type="primary" size="small" @click="dialog_create_network_dns_visible = true">新建Dns记录</el-button>
			<el-table :data="network_dns_list" style="width: 100%" max-height="300" height="300">
				<el-table-column label="IP" prop="ip" width="300" />
				<el-table-column label="Domain" prop="domain" />
				<el-table-column label="操作" width="200px">
					<template #default="scope">
						<el-button type="text" size="small" @click="destroy_network_dns(scope.row.id)">删除</el-button>
					</template>
				</el-table-column>
			</el-table>
		</el-dialog>

		<el-dialog title="Nat管理" :visible.sync="dialog_network_component_nat_visible" width="800px" :close-on-click-modal="false">
			<el-button type="primary" size="small" @click="dialog_create_network_component_nat_visible = true">新建转发</el-button>
			<el-table :data="network_component_nat_list" style="width: 100%" max-height="300" height="300">
				<el-table-column label="ID" prop="natId" />
				<el-table-column label="协议" prop="protocol" />
				<el-table-column label="本地端口" prop="localPort" />
				<el-table-column label="目标IP" prop="remoteIp" />
				<el-table-column label="目标端口" prop="remotePort" />
				<el-table-column label="操作" width="200px">
					<template #default="scope">
						<el-button type="text" size="small" @click="destroy_network_componet_nat(scope.row.natId)">删除</el-button>
					</template>
				</el-table-column>
			</el-table>
		</el-dialog>
		<el-dialog title="创建Nat" :visible.sync="dialog_create_network_component_nat_visible" width="500px" :close-on-click-modal="false">
			<el-form :model="create_network_component_nat" label-width="100px" class="demo-ruleForm">
				<el-form-item label="组件ID" prop="componentId">
					<el-input v-model="create_network_component_nat.componentId" :disabled="true"></el-input>
				</el-form-item>
				<el-form-item label="协议" prop="protocol">
					<el-select v-model="create_network_component_nat.protocol">
						<el-option label="tcp" value="tcp"></el-option>
						<el-option label="udp" value="udp"></el-option>
					</el-select>
				</el-form-item>
				<el-form-item label="本地端口" prop="localPort">
					<el-input v-model="create_network_component_nat.localPort"></el-input>
				</el-form-item>
				<el-form-item label="目标IP" prop="remoteIp">
					<el-input v-model="create_network_component_nat.remoteIp"></el-input>
				</el-form-item>
				<el-form-item label="目标端口" prop="remotePort">
					<el-input v-model="create_network_component_nat.remotePort"></el-input>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="create_network_nat_click">创建</el-button>
					<el-button @click="dialog_create_network_component_nat_visible = false">取消</el-button>
				</el-form-item>
			</el-form>
		</el-dialog>
		<el-dialog title="修改Slave数量" :visible.sync="dialog_component_nat_change_slave_number_visible" width="320px" :close-on-click-modal="false">
			<el-form :model="update_component_slave_number" label-width="70px" class="demo-ruleForm" style="width: 100%">
				<el-form-item label="数量" prop="number">
					<el-input-number v-model="update_component_slave_number.number" :min="0" :max="10" label="Slave数量"></el-input-number>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="update_component_slave_number_click">修改</el-button>
					<el-button @click="dialog_component_nat_change_slave_number_visible = false">取消</el-button>
				</el-form-item>
			</el-form>
		</el-dialog>
	</div>
</template>
<script>
import Notify from '@/api/notify'
import { getNetworkInfo, pauseNetwork, registerNetwork, destroyNetwork, getSystemGuestList, getNetworkDnsList, destroyNetworDns, createNetworkDns, getNetworkComponentList, updateNetworkComponentSlaveNumber, createNetworkComponent, destroyNetworkComponent, destroyNetworkComponentNat, getNetworkComponentNatList, createNetworkComponentNat } from '@/api/api'
import util from '@/api/util'
import GuestInfoComponent from '@/components/GuestInfoComponent'

export default {
	name: 'NetworkInfoComponent',
	data() {
		return {
			dialog_component_nat_change_slave_number_visible: false,
			dialog_network_dns_visible: false,
			dialog_create_network_dns_visible: false,
			dialog_create_network_component_visible: false,
			dialog_network_component_nat_visible: false,
			dialog_create_network_component_nat_visible: false,
			network_loading: false,
			system_guests: [],
			networks: [],
			components: [],
			network_dns_list: [],
			network_component_nat_list: [],
			show_type: 0,
			update_component_slave_number: {
				componentId: 0,
				number: 0
			},
			create_network_dns: {
				networkId: 0,
				domain: '',
				ip: ''
			},
			create_network_component_nat: {
				componentId: 0,
				protocol: 'tcp',
				localPort: '',
				remoteIp: '',
				remotePort: ''
			},
			create_network_componet: {
				networkId: 0,
				componentType: 2
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
	computed: {},
	methods: {
		print_scope(s) {
			console.log(s)
		},
		on_back_click() {
			this.show_network_id = 0
			this.$emit('back')
		},
		change_componet_show(index, component) {
			this.$set(this.components, index, { ...component, is_show: !component.is_show })
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
		async show_network_component_nat_list(component) {
			if (this.dialog_network_component_nat_visible) {
				return
			}
			this.dialog_network_component_nat_visible = true
			this.create_network_component_nat.componentId = component.componentId
			this.network_component_nat_list = []
			await getNetworkComponentNatList({ componentId: component.componentId }).then((res) => {
				if (res.code === 0) {
					this.network_component_nat_list = res.data
					this.$forceUpdate()
				}
			})
		},
		async show_network_dns_list() {
			if (this.dialog_network_dns_visible) {
				return
			}
			this.dialog_network_dns_visible = true

			this.network_dns_list = []
			await getNetworkDnsList({ networkId: this.show_network_id }).then((res) => {
				if (res.code === 0) {
					this.network_dns_list = res.data
				}
			})
		},
		show_update_component_slave_number(component) {
			this.update_component_slave_number.componentId = component.componentId
			this.update_component_slave_number.number = component.componentSlaveNumber
			this.dialog_component_nat_change_slave_number_visible = true
		},
		update_component_slave_number_click() {
			updateNetworkComponentSlaveNumber(this.update_component_slave_number).then((res) => {
				if (res.code !== 0) {
					this.$notify.error({
						title: '错误',
						message: `更改网络组件数量失败:${res.message}`
					})
				} else {
					this.dialog_component_nat_change_slave_number_visible = 0
				}
			})
		},

		get_component_guest(componentId) {
			return this.system_guests.filter((v) => {
				return v.componentId === componentId
			})
		},
		refresh_component_guest() {
			this.components.forEach((component, index) => {
				this.$set(this.components, index, { ...component, guests: this.get_component_guest(component.componentId) })
			})
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
			this.create_network_componet.networkId = show_network.networkId
			await this.load_system_guest(show_network)
			await this.load_system_component(show_network)
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

		async load_system_component(network) {
			this.components = []
			await getNetworkComponentList({ networkId: network.networkId }).then((res) => {
				if (res.code === 0) {
					this.components = res.data
					this.refresh_component_guest()
				}
			})
		},
		async load_system_guest(network) {
			this.system_guests = []
			this.network_loading = true
			await getSystemGuestList({ networkId: network.networkId })
				.then((res) => {
					if (res.code === 0) {
						this.system_guests = res.data.map((v) => ({ ...v, guests: [] }))
						this.refresh_component_guest()
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
				this.refresh_component_guest()
				this.$forceUpdate()
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
				this.refresh_component_guest()
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
		create_network_nat_click() {
			createNetworkComponentNat(this.create_network_component_nat).then((res) => {
				if (res.code === 0) {
					this.create_network_component_nat.localPort = ''
					this.create_network_component_nat.remoteIp = ''
					this.create_network_component_nat.remotePort = ''
					this.create_network_component_nat.protocol = 'tcp'

					this.network_component_nat_list.push(res.data)
					this.dialog_create_network_component_nat_visible = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `添加Nat转发失败:${res.message}`
					})
				}
			})
		},
		create_network_component() {
			createNetworkComponent(this.create_network_componet).then((res) => {
				if (res.code === 0) {
					let findIndex = this.components.findIndex((v) => v.componentId === res.data.componentId)
					if (findIndex < 0) {
						this.components.push({ ...res.data, guest: [] })
						this.refresh_component_guest()
					}
					this.dialog_create_network_component_visible = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `添加系统组件失败:${res.message}`
					})
				}
			})
		},
		destroy_network_componet_nat(natId) {
			this.$confirm('删除nat记录, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					console.log(natId)
					destroyNetworkComponentNat({ natId: natId }).then((res) => {
						if (res.code === 0) {
							let findIndex = this.network_component_nat_list.findIndex((v) => v.natId === natId)
							if (findIndex >= 0) {
								this.network_component_nat_list.splice(findIndex, 1)
							}
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除dnat记录失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
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
		destroy_network_component(component) {
			this.$confirm('销毁网络组件, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					destroyNetworkComponent({ componentId: component.componentId }).then((res) => {
						if (res.code === 0) {
							let findIndex = this.components.findIndex((v) => v.componentId === component.componentId)
							if (findIndex >= 0) {
								this.components.splice(findIndex, 1)
							}
						} else {
							this.$notify.error({
								title: '错误',
								message: `销毁网络组件失败:${res.message}`
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
						this.$set(this.network_dns_list, findIndex, res.data)
					} else {
						this.network_dns_list.push(res.data)
					}
				} else if (res.code == 11000001) {
					let findIndex = this.network_dns_list.findIndex((v) => v.id === notify.id)
					if (findIndex >= 0) {
						this.network_dns_list.splice(findIndex, 1)
					}
				}
			} else if (notify.type === 11) {
				let res = notify.data
				if (res.code == 0 && res.data.networkId == this.show_network.networkId) {
					let findIndex = this.components.findIndex((v) => v.componentId === notify.id)
					if (findIndex >= 0) {
						let compoent = this.components[findIndex]
						this.$set(this.components, findIndex, { ...res.data, guests: compoent.guests, is_show: compoent.is_show })
					} else {
						this.components.push(res.data)
					}
					this.refresh_component_guest()
				} else if (res.code == 1000003) {
					let findIndex = this.components.findIndex((v) => v.componentId === notify.id)
					if (findIndex >= 0) {
						this.components.splice(findIndex, 1)
					}
				}
			}
		}
	}
}
</script>
<style scoped>
</style>