<template>
	<el-dialog title="挂载网卡" :visible.sync="attach_network_dialog_visiable" width="400px">
		<el-form :model="attach_network_guest" label-width="100px">
			<el-form-item label="网络">
				<el-select v-model="attach_network_guest.networkId" style="width: 100%" placeholder="请选择网络">
					<el-option v-for="item in this.networks" :key="item.networkId" :label="item.name" :value="item.networkId" />
				</el-select>
			</el-form-item>
			<el-form-item label="驱动">
				<el-select v-model="attach_network_guest.driveType" style="width: 100%" placeholder="请选择网卡驱动">
					<el-option label="virtio" value="virtio" />
					<el-option label="rtl8139" value="rtl8139" />
					<el-option label="e1000" value="e1000" />
				</el-select>
			</el-form-item>
		</el-form>
		<span slot="footer" class="dialog-footer">
			<el-button @click="attach_network_dialog_visiable = false">取 消</el-button>
			<el-button type="primary" @click="attach_network_click">确 定</el-button>
		</span>
	</el-dialog>
</template>
<script>
import { attachGuestNetwork, getNetworkList } from '@/api/api'
export default {
	data() {
		return {
			attach_network_dialog_visiable: false,
			networks: [],
			attach_network_guest: {
				guestId: 0,
				networkId: '',
				driveType: 'virtio'
			}
		}
	},
	methods: {
		async init(guest) {
			this.attach_network_guest.guestId = guest.guestId
			this.attach_network_guest.networkId = ''
			this.attach_network_dialog_visiable = true
			await this.load_all_networks()
		},
		async load_all_networks() {
			await getNetworkList().then((res) => {
				if (res.code === 0) {
					this.networks = res.data.filter((v) => v.status === 2)
				}
			})
		},
		attach_network_click() {
			attachGuestNetwork(this.attach_network_guest).then((res) => {
				if (res.code === 0) {
					this.$emit('onGuestAttachCallback', res.data.guest, res.data.network)
					this.attach_network_dialog_visiable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `虚拟机附加网卡失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>