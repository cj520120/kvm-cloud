<template>
	<div class="dashboard-container">
		<el-row class="panel-group">
			<div class="card-panel">
				<div class="card-panel-icon-wrapper icon-message" @click="go_route('Network')">
					<i class="el-icon-platform-eleme" style="font-size: 48px" />
				</div>
				<div class="card-panel-description">
					<div class="card-panel-text">网络数量</div>
					<span class="card-panel-num">{{ this.networks.length }}</span>
				</div>
			</div>
			<div class="card-panel">
				<div class="card-panel-icon-wrapper icon-message" @click="go_route('Storage')">
					<i class="el-icon-coin" style="font-size: 48px" />
				</div>
				<div class="card-panel-description">
					<div class="card-panel-text">存储池数量</div>
					<span class="card-panel-num">{{ this.storages.length }}</span>
				</div>
			</div>

			<div class="card-panel">
				<div class="card-panel-icon-wrapper icon-message" @click="go_route('Host')">
					<i class="el-icon-monitor" style="font-size: 48px" class-name="card-panel-icon" />
				</div>
				<div class="card-panel-description">
					<div class="card-panel-text">主机数量</div>
					<span class="card-panel-num">{{ this.hosts.length }}</span>
				</div>
			</div>
			<div class="card-panel">
				<div class="card-panel-icon-wrapper icon-message" @click="go_route('Guest')">
					<i class="el-icon-s-platform" style="font-size: 48px" class-name="card-panel-icon" />
				</div>
				<div class="card-panel-description">
					<div class="card-panel-text">虚拟机数量</div>
					<span class="card-panel-num">{{ get_runing_guest_number() }} /{{ this.guests.length }}</span>
				</div>
			</div>
			<div class="card-panel">
				<div class="card-panel-icon-wrapper icon-message" @click="go_route('Volume')">
					<i class="el-icon-bank-card" style="font-size: 48px" class-name="card-panel-icon" />
				</div>
				<div class="card-panel-description">
					<div class="card-panel-text">磁盘数量</div>
					<span class="card-panel-num">{{ this.volumes.length }}</span>
				</div>
			</div>
		</el-row>
		<el-row>
			<el-card>
				<div slot="header" class="clearfix">
					<span>主机信息</span>
				</div>
				<el-container>
					<el-aside width="300px">
						<el-row>
							<el-col :span="12">
								<el-tooltip class="item" effect="dark" :content="'已使用:' + get_allocat_cpu() + '核 / 总共:' + get_total_cpu() + '核'" placement="top">
									<el-progress :format="get_cpu_progress_title" type="circle" :percentage="totalCpuPercentage" color="#67C23A"></el-progress>
								</el-tooltip>
							</el-col>
							<el-col :span="12">
								<el-tooltip class="item" effect="dark" :content="'已使用:' + get_memory_desplay(get_allocat_memory()) + ' / 总共:' + get_memory_desplay(get_total_memory())" placement="top">
									<el-progress :format="get_memory_progress_title" type="circle" :percentage="totalMemoryePercentage" color="#67C23A"></el-progress>
								</el-tooltip>
							</el-col>
						</el-row>
					</el-aside>
					<el-container>
						<el-table :data="hosts" style="width: 100%">
							<el-table-column label="名称" prop="displayName" max-width="200" show-overflow-tooltip />
							<el-table-column label="主机IP" prop="hostIp" width="150" show-overflow-tooltip />
							<el-table-column label="CPU" max-width="150">
								<template #default="scope">
									<el-tooltip class="item" effect="dark" :content="'已使用:' + scope.row.allocationCpu + '核 / 总共:' + scope.row.totalCpu + '核'" placement="top">
										<el-progress color="#67C23A" :percentage="scope.row.totalCpu <= 0 ? 0 : Math.floor((scope.row.allocationCpu * 100) / scope.row.totalCpu)"></el-progress>
									</el-tooltip>
								</template>
							</el-table-column>
							<el-table-column label="内存" max-width="150">
								<template #default="scope">
									<el-tooltip class="item" effect="dark" :content="'已使用:' + get_memory_desplay(scope.row.allocationMemory) + ' / 总共:' + get_memory_desplay(scope.row.totalMemory)" placement="top">
										<el-progress color="#67C23A" :percentage="scope.row.totalMemory <= 0 ? 0 : Math.floor((scope.row.allocationMemory * 100) / scope.row.totalMemory)"></el-progress>
									</el-tooltip>
								</template>
							</el-table-column>
							<el-table-column></el-table-column>
						</el-table>
					</el-container>
				</el-container>
			</el-card>
			<el-card class="dashbord-card">
				<div slot="header" class="clearfix">
					<span>存储池</span>
				</div>
				<el-container>
					<el-aside width="200px">
						<el-tooltip class="item" effect="dark" :content="'已用:' + get_storage_desplay(get_allocat_storage()) + ' / 总共:' + get_storage_desplay(get_total_capacity())" placement="top">
							<el-progress type="circle" :format="get_storage_progress_title" :percentage="totalStoragePercentage" color="#67C23A"></el-progress>
						</el-tooltip>
					</el-aside>
					<el-container>
						<el-table :data="storages">
							<el-table-column label="名称" prop="description" width="250" show-overflow-tooltip />
							<el-table-column label="类型" prop="type" width="120" />
							<el-table-column label="容量" prop="capacity" width="200">
								<template #default="scope">
									<el-tooltip class="item" effect="dark" :content="'已用:' + get_storage_desplay(scope.row.allocation) + ' / 总共:' + get_storage_desplay(scope.row.capacity)" placement="top">
										<el-progress color="#67C23A" :percentage="scope.row.capacity <= 0 ? 0 : Math.floor((scope.row.allocation * 100) / scope.row.capacity)"></el-progress>
									</el-tooltip>
								</template>
							</el-table-column>
							<el-table-column></el-table-column>
						</el-table>
					</el-container>
				</el-container>
			</el-card>
		</el-row>
	</div>
