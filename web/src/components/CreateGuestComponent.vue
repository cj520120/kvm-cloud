<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="on_back_click" content="创建虚拟机" style="color: #409eff"></el-page-header>
		</el-row>
		<el-row>
			<el-form ref="createForm" :model="create_guest" label-width="100px" class="demo-ruleForm">
				<el-row>
					<el-col :span="12">
						<el-form-item label="标签" prop="description">
							<el-input v-model="create_guest.description"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="总线方式">
							<el-select v-model="create_guest.busType" style="width: 100%" placeholder="总线方式">
								<el-option label="virtio" value="virtio" />
								<el-option label="ide" value="ide" />
								<el-option label="scsi" value="scsi" />
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="12">
						<el-form-item label="运行主机">
							<el-select v-model="create_guest.hostId" style="width: 100%">
								<el-option label="随机" :value="0"></el-option>
								<el-option v-for="item in this.hosts" :key="item.hostId" :label="item.displayName" :value="item.hostId" />
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="架构">
							<el-select v-model="create_guest.schemeId" style="width: 100%" placeholder="请选择架构">
								<el-option v-for="item in this.schemes" :key="item.schemeId" :label="item.name" :value="item.schemeId" />
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="12">
						<el-form-item label="网络">
							<el-select v-model="create_guest.networkId" style="width: 100%" placeholder="请选择网络">
								<el-option v-for="item in this.networks" :key="item.networkId" :label="item.name" :value="item.networkId" />
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="网络驱动">
							<el-select v-model="create_guest.networkDeviceType" style="width: 100%" placeholder="请选择网卡驱动">
								<el-option label="virtio" value="virtio" />
								<el-option label="rtl8139" value="rtl8139" />
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="12">
						<el-form-item label="安装方式">
							<el-select v-model="create_guest.type" style="width: 100%" placeholder="请选择安装方式">
								<el-option label="ISO镜像" :value="0" />
								<el-option label="模版安装" :value="1" />
								<el-option label="快照安装" :value="2" />
								<el-option label="现有磁盘" :value="3" />
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="磁盘类型" v-if="create_guest.type !== 3">
							<el-select v-model="create_guest.volumeType" style="width: 100%">
								<el-option label="raw" value="raw"></el-option>
								<el-option label="qcow" value="qcow"></el-option>
								<el-option label="qcow2" value="qcow2"></el-option>
								<el-option label="vdi" value="vdi"></el-option>
								<el-option label="vmdk" value="vmdk"></el-option>
								<el-option label="vpc" value="vpc"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="12">
						<el-form-item label="ISO模版" v-if="create_guest.type === 0">
							<el-select v-model="create_guest.isoTemplateId" style="width: 100%" placeholder="请选择光盘镜像">
								<el-option v-for="item in this.iso_template" :key="item.templateId" :label="item.name" :value="item.templateId" />
							</el-select>
						</el-form-item>
						<el-form-item label="系统模版" v-if="create_guest.type === 1">
							<el-select v-model="create_guest.diskTemplateId" style="width: 100%" placeholder="请选择模版">
								<el-option v-for="item in this.disk_template" :key="item.templateId" :label="item.name" :value="item.templateId" />
							</el-select>
						</el-form-item>
						<el-form-item label="快照模版" v-if="create_guest.type === 2">
							<el-select v-model="create_guest.snapshotVolumeId" style="width: 100%" placeholder="请选择系统快照">
								<el-option v-for="item in this.snapshot_template" :key="item.snapshotVolumeId" :label="item.name" :value="item.snapshotVolumeId" />
							</el-select>
						</el-form-item>
						<el-form-item label="可用磁盘" v-if="create_guest.type === 3">
							<el-select v-model="create_guest.volumeId" style="width: 100%" placeholder="请选择系统磁盘">
								<el-option v-for="item in this.attach_volumes" :key="item.volumeId" :label="item.description" :value="item.volumeId" />
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="存储池" v-if="create_guest.type !== 3">
							<el-select v-model="create_guest.storageId" style="width: 100%">
								<el-option label="随机" :value="0"></el-option>
								<el-option v-for="item in this.storages" :key="item.storageId" :label="item.description" :value="item.storageId" />
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>
				<el-form-item label="磁盘大小" v-if="create_guest.type === 0">
					<el-input v-model="create_guest.size"></el-input>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="create_guest_click">立即创建</el-button>
					<el-button @click="on_back_click">取消</el-button>
				</el-form-item>
			</el-form>
		</el-row>
	</el-card>
