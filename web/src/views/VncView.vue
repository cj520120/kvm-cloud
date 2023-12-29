<template>
	<div class="main">
		<el-container>
			<el-header class="tool_tip" height="40px" align="middle" justify="center">
				<div class="logo">
					<div style="text-align: center; height: 40px; align: center; float: left">
						<img src="../assets/logo.png" style="margin-top: 10px; width: 16px; height: 16px" />
					</div>
					<div style="float: left; margin-left: 5px; font: bold 14px Helvetica; color: grary; padding-top: 11px; width: 100px">KVM Cloud</div>
				</div>
				<div class="status">
					{{ status }}
				</div>
				<div class="controller">
					<div @click="on_send_ctrl_alt_del" class="btn">发送 Ctrl+Alt+Del</div>
				</div>
			</el-header>
			<el-main><div ref="vncContainer"></div></el-main>
			<el-footer class="footer" height="40px">
				<div></div>
			</el-footer>
		</el-container>
	</div>
</template>
    
<script>
import RFB from '@novnc/novnc/core/rfb'
import { getGuestVncPassword } from '@/api/api'
export default {
	name: 'VncView',
	inject: ['check_full_screen'],
	data() {
		return {
			id: 0,
			description: '',
			rfb: null,
			url: '',
			status: '连接中...'
		}
	},
	mounted() {
		this.check_full_screen(true)
		this.id = this.$route.query.id
		this.description = this.$route.query.description
		let protocol
		if (window.location.protocol === 'https:') {
			protocol = 'wss'
		} else {
			protocol = 'ws'
		}
		this.status = `正在连接[${this.description}]...`
		this.url = process.env.NODE_ENV === 'production' ? `${protocol}://${window.location.host}/api/vnc/${this.id}` : `${protocol}://192.168.2.193:8080/api/vnc/${this.id}`
		this.connect()
	},
	beforeDestroy() {
		this.check_full_screen(false)
	},
	methods: {
		connect() {
			getGuestVncPassword({ guestId: this.id }).then((res) => {
				if (res.code === 0) {
					const container = this.$refs.vncContainer
					this.rfb = new RFB(container, this.url, { credentials: { password: res.data } })
					this.rfb.addEventListener('connect', this.on_connect_success)
					this.rfb.addEventListener('disconnect', this.on_disconnect)
					this.rfb.addEventListener('credentialsrequired', this.on_required_password)
					this.rfb.scaleViewport = false
					this.rfb.resizeSession = true
				}
			})
		},
		on_connect_success() {
			this.status = `连接成功[${this.description}]`
		},
		on_disconnect(msg) {
			this.status = `远程连接已断开[${this.description}]`
			if (msg.detail.clean) {
				this.status = `正在重新连接[${this.description}]...`
				this.connect()
			}
		},
		on_required_password() {
			getGuestVncPassword({ guestId: this.id }).then((res) => {
				if (res.code === 0 && res.data) {
					this.rfb.sendCredentials({ password: res.data })
				}
			})
		},
		on_send_ctrl_alt_del() {
			this.rfb.sendCtrlAltDel()
			this.rfb.focus()
		},
		sendKey(keysym, code, down) {
			this.rfb.sendKey(keysym, code, down)
			this.rfb.focus()
		}
	}
}
</script>
    
    <style scoped>
.main {
	margin: 0px;
	padding: 0px;
	background-color: rgb(40, 40, 40);
	min-height: 100vh;
	min-width: 100vw;
	height: fit-content;
	width: fit-content;
	display: flex;
	flex-direction: column;
}
.tool_tip {
	background-color: rgb(92, 92, 92);
	color: white;
	font: bold 12px Helvetica;
	border-bottom: 1px outset;
	padding: 0px;
	border-bottom: 1px outset;
}
.tool_tip .logo {
	padding: 0px;
	padding-left: 20px;
	margin-top: 0px;
	width: 200px;
	height: 100%;
	float: left;
}
.tool_tip .status {
	width: calc(100% - 400px) !important;
	height: 100%;
	float: left;
	line-height: 40px;
}
footer {
	color: white;
	font: bold 12px Helvetica;
	border-bottom: 1px outset;
}
.tooltip_status {
	padding-top: 12px;
}
.tool_tip .controller {
	widows: 200px;
	height: 40px;
}
.tool_tip .controller .btn {
	float: right;
	cursor: pointer;
	color: white;
	margin: 10px;
	padding: 5px;
	padding-left: 5px;
	border: 1px outset;
}
#screen {
	flex: 1; /* fill remaining space */
	overflow: hidden;
	width: 100vw;
	height: 100vh;
}
</style>