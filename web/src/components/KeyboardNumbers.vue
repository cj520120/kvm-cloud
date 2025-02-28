<template>
	<div class="keyboard-numbers">
		<div class="number-buttons">
			<el-button size="mini" v-for="(letters, index) in currentNumbers" :key="'shift-' + index" @click="$emit('send-key', letters.keysym)">
				{{ letters.display }}
			</el-button>
		</div>
	</div>
</template>
  
  <script>
export default {
	name: 'KeyboardNumbers',
	computed: {
		currentNumbers() {
			const baseChars = Array.from({ length: 10 }, (_, i) => {
				let display = `${i}`
				return {
					display: display,
					keysym: `XK_${display}`
				}
			})
			return baseChars
		}
	}
}
</script>
  
  <style scoped>
.number-buttons {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 4px;
}
.number-buttons > :first-child {
	margin-left: 10px;
}
</style>