</template>
<script>
import { getNotAttachVolumeList, getTemplateList, getStorageList, getSnapshotList, getNetworkList, getHostList, getSchemeList, createGuest } from '@/api/api'
export default {
	data() {
		return {
			create_guest: {
				type: 0,
				description: '',
				busType: 'virtio',
				hostId: 0,
				schemeId: '',
				networkId: '',
				networkDeviceType: 'virtio',
				volumeType: 'qcow2',
				isoTemplateId: '',
				diskTemplateId: '',
				snapshotVolumeId: '',
				volumeId: '',
				storageId: 0,
				size: 100
			},
			iso_template: [],
			attach_volumes: [],
			disk_template: [],
			storages: [],
			snapshot_template: [],
			schemes: [],
			hosts: [],
			networks: []
		}
	},
	methods: {
		on_back_click() {
			this.$emit('back')
		},
		async load_all_attach_volumes() {
			await getNotAttachVolumeList().then((res) => {
				if (res.code === 0) {
					this.attach_volumes = res.data
				}
			})
		},
		async load_all_template() {
			await getTemplateList().then((res) => {
				if (res.code === 0) {
					this.iso_template = res.data.filter((v) => v.templateType == 0 && v.status === 2)
					this.disk_template = res.data.filter((v) => v.templateType == 2 && v.status === 2)
				}
			})
		},
		async load_all_storage() {
			await getStorageList().then((res) => {
				if (res.code == 0) {
					this.storages = res.data.filter((v) => v.status == 1)
				}
			})
		},
		async load_all_snapshot() {
			await getSnapshotList().then((res) => {
				if (res.code == 0) {
					this.snapshot_template = res.data.filter((v) => v.status == 1)
				}
			})
		},
		async load_all_networks() {
			await getNetworkList().then((res) => {
				if (res.code === 0) {
					this.networks = res.data.filter((v) => v.status === 2)
				}
			})
		},
		async load_all_host() {
			await getHostList().then((res) => {
				if (res.code === 0) {
					this.hosts = res.data.filter((v) => v.status == 1)
				}
			})
		},
		async load_all_schemes() {
			await getSchemeList().then((res) => {
				if (res.code === 0) {
					this.schemes = res.data
				}
			})
		},
		async init() {
			this.load_all_attach_volumes()
			this.load_all_host()
			this.load_all_template()
			this.load_all_storage()
			this.load_all_schemes()
			this.load_all_networks()
			this.load_all_snapshot()

			if (this.$refs['createForm']) {
				this.$refs['createForm'].resetFields()
			}
			this.create_guest.isoTemplateId = ''
			this.create_guest.diskTemplateId = ''
			this.create_guest.snapshotVolumeId = ''
			this.create_guest.volumeId = ''
			this.create_guest.type = 0
		},
		create_guest_click() {
			switch (this.create_guest.type) {
				case 0:
					this.create_guest.diskTemplateId = 0
					this.create_guest.snapshotVolumeId = 0
					this.create_guest.volumeId = 0
					break
				case 1:
					this.create_guest.isoTemplateId = 0
					this.create_guest.snapshotVolumeId = 0
					this.create_guest.volumeId = 0
					this.create_guest.size = 0
					break
				case 2:
					this.create_guest.isoTemplateId = 0
					this.create_guest.diskTemplateId = 0
					this.create_guest.volumeId = 0
					this.create_guest.size = 0
					break
				case 3:
					this.create_guest.isoTemplateId = 0
					this.create_guest.diskTemplateId = 0
					this.create_guest.snapshotVolumeId = 0
					this.create_guest.size = 0
					break
			}
			createGuest(this.create_guest).then((res) => {
				if (res.code === 0) {
					this.$emit('onGuestUpdate', res.data)
					this.$nextTick(() => {
						this.on_back_click()
					})
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建虚拟机失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>