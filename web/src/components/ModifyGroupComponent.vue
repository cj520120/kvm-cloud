<template>
	<el-card class="box-card">
		<el-row slot="header">
			<el-page-header @back="on_back_click()" content="修改群组" style="color: #409eff"></el-page-header>
		</el-row>
		<el-row>
			<el-form :model="modify_group" label-width="100px" class="demo-ruleForm">
				<el-form-item label="名称" prop="name">
					<el-input v-model="modify_group.groupName"></el-input>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="modify_group_click">修改</el-button>
					<el-button @click="on_back_click">取消</el-button>
				</el-form-item>
			</el-form>
		</el-row>
	</el-card>
</template>
<script>
import { modifyGroup } from '@/api/api'
export default {
	data() {
		return {
			modify_group: {
				groupId: 0,
				groupName: ''
			}
		}
	},
	methods: {
		on_back_click() {
			this.$emit('back')
		},
		on_notify_update_group(host) {
			this.$emit('onSchemeUpdate', host)
		},
		init(group) {
			this.modify_group.groupId = group.groupId
			this.modify_group.groupName = group.groupName
		},

		modify_group_click() {
			this.$confirm('修改群组, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					modifyGroup(this.modify_group).then((res) => {
						if (res.code === 0) {
							this.on_notify_update_group(res.data)
							this.on_back_click()
						} else {
							this.$notify.error({
								title: '错误',
								message: `修改群组失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>