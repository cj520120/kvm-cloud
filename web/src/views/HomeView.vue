<template>
	<div>
		<el-container>
			<el-main>
				<el-card class="box-card">
					<div slot="header" class="clearfix">
						<span>系统汇总</span>
					</div>
					<el-row :gutter="20">
						<el-col :span="4">
							<div>
								<el-statistic group-separator="," :value="this.networks.length" title="网络数量"></el-statistic>
							</div>
						</el-col>
						<el-col :span="4">
							<div>
								<el-statistic group-separator="," :value="this.storages.length" title="存储池数量"></el-statistic>
							</div>
						</el-col>
						<el-col :span="4">
							<div>
								<el-statistic group-separator="," :value="this.hosts.length" title="主机数量"></el-statistic>
							</div>
						</el-col>
						<el-col :span="4">
							<div>
								<el-statistic group-separator="," :value="this.guests.length" title="虚拟机数量">
									<template slot="formatter">{{ get_runing_guest_number() }} /{{ this.guests.length }}</template>
								</el-statistic>
							</div>
						</el-col>
						<el-col :span="4">
							<div>
								<el-statistic group-separator="," :value="this.volumes.length" title="磁盘数量"></el-statistic>
							</div>
						</el-col>
					</el-row>
					<el-divider></el-divider>
					<el-card>
						<div slot="header" class="clearfix">
							<span>主机信息</span>
						</div>

						<div class="component">
							<div>
								<el-tooltip class="item" effect="dark" :content="'已使用:' + get_allocat_cpu() + '核 / 总共:' + get_total_cpu() + '核'" placement="top">
									<el-progress :format="get_cpu_progress_title" type="circle" :percentage="totalCpuPercentage" color="#67C23A"></el-progress>
								</el-tooltip>
							</div>
							<div>
								<el-tooltip class="item" effect="dark" :content="'已使用:' + get_memory_desplay(get_allocat_memory()) + ' / 总共:' + get_memory_desplay(get_total_memory())" placement="top">
									<el-progress :format="get_memory_progress_title" type="circle" :percentage="totalMemoryePercentage" color="#67C23A"></el-progress>
								</el-tooltip>
							</div>
							<div>
								<el-table :data="hosts" style="width:100%">
									<el-table-column label="名称" prop="displayName" max-width="200"  show-overflow-tooltip/>
									<el-table-column label="主机IP" prop="hostIp" width="150"  show-overflow-tooltip/>
									<el-table-column label="CPU"  max-width="150" >
										<template #default="scope">
											<el-tooltip class="item" effect="dark" :content="'已使用:' + scope.row.allocationCpu + '核 / 总共:' + scope.row.totalCpu + '核'" placement="top">
												<el-progress color="#67C23A" :percentage="scope.row.totalCpu <= 0 ? 0 : Math.floor((scope.row.allocationCpu * 100) / scope.row.totalCpu)"></el-progress>
											</el-tooltip>
										</template>
									</el-table-column>
									<el-table-column label="内存"  max-width="150"  >
										<template #default="scope">
											<el-tooltip class="item" effect="dark" :content="'已使用:' + get_memory_desplay(scope.row.allocationMemory) + ' / 总共:' + get_memory_desplay(scope.row.totalMemory)" placement="top">
												<el-progress color="#67C23A" :percentage="scope.row.totalMemory <= 0 ? 0 : Math.floor((scope.row.allocationMemory * 100) / scope.row.totalMemory)"></el-progress>
											</el-tooltip>
										</template>
									</el-table-column>
									<el-table-column></el-table-column>
								</el-table>
							</div>
						</div>
					</el-card>
					<el-divider></el-divider>
					<el-card>
						<div slot="header" class="clearfix">
							<span>存储池</span>
						</div>
						<div class="storage_component">
							<div>
								<el-tooltip class="item" effect="dark" :content="'已用:' + get_storage_desplay(get_allocat_storage()) + ' / 总共:' + get_storage_desplay(get_total_capacity())" placement="top">
									<el-progress type="circle" :format="get_storage_progress_title" :percentage="totalStoragePercentage" color="#67C23A"></el-progress>
								</el-tooltip>
							</div>
							<div>
								<el-table :data="storages">
									<el-table-column label="名称" prop="description" max-width="200"  show-overflow-tooltip/>
									<el-table-column label="类型" prop="type" width="120" />
									<el-table-column label="容量" prop="capacity" width="150">
										<template #default="scope">
											<el-tooltip class="item" effect="dark" :content="'已用:' + get_storage_desplay(scope.row.allocation) + ' / 总共:' + get_storage_desplay(scope.row.capacity)" placement="top">
												<el-progress color="#67C23A" :percentage="scope.row.capacity <= 0 ? 0 : Math.floor((scope.row.allocation * 100) / scope.row.capacity)"></el-progress>
											</el-tooltip>
										</template>
									</el-table-column>
									<el-table-column></el-table-column>
								</el-table>
							</div>
						</div>
					</el-card>
				</el-card>
			</el-main>
		</el-container>
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
.like {
	cursor: pointer;
	font-size: 25px;
	display: inline-block;
}
.component {
	display: grid;
	grid-template-columns: 150px 150px auto;
	max-width: 100%;
	overflow: hidden;
}
.storage_component {
	display: grid;
	grid-template-columns: 150px auto;
	max-width: 100%;
	overflow: hidden;
}
</style>