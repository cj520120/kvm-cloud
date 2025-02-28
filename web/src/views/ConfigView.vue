<template>
	<div>
		<el-container>
			<el-main>
				<ConfigComponent ref="ConfigComponentRef" />
			</el-main>
		</el-container>
	</div>
</template>
<script>
import ConfigComponent from '@/components/ConfigComponent.vue'
export default {
	name: 'ConfigView',
	components: { ConfigComponent },
	data() {
		return {}
	},
	mounted() {
		this.$refs.ConfigComponentRef.init(0, 0)
	}
}
</script>
<style lang="postcss" scoped>
.table_action button {
	margin: 0.1em;
}
</style>