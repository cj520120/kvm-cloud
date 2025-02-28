<template>
	<div>
		<el-container>
			<el-main>
				<el-card class="box-card" v-show="this.show_type === 0">
					<el-row slot="header" class="clearfix" style="height: 20px">
						<el-button size="mini" type="primary" icon="el-icon-circle-plus-outline" @click="show_crate_group_click">创建群组</el-button>
					</el-row>
					<el-row>
						<el-table :v-loading="data_loading" :data="groups" style="width: 100%">
							<el-table-column label="ID" prop="groupId" width="80" />
							<el-table-column label="名称" prop="groupName" show-overflow-tooltip />
							<el-table-column label="操作" min-width="380">
								<template #default="scope">
									<el-button @click="show_modify_group(scope.row)" type="" size="mini">修改</el-button>
									<el-button @click="destroy_group(scope.row)" type="danger" size="mini">删除</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-row>
				</el-card>
				<CreateGroupComponent ref="CreateGroupComponentRef" @back="show_group_list()" @onGroupUpdate="update_group" v-show="this.show_type === 1" />
				<ModifyGroupComponent ref="ModifyGroupComponentRef" @back="show_group_list()" @onGroupUpdate="update_group" v-show="this.show_type === 2" />
			</el-main>
		</el-container>
	</div>
</template>
<script>
import { getGroupList, destroyGroup } from '@/api/api'
import CreateGroupComponent from '@/components/CreateGroupComponent'
import ModifyGroupComponent from '@/components/ModifyGroupComponent'
import Notify from '@/api/notify'
import util from '@/api/util'
export default {
	name: 'groupView',
	components: { CreateGroupComponent, ModifyGroupComponent },
	data() {
		return {
			data_loading: false,
			current_group_id: 0,
			show_type: -1,
			show_group: {},

			groups: []
		}
	},
	mixins: [Notify, util],
	mounted() {
		this.show_type = 0
		this.init_view()
	},
	created() {
		this.subscribe_notify(this.$options.name, this.dispatch_notify_message)
		this.subscribe_connect_notify(this.$options.name, this.reload_page)
		this.init_notify()
	},
	beforeDestroy() {
		this.unsubscribe_notify(this.$options.name)
		this.unsubscribe_connect_notify(this.$options.name)
	},
	methods: {
		async reload_page() {
			this.init_view()
		},
		async init_view() {
			this.data_loading = true
			await getGroupList()
				.then((res) => {
					if (res.code == 0) {
						this.groups = res.data
					}
				})
				.finally(() => {
					this.data_loading = false
				})
		},
		update_group(group) {
			let findIndex = this.groups.findIndex((item) => item.groupId === group.groupId)
			if (findIndex >= 0) {
				this.$set(this.groups, findIndex, group)
			} else {
				this.groups.push(group)
			}
			this.$forceUpdate()
		},
		dispatch_notify_message(notify) {
			if (notify.type === 9) {
				let res = notify.data
				if (res.code == 0) {
					this.update_group(res.data)
				} else if (res.code == 10000001) {
					let findIndex = this.groups.findIndex((v) => v.groupId === notify.id)
					if (findIndex >= 0) {
						this.groups.splice(findIndex, 1)
					}
				}
				this.$forceUpdate()
			}
		},
		show_group_list() {
			this.show_type = 0
		},
		show_crate_group_click() {
			this.show_type = 1
			this.$refs.CreateGroupComponentRef.init()
		},
		show_modify_group(group) {
			this.$refs.ModifyGroupComponentRef.init(group)
			this.show_type = 2
		},
		destroy_group(group) {
			this.$confirm('删除群组, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					destroyGroup({ groupId: group.groupId }).then((res) => {
						if (res.code === 0) {
							let findIndex = this.groups.findIndex((item) => item.groupId === group.groupId)
							if (findIndex >= 0) {
								this.groups.splice(findIndex, 1)
							}
							this.show_type = 0
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除群组失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		}
	}
}
</script>
<style lang="postcss" scoped>
.table_action button {
	margin: 0.1em;
}
</style>