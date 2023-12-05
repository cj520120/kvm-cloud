<template>
	<el-dialog title="启动虚拟机" :close-on-click-modal="false" :visible.sync="start_dialog_visiable" width="400px">
		<el-form :model="start_guest" label-width="100px">
			<el-form-item label="选择主机">
				<el-select v-model="start_guest.hostId" style="width: 100%">
					<el-option label="随机" :value="0"></el-option>
					<el-option v-for="item in this.hosts" :key="item.hostId" :label="item.displayName" :value="item.hostId" :v-loading="true" />
				</el-select>
			</el-form-item>
		</el-form>
		<span slot="footer" class="dialog-footer">
			<el-button @click="start_dialog_visiable = false">取 消</el-button>
			<el-button type="primary" @click="start_guest_click">确 定</el-button>
		</span>
	</el-dialog>
</template>
<script>
import { startGuest, getHostList } from '@/api/api'
export default {
	data() {
		return {
			start_dialog_visiable: false,
			start_guest: {
				hostId: 0,
				guestId: 0
			},
			hosts: []
		}
	},
	methods: {
		async init(guest) {
			this.start_guest.guestId = guest.guestId
			this.start_guest.hostId = 0
			this.start_dialog_visiable = true
			await this.load_all_host()
		},
		async load_all_host() {
			await getHostList().then((res) => {
				if (res.code === 0) {
					this.hosts = res.data.filter((v) => v.status == 1)
				}
			})
		},
		start_guest_click() {
			startGuest(this.start_guest).then((res) => {
				if (res.code === 0) {
					this.$emit('onGuestUpdate', res.data)
					this.start_dialog_visiable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `启动虚拟机失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>