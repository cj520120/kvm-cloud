<template>
	<el-dialog title="挂载光驱" :close-on-click-modal="false" :visible.sync="attach_cd_room_dialog_visiable" width="400px">
		<el-select v-model="attach_cd_room_guest.templateId" style="width: 100%" placeholder="请选择光盘镜像">
			<el-option v-for="item in this.iso_template" :key="item.templateId" :label="item.name" :value="item.templateId" />
		</el-select>
		<span slot="footer" class="dialog-footer">
			<el-button @click="attach_cd_room_dialog_visiable = false">取 消</el-button>
			<el-button type="primary" @click="attach_cd_room_guest_click">确 定</el-button>
		</span>
	</el-dialog>
</template>
<script>
import { attachGuestCdRoom, getTemplateList } from '@/api/api'
export default {
	data() {
		return {
			attach_cd_room_dialog_visiable: false,
			iso_template: [],
			attach_cd_room_guest: {
				guestId: 0,
				templateId: ''
			}
		}
	},
	methods: {
		async init(guest) {
			this.attach_cd_room_guest.guestId = guest.guestId
			this.attach_cd_room_guest.templateId = ''
			this.attach_cd_room_dialog_visiable = true
			await this.load_iso_template()
		},
		async load_iso_template() {
			await getTemplateList().then((res) => {
				if (res.code === 0) {
					this.iso_template = res.data.filter((v) => v.templateType == 0 && v.status === 2)
				}
			})
		},
		attach_cd_room_guest_click() {
			attachGuestCdRoom(this.attach_cd_room_guest).then((res) => {
				if (res.code === 0) {
					this.$emit('onGuestUpdate', res.data)
					this.attach_cd_room_dialog_visiable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `挂载虚拟机光驱失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>