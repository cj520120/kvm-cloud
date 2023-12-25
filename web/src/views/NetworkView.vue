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
							<el-table-column label="桥接方式" prop="bridgeType" width="150">
								<template #default="scope">
									<el-tag>{{ get_bridge_type(scope.row) }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="子网" prop="subnet" width="150">
								<template #default="scope">{{ scope.row.subnet }}/{{ netmask2CIDR(scope.row.mask) }}</template>
							</el-table-column>
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
				<CreateNetworkComponent ref="CreateNetworkComponentRef" @onNetworkUpdate="update_network_info" @back="show_network_list()" v-show="this.show_type === 2" />
			</el-main>
		</el-container>
	</div>
</template>
<script>
import util from '@/api/util'
import { getNetworkList, pauseNetwork, registerNetwork, destroyNetwork } from '@/api/api'

import NetworkInfoComponent from '@/components/NetworkInfoComponent'
import CreateNetworkComponent from '@/components/CreateNetworkComponent'
import Notify from '@/api/notify'
export default {
	name: 'NetworkView',
	components: { NetworkInfoComponent, CreateNetworkComponent },
	data() {
		return {
			data_loading: false,
			show_type: -1,
			networks: []
		}
	},
	mounted() {
		this.show_type = 0
		this.init_view()
	},
	created() {
		this.subscribe_notify(this.$options.name, this.dispatch_notify_message)
		this.subscribe_connect_notify(this.$options.name, this.reload_page)
		this.init_notify()
	},
	beforeDestroy() {
		this.unsubscribe_notify(this.$options.name)
		this.unsubscribe_connect_notify(this.$options.name)
	},
	mixins: [Notify, util],
	methods: {
		async reload_page() {
			this.init_view()
		},
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
		netmask2CIDR(netmask) {
			return (
				netmask
					.split('.')
					.map(Number)
					.map((part) => (part >>> 0).toString(2))
					.join('')
					.split('1').length - 1
			)
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
		dispatch_notify_message(notify) {
			if (notify.type === 3) {
				let res = notify.data
				if (res.code == 0) {
					this.update_network_info(res.data)
				} else if (res.code == 1000001) {
					let findIndex = this.networks.findIndex((v) => v.networkId === notify.id)
					if (findIndex >= 0) {
						this.networks.splice(findIndex, 1)
					}
				}
				this.$forceUpdate()
			}
		},
		show_network_list() {
			this.show_type = 0
		},
		show_create_network() {
			this.$refs.CreateNetworkComponentRef.init()
			this.show_type = 2
		},
		show_network_info_click(network) {
			this.$refs.NetworkInfoComponentRef.init_network(this.networks, network)
			this.show_type = 1
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