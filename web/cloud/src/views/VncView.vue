<template>
	<div class="main">
		<div id="top_bar">
			<div id="status">{{ status }}</div>
			<div id="sendCtrlAltDelButton" @onclick="on_send_ctrl_alt_del">发送 Ctrl+Alt+Del</div>
		</div>
		<div id="screen"></div>
	</div>
</template>
    
    <script>
import RFB from '@novnc/novnc/core/rfb'
import { getGuestVncPassword } from '@/api/api'
export default {
	name: 'VncView',
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
		this.id = this.$route.query.id
		this.description = this.$route.query.description
		let protocol
		if (window.location.protocol === 'https:') {
			protocol = 'wss'
		} else {
			protocol = 'ws'
		}
		this.status = `正在连接[${this.description}]...`
		this.url = process.env.NODE_ENV === 'production' ? `${protocol}://${window.location.host}/api/vnc/${this.id}` : `${protocol}://localhost:8080/api/vnc/${this.id}`
		this.connect()
	},
	methods: {
		connect() {
			getGuestVncPassword({ guestId: this.id }).then((res) => {
				if (res.code === 0) {
					this.rfb = new RFB(document.getElementById('screen'), this.url, { credentials: { password: res.data } })
					this.rfb.addEventListener('connect', this.on_connect_success)
					this.rfb.addEventListener('disconnect', this.on_disconnect)
					this.rfb.addEventListener('credentialsrequired', this.on_required_password)
					this.rfb.scaleViewport = false
					this.rfb.resizeSession = false
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
		}
	}
}
</script>
    
    <style scoped>
.main {
	margin: 0;
	background-color: dimgrey;
	height: 100vh;
	width: 100vw;
	display: flex;
	flex-direction: column;
}
#top_bar {
	background-color: #6e84a3;
	color: white;
	font: bold 12px Helvetica;
	padding: 6px 5px 4px 5px;
	border-bottom: 1px outset;
	height: 20px;
}
#status {
	text-align: center;
}
#sendCtrlAltDelButton {
	position: fixed;
	top: 0px;
	right: 0px;
	border: 1px outset;
	padding: 5px 5px 4px 5px;
	cursor: pointer;
}

#screen {
	flex: 1; /* fill remaining space */
	overflow: hidden;
	width: 100vw;
	height: 100vh;
}
</style>