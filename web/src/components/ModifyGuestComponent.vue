<template>
	<el-dialog title="修改配置" :visible.sync="modify_guest_dialog_visiable" width="400px">
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
			<el-form-item label="计算方案">
				<el-select v-model="modify_guest.schemeId" style="width: 100%" placeholder="请选择计算方案">
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
			modify_guest: {
				guestId: 0,
				groupId: 0,
				busType: '',
				description: '',
				schemeId: ''
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