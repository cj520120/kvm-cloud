<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="on_back_click()" content="创建群组" style="color: #409eff"></el-page-header>
		</el-row>
		<el-row>
			<el-form ref="createForm" :model="create_group" label-width="100px" class="demo-ruleForm">
				<el-form-item label="名称" prop="groupName">
					<el-input v-model="create_group.groupName"></el-input>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="create_group_click">立即创建</el-button>
					<el-button @click="on_back_click">取消</el-button>
				</el-form-item>
			</el-form>
		</el-row>
	</el-card>
</template>
<script>
import { createGroup } from '@/api/api'
export default {
	data() {
		return {
			create_group: {
				groupName: ''
			}
		}
	},
	methods: {
		on_back_click() {
			this.$emit('back')
		},
		on_notify_update_group(host) {
			this.$emit('onGroupUpdate', host)
		},
		init() {
			if (this.$refs['createForm']) {
				this.$refs['createForm'].resetFields()
			}
		},
		create_group_click() {
			createGroup(this.create_group).then((res) => {
				if (res.code === 0) {
					this.on_notify_update_group(res.data)
					this.on_back_click()
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建群组失败:${res.message}`
					})
				}
			})
		}
	}
}
</script>