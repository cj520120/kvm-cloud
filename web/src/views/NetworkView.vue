<template>
	<div>
		<el-container>
			<el-main>
				<el-card class="box-card" v-show="this.show_type === 0">
					<el-row slot="header" class="clearfix" style="height: 20px">
						<el-button style="float: left; padding: 3px 0" type="text" @click="show_create_network">创建网络</el-button>
					</el-row>
					<el-row>
						<el-table :v-loading="data_loading" :data="networks" style="width: 100%">
							<el-table-column label="ID" prop="networkId" width="80" />
							<el-table-column label="名称" prop="name" width="120" show-overflow-tooltip />
							<el-table-column label="桥接网卡" prop="bridge" width="120" />
							<el-table-column label="网络类型" prop="type" width="100">
								<template #default="scope">
									<el-tag>{{ get_network_type(scope.row) }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="状态" prop="status" width="100">
								<template #default="scope">
									<el-tag :type="scope.row.status === 2 ? 'success' : 'danger'">{{ get_network_status(scope.row) }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="操作" min-width="380">
								<template #default="scope">
									<el-button @click="show_network_info_click(scope.row)" type="" size="mini">网络详情</el-button>
									<el-button @click="register_network(scope.row)" type="success" size="mini">重新注册</el-button>
									<el-button @click="pasue_network(scope.row)" type="warning" size="mini" v-if="scope.row.status !== 3">开始维护</el-button>
									<el-button @click="destroy_network(scope.row)" type="danger" size="mini">销毁网络</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-row>
				</el-card>
				<NetworkInfoComponent ref="NetworkInfoComponentRef" @onNetworkUpdate="update_network_info" @back="show_network_list()" v-show="this.show_type === 1" />

				<el-card class="box-card" v-show="this.show_type === 2">
					<el-row slot="header">
						<el-page-header @back="show_network_list()" content="创建网络" style="color: #409eff"></el-page-header>
					</el-row>
					<el-row>
						<el-form ref="createForm" :model="create_network" label-width="100px" class="demo-ruleForm">
							<el-row :gutter="24">
								<el-col :span="12">
									<el-form-item label="网络名称" prop="name">
										<el-input v-model="create_network.name"></el-input>
									</el-form-item>
								</el-col>
								<el-col :span="12">
									<el-form-item label="网络类型" prop="type">
										<el-select v-model="create_network.type" style="width: 100%">
											<el-option label="基础网络" :value="0"></el-option>
											<!-- <el-option label="Vlan网络" :value="1"></el-option> -->
										</el-select>
									</el-form-item>
								</el-col>
							</el-row>

							<el-row :gutter="24">
								<el-col :span="12">
									<el-form-item label="起始IP" prop="startIp"><el-input v-model="create_network.startIp"></el-input></el-form-item>
								</el-col>
								<el-col :span="12">
									<el-form-item label="结束IP" prop="endIp"><el-input v-model="create_network.endIp"></el-input></el-form-item>
								</el-col>
							</el-row>

							<el-row :gutter="24">
								<el-col :span="12">
									<el-form-item label="网关地址" prop="gateway"><el-input v-model="create_network.gateway"></el-input></el-form-item>
								</el-col>
								<el-col :span="12">
									<el-form-item label="子网掩码" prop="mask"><el-input v-model="create_network.mask"></el-input></el-form-item>
								</el-col>
							</el-row>

							<el-row :gutter="24">
								<el-col :span="12">
									<el-form-item label="子网地址" prop="subnet"><el-input v-model="create_network.subnet"></el-input></el-form-item>
								</el-col>
								<el-col :span="12">
									<el-form-item label="广播地址" prop="broadcast"><el-input v-model="create_network.broadcast"></el-input></el-form-item>
								</el-col>
							</el-row>
							<el-row :gutter="24">
								<el-col :span="24">
									<el-form-item label="DNS" prop="dns"><el-input v-model="create_network.dns"></el-input></el-form-item>
								</el-col>
							</el-row>

							<el-row :gutter="24">
								<el-col :span="12">
									<el-form-item label="桥接网卡" prop="bridge"><el-input v-model="create_network.bridge"></el-input></el-form-item>
								</el-col>
							</el-row>

							<el-row :gutter="24" v-if="create_network.type === 1">
								<el-col :span="12">
									<el-form-item label="VLAN ID" prop="vlanId"><el-input v-model="create_network.vlanId"></el-input></el-form-item>
								</el-col>
								<el-col :span="12">
									<el-form-item label="基础网络" prop="basicNetworkId">
										<el-select v-model="create_network.basicNetworkId" placeholder="请选择基础网络" style="width: 100%">
											<el-option :label="item.name" :value="item.networkId" v-show="item.type === 0" v-for="item in networks" :key="item.networkId"></el-option>
										</el-select>
									</el-form-item>
								</el-col>
							</el-row>
							<el-form-item>
								<el-button type="primary" @click="create_network_click">立即创建</el-button>
								<el-button @click="show_network_list">取消</el-button>
							</el-form-item>
						</el-form>
					</el-row>
				</el-card>
			</el-main>
		</el-container>
	</div>
</template>
<script>
import util from '@/api/util'
import { getNetworkList, getNetworkInfo, pauseNetwork, registerNetwork, destroyNetwork, createNetwork, getGuestInfo } from '@/api/api'

import NetworkInfoComponent from '@/components/NetworkInfoComponent'
import Notify from '@/api/notify'
export default {
	name: 'NetworkView',
	components: { NetworkInfoComponent },
	data() {
		return {
			data_loading: false,
			show_type: -1,
			create_network: {
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
			},
			networks: []
		}
	},
	mounted() {
		this.show_type = 0
		this.init_view()
		this.init_notify()
	},
	mixins: [Notify, util],
	methods: {
		async init_view() {
			this.data_loading = true
			await getNetworkList()
				.then((res) => {
					if (res.code == 0) {
						this.networks = res.data
					}
				})
				.finally(() => {
					this.data_loading = false
				})
		},
		get_parent_network(network) {
			let find = this.networks.find((v) => v.networkId === network.basicNetworkId)
			return find || { name: '-' }
		},
		update_network_info(network) {
			let findIndex = this.networks.findIndex((item) => item.networkId === network.networkId)
			if (findIndex >= 0) {
				this.$set(this.networks, findIndex, network)
			} else {
				this.networks.push(network)
			}

			this.$refs.NetworkInfoComponentRef.refresh_network(network)
		},
		handle_notify_message(notify) {
			if (notify.type === 3) {
				getNetworkInfo({ networkId: notify.id }).then((res) => {
					if (res.code == 0) {
						this.update_network_info(res.data)
					} else if (res.code == 1000001) {
						let findIndex = this.networks.findIndex((v) => v.networkId === notify.id)
						if (findIndex >= 0) {
							this.networks.splice(findIndex, 1)
						}
					}
					this.$forceUpdate()
				})
			} else if (notify.type === 1) {
				getGuestInfo({ guestId: notify.id }).then((res) => {
					if (res.code == 0) {
						this.$refs.NetworkInfoComponentRef.update_guest_info(res.data)
					} else if (res.code == 2000001) {
						this.$refs.NetworkInfoComponentRef.delete_guest(res.data)
					}
				})
			}
		},
		show_network_list() {
			this.show_type = 0
		},
		show_create_network() {
			if (this.$refs['createForm']) {
				this.$refs['createForm'].resetFields()
			}
			this.show_type = 2
		},
		show_network_info_click(network) {
			this.$refs.NetworkInfoComponentRef.init_network(network)
			this.show_type = 1
		},
		create_network_click() {
			if (this.create_network.type === 0) {
				this.create_network.vlanId = 0
				this.create_network.basicNetworkId = 0
			}
			createNetwork(this.create_network).then((res) => {
				if (res.code === 0) {
					this.update_network_info(res.data)
					this.show_type = 0
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建网络失败:${res.message}`
					})
				}
			})
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
							this.update_network_info(res.data)
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
							this.update_network_info(res.data)
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
							this.update_network_info(res.data)
							this.show_type = 0
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
<style lang="postcss" scoped>
.table_action button {
	margin: 0.1em;
}
</style>