<template>
	<div>
		<HeadViewVue />
		<el-container>
			<el-aside width="200px">
				<NavViewVue current="Host" />
			</el-aside>
			<el-main>
				<el-card class="box-card" v-if="this.show_type === 0">
					<el-row slot="header" class="clearfix" style="height: 20px">
						<el-button style="float: left; padding: 3px 0" type="text" @click="show_create_host">创建主机</el-button>
					</el-row>
					<el-row>
						<el-table :v-loading="data_loading" :data="hosts" style="width: 100%">
							<el-table-column label="ID" prop="hostId" width="80" />
							<el-table-column label="名称" prop="displayName" width="120" />
							<el-table-column label="主机IP" prop="hostIp" width="120" />
							<el-table-column label="CPU" prop="hostIp" width="180">
								<template #default="scope">
									<el-tooltip class="item" effect="dark" :content="'已使用:' + scope.row.allocationCpu + '核 / 总共:' + scope.row.totalCpu + '核'" placement="top">
										<el-progress color="#67C23A" :percentage="parseInt((scope.row.allocationCpu * 100) / scope.row.totalCpu)"></el-progress>
									</el-tooltip>
								</template>
							</el-table-column>
							<el-table-column label="内存" prop="hostIp" width="180">
								<template #default="scope">
									<el-tooltip class="item" effect="dark" :content="'已使用:' + get_memory_desplay(scope.row.allocationMemory) + ' / 总共:' + get_memory_desplay(scope.row.totalMemory)" placement="top">
										<el-progress color="#67C23A" :percentage="parseInt((scope.row.allocationMemory * 100) / scope.row.totalMemory)"></el-progress>
									</el-tooltip>
								</template>
							</el-table-column>
							<el-table-column label="状态" prop="status" width="100">
								<template #default="scope">
									<el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ get_host_status(scope.row) }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="操作">
								<template #default="scope">
									<el-button @click="show_host_info(scope.row)" type="" size="mini">主机详情</el-button>
									<el-button @click="register_host(scope.row)" type="success" size="mini">重新注册</el-button>
									<el-button @click="pasue_host(scope.row)" type="warning" size="mini" v-if="scope.row.status !== 3">开始维护</el-button>
									<el-button @click="destroy_host(scope.row)" type="danger" size="mini">销毁主机</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-row>
				</el-card>
				<el-card class="box-card" v-if="this.show_type === 1">
					<el-row slot="header">
						<el-page-header @back="show_host_list" content="主机详情"></el-page-header>
					</el-row>
					<el-row style="text-align: left; margin: 20px 0">
						<el-button @click="register_host(show_host)" type="success" size="mini">重新注册</el-button>
						<el-button @click="pasue_host(show_host)" type="warning" size="mini" v-if="show_host.status !== 3">开始维护</el-button>
						<el-button @click="destroy_host(show_host)" type="danger" size="mini">销毁主机</el-button>
					</el-row>
					<el-row>
						<el-descriptions :column="2" size="medium" border>
							<el-descriptions-item label="ID">{{ show_host.hostId }}</el-descriptions-item>
							<el-descriptions-item label="主机名">{{ show_host.hostName }}</el-descriptions-item>
							<el-descriptions-item label="显示名称">{{ show_host.displayName }}</el-descriptions-item>
							<el-descriptions-item label="主机IP">{{ show_host.hostIp }}</el-descriptions-item>
							<el-descriptions-item label="网卡名称">{{ show_host.nic }}</el-descriptions-item>
							<el-descriptions-item label="通信地址">{{ show_host.uri }}</el-descriptions-item>
							<el-descriptions-item label="主机架构">{{ show_host.arch }}</el-descriptions-item>
							<el-descriptions-item label="虚拟化类型">{{ show_host.hypervisor }}</el-descriptions-item>
							<el-descriptions-item label="内存">
								<el-tooltip class="item" effect="dark" :content="'已使用:' + get_memory_desplay(show_host.allocationMemory) + ' / 总共:' + get_memory_desplay(show_host.totalMemory)" placement="top">
									<el-progress color="#67C23A" :percentage="parseInt((show_host.allocationMemory * 100) / show_host.totalMemory)"></el-progress>
								</el-tooltip>
							</el-descriptions-item>
							<el-descriptions-item label="CPU">
								<el-tooltip class="item" effect="dark" :content="'已使用:' + show_host.allocationCpu + '核 / 总共:' + show_host.totalCpu + '核'" placement="top" style="width: 150px">
									<el-progress color="#67C23A" :percentage="parseInt((show_host.allocationCpu * 100) / show_host.totalCpu)"></el-progress>
								</el-tooltip>
							</el-descriptions-item>
							<el-descriptions-item label="Cores">{{ show_host.cores }}</el-descriptions-item>
							<el-descriptions-item label="Sockets">{{ show_host.sockets }}</el-descriptions-item>
							<el-descriptions-item label="Threads">{{ show_host.threads }}</el-descriptions-item>
							<el-descriptions-item label="Eemulator">{{ show_host.emulator }}</el-descriptions-item>
							<el-descriptions-item label="状态">
								<el-tag :type="show_host.status === 1 ? 'success' : 'danger'">{{ get_host_status(show_host) }}</el-tag>
							</el-descriptions-item>
						</el-descriptions>
					</el-row>
				</el-card>
				<el-card class="box-card" v-if="this.show_type === 2">
					<el-row slot="header">
						<el-page-header @back="show_host_list()" content="创建主机" style="color: #409eff"></el-page-header>
					</el-row>
					<el-row>
						<el-form ref="createForm" :model="create_host" label-width="100px" class="demo-ruleForm">
							<el-form-item label="显示名称" prop="nadisplayNameme">
								<el-input v-model="create_host.displayName"></el-input>
							</el-form-item>
							<el-form-item label="主机IP" prop="hostIp"><el-input v-model="create_host.hostIp"></el-input></el-form-item>

							<el-form-item label="网卡名称" prop="nic"><el-input v-model="create_host.nic"></el-input></el-form-item>

							<el-form-item label="通信地址" prop="uri"><el-input v-model="create_host.uri"></el-input></el-form-item>

							<el-form-item>
								<el-button type="primary" @click="create_host_click">立即创建</el-button>
								<el-button @click="show_host_list">取消</el-button>
							</el-form-item>
						</el-form>
					</el-row>
				</el-card>
			</el-main>
		</el-container>
	</div>