</template>
<script>
import { getGuestList, getHostList, getNetworkList, getStorageList, getVolumeList } from '@/api/api'
export default {
	name: 'guestView',
	components: {},
	data() {
		return {
			hosts: [],
			networks: [],
			storages: [],
			volumes: [],
			guests: [],
			totalStoragePercentage: 0,
			totalCpuPercentage: 0,
			totalMemoryePercentage: 0
		}
	},
	mounted() {
		this.init_data()
	},
	methods: {
		go_route(path) {
			this.$router.push({ path: path })
		},
		init_data() {
			getHostList().then((res) => {
				if (res.code === 0) {
					this.hosts = res.data
					this.totalCpuPercentage = this.get_total_cpu() <= 0 ? 0 : Math.floor((this.get_allocat_cpu() * 100) / this.get_total_cpu())
					this.totalMemoryePercentage = this.get_total_memory() <= 0 ? 0 : Math.floor((this.get_allocat_memory() * 100) / this.get_total_memory())
				}
			})
			getNetworkList().then((res) => {
				if (res.code === 0) {
					this.networks = res.data
				}
			})
			getStorageList().then((res) => {
				if (res.code === 0) {
					this.storages = res.data
				}
			})
			getVolumeList().then((res) => {
				if (res.code === 0) {
					this.volumes = res.data
				}
			})
			getGuestList().then((res) => {
				if (res.code === 0) {
					this.guests = res.data
				}
			})
			getStorageList().then((res) => {
				if (res.code === 0) {
					this.storages = res.data
					this.totalStoragePercentage = this.get_total_capacity() <= 0 ? 0 : parseInt((this.get_allocat_storage() * 100) / this.get_total_capacity())
				}
			})
		},
		get_runing_guest_number() {
			return this.guests.filter((v) => v.status === 2).length
		},
		get_storage_desplay(size) {
			if (size > 1024 * 1024 * 1024 * 1024) {
				return (size / (1024 * 1024 * 1024 * 1024)).toFixed(2) + ' TB'
			} else if (size > 1024 * 1024 * 1024) {
				return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
			} else if (size > 1024 * 1024) {
				return (size / (1024 * 1024)).toFixed(2) + ' MB'
			} else if (size > 1024) {
				return (size / 1024).toFixed(2) + '  KB'
			} else {
				return size + '  bytes'
			}
		},
		get_memory_desplay(memory) {
			if (memory > 1024 * 1024) {
				return (memory / (1024 * 1024)).toFixed(2) + ' GB'
			} else if (memory > 1024) {
				return (memory / 1024).toFixed(2) + '  MB'
			}
		},
		get_total_memory() {
			let totalMemory = 0
			this.hosts.forEach((host) => (totalMemory += host.totalMemory))
			return totalMemory
		},
		get_allocat_memory() {
			let allocationMemory = 0
			this.hosts.forEach((host) => (allocationMemory += host.allocationMemory))
			return allocationMemory
		},
		get_total_cpu() {
			let totalCpu = 0
			this.hosts.forEach((host) => (totalCpu += host.totalCpu))
			return totalCpu
		},
		get_allocat_cpu() {
			let allocationCpu = 0
			this.hosts.forEach((host) => (allocationCpu += host.allocationCpu))
			return allocationCpu
		},
		locat_cpu() {
			let allocationCpu = 0
			this.hosts.forEach((host) => (allocationCpu += host.allocationCpu))
			return allocationCpu
		},
		get_total_capacity() {
			let capacity = 0
			this.storages.forEach((storage) => (capacity += storage.capacity))
			return capacity
		},
		get_allocat_storage() {
			let allocation = 0
			this.storages.forEach((storage) => (allocation += storage.allocation))
			return allocation
		},
		get_cpu_progress_title(percentage) {
			return '总内核:' + percentage + '%'
		},
		get_memory_progress_title(percentage) {
			return '总内存:' + percentage + '%'
		},
		get_storage_progress_title(percentage) {
			return '总存储:' + percentage + '%'
		}
	}
}
</script>
<style lang="scss" scoped>
</style>