<template>
	<div>
		<div>
			<div class="global-group">
				<el-checkbox-group v-model="modifierKeys">
					<el-checkbox v-for="keybord in comboKeybord" :label="keybord" :key="keybord" style="color: white">{{ keybord }}</el-checkbox>
				</el-checkbox-group>
			</div>
			<el-button size="mini" @click="$emit('send-controller-key', modifierKeys)" style="margin-left: 10px">发送</el-button>
		</div>
	</div>
</template>
  
  <script>
export default {
	name: 'KeyboardGlobal',
	data() {
		return {
			modifierKeys: [],
			comboKeybord: ['Shift', 'Ctrl', 'Alt', 'Del']
		}
	}
}
</script>
  
  <style scoped>
.global-group {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 4px;
}
.global-group > :first-child {
	margin-left: 10px;
}
</style>