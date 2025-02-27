<template>
	<div>
		<el-container>
			<el-main>
				<el-card class="box-card" v-show="this.show_type === 0">
					<el-row slot="header" class="clearfix" style="height: 20px">
						<el-button size="mini" type="primary" icon="el-icon-circle-plus-outline" @click="show_create_host">创建主机</el-button>
					</el-row>
					<el-row>
						<el-table :v-loading="data_loading" :data="hosts" style="width: 100%">
							<el-table-column label="ID" prop="hostId" width="80" />
							<el-table-column label="名称" prop="displayName" show-overflow-tooltip />
							<el-table-column label="主机IP" prop="hostIp" width="120" />
							<el-table-column label="CPU" prop="hostIp" width="120">
								<template #default="scope">
									<el-tooltip class="item" effect="dark" :content="'已使用:' + scope.row.allocationCpu + '核 / 总共:' + scope.row.totalCpu + '核'" placement="top">
										<el-progress color="#67C23A" :percentage="scope.row.totalCpu <= 0 ? 0 : Math.min(100, Math.floor((scope.row.allocationCpu * 100) / scope.row.totalCpu))"></el-progress>
									</el-tooltip>
								</template>
							</el-table-column>
							<el-table-column label="内存" prop="hostIp" width="120">
								<template #default="scope">
									<el-tooltip class="item" effect="dark" :content="'已使用:' + get_memory_display_size(scope.row.allocationMemory) + ' / 总共:' + get_memory_display_size(scope.row.totalMemory)" placement="top">
										<el-progress color="#67C23A" :percentage="scope.row.totalMemory <= 0 ? 0 : Math.min(100, Math.floor((scope.row.allocationMemory * 100) / scope.row.totalMemory))"></el-progress>
									</el-tooltip>
								</template>
							</el-table-column>
							<el-table-column label="状态" prop="status" width="100">
								<template #default="scope">
									<el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ get_host_status(scope.row) }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="操作" width="400">
								<template #default="scope">
									<el-button @click="show_host_info_click(scope.row)" type="" size="mini">主机详情</el-button>
									<el-button @click="register_host(scope.row)" type="success" size="mini">重新注册</el-button>
									<el-button @click="pasue_host(scope.row)" type="warning" size="mini" v-if="scope.row.status !== 3">开始维护</el-button>
									<el-button @click="destroy_host(scope.row)" type="danger" size="mini">销毁主机</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-row>
				</el-card>
				<HostInfoComponent ref="HostInfoComponentRef" @back="show_host_list" @onHostUpdate="update_host_info" v-show="this.show_type === 1" />
				<CreateHostComponent ref="CreateHostComponentRef" @back="show_host_list" @onHostUpdate="update_host_info" v-show="this.show_type === 2" />
			</el-main>
		</el-container>
	</div>
</template>
<script>
import util from '@/api/util'
import HostInfoComponent from '@/components/HostInfoComponent'
import CreateHostComponent from '@/components/CreateHostComponent'
import { getHostList, pauseHost, registerHost, destroyHost } from '@/api/api'
import Notify from '@/api/notify'
export default {
	name: 'hostView',
	components: { HostInfoComponent, CreateHostComponent },
	data() {
		return {
			data_loading: false,
			show_type: -1,
			hosts: []
		}
	},
	mixins: [Notify, util],

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
	methods: {
		async reload_page() {
			this.init_view()
		},
		async init_view() {
			this.data_loading = true
			await getHostList()
				.then((res) => {
					if (res.code == 0) {
						this.hosts = res.data
					}
				})
				.finally(() => {
					this.data_loading = false
				})
		},
		show_host_list() {
			this.show_type = 0
		},
		show_create_host() {
			this.$refs.CreateHostComponentRef.init()
			this.show_type = 2
		},
		show_host_info_click(host) {
			this.show_host = host
			this.$refs.HostInfoComponentRef.init_host(host)

			this.show_type = 1
		},
		update_host_info(host) {
			let findIndex = this.hosts.findIndex((item) => item.hostId === host.hostId)
			if (findIndex >= 0) {
				this.$set(this.hosts, findIndex, host)
			} else {
				this.hosts.push(host)
			}
			this.$refs.HostInfoComponentRef.refresh_host(host)
		},
		dispatch_notify_message(notify) {
			if (notify.type === 4) {
				let res = notify.data
				if (res.code == 0) {
					this.update_host_info(res.data)
				} else if (res.code == 7000001) {
					let findIndex = this.hosts.findIndex((v) => v.hostId === notify.id)
					if (findIndex >= 0) {
						this.hosts.splice(findIndex, 1)
					}
				}
			}
		},
		pasue_host(host) {
			this.$confirm('维护主机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					pauseHost({ hostId: host.hostId }).then((res) => {
						if (res.code === 0) {
							this.update_host_info(res.data)
						} else {
							this.$notify.error({
								title: '错误',
								message: `暂停主机失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		register_host(host) {
			this.$confirm('重新注册主机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					registerHost({ hostId: host.hostId }).then((res) => {
						if (res.code === 0) {
							this.update_host_info(res.data)
						} else {
							this.$notify.error({
								title: '错误',
								message: `注册主机失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		destroy_host(host) {
			this.$confirm('删除当前主机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					destroyHost({ hostId: host.hostId }).then((res) => {
						if (res.code === 0) {
							let findIndex = this.hosts.findIndex((item) => item.hostId === host.hostId)
							if (findIndex >= 0) {
								this.hosts.splice(findIndex, 1)
							}
							this.show_type = 0
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除主机失败:${res.message}`
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