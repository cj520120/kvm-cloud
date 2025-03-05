<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="on_back_click" content="主机详情" v-loading="host_loading"></el-page-header>
		</el-row>
		<el-row style="text-align: left; margin: 20px 0">
			<el-button @click="register_host(show_host)" type="success" size="mini">重新注册</el-button>
			<el-button @click="pasue_host(show_host)" type="warning" size="mini" v-show="show_host.status !== 3">开始维护</el-button>
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
				<el-descriptions-item label="操作系统">{{ show_host.osName }}</el-descriptions-item>
				<el-descriptions-item label="系统版本">{{ show_host.osVersion }}</el-descriptions-item>
				<el-descriptions-item label="制造商">{{ show_host.vendor }}</el-descriptions-item>
				<el-descriptions-item label="Cpu型号">{{ show_host.model }}</el-descriptions-item>
				<el-descriptions-item label="Cpu主频">{{ (show_host.frequency / 1000000000).toFixed(2) }}G</el-descriptions-item>
				<el-descriptions-item label="虚拟化类型">{{ show_host.hypervisor }}</el-descriptions-item>
				<el-descriptions-item label="内存">
					<el-tooltip class="item" effect="dark" :content="'已使用:' + get_memory_display_size(show_host.allocationMemory) + ' / 总共:' + get_memory_display_size(show_host.totalMemory)" placement="top">
						<el-progress color="#67C23A" :percentage="show_host.totalMemory <= 0 ? 0 : Math.min(100, Math.floor((show_host.allocationMemory * 100) / show_host.totalMemory))"></el-progress>
					</el-tooltip>
				</el-descriptions-item>
				<el-descriptions-item label="CPU">
					<el-tooltip class="item" effect="dark" :content="'已使用:' + show_host.allocationCpu + '核 / 总共:' + show_host.totalCpu + '核'" placement="top" style="width: 150px">
						<el-progress color="#67C23A" :percentage="show_host.totalCpu <= 0 ? 0 : Math.min(100, Math.floor((show_host.allocationCpu * 100) / show_host.totalCpu))"></el-progress>
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

		<el-row>
			<el-tabs>
				<el-tab-pane label="系统配置">
					<ConfigComponent ref="ConfigComponentRef" />
				</el-tab-pane>
			</el-tabs>
		</el-row>
	</el-card>
</template>
<script>
import Notify from '@/api/notify'
import util from '@/api/util'
import { getHostInfo, pauseHost, registerHost, destroyHost } from '@/api/api'

import ConfigComponent from '@/components/ConfigComponent.vue'
export default {
	name: 'HostInfoComponent',
	data() {
		return {
			host_loading: false,
			show_host_id: 0,
			show_host: {
				hostId: 0,
				displayName: '',
				hostIp: '127.0.0.1',
				hostName: '-',
				nic: '-',
				uri: 'http://127.0.0.1:8081',
				allocationMemory: 0,
				allocationCpu: 0,
				totalMemory: 0,
				totalCpu: 0,
				arch: '-',
				hypervisor: 'QEMU',
				emulator: '-',
				cores: 0,
				threads: 0,
				sockets: 0,
				status: 1
			}
		}
	},
	mixins: [Notify, util],
	components: { ConfigComponent },
	created() {
		this.show_host_id = 0
		this.subscribe_notify(this.$options.name, this.dispatch_notify_message)
		this.subscribe_connect_notify(this.$options.name, this.reload_page)
		this.init_notify()
	},
	beforeDestroy() {
		this.unsubscribe_notify(this.$options.name)
		this.unsubscribe_connect_notify(this.$options.name)
		this.show_host_id = 0
	},
	methods: {
		on_back_click() {
			this.show_host_id = 0
			this.$emit('back')
		},
		on_notify_update_host_info(host) {
			this.refresh_host(host)
			this.$emit('onHostUpdate', host)
		},
		async reload_page() {
			if (this.show_host_id > 0) {
				this.host_loading = true
				await getHostInfo({ hostId: this.show_host_id })
					.then((res) => {
						if (res.code === 0) {
							this.init_host(res.data)
						} else {
							this.$alert(`获取主机信息失败:${res.message}`, '提示', {
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
			}
		},
		init_host(host) {
			this.show_host_id = host.hostId
			this.show_host = host
			this.host_loading = false
			this.$refs.ConfigComponentRef.init(1, host.hostId)
		},
		refresh_host(host) {
			if (this.show_host.hostId === host.hostId) {
				this.show_host = host
			}
		},
		async init(hostId) {
			this.show_host_id = hostId
			this.reload_page()
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
							this.on_notify_update_host_info(res.data)
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
							this.on_notify_update_host_info(res.data)
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
							this.on_back_click()
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除主机失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		dispatch_notify_message(notify) {
			if (notify.type === 4 && this.show_host.hostId === notify.id) {
				let res = notify.data
				if (res.code == 0) {
					this.refresh_host(res.data)
				} else if (res.code == 2000001) {
					this.on_back_click()
				}
			}
		}
	}
}
</script>