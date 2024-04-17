<template>
	<div>
		<el-container>
			<el-main>
				<el-card class="box-card" v-show="this.show_type === 0">
					<el-row slot="header" class="clearfix" style="height: 20px">
						<el-button size="mini" type="primary" icon="el-icon-circle-plus-outline" @click="show_create_ssh_click">创建密钥</el-button>
					</el-row>
					<el-row>
						<el-table :v-loading="data_loading" :data="sshs" style="width: 100%">
							<el-table-column label="ID" prop="id" width="80" />
							<el-table-column label="名称" prop="name" show-overflow-tooltip />
							<el-table-column label="操作" min-width="380">
								<template #default="scope">
									<el-button @click="destroy_sshAuthorized(scope.row)" type="danger" size="mini">删除</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-row>
				</el-card>
				<CreateSshComponent ref="CreateSshComponentRef" @back="show_ssh_list()" @onSshUpdate="update_ssh" v-show="this.show_type === 1" />
			</el-main>
		</el-container>
	</div>
</template>
<script>
import { getSshList, destroySsh } from '@/api/api'
import CreateSshComponent from '@/components/CreateSshComponent'
import Notify from '@/api/notify'
import util from '@/api/util'
export default {
	name: 'sshAuthorizedView',
	components: { CreateSshComponent },
	data() {
		return {
			data_loading: false,
			current_ssh_id: 0,
			show_type: -1,
			show_ssh: {},

			sshs: []
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
			await getSshList()
				.then((res) => {
					if (res.code == 0) {
						this.sshs = res.data
					}
				})
				.finally(() => {
					this.data_loading = false
				})
		},
		update_ssh(ssh) {
			let findIndex = this.sshs.findIndex((item) => item.id === ssh.id)
			if (findIndex >= 0) {
				this.$set(this.sshs, findIndex, ssh)
			} else {
				this.sshs.push(ssh)
			}
			this.$forceUpdate()
		},
		dispatch_notify_message(notify) {
			if (notify.type === 13) {
				let res = notify.data
				if (res.code == 0) {
					this.update_ssh(res.data)
				} else if (res.code == 12000001) {
					let findIndex = this.sshs.findIndex((v) => v.id === notify.id)
					if (findIndex >= 0) {
						this.sshs.splice(findIndex, 1)
					}
				}
				this.$forceUpdate()
			}
		},
		show_ssh_list() {
			this.show_type = 0
		},
		show_create_ssh_click() {
			this.show_type = 1
			this.$refs.CreateSshComponentRef.init()
		},
		destroy_sshAuthorized(ssh) {
			this.$confirm('删除密钥, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					destroySsh({ id: ssh.id }).then((res) => {
						if (res.code === 0) {
							let findIndex = this.sshs.findIndex((item) => item.id === ssh.id)
							if (findIndex >= 0) {
								this.sshs.splice(findIndex, 1)
							}
							this.show_type = 0
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除密钥失败:${res.message}`
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