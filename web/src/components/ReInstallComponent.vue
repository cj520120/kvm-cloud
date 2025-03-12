<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="on_back_click" content="重装虚拟机" style="color: #409eff"></el-page-header>
		</el-row>
		<el-row>
			<el-form ref="reInstallForm" :model="reinstall_guest" label-width="100px" class="demo-ruleForm">
				<el-row>
					<el-col :span="8">
						<el-form-item label="安装方式">
							<el-select v-model="reinstall_guest.type" style="width: 100%" placeholder="请选择安装方式">
								<el-option label="ISO镜像" :value="0" />
								<el-option label="模版安装" :value="1" />
								<el-option label="现有磁盘" :value="3" />
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="ISO模版" v-if="reinstall_guest.type === 0">
							<el-select v-model="reinstall_guest.isoTemplateId" style="width: 100%" placeholder="请选择光盘镜像">
								<el-option v-for="item in this.iso_template" :key="item.templateId" :label="item.name" :value="item.templateId" />
							</el-select>
						</el-form-item>
						<el-form-item label="系统模版" v-if="reinstall_guest.type === 1">
							<el-select v-model="reinstall_guest.diskTemplateId" style="width: 100%" placeholder="请选择模版">
								<el-option v-for="item in this.disk_template" :key="item.templateId" :label="item.name" :value="item.templateId" />
							</el-select>
						</el-form-item>
						<el-form-item label="可用磁盘" v-if="reinstall_guest.type === 3">
							<el-select v-model="reinstall_guest.volumeId" style="width: 100%" placeholder="请选择系统磁盘">
								<el-option v-for="item in this.attach_volumes" :key="item.volumeId" :label="item.description" :value="item.volumeId" />
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="8">
						<el-form-item label="存储池">
							<el-select v-model="reinstall_guest.storageId" style="width: 100%" :disabled="reinstall_guest.type === 3">
								<el-option label="随机" :value="0"></el-option>
								<el-option v-for="item in this.storages" :key="item.storageId" :label="item.description" :value="item.storageId" />
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="操作系统">
							<el-select v-model="reinstall_guest.systemCategory" style="width: 100%" placeholder="操作系统">
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
						<el-form-item label="固件">
							<el-select v-model="reinstall_guest.bootstrapType" style="width: 100%">
								<el-option label="BIOS" :value="0" />
								<el-option label="UEFI" :value="1" />
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="8">
						<el-form-item label="主机名">
							<el-input v-model="meta_config.hostName" :disabled="reinstall_guest.type !== 1 || reinstall_guest.systemCategory == 300"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="密码">
							<el-input v-model="user_config.password" :show-password="true" type="password" :disabled="reinstall_guest.type !== 1 || reinstall_guest.systemCategory == 300"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="登录密钥">
							<el-select v-model="user_config.sshId" style="width: 100%" placeholder="登录密钥" :disabled="reinstall_guest.type !== 1 || reinstall_guest.systemCategory == 300">
								<el-option v-for="item in this.sshs" :key="item.id" :label="item.name" :value="item.id" />
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="8">
						<el-form-item label="磁盘总线">
							<el-select v-model="reinstall_guest.deviceBus" style="width: 100%" placeholder="总线方式">
								<el-option label="scsi" value="scsi" />
								<el-option label="virtio" value="virtio" />
								<el-option label="ide" value="ide" />
								<el-option label="sata" value="sata" />
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="磁盘大小">
							<el-input v-model="reinstall_guest.size" :disabled="reinstall_guest.type === 3">
								<template slot="append">GB</template>
							</el-input>
						</el-form-item>
					</el-col>
				</el-row>
				<el-form-item>
					<el-button type="primary" @click="reinstall_guest_click">立即重装</el-button>
					<el-button @click="on_back_click">取消</el-button>
				</el-form-item>
			</el-form>
		</el-row>
	</el-card>
</template>
<script>
import { reInstallGuest, getNotAttachVolumeList, getTemplateList, getStorageList, getSshList } from '@/api/api'
export default {
	data() {
		return {
			reinstall_guest: {
				guestId: 0,
				type: 0,
				isoTemplateId: '',
				diskTemplateId: '',
				volumeId: '',
				storageId: 0,
				deviceBus: 'scsi',
				size: 100,
				bootstrapType: 0,
				systemCategory: 100
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
			sshs: [{ id: 0, name: '无' }]
		}
	},
	methods: {
		on_back_click() {
			this.$emit('back')
		},
		async load_all_attach_volumes() {
			await getNotAttachVolumeList({ guestId: this.reinstall_guest.guestId }).then((res) => {
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
		async load_all_ssh() {
			await getSshList().then((res) => {
				if (res.code == 0) {
					this.sshs = [{ id: 0, name: '无' }, ...res.data]
				}
			})
		},
		async init(guest) {
			this.show_type = 0
			this.load_all_attach_volumes()
			this.load_all_template()
			this.load_all_storage()
			this.load_all_ssh()
			this.reinstall_guest.guestId = guest.guestId
			this.reinstall_guest.isoTemplateId = ''
			this.reinstall_guest.diskTemplateId = ''
			this.reinstall_guest.volumeId = ''
			this.reinstall_guest.type = 0
			this.reinstall_guest.systemCategory = guest.systemCategory
			this.reinstall_guest.bootstrapType = guest.bootstrapType
			this.user_config.password = ''
			this.user_config.sshId = 0
			this.meta_config.hostName = ''
		},
		reinstall_guest_click() {
			switch (this.reinstall_guest.type) {
				case 0:
					this.reinstall_guest.diskTemplateId = 0
					this.reinstall_guest.volumeId = 0
					break
				case 1:
					this.reinstall_guest.isoTemplateId = 0
					this.reinstall_guest.volumeId = 0
					this.reinstall_guest.size = 0
					break
				case 2:
					this.reinstall_guest.isoTemplateId = 0
					this.reinstall_guest.diskTemplateId = 0
					this.reinstall_guest.volumeId = 0
					this.reinstall_guest.size = 0
					break
				case 3:
					this.reinstall_guest.isoTemplateId = 0
					this.reinstall_guest.diskTemplateId = 0
					this.reinstall_guest.size = 0
					break
			}
			this.$confirm('重装虚拟机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
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
					let reinstall_guest_data = { ...this.reinstall_guest, metaData: JSON.stringify(meta_map), userData: JSON.stringify(user_map) }
					reInstallGuest(reinstall_guest_data).then((res) => {
						if (res.code === 0) {
							this.$emit('finish', res.data)
						} else {
							this.$notify.error({
								title: '错误',
								message: `重装虚拟机失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>