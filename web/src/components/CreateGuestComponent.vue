<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="on_back_click" content="创建虚拟机" style="color: #409eff"></el-page-header>
		</el-row>
		<el-row>
			<el-form ref="createForm" :model="create_guest" label-width="100px" class="demo-ruleForm">
				<el-row>
					<el-col :span="8">
						<el-form-item label="名称" prop="description">
							<el-input v-model="create_guest.description"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="总线方式">
							<el-select v-model="create_guest.busType" style="width: 100%" placeholder="总线方式">
								<el-option label="virtio" value="virtio" />
								<el-option label="ide" value="ide" />
								<el-option label="scsi" value="scsi" />
								<el-option label="sata" value="sata" />
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="固件类型">
							<el-select v-model="create_guest.bootstrapType" style="width: 100%">
								<el-option label="BIOS" :value="0" />
								<el-option label="UEFI" :value="1" />
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="8">
						<el-form-item label="配置">
							<el-select v-model="create_guest.schemeId" style="width: 100%" placeholder="请选择虚拟机配置">
								<el-option v-for="item in this.schemes" :key="item.schemeId" :label="item.name" :value="item.schemeId" />
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="存储池">
							<el-select v-model="create_guest.storageId" style="width: 100%">
								<el-option label="随机" :value="0"></el-option>
								<el-option v-for="item in this.storages" :key="item.storageId" :label="item.description" :value="item.storageId" />
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="运行主机">
							<el-select v-model="create_guest.hostId" style="width: 100%">
								<el-option label="随机" :value="0"></el-option>
								<el-option v-for="item in select_host" :key="item.hostId" :label="item.displayName" :value="item.hostId" />
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="8">
						<el-form-item label="网络">
							<el-select v-model="create_guest.networkId" style="width: 100%" placeholder="请选择网络">
								<el-option v-for="item in this.networks" :key="item.networkId" :label="item.name" :value="item.networkId" />
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="网络驱动">
							<el-select v-model="create_guest.networkDeviceType" style="width: 100%" placeholder="请选择网卡驱动">
								<el-option label="virtio" value="virtio" />
								<el-option label="rtl8139" value="rtl8139" />
								<el-option label="e1000" value="e1000" />
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="8">
						<el-form-item label="安装方式">
							<el-select v-model="create_guest.type" style="width: 100%" placeholder="请选择安装方式">
								<el-option label="ISO镜像" :value="0" />
								<el-option label="模版安装" :value="1" />
								<el-option label="现有磁盘" :value="3" />
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="操作系统">
							<el-select v-model="create_guest.systemCategory" style="width: 100%" placeholder="操作系统">
								<el-option label="Centos" :value="101" />
								<el-option label="Ubuntu" :value="102" />
								<el-option label="Windows" :value="300" />
								<el-option label="Deepin" :value="103" />
								<el-option label="RedHat" :value="104" />
								<el-option label="Debian" :value="105" />
								<el-option label="OpenEuler" :value="106" />
								<el-option label="UOS" :value="107" />
								<el-option label="OracleLinux" :value="108" />
								<el-option label="Linux" :value="100" />
								<el-option label="Unix" :value="200" />
								<el-option label="Android" :value="400" />
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="8">
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
						<el-form-item label="可用磁盘" v-if="create_guest.type === 3">
							<el-select v-model="create_guest.volumeId" style="width: 100%" placeholder="请选择系统磁盘">
								<el-option v-for="item in this.attach_volumes" :key="item.volumeId" :label="item.description" :value="item.volumeId" />
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="8">
						<el-form-item label="群组">
							<el-select v-model="create_guest.groupId" style="width: 100%" placeholder="请选择群组">
								<el-option v-for="item in this.groups" :key="item.groupId" :label="item.groupName" :value="item.groupId" />
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="磁盘大小">
							<el-input v-model="create_guest.size" :disabled="create_guest.type !== 0 && create_guest.type !== 1"><template slot="append">GB</template></el-input>
						</el-form-item>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="8">
						<el-form-item label="主机名">
							<el-input v-model="meta_config.hostName" :disabled="create_guest.type !== 1 || create_guest.systemCategory == 300"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="密码">
							<el-input v-model="user_config.password" :show-password="true" type="password" :disabled="create_guest.type !== 1 || create_guest.systemCategory == 300"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="登录密钥">
							<el-select v-model="user_config.sshId" style="width: 100%" placeholder="登录密钥" :disabled="create_guest.type !== 1 || create_guest.systemCategory == 300">
								<el-option v-for="item in this.sshs" :key="item.id" :label="item.name" :value="item.id" />
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>
				<el-form-item>
					<el-button type="primary" @click="create_guest_click">立即创建</el-button>
					<el-button @click="on_back_click">取消</el-button>
				</el-form-item>
			</el-form>
		</el-row>
	</el-card>
