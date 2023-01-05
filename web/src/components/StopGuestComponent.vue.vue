<template>
	<el-dialog title="关闭虚拟机" :visible.sync="stop_dialog_visiable" width="400px">
		<el-checkbox v-model="stop_guest.force">强制关闭</el-checkbox>
		<span slot="footer" class="dialog-footer">
			<el-button @click="stop_dialog_visiable = false">取 消</el-button>
			<el-button type="primary" @click="stop_guest_click">确 定</el-button>
		</span>
	</el-dialog>
</template>
<script>
import { stopGuest } from '@/api/api'
export default {
	data() {
		return {
			stop_dialog_visiable: false,
			stop_guest: {
				hostId: 0,
				force: false
			}
		}
	},
	methods: {
		async init(guest) {
			this.stop_guest.guestId = guest.guestId
			this.stop_guest.force = false
			this.stop_dialog_visiable = true
		},
		stop_guest_click() {
			this.$confirm('停止当前虚拟机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					stopGuest(this.stop_guest).then((res) => {
						if (res.code === 0) {
							this.$emit('onGuestUpdate', res.data)
							this.stop_dialog_visiable = false
						} else {
							this.$notify.error({
								title: '错误',
								message: `启动虚拟机失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>