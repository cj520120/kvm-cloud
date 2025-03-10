<template>
	<el-dialog title="挂载磁盘" :close-on-click-modal="false" :visible.sync="attach_volume_dialog_visiable" width="400px">
		<el-form :model="attach_volume_guest" label-width="100px">
			<el-form-item label="磁盘">
				<el-select v-model="attach_volume_guest.volumeId" style="width: 100%" placeholder="请选择磁盘">
					<el-option v-for="item in this.attach_volumes" :key="item.volumeId" :label="item.description" :value="item.volumeId" />
				</el-select>
			</el-form-item>

			<el-form-item label="驱动方式">
				<el-select v-model="attach_volume_guest.deviceType" style="width: 100%" placeholder="驱动方式">
					<el-option label="virtio" value="virtio" />
					<el-option label="scsi" value="scsi" />
					<el-option label="sata" value="sata" />
				</el-select>
			</el-form-item>
		</el-form>
		<span slot="footer" class="dialog-footer">
			<el-button @click="attach_volume_dialog_visiable = false">取 消</el-button>
			<el-button type="primary" @click="attach_volume_click">确 定</el-button>
		</span>
	</el-dialog>
</template>
<script>
import { getNotAttachVolumeList, attachGuestDisk } from '@/api/api'

export default {
	data() {
		return {
			attach_volume_dialog_visiable: false,
			attach_volumes: [],
			attach_volume_guest: {
				guestId: 0,
				volumeId: '',
				deviceType: 'virtio'
			}
		}
	},
	methods: {
		async load_all_attach_volumes() {
			await getNotAttachVolumeList({ guestId: this.attach_volume_guest.guestId }).then((res) => {
				if (res.code === 0) {
					this.attach_volumes = res.data
				}
			})
		},
		async init(guest) {
			this.attach_volume_guest.guestId = guest.guestId
			this.attach_volume_guest.volumeId = ''
			this.attach_volume_dialog_visiable = true
			this.attach_volumes = []
			await this.load_all_attach_volumes()
		},
		attach_volume_click() {
			attachGuestDisk(this.attach_volume_guest).then((res) => {
				if (res.code === 0) {
					this.$emit('onVoumeAttachCallBack', res.data.guest, res.data.volume)
					this.attach_volume_dialog_visiable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: ` 挂载磁盘失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>