</template>
<script>
import { getNotAttachVolumeList, getTemplateList, getStorageList, getNetworkList, getHostList, getSchemeList, createGuest, getGroupList, getSshList } from '@/api/api'
export default {
	data() {
		return {
			create_guest: {
				type: 0,
				groupId: 0,
				description: '',
				busType: 'virtio',
				hostId: 0,
				schemeId: '',
				networkId: '',
				networkDeviceType: 'virtio',
				isoTemplateId: '',
				diskTemplateId: '',
				volumeId: '',
				storageId: 0,
				systemCategory: 101,
				size: 100,
				bootstrapType: 0
			},
			meta_config: {
				hostName: ''
			},
			user_config: {
				password: '',
				sshId: 0
			},
			iso_template: [],
			attach_volumes: [],
			disk_template: [],
			storages: [],
			schemes: [],
			hosts: [],
			networks: [],
			groups: [],
			sshs: [{ id: 0, name: '无' }]
		}
	},
	computed: {
		select_host() {
			return this.hosts.filter((v) => {
				if (this.create_guest.bootstrapType === 0) {
					return true
				} else if (this.create_guest.bootstrapType === 1) {
					if (v.uefiType && v.uefiType != '' && v.uefiPath && v.uefiPath != '') {
						return true
					}
				}
				if (v.hostId === this.create_guest.hostId) {
					this.create_guest.hostId = 0
				}
				return false
			})
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
					this.storages = res.data.filter((v) => v.status == 1 && (v.supportCategory & 2) === 2)
				}
			})
		},
		async load_all_ssh() {
			await getSshList().then((res) => {
				if (res.code == 0) {
					this.sshs = [{ id: 0, name: '无' }, ...res.data]
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
		async load_all_groups() {
			await getGroupList().then((res) => {
				if (res.code === 0) {
					this.groups = [{ groupId: 0, groupName: '默认' }, ...res.data]
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
			this.load_all_groups()
			this.load_all_ssh()
			if (this.$refs['createForm']) {
				this.$refs['createForm'].resetFields()
			}
			this.create_guest.isoTemplateId = ''
			this.create_guest.diskTemplateId = ''
			this.create_guest.volumeId = ''
			this.create_guest.type = 0
			this.create_guest.groupId = 0
			this.create_guest.systemCategory = 101
			this.create_guest.bootstrapType = 0
			this.user_config.password = ''
			this.user_config.sshId = 0
			this.meta_config.hostName = ''
		},
		create_guest_click() {
			switch (this.create_guest.type) {
				case 0:
					this.create_guest.diskTemplateId = 0
					this.create_guest.volumeId = 0
					this.create_guest.password = ''
					break
				case 1:
					this.create_guest.isoTemplateId = 0
					this.create_guest.volumeId = 0
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
					this.create_guest.size = 0
					break
			}
			let meta_map = {}
			let user_map = {}
			if (this.meta_config.hostName) {
				meta_map = { ...user_map, hostname: this.meta_config.hostName, 'local-hostname': this.meta_config.hostName }
			}
			if (this.user_config.password) {
				user_map = { ...user_map, password: this.user_config.password }
			}
			if (this.user_config.sshId > 0) {
				user_map = { ...user_map, sshId: this.user_config.sshId + '' }
			}
			let create_request_data = { ...this.create_guest, metaData: JSON.stringify(meta_map), userData: JSON.stringify(user_map) }
			createGuest(create_request_data).then((res) => {
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
<style scoped>
</style>