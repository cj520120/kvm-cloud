<template>
	<el-dialog title="迁移虚拟机" :visible.sync="migrate_dialog_visiable" width="400px">
		<el-form :model="migrate_guest" label-width="100px">
			<el-form-item label="选择主机">
				<el-select v-model="migrate_guest.hostId" style="width: 100%">
					<el-option label="随机" :value="0"></el-option>
					<el-option v-for="item in this.hosts" :key="item.hostId" :label="item.displayName" :value="item.hostId" :v-loading="true" />
				</el-select>
			</el-form-item>
		</el-form>
		<span slot="footer" class="dialog-footer">
			<el-button @click="migrate_dialog_visiable = false">取 消</el-button>
			<el-button type="primary" @click="migrate_guest_click">确 定</el-button>
		</span>
	</el-dialog>
</template>
<script>
import { migrateGuest, getHostList } from '@/api/api'
export default {
	data() {
		return {
			migrate_dialog_visiable: false,
			migrate_guest: {
				hostId: 0,
				guestId: 0
			},
			last_runing_host_id:0,
			hosts: []
		}
	},
	methods: {
		async init(guest) {
			this.migrate_guest.guestId = guest.guestId
			this.migrate_guest.hostId = 0
			this.last_runing_host_id=guest.hostId
			this.migrate_dialog_visiable = true
			await this.load_all_host()
		},
		async load_all_host() {
			await getHostList().then((res) => {
				if (res.code === 0) {
					this.hosts = res.data.filter((v) => v.status === 1&&v.hostId!==this.last_runing_host_id)
				}
			})
		},
		migrate_guest_click() {
			migrateGuest(this.migrate_guest).then((res) => {
				if (res.code === 0) {
					this.$emit('onGuestUpdate', res.data)
					this.migrate_dialog_visiable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `迁移虚拟机失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>