</template>
<script>
import { getHostList, getHostInfo, pauseHost, registerHost, destroyHost, createHost } from '@/api/api'
import Notify from '@/api/notify'
import NavViewVue from './NavView.vue'
import HeadViewVue from './HeadView.vue'
export default {
	name: 'hostView',
	components: {
		NavViewVue,
		HeadViewVue
	},
	data() {
		return {
			data_loading: false,
			show_type: 0,
			show_host: {},
			create_host: {
				displayName: '',
				hostIp: '',
				nic: '',
				uri: ''
			},
			hosts: []
		}
	},
	mixins: [Notify],
	created() {
		this.init_view()
		this.init_notify()
	},
	methods: {
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
		get_host_status(host) {
			switch (host.status) {
				case 0:
					return '正在创建'
				case 1:
					return '在线'
				case 2:
					return '离线'
				case 3:
					return '正在维护'
				case 4:
					return '主机错误'
				default:
					return `未知状态[${host.status}]`
			}
		},
		show_host_list() {
			this.show_type = 0
		},
		show_create_host() {
			if (this.$refs['createForm']) {
				this.$refs['createForm'].resetFields()
			}
			this.show_type = 2
		},
		show_host_info(host) {
			this.show_host = host
			this.show_type = 1
		},
		update_host_info(host) {
			let findIndex = this.hosts.findIndex((item) => item.hostId === host.hostId)
			if (findIndex >= 0) {
				this.$set(this.hosts, findIndex, host)
			} else {
				this.hosts.push(host)
			}
			if (this.show_host && this.show_host.hostId === host.hostId) {
				this.show_host = host
			}
		},
		handle_notify_message(notify) {
			if (notify.type === 4) {
				getHostInfo({ hostId: notify.id }).then((res) => {
					if (res.code == 0) {
						this.update_host_info(res.data)
					}else if (res.code == 7000001) {
						let findIndex = this.hosts.findIndex((v) => v.hostId ===notify.id)
						if (findIndex >= 0) {
							this.hosts.splice(findIndex, 1)
						}
					}
				})
			}
		},
		create_host_click() {
			createHost(this.create_host).then((res) => {
				if (res.code === 0) {
					this.update_host_info(res.data)
					this.show_type = 0
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建主机失败:${res.message}`
					})
				}
			})
		},
		get_memory_desplay(memory) {
			if (memory > 1024 * 1024) {
				return (memory / (1024 * 1024)).toFixed(2) + ' GB'
			} else if (memory > 1024) {
				return (memory / 1024).toFixed(2) + '  MB'
			}
		},
		pasue_host(host) {
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
		},
		register_host(host) {
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
		},
		destroy_host(host) {
			destroyHost({ hostId: host.hostId }).then((res) => {
				if (res.code === 0) {
					let findIndex = this.hosts.findIndex((item) => item.hostId === host.hostId)
					if (findIndex >= 0) {
						this.hosts.splice(findIndex, 1)
					}
				} else {
					this.$notify.error({
						title: '错误',
						message: `删除主机失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>
<style lang="postcss" scoped>
.table_action button {
	margin: 0.1em;
}
</style>