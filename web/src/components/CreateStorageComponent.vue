<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="on_back_click()" content="创建存储池" style="color: #409eff"></el-page-header>
		</el-row>
		<el-row>
			<el-form ref="createForm" :model="create_storage" label-width="150px" class="demo-ruleForm">
				<el-form-item label="名称" prop="description">
					<el-input v-model="create_storage.description"></el-input>
				</el-form-item>
				<el-form-item label="存储池类型" prop="type">
					<el-select v-model="create_storage.type" style="width: 100%">
						<el-option label="NFS" value="nfs"></el-option>
						<el-option label="Glusterfs" value="glusterfs"></el-option>
						<el-option label="Ceph Rbd" value="ceph-rbd"></el-option>
					</el-select>
				</el-form-item>
				<el-form-item label="路径" prop="path" v-if="create_storage.type === 'nfs'">
					<el-input v-model="create_storage.path"></el-input>
				</el-form-item>
				<el-form-item label="Glusterfs磁盘名" prop="path" v-if="this.create_storage.type === 'glusterfs'">
					<el-input v-model="create_storage.volume"></el-input>
				</el-form-item>
				<el-form-item label="存储池" prop="path" v-if="this.create_storage.type === 'ceph-rbd'">
					<el-input v-model="create_storage.pool"></el-input>
				</el-form-item>
				<el-form-item label="用户" prop="path" v-if="this.create_storage.type === 'ceph-rbd'">
					<el-input v-model="create_storage.username"></el-input>
				</el-form-item>
				<el-form-item label="密钥" prop="path" v-if="this.create_storage.type === 'ceph-rbd'">
					<el-input v-model="create_storage.secret"></el-input>
				</el-form-item>
				<el-form-item label="NFS地址" prop="uri" v-if="create_storage.type === 'nfs'">
					<el-input v-model="create_storage.uri"></el-input>
				</el-form-item>
				<el-form-item label="Glusterfs地址" prop="uri" v-if="this.create_storage.type === 'glusterfs'">
					<el-input v-model="create_storage.uri"></el-input>
				</el-form-item>
				<el-form-item label="Ceph地址" prop="uri" v-if="this.create_storage.type === 'ceph-rbd'">
					<el-input v-model="create_storage.uri"></el-input>
				</el-form-item>
				<el-form-item label="允许范围">
					<el-checkbox-group v-model="storage_support_category_select">
						<el-checkbox :label="1">模版</el-checkbox>
						<el-checkbox :label="2">磁盘</el-checkbox>
					</el-checkbox-group>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="create_storage_click">立即创建</el-button>
					<el-button @click="on_back_click">取消</el-button>
				</el-form-item>
			</el-form>
		</el-row>
	</el-card>
</template>
<script>
import { createStorage } from '@/api/api'
export default {
	data() {
		return {
			storage_support_category_select: [1, 2],
			create_storage: {
				supportCategory: 0,
				description: '',
				type: 'nfs',
				param: '',
				volume: '',
				path: '',
				pool: '',
				username: '',
				secret: '',
				uri: ''
			}
		}
	},
	methods: {
		on_back_click() {
			this.$emit('back')
		},
		on_notify_update_scheme(storage) {
			this.$emit('onStorageUpdate', storage)
		},
		init() {
			if (this.$refs['createForm']) {
				this.$refs['createForm'].resetFields()
			}
		},
		create_storage_click() {
			let supportCategory = 0
			this.storage_support_category_select.forEach((val) => {
				supportCategory |= val
			})
			let data = {
				description: this.create_storage.description,
				name: this.create_storage.name,
				type: this.create_storage.type,
				supportCategory: supportCategory,
				param: '{}'
			}
			if (this.create_storage.type === 'nfs') {
				data.param = JSON.stringify({
					path: this.create_storage.path,
					uri: this.create_storage.uri
				})
			} else if (this.create_storage.type === 'glusterfs') {
				data.param = JSON.stringify({
					volume: this.create_storage.volume,
					uri: this.create_storage.uri
				})
			} else if (this.create_storage.type === 'ceph-rbd') {
				data.param = JSON.stringify({
					pool: this.create_storage.pool,
					username: this.create_storage.username,
					secret: this.create_storage.secret,
					uri: this.create_storage.uri
				})
			} else {
				this.$notify.error({
					title: '错误',
					message: `不支持的存储池:${this.create_storage.type}`
				})
				return
			}
			createStorage(data).then((res) => {
				if (res.code === 0) {
					this.on_notify_update_scheme(res.data)
					this.on_back_click()
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建存储池失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>