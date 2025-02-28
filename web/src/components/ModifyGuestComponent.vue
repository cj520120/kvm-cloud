<template>
	<el-dialog title="修改配置" :close-on-click-modal="false" :visible.sync="modify_guest_dialog_visiable" width="400px">
		<el-form :model="modify_guest" label-width="100px">
			<el-form-item label="名称" prop="description">
				<el-input v-model="modify_guest.description"></el-input>
			</el-form-item>
			<el-form-item label="总线方式">
				<el-select v-model="modify_guest.busType" style="width: 100%" placeholder="总线方式">
					<el-option label="virtio" value="virtio" />
					<el-option label="ide" value="ide" />
					<el-option label="scsi" value="scsi" />
				</el-select>
			</el-form-item>
			<el-form-item label="操作系统">
				<el-select v-model="modify_guest.systemCategory" style="width: 100%" placeholder="操作系统">
					<el-option label="Centos" :value="101" />
					<el-option label="Ubuntu" :value="102" />
					<el-option label="Windows" :value="300" />
					<el-option label="Deepin" :value="103" />
					<el-option label="RedHat" :value="104" />
					<el-option label="Debian" :value="105" />
					<el-option label="OpenEuler" :value="106" />
					<el-option label="Linux" :value="100" />
					<el-option label="Unix" :value="200" />
					<el-option label="Android" :value="400" />
				</el-select>
			</el-form-item>
			<el-form-item label="固件">
				<el-select v-model="modify_guest.bootstrapType" style="width: 100%">
					<el-option label="BIOS" :value="0" />
					<el-option label="UEFI" :value="1" />
				</el-select>
			</el-form-item>
			<el-form-item label="配置">
				<el-select v-model="modify_guest.schemeId" style="width: 100%" placeholder="请选择虚拟机配置">
					<el-option v-for="item in this.schemes" :key="item.schemeId" :label="item.name" :value="item.schemeId" />
				</el-select>
			</el-form-item>
			<el-form-item label="群组">
				<el-select v-model="modify_guest.groupId" style="width: 100%" placeholder="请选择群组">
					<el-option v-for="item in this.groups" :key="item.groupId" :label="item.groupName" :value="item.groupId" />
				</el-select>
			</el-form-item>
		</el-form>
		<span slot="footer" class="dialog-footer">
			<el-button @click="modify_guest_dialog_visiable = false">取 消</el-button>
			<el-button type="primary" @click="modify_guest_click">确 定</el-button>
		</span>
	</el-dialog>
</template>
<script>
import { getGroupList, getSchemeList, modifyGuest } from '@/api/api'
export default {
	data() {
		return {
			modify_guest_dialog_visiable: false,
			groups: [],
			schemes: [],
			modify_guest: {
				guestId: 0,
				groupId: 0,
				busType: '',
				description: '',
				schemeId: '',
				bootstrapType: 0,
				systemCategory: 101
			}
		}
	},
	methods: {
		async init(guest) {
			this.groups = [{ groupId: 0, groupName: '默认' }]
			this.modify_guest.guestId = guest.guestId
			this.modify_guest.groupId = guest.groupId
			this.modify_guest.description = guest.description
			this.modify_guest.schemeId = guest.schemeId
			this.modify_guest.busType = guest.busType
			this.modify_guest_dialog_visiable = true
			this.modify_guest.systemCategory = guest.systemCategory
			this.modify_guest.bootstrapType = guest.bootstrapType
			await this.load_all_schemes()
			await this.load_all_groups()
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
		modify_guest_click() {
			modifyGuest(this.modify_guest).then((res) => {
				if (res.code === 0) {
					this.$emit('onGuestUpdate', res.data)
					this.modify_guest_dialog_visiable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: ` 修改主机信息失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>