<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="on_back_click()" content="创建网络" style="color: #409eff"></el-page-header>
		</el-row>
		<el-row>
			<el-form ref="createForm" :model="create_network" label-width="100px" class="demo-ruleForm">
				<el-row :gutter="24">
					<el-col :span="8">
						<el-form-item label="网络名称" prop="name">
							<el-input v-model="create_network.name"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="网络类型" prop="type">
							<el-select v-model="create_network.type" style="width: 100%">
								<el-option label="基础网络" :value="0"></el-option>
								<el-option label="Vlan网络(只允许OpenSitch方式)" :value="1"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>

				<el-row :gutter="24">
					<el-col :span="8">
						<el-form-item label="起始IP" prop="startIp"><el-input v-model="create_network.startIp"></el-input></el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="结束IP" prop="endIp"><el-input v-model="create_network.endIp"></el-input></el-form-item>
					</el-col>
				</el-row>

				<el-row :gutter="24" v-if="create_network.type === 1">
					<el-col :span="8">
						<el-form-item label="网关类型" prop="mask">
							<el-select v-model="gateway_type" style="width: 100%">
								<el-option label="模拟网关" :value="0"></el-option>
								<el-option label="硬件网关" :value="1"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="网关地址" prop="gateway" v-if="gateway_type == 1"><el-input v-model="create_network.gateway"></el-input></el-form-item>
					</el-col>
				</el-row>
				<el-row :gutter="24">
					<el-col :span="8">
						<el-form-item label="子网掩码" prop="mask"><el-input v-model="create_network.mask"></el-input></el-form-item>
					</el-col>
					<el-col :span="8" v-if="create_network.type === 0">
						<el-form-item label="网关地址" prop="gateway"><el-input v-model="create_network.gateway"></el-input></el-form-item>
					</el-col>
					<el-col :span="8" v-if="create_network.type === 1">
						<el-form-item label="VLAN ID" prop="vlanId"><el-input v-model="create_network.vlanId"></el-input></el-form-item>
					</el-col>
				</el-row>

				<el-row :gutter="24">
					<el-col :span="8">
						<el-form-item label="子网地址" prop="subnet"><el-input v-model="create_network.subnet"></el-input></el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="广播地址" prop="broadcast"><el-input v-model="create_network.broadcast"></el-input></el-form-item>
					</el-col>
				</el-row>
				<el-row :gutter="24">
					<el-col :span="8">
						<el-form-item label="DNS" prop="dns"><el-input v-model="create_network.dns"></el-input></el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="搜索域" prop="domain"><el-input v-model="create_network.domain"></el-input></el-form-item>
					</el-col>
				</el-row>

				<el-row :gutter="24">
					<el-col :span="8" v-if="create_network.type === 0">
						<el-form-item label="桥接网卡" prop="bridge">
							<el-input v-model="create_network.bridge"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="8" v-if="create_network.type === 0">
						<el-form-item label="桥接方式" prop="bridgeType">
							<el-select v-model="create_network.bridgeType" style="width: 100%">
								<el-option label="基础桥接" :value="0"></el-option>
								<el-option label="OpenSwitch桥接" :value="1"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="8" v-if="create_network.type === 1">
						<el-form-item label="基础网络" prop="basicNetworkId">
							<el-select v-model="create_network.basicNetworkId" placeholder="请选择基础网络" style="width: 100%">
								<el-option :label="item.name" :value="item.networkId" v-show="item.type === 0" v-for="item in networks" :key="item.networkId"></el-option>
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>
				<el-form-item>
					<el-button type="primary" @click="create_network_click">立即创建</el-button>
					<el-button @click="on_back_click">取消</el-button>
				</el-form-item>
			</el-form>
		</el-row>
	</el-card>
</template>
<script>
import { createNetwork, getNetworkList } from '@/api/api'
export default {
	data() {
		return {
			networks: [],
			create_network: {
				name: '',
				startIp: '',
				endIp: '',
				gateway: '',
				mask: '',
				subnet: '',
				broadcast: '',
				bridge: '',
				bridgeType: 0,
				dns: '',
				type: 0,
				domain: 'cj.kvm.internal',
				vlanId: 100,
				basicNetworkId: ''
			},
			gateway_type: 0
		}
	},
	methods: {
		on_back_click() {
			this.$emit('back')
		},
		on_notify_update_network(host) {
			this.$emit('onNetworkUpdate', host)
		},
		async init() {
			if (this.$refs['createForm']) {
				this.$refs['createForm'].resetFields()
			}
			this.gateway_type = 0
			await getNetworkList()
				.then((res) => {
					if (res.code == 0) {
						this.networks = res.data
					}
				})
				.finally(() => {
					this.data_loading = false
				})
		},
		create_network_click() {
			if (this.create_network.type === 0) {
				this.create_network.vlanId = 0
				this.create_network.basicNetworkId = 0
			}
			if (this.create_network.type === 1 && this.gateway_type === 0) {
				this.create_network.gateway = ''
			}
			createNetwork(this.create_network).then((res) => {
				if (res.code === 0) {
					this.on_notify_update_network(res.data)
					this.on_back_click()
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建网络失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>