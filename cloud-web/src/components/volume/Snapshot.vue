<template>
	<el-dialog title="快照信息" :visible.sync="dialog_visible" center width="700px">
		<el-table :data="table_snapshot" style="width: 100%" v-loading="data_loading">
			<el-table-column label="名称" prop="tag" width="300"></el-table-column>
			<el-table-column label="创建时间" prop="createTime" width="200">
				<template slot-scope="scope">
					<span>{{ parse_date(scope.row.createTime) }}</span>
				</template>
			</el-table-column>
			<el-table-column label="操作">
				<template slot-scope="scope">
					<el-button plain size="small" type="primary" v-loading="scope.row.loading" @click="on_revert_click(scope.row)">恢复</el-button>
					<el-button plain size="small" type="primary" v-loading="scope.row.loading" @click="on_delete_click(scope.row)">删除</el-button>
				</template>
			</el-table-column>
		</el-table>
	</el-dialog>
</template>

<script>
export default {
	name: 'Snapshot',
	data() {
		return {
			table_snapshot: [],
			dialog_visible: false,
			data_loading: false,
			modify: {
				id: 0
			}
		}
	},
	methods: {
		init_data(volume_id) {
			this.modify.id = volume_id
			this.modify.name = ''
			this.table_snapshot = []
			this.dialog_visible = true
			this.data_loading = true

			return this.axios_get(`/management/volume/snapshot?id=${this.modify.id}`).then((res) => {
				if (res.data.code === 0 && res.data.data) {
					console.log(res.data.data)
					this.table_snapshot = res.data.data.map((item) => {
						return { ...item, loading: false }
					})
					console.log(this.table_snapshot)
				}
				this.data_loading = false
			})
		},
		on_revert_click(data) {
			this.modify.name = data.tag
			const load = this.$loading({
				lock: true,
				text: '正在恢复快照，该过程需要一段时间，请耐心等待....',
				spinner: 'el-icon-loading',
				background: 'rgba(0, 0, 0, 0.7)'
			})
			this.post_data(`/management/volume/snapshot/revert`, this.modify).finally(() => {
				load.close()
			})
		},
		on_delete_click(data) {
			this.modify.name = data.tag
			const load = this.$loading({
				lock: true,
				text: '正在删除快照，该过程需要一段时间，请耐心等待....',
				spinner: 'el-icon-loading',
				background: 'rgba(0, 0, 0, 0.7)'
			})
			this.post_data(`/management/volume/snapshot/delete`, this.modify)
				.then((res) => {
					if (res.data.code == 0) {
						let findIndex = this.table_snapshot.findIndex((item) => item.tag === data.tag)
						this.$delete(this.table_snapshot, findIndex)
					}
				})
				.finally(() => {
					load.close()
				})
		}
	}
}
</script>

<style scoped>
</style>