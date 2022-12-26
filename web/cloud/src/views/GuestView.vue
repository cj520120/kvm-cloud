<template>
	<div>
		<el-container>
			<el-main>
				<el-card class="box-card" v-show="this.show_type === 0">
					<el-row slot="header" class="clearfix" style="height: 20px">
						<el-col :span="14">
							<el-link type="primary" @click="show_create_guest_click">创建虚拟机</el-link>

							<el-link style="padding: 0 10px" :disabled="!select_guests.length" type="primary" @click="batch_start_guest_click">批量启动</el-link>
							<el-link :disabled="!select_guests.length" type="danger" @click="batch_stop_guest_click">批量停止</el-link>
						</el-col>
						<el-col :span="10"><el-input style="float: right; width: 300px; margin-bottom: 10px" placeholder="请输入搜索关键字" v-model="keyword" @input="update_guest_show_page"></el-input></el-col>
					</el-row>
					<el-row>
						<el-table :v-loading="data_loading" :data="show_table_guests" style="width: 100%" @selection-change="handleSelectionChange">
							<el-table-column type="selection" width="55"></el-table-column>
							<el-table-column label="ID" prop="guestId" width="80" />
							<el-table-column label="实例名" prop="name" width="180" />
							<el-table-column label="标签" prop="description" width="180" />
							<el-table-column label="IP地址" prop="guestIp" width="150" />
							<el-table-column label="配置" prop="cpu" width="150">
								<template #default="scope">{{ scope.row.cpu }}核/{{ get_memory_desplay(scope.row.memory) }}</template>
							</el-table-column>
							<el-table-column label="类型" width="100">
								<template #default="scope">
									<el-tag>{{ scope.row.type === 0 ? '系统主机' : '用户主机' }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="状态" prop="status" width="100">
								<template #default="scope">
									<el-tag :type="scope.row.status === 2 ? 'success' : 'danger'">{{ get_guest_status(scope.row) }}</el-tag>
								</template>
							</el-table-column>
							<el-table-column label="操作">
								<template #default="scope">
									<el-dropdown size="small" @click="show_guest_info_click(scope.row)" split-button placement="bottom-end" type="primary" @command="menu_command_click">
										虚拟机管理
										<el-dropdown-menu slot="dropdown">
											<el-dropdown-item :command="{ guest: scope.row, command: 'info' }">虚拟机详情</el-dropdown-item>
											<el-dropdown-item :command="{ guest: scope.row, command: 'start' }" divided :disabled="scope.row.status !== 4">启动虚拟机</el-dropdown-item>
											<el-dropdown-item :command="{ guest: scope.row, command: 'stop' }" :disabled="scope.row.status !== 2">停止虚拟机</el-dropdown-item>
											<el-dropdown-item :command="{ guest: scope.row, command: 'vnc' }" :disabled="scope.row.status !== 2">远程桌面</el-dropdown-item>
											<el-dropdown-item :command="{ guest: scope.row, command: 'reboot' }" :disabled="scope.row.status !== 2">重启虚拟机</el-dropdown-item>
											<el-dropdown-item :command="{ guest: scope.row, command: 'attach_cd' }" :disabled="scope.row.cdRoom !== 0" v-if="scope.row.type !== 0">挂载光驱</el-dropdown-item>
											<el-dropdown-item :command="{ guest: scope.row, command: 'detach_cd' }" :disabled="scope.row.cdRoom === 0" v-if="scope.row.type !== 0">卸载光驱</el-dropdown-item>
											<el-dropdown-item :command="{ guest: scope.row, command: 'destroy' }" divided>销毁虚拟机</el-dropdown-item>
										</el-dropdown-menu>
									</el-dropdown>
								</template>
							</el-table-column>
						</el-table>
						<el-pagination :current-page="current_page" :page-size="page_size" :page-sizes="[5, 10, 20, 50, 100, 200]" :total="total_size" layout="total, sizes, prev, pager, next, jumper" @size-change="on_page_size_change" @current-change="on_current_page_change"></el-pagination>
					</el-row>
				</el-card>
				<el-card class="box-card" v-show="this.show_type === 1">
					<el-row slot="header">
						<el-page-header @back="show_guest_list_page" content="虚拟机详情"></el-page-header>
					</el-row>
					<el-row style="text-align: left; margin: 20px 0">
						<el-button @click="show_start_guest_click(show_guest_info.current_guest)" type="primary" size="mini" :disabled="show_guest_info.current_guest.status !== 4">启动虚拟机</el-button>
						<el-button @click="show_stop_guest_click(show_guest_info.current_guest)" type="primary" size="mini">停止虚拟机</el-button>
						<el-button @click="reboot_guest_click(show_guest_info.current_guest)" type="primary" size="mini" :disabled="show_guest_info.current_guest.status !== 2" v-if="show_guest_info.current_guest.type !== 0">重启虚拟机</el-button>
						<el-button @click="show_reinstall_guest_click(show_guest_info.current_guest)" type="primary" size="mini" :disabled="show_guest_info.current_guest.status !== 4" v-if="show_guest_info.current_guest.type !== 0">重装系统</el-button>

						<el-button @click="show_modify_guest_click(show_guest_info.current_guest)" type="primary" size="mini" :disabled="show_guest_info.current_guest.status !== 4" v-if="show_guest_info.current_guest.type !== 0">修改配置</el-button>
						<el-button @click="vnc_click(show_guest_info.current_guest)" type="primary" size="mini">远程桌面</el-button>
						<el-button @click="show_attach_cd_room_click(show_guest_info.current_guest)" type="primary" size="mini" :disabled="show_guest_info.current_guest.cdRoom !== 0" v-if="show_guest_info.current_guest.type !== 0">挂载光驱</el-button>
						<el-button @click="detach_guest_cd_room_click(show_guest_info.current_guest)" type="primary" size="mini" :disabled="show_guest_info.current_guest.cdRoom === 0" v-if="show_guest_info.current_guest.type !== 0">卸载光驱</el-button>
						<el-button @click="show_attach_network_click(show_guest_info.current_guest)" type="primary" size="mini" v-if="show_guest_info.current_guest.type !== 0">添加网卡</el-button>
						<el-button @click="show_attach_volume_click(show_guest_info.current_guest)" type="primary" size="mini" v-if="show_guest_info.current_guest.type !== 0">挂载磁盘</el-button>

						<el-button @click="destroy_guest(show_guest_info.current_guest)" type="danger" size="mini">销毁虚拟机</el-button>
					</el-row>
					<el-row>
						<el-descriptions :column="2" size="medium" border>
							<el-descriptions-item label="ID">{{ show_guest_info.current_guest.guestId }}</el-descriptions-item>
							<el-descriptions-item label="虚拟机名">{{ show_guest_info.current_guest.description }}</el-descriptions-item>
							<el-descriptions-item label="总线类型">{{ show_guest_info.current_guest.busType }}</el-descriptions-item>
							<el-descriptions-item label="CPU">{{ show_guest_info.current_guest.cpu }}核</el-descriptions-item>
							<el-descriptions-item label="内存">{{ get_memory_desplay(show_guest_info.current_guest.memory) }}</el-descriptions-item>
							<el-descriptions-item label="配额">{{ show_guest_info.current_guest.speed }}</el-descriptions-item>
							<el-descriptions-item label="光盘">{{ show_guest_info.template.name }}</el-descriptions-item>
							<el-descriptions-item label="运行主机">
								<el-link :href="`/#/Host?id=${show_guest_info.host.hostId}`" type="primary" v-if="show_guest_info.host.hostId !== 0" :underline="false">{{ show_guest_info.host.displayName }}</el-link>
								<span v-if="show_guest_info.host.hostId === 0" :underline="false">{{ show_guest_info.host.displayName }}</span>
							</el-descriptions-item>
							<el-descriptions-item label="架构方案">
								<el-link :href="`/#/Scheme?id=${show_guest_info.scheme.schemeId}`" type="primary" v-if="show_guest_info.scheme.schemeId !== 0" :underline="false">{{ show_guest_info.scheme.name }}</el-link>
								<span v-if="show_guest_info.scheme.schemeId === 0" :underline="false">{{ show_guest_info.scheme.name }}</span>
							</el-descriptions-item>
							<el-descriptions-item label="虚拟机类型">
								<el-tag>{{ show_guest_info.current_guest.type === 0 ? '系统' : '用户' }}</el-tag>
							</el-descriptions-item>
							<el-descriptions-item label="虚拟机IP">{{ show_guest_info.current_guest.guestIp }}</el-descriptions-item>
							<el-descriptions-item label="状态">
								<el-tag :type="show_guest_info.current_guest.status === 2 ? 'success' : 'danger'">{{ get_guest_status(show_guest_info.current_guest) }}</el-tag>
							</el-descriptions-item>
						</el-descriptions>
					</el-row>
					<br />
					<el-row>
						<el-tabs type="border-card">
							<el-tab-pane label="磁盘">
								<el-table :v-loading="show_guest_info.volume_loading" :data="show_guest_info.volumes" style="width: 100%">
									<el-table-column label="ID" prop="volumeId" width="80" />
									<el-table-column label="描述" prop="description" width="200" />
									<el-table-column label="容量" prop="capacity" width="150">
										<template #default="scope">
											{{ get_volume_desplay_size(scope.row.capacity) }}
										</template>
									</el-table-column>
									<el-table-column label="已使用" prop="allocation" width="150">
										<template #default="scope">
											{{ get_volume_desplay_size(scope.row.allocation) }}
										</template>
									</el-table-column>
									<el-table-column label="路径" prop="path" show-overflow-tooltip />
									<el-table-column label="操作" width="180">
										<template #default="scope">
											<el-link type="primary" :href="`/#/Volume?id=${scope.row.volumeId}`">详情</el-link>
											<el-link style="margin-left: 10px" type="danger" @click="detach_volume_click(scope.row)" :disabled="scope.row.attach.deviceId === 0">卸载磁盘</el-link>
										</template>
									</el-table-column>
								</el-table>
							</el-tab-pane>
							<el-tab-pane label="网卡">
								<el-table :v-loading="show_guest_info.network_loading" :data="show_guest_info.networks" style="width: 100%">
									<el-table-column label="MAC" prop="mac" width="200" />
									<el-table-column label="IP" prop="ip" width="200" />
									<el-table-column label="驱动类型" prop="driveType" width="150" />
									<el-table-column label="操作">
										<template #default="scope">
											<el-link type="danger" @click="detach_network_click(scope.row)" :disabled="scope.row.deviceId === 0">卸载网卡</el-link>
										</template>
									</el-table-column>
								</el-table>
							</el-tab-pane>
						</el-tabs>
					</el-row>
				</el-card>
				<el-card class="box-card" v-show="this.show_type === 2">
					<el-row slot="header">
						<el-page-header @back="show_guest_list_page()" content="创建虚拟机" style="color: #409eff"></el-page-header>
					</el-row>
					<el-row>
						<el-form ref="createForm" :model="create_guest" label-width="100px" class="demo-ruleForm">
							<el-row>
								<el-col :span="12">
									<el-form-item label="名称" prop="description">
										<el-input v-model="create_guest.description"></el-input>
									</el-form-item>
								</el-col>
								<el-col :span="12">
									<el-form-item label="总线方式">
										<el-select v-model="create_guest.busType" style="width: 100%" placeholder="总线方式">
											<el-option label="virtio" value="virtio" />
											<el-option label="ide" value="ide" />
											<el-option label="scsi" value="scsi" />
										</el-select>
									</el-form-item>
								</el-col>
							</el-row>
							<el-row>
								<el-col :span="12">
									<el-form-item label="运行主机">
										<el-select v-model="create_guest.hostId" style="width: 100%">
											<el-option label="随机" :value="0"></el-option>
											<el-option v-for="item in this.hosts" :key="item.hostId" :label="item.displayName" :value="item.hostId" />
										</el-select>
									</el-form-item>
								</el-col>
								<el-col :span="12">
									<el-form-item label="架构">
										<el-select v-model="create_guest.schemeId" style="width: 100%" placeholder="请选择架构">
											<el-option v-for="item in this.schemes" :key="item.schemeId" :label="item.name" :value="item.schemeId" />
										</el-select>
									</el-form-item>
								</el-col>
							</el-row>
							<el-row>
								<el-col :span="12">
									<el-form-item label="网络">
										<el-select v-model="create_guest.networkId" style="width: 100%" placeholder="请选择网络">
											<el-option v-for="item in this.networks" :key="item.networkId" :label="item.name" :value="item.networkId" />
										</el-select>
									</el-form-item>
								</el-col>
								<el-col :span="12">
									<el-form-item label="网络驱动">
										<el-select v-model="create_guest.networkDeviceType" style="width: 100%" placeholder="请选择网卡驱动">
											<el-option label="virtio" value="virtio" />
											<el-option label="rtl8139" value="rtl8139" />
										</el-select>
									</el-form-item>
								</el-col>
							</el-row>
							<el-row>
								<el-col :span="12">
									<el-form-item label="安装方式">
										<el-select v-model="create_guest.type" style="width: 100%" placeholder="请选择安装方式">
											<el-option label="ISO镜像" :value="0" />
											<el-option label="模版安装" :value="1" />
											<el-option label="快照安装" :value="2" />
											<el-option label="现有磁盘" :value="3" />
										</el-select>
									</el-form-item>
								</el-col>
								<el-col :span="12">
									<el-form-item label="磁盘类型" v-if="create_guest.type !== 3">
										<el-select v-model="create_guest.volumeType" style="width: 100%">
											<el-option label="raw" value="raw"></el-option>
											<el-option label="qcow" value="qcow"></el-option>
											<el-option label="qcow2" value="qcow2"></el-option>
											<el-option label="vdi" value="vdi"></el-option>
											<el-option label="vmdk" value="vmdk"></el-option>
											<el-option label="vpc" value="vpc"></el-option>
										</el-select>
									</el-form-item>
								</el-col>
							</el-row>
							<el-row>
								<el-col :span="12">
									<el-form-item label="ISO模版" v-if="create_guest.type === 0">
										<el-select v-model="create_guest.isoTemplateId" style="width: 100%" placeholder="请选择光盘镜像">
											<el-option v-for="item in this.iso_template" :key="item.templateId" :label="item.name" :value="item.templateId" />
										</el-select>
									</el-form-item>
									<el-form-item label="系统模版" v-if="create_guest.type === 1">
										<el-select v-model="create_guest.diskTemplateId" style="width: 100%" placeholder="请选择模版">
											<el-option v-for="item in this.disk_template" :key="item.templateId" :label="item.name" :value="item.templateId" />
										</el-select>
									</el-form-item>
									<el-form-item label="快照模版" v-if="create_guest.type === 2">
										<el-select v-model="create_guest.snapshotVolumeId" style="width: 100%" placeholder="请选择系统快照">
											<el-option v-for="item in this.snapshot_template" :key="item.snapshotVolumeId" :label="item.name" :value="item.snapshotVolumeId" />
										</el-select>
									</el-form-item>
									<el-form-item label="可用磁盘" v-if="create_guest.type === 3">
										<el-select v-model="create_guest.volumeId" style="width: 100%" placeholder="请选择系统磁盘">
											<el-option v-for="item in this.attach_volumes" :key="item.volumeId" :label="item.description" :value="item.volumeId" />
										</el-select>
									</el-form-item>
								</el-col>
								<el-col :span="12">
									<el-form-item label="存储池" v-if="create_guest.type !== 3">
										<el-select v-model="create_guest.storageId" style="width: 100%">
											<el-option label="随机" :value="0"></el-option>
											<el-option v-for="item in this.storages" :key="item.storageId" :label="item.name" :value="item.storageId" />
										</el-select>
									</el-form-item>
								</el-col>
							</el-row>
							<el-form-item label="磁盘大小" v-if="create_guest.type === 0">
								<el-input v-model="create_guest.size"></el-input>
							</el-form-item>
							<el-form-item>
								<el-button type="primary" @click="create_guest_click">立即创建</el-button>
								<el-button @click="show_guest_list_page">取消</el-button>
							</el-form-item>
						</el-form>
					</el-row>
				</el-card>

				<el-card class="box-card" v-show="this.show_type === 3">
					<el-row slot="header">
						<el-page-header @back="show_guest_list_page()" content="重装虚拟机" style="color: #409eff"></el-page-header>
					</el-row>
					<el-row>
						<el-form ref="reInstallForm" :model="reinstall_guest" label-width="100px" class="demo-ruleForm">
							<el-row>
								<el-col :span="12">
									<el-form-item label="安装方式">
										<el-select v-model="reinstall_guest.type" style="width: 100%" placeholder="请选择安装方式">
											<el-option label="ISO镜像" :value="0" />
											<el-option label="模版安装" :value="1" />
											<el-option label="快照安装" :value="2" />
											<el-option label="现有磁盘" :value="3" />
										</el-select>
									</el-form-item>
								</el-col>
								<el-col :span="12">
									<el-form-item label="磁盘类型" v-if="reinstall_guest.type !== 3">
										<el-select v-model="reinstall_guest.volumeType" style="width: 100%">
											<el-option label="raw" value="raw"></el-option>
											<el-option label="qcow" value="qcow"></el-option>
											<el-option label="qcow2" value="qcow2"></el-option>
											<el-option label="vdi" value="vdi"></el-option>
											<el-option label="vmdk" value="vmdk"></el-option>
											<el-option label="vpc" value="vpc"></el-option>
										</el-select>
									</el-form-item>
								</el-col>
							</el-row>
							<el-row>
								<el-col :span="12">
									<el-form-item label="ISO模版" v-if="reinstall_guest.type === 0">
										<el-select v-model="reinstall_guest.isoTemplateId" style="width: 100%" placeholder="请选择光盘镜像">
											<el-option v-for="item in this.iso_template" :key="item.templateId" :label="item.name" :value="item.templateId" />
										</el-select>
									</el-form-item>
									<el-form-item label="系统模版" v-if="reinstall_guest.type === 1">
										<el-select v-model="reinstall_guest.diskTemplateId" style="width: 100%" placeholder="请选择模版">
											<el-option v-for="item in this.disk_template" :key="item.templateId" :label="item.name" :value="item.templateId" />
										</el-select>
									</el-form-item>
									<el-form-item label="快照模版" v-if="reinstall_guest.type === 2">
										<el-select v-model="reinstall_guest.snapshotVolumeId" style="width: 100%" placeholder="请选择系统快照">
											<el-option v-for="item in this.snapshot_template" :key="item.snapshotVolumeId" :label="item.name" :value="item.snapshotVolumeId" />
										</el-select>
									</el-form-item>
									<el-form-item label="可用磁盘" v-if="reinstall_guest.type === 3">
										<el-select v-model="reinstall_guest.volumeId" style="width: 100%" placeholder="请选择系统磁盘">
											<el-option v-for="item in this.attach_volumes" :key="item.volumeId" :label="item.description" :value="item.volumeId" />
										</el-select>
									</el-form-item>
								</el-col>
								<el-col :span="12">
									<el-form-item label="存储池" v-if="reinstall_guest.type !== 3">
										<el-select v-model="reinstall_guest.storageId" style="width: 100%">
											<el-option label="随机" :value="0"></el-option>
											<el-option v-for="item in this.storages" :key="item.storageId" :label="item.name" :value="item.storageId" />
										</el-select>
									</el-form-item>
								</el-col>
							</el-row>
							<el-form-item label="磁盘大小" v-if="reinstall_guest.type === 0">
								<el-input v-model="reinstall_guest.size"></el-input>
							</el-form-item>
							<el-form-item>
								<el-button type="primary" @click="reinstall_guest_click">立即重装</el-button>
								<el-button @click="show_guest_list_page">取消</el-button>
							</el-form-item>
						</el-form>
					</el-row>
				</el-card>
			</el-main>
		</el-container>

		<el-dialog title="启动虚拟机" :visible.sync="start_dialog_visiable" width="400px">
			<el-form :model="start_guest" label-width="100px">
				<el-form-item label="选择主机">
					<el-select v-model="start_guest.hostId" style="width: 100%">
						<el-option label="随机" :value="0"></el-option>
						<el-option v-for="item in this.hosts" :key="item.hostId" :label="item.displayName" :value="item.hostId" :v-loading="true" />
					</el-select>
				</el-form-item>
			</el-form>
			<span slot="footer" class="dialog-footer">
				<el-button @click="start_dialog_visiable = false">取 消</el-button>
				<el-button type="primary" @click="start_guest_click">确 定</el-button>
			</span>
		</el-dialog>
		<el-dialog title="关闭虚拟机" :visible.sync="stop_dialog_visiable" width="400px">
			<el-checkbox v-model="stop_guest.force">强制关闭</el-checkbox>
			<span slot="footer" class="dialog-footer">
				<el-button @click="stop_dialog_visiable = false">取 消</el-button>
				<el-button type="primary" @click="stop_guest_click">确 定</el-button>
			</span>
		</el-dialog>
		<el-dialog title="挂载光驱" :visible.sync="attach_cd_room_dialog_visiable" width="400px">
			<el-select v-model="attach_cd_room_guest.templateId" style="width: 100%" placeholder="请选择光盘镜像">
				<el-option v-for="item in this.iso_template" :key="item.templateId" :label="item.name" :value="item.templateId" />
			</el-select>
			<span slot="footer" class="dialog-footer">
				<el-button @click="attach_cd_room_dialog_visiable = false">取 消</el-button>
				<el-button type="primary" @click="attach_cd_room_guest_click">确 定</el-button>
			</span>
		</el-dialog>
		<el-dialog title="挂载网卡" :visible.sync="attach_network_dialog_visiable" width="400px">
			<el-form :model="start_guest" label-width="100px">
				<el-form-item label="网络">
					<el-select v-model="attach_network_guest.networkId" style="width: 100%" placeholder="请选择网络">
						<el-option v-for="item in this.networks" :key="item.networkId" :label="item.name" :value="item.networkId" />
					</el-select>
				</el-form-item>
				<el-form-item label="驱动">
					<el-select v-model="attach_network_guest.driveType" style="width: 100%" placeholder="请选择网卡驱动">
						<el-option label="virtio" value="virtio" />
						<el-option label="rtl8139" value="rtl8139" />
					</el-select>
				</el-form-item>
			</el-form>
			<span slot="footer" class="dialog-footer">
				<el-button @click="attach_network_dialog_visiable = false">取 消</el-button>
				<el-button type="primary" @click="attach_network_click">确 定</el-button>
			</span>
		</el-dialog>
		<el-dialog title="挂载磁盘" :visible.sync="attach_volume_dialog_visiable" width="400px">
			<el-form :model="start_guest" label-width="100px">
				<el-form-item label="磁盘">
					<el-select v-model="attach_volume_guest.volumeId" style="width: 100%" placeholder="请选择磁盘">
						<el-option v-for="item in this.attach_volumes" :key="item.volumeId" :label="item.description" :value="item.volumeId" />
					</el-select>
				</el-form-item>
			</el-form>
			<span slot="footer" class="dialog-footer">
				<el-button @click="attach_volume_dialog_visiable = false">取 消</el-button>
				<el-button type="primary" @click="attach_volume_click">确 定</el-button>
			</span>
		</el-dialog>
		<el-dialog title="修改配置" :visible.sync="modify_guest_dialog_visiable" width="400px">
			<el-form :model="start_guest" label-width="100px">
				<el-form-item label="名称" prop="description">
					<el-input v-model="modify_guest.description"></el-input>
				</el-form-item>
				<el-form-item label="总线方式">
					<el-select v-model="modify_guest.busType" style="width: 100%" placeholder="总线方式">
						<el-option label="virtio" value="virtio" />
						<el-option label="ide" value="ide" />
						<el-option label="scsi" value="scsi" />
					</el-select>
				</el-form-item>
				<el-form-item label="计算方案">
					<el-select v-model="modify_guest.schemeId" style="width: 100%" placeholder="请选择计算方案">
						<el-option v-for="item in this.schemes" :key="item.schemeId" :label="item.name" :value="item.schemeId" />
					</el-select>
				</el-form-item>
			</el-form>
			<span slot="footer" class="dialog-footer">
				<el-button @click="modify_guest_dialog_visiable = false">取 消</el-button>
				<el-button type="primary" @click="modify_guest_click">确 定</el-button>
			</span>
		</el-dialog>
	</div>
</template>
<script>
import { getStorageList, getGuestInfo, destroyGuest, createGuest, startGuest, rebootGuest, stopGuest, getHostList, detachGuestCdRoom, getTemplateList, attachGuestCdRoom, getTemplateInfo, getSchemeInfo, getHostInfo, getGuestNetworks, getGuestVolumes, getNetworkList, attachGuestNetwork, detachGuestNetwork, getNotAttachVolumeList, attachGuestDisk, detachGuestDisk, modifyGuest, getSchemeList, getSnapshotList, reInstallGuest, getUserGuestList, batchStoptGuest, batchStartGuest } from '@/api/api'
import Notify from '@/api/notify'
export default {
	name: 'guestView',
	components: {},
	data() {
		return {
			current_guest_id: 0,
			data_loading: false,
			start_dialog_visiable: false,
			stop_dialog_visiable: false,
			attach_cd_room_dialog_visiable: false,
			attach_network_dialog_visiable: false,
			attach_volume_dialog_visiable: false,
			modify_guest_dialog_visiable: false,
			current_loading: false,
			show_type: -1,
			show_guest_info: {
				guestId: 0,
				volume_loading: false,
				network_loading: false,
				current_guest: {},
				host: {
					hostId: 0,
					displayName: '-'
				},
				template: {
					templateId: 0,
					name: '-'
				},
				scheme: {
					schemeId: 0,
					name: '-'
				},
				volumes: [],
				networks: []
			},
			create_guest: {
				type: 0,
				description: '',
				busType: 'virtio',
				hostId: 0,
				schemeId: '',
				networkId: '',
				networkDeviceType: 'virtio',
				volumeType: 'qcow2',
				isoTemplateId: '',
				diskTemplateId: '',
				snapshotVolumeId: '',
				volumeId: '',
				storageId: 0,
				size: 100
			},
			reinstall_guest: {
				guestId: 0,
				type: 0,
				volumeType: 'qcow2',
				isoTemplateId: '',
				diskTemplateId: '',
				snapshotVolumeId: '',
				volumeId: '',
				storageId: 0,
				size: 100
			},
			start_guest: {
				hostId: 0,
				guestId: 0
			},
			stop_guest: {
				hostId: 0,
				force: false
			},
			attach_cd_room_guest: {
				guestId: 0,
				templateId: ''
			},
			attach_network_guest: {
				guestId: 0,
				networkId: '',
				driveType: 'virtio'
			},
			attach_volume_guest: {
				guestId: 0,
				volumeId: ''
			},
			modify_guest: {
				guestId: 0,
				busType: '',
				description: '',
				schemeId: ''
			},
			keyword: '',
			guests: [],
			storages: [],
			hosts: [],
			networks: [],
			iso_template: [],
			disk_template: [],
			snapshot_template: [],
			attach_volumes: [],
			schemes: [],
			select_guests: [],
			current_page: 1,
			page_size: 10,
			total_size: 0
		}
	},
	mixins: [Notify],

	mounted() {
		this.current_guest_id = this.$route.query.id
		if (this.current_guest_id) {
			this.show_type == 2
			this.current_loading = true
			getGuestInfo({ guestId: this.current_guest_id })
				.then((res) => {
					if (res.code === 0) {
						this.show_guest_info_click(res.data)
					} else {
						this.$alert(`获取虚拟机信息失败:${res.message}`, '提示', {
							dangerouslyUseHTMLString: true,
							confirmButtonText: '返回',
							type: 'error'
						}).then(() => {
							this.show_type = 0
						})
					}
				})
				.finally(() => {
					this.current_loading = false
				})
		} else {
			this.show_type = 0
		}
		this.init_view()
		this.init_notify()
	},
	computed: {
		show_table_guests() {
			return this.guests.filter((v) => {
				return v.isShow === undefined || v.isShow
			})
		}
	},
	methods: {
		handleSelectionChange(val) {
			this.select_guests = val
		},
		async init_view() {
			this.data_loading = true
			await getUserGuestList()
				.then((res) => {
					if (res.code == 0) {
						this.guests = res.data
						this.update_guest_show_page()
					}
				})
				.finally(() => {
					this.data_loading = false
				})
		},
		async load_all_schemes() {
			await getSchemeList().then((res) => {
				if (res.code === 0) {
					this.schemes = res.data
				}
			})
		},
		async load_all_attach_volumes() {
			await getNotAttachVolumeList().then((res) => {
				if (res.code === 0) {
					this.attach_volumes = res.data
				}
			})
		},
		async load_all_host() {
			await getHostList().then((res) => {
				if (res.code === 0) {
					this.hosts = res.data.filter((v) => v.status == 1)
				}
			})
		},
		async load_all_template() {
			await getTemplateList().then((res) => {
				if (res.code === 0) {
					this.iso_template = res.data.filter((v) => v.templateType == 0 && v.status === 2)
					this.disk_template = res.data.filter((v) => v.templateType == 2 && v.status === 2)
				}
			})
		},
		async load_all_storage() {
			await getStorageList().then((res) => {
				if (res.code == 0) {
					this.storages = res.data.filter((v) => v.status == 1)
				}
			})
		},
		async load_all_snapshot() {
			await getSnapshotList().then((res) => {
				if (res.code == 0) {
					this.snapshot_template = res.data.filter((v) => v.status == 1)
				}
			})
		},
		async load_all_networks() {
			await getNetworkList().then((res) => {
				if (res.code === 0) {
					this.networks = res.data.filter((v) => v.status === 2)
				}
			})
		},
		async load_current_guest_template(guest) {
			this.show_guest_info.template.templateId = guest.cdRoom
			this.show_guest_info.template.name = '-'
			if (this.show_guest_info.template.templateId > 0) {
				await getTemplateInfo({ templateId: guest.cdRoom }).then((res) => {
					if (res.code === 0) {
						this.show_guest_info.template.name = res.data.name
					}
				})
			}
		},
		async load_current_guest_scheme(guest) {
			this.show_guest_info.scheme.schemeId = guest.schemeId
			this.show_guest_info.scheme.name = '-'
			if (this.show_guest_info.scheme.schemeId > 0) {
				await getSchemeInfo({ schemeId: guest.schemeId }).then((res) => {
					if (res.code === 0) {
						this.show_guest_info.scheme.name = res.data.name
					}
				})
			}
		},
		async load_current_guest_host(guest) {
			this.show_guest_info.host.hostId = guest.hostId
			this.show_guest_info.host.displayName = '-'
			if (this.show_guest_info.host.hostId > 0) {
				await getHostInfo({ hostId: guest.hostId }).then((res) => {
					if (res.code === 0) {
						this.show_guest_info.host.displayName = res.data.displayName
					}
				})
			}
		},
		load_current_guest_volume(guest) {
			this.show_guest_info.volume_loading = true
			getGuestVolumes({ guestId: guest.guestId })
				.then((res) => {
					if (res.code === 0) {
						this.show_guest_info.volumes = res.data
					}
				})
				.finally(() => {
					this.show_guest_info.volume_loading = false
				})
		},
		load_current_guest_network(guest) {
			this.show_guest_info.network_loading = true
			getGuestNetworks({ guestId: guest.guestId })
				.then((res) => {
					if (res.code === 0) {
						this.show_guest_info.networks = res.data
					}
				})
				.finally(() => {
					this.show_guest_info.network_loading = false
				})
		},
		on_current_page_change(current_page) {
			this.current_page = current_page
			this.update_guest_show_page()
		},
		on_page_size_change(page_size) {
			this.page_size = page_size
			this.update_guest_show_page()
		},
		update_guest_show_page() {
			let nCount = 0
			this.guests.forEach((item, index) => {
				let hasKeyword = true
				let searchKeyword = this.keyword.trim().toLowerCase()
				if (searchKeyword !== '') {
					hasKeyword = '' + item.guestId === searchKeyword || item.description.toLowerCase().indexOf(searchKeyword) >= 0 || item.name.toLowerCase().indexOf(searchKeyword) >= 0 || item.guestIp.toLowerCase().indexOf(searchKeyword) >= 0
				}
				if (hasKeyword) {
					nCount++
					if (nCount <= this.page_size * (this.current_page - 1) || nCount > this.page_size * this.current_page) {
						item.isShow = false
					} else {
						item.isShow = true
					}
				} else {
					item.isShow = false
				}
				this.$set(this.guests, index, item)
			})
			this.total_size = nCount
		},
		async update_guest_info(guest) {
			let findIndex = this.guests.findIndex((item) => item.guestId === guest.guestId)
			if (findIndex >= 0) {
				this.$set(this.guests, findIndex, guest)
			} else if (guest.type === 1) {
				//只处理用户系统
				let index = this.page_size * (this.current_page - 1)
				this.guests.splice(index, 0, guest)
			}
			if (this.show_guest_info.guestId === guest.guestId) {
				console.log('update guest', guest.status)
				this.show_guest_info.current_guest = guest
				await this.load_current_guest_template(guest)
				await this.load_current_guest_host(guest)
				await this.load_current_guest_scheme(guest)
				this.$forceUpdate()
			}
		},
		get_memory_desplay(memory) {
			if (memory > 1024 * 1024) {
				return (memory / (1024 * 1024)).toFixed(2) + ' GB'
			} else if (memory > 1024) {
				return (memory / 1024).toFixed(2) + '  MB'
			}
		},
		get_volume_desplay_size(size) {
			if (size > 1024 * 1024 * 1024 * 1024) {
				return (size / (1024 * 1024 * 1024 * 1024)).toFixed(2) + ' TB'
			} else if (size > 1024 * 1024 * 1024) {
				return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
			} else if (size > 1024 * 1024) {
				return (size / (1024 * 1024)).toFixed(2) + ' MB'
			} else if (size > 1024) {
				return (size / 1024).toFixed(2) + '  KB'
			} else {
				return size + '  bytes'
			}
		},
		get_guest_status(guest) {
			switch (guest.status) {
				case 0:
					return '正在创建'
				case 1:
					return '正在启动'
				case 2:
					return '正在运行'
				case 3:
					return '正在停止'
				case 4:
					return '已停止'
				case 5:
					return '重启中'
				case 6:
					return '虚拟机错误'
				default:
					return `未知状态[${guest.status}]`
			}
		},
		handle_notify_message(notify) {
			if (notify.type === 1) {
				getGuestInfo({ guestId: notify.id }).then((res) => {
					if (res.code == 0) {
						this.update_guest_info(res.data)
					} else if (res.code == 2000001) {
						let findIndex = this.guests.findIndex((v) => v.guestId === notify.id)
						if (findIndex >= 0) {
							this.guests.splice(findIndex, 1)
						}
					}
				})
			}
		},
		show_guest_list_page() {
			this.show_type = 0
		},
		async show_create_guest_click() {
			this.load_all_attach_volumes()
			this.load_all_host()
			this.load_all_template()
			this.load_all_storage()
			this.load_all_schemes()
			this.load_all_networks()
			this.load_all_snapshot()

			if (this.$refs['createForm']) {
				this.$refs['createForm'].resetFields()
			}
			this.create_guest.isoTemplateId = ''
			this.create_guest.diskTemplateId = ''
			this.create_guest.snapshotVolumeId = ''
			this.create_guest.volumeId = ''
			this.create_guest.type = 0
			this.show_type = 2
		},
		create_guest_click() {
			switch (this.create_guest.type) {
				case 0:
					this.create_guest.diskTemplateId = 0
					this.create_guest.snapshotVolumeId = 0
					this.create_guest.volumeId = 0
					break
				case 1:
					this.create_guest.isoTemplateId = 0
					this.create_guest.snapshotVolumeId = 0
					this.create_guest.volumeId = 0
					this.create_guest.size = 0
					break
				case 2:
					this.create_guest.isoTemplateId = 0
					this.create_guest.diskTemplateId = 0
					this.create_guest.volumeId = 0
					this.create_guest.size = 0
					break
				case 3:
					this.create_guest.isoTemplateId = 0
					this.create_guest.diskTemplateId = 0
					this.create_guest.snapshotVolumeId = 0
					this.create_guest.size = 0
					break
			}
			createGuest(this.create_guest).then((res) => {
				if (res.code === 0) {
					this.update_guest_info(res.data)
					this.show_type = 0
				} else {
					this.$notify.error({
						title: '错误',
						message: `创建虚拟机失败:${res.message}`
					})
				}
			})
		},
		reinstall_guest_click() {
			switch (this.reinstall_guest.type) {
				case 0:
					this.reinstall_guest.diskTemplateId = 0
					this.reinstall_guest.snapshotVolumeId = 0
					this.reinstall_guest.volumeId = 0
					break
				case 1:
					this.reinstall_guest.isoTemplateId = 0
					this.reinstall_guest.snapshotVolumeId = 0
					this.reinstall_guest.volumeId = 0
					this.reinstall_guest.size = 0
					break
				case 2:
					this.reinstall_guest.isoTemplateId = 0
					this.reinstall_guest.diskTemplateId = 0
					this.reinstall_guest.volumeId = 0
					this.reinstall_guest.size = 0
					break
				case 3:
					this.reinstall_guest.isoTemplateId = 0
					this.reinstall_guest.diskTemplateId = 0
					this.reinstall_guest.snapshotVolumeId = 0
					this.reinstall_guest.size = 0
					break
			}
			this.$confirm('重装虚拟机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					reInstallGuest(this.reinstall_guest).then((res) => {
						if (res.code === 0) {
							this.update_guest_info(res.data)
							this.show_type = 0
						} else {
							this.$notify.error({
								title: '错误',
								message: `重装虚拟机失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		reboot_guest_click(guest) {
			this.$confirm('重启当前虚拟机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					rebootGuest({ guestId: guest.guestId }).then((res) => {
						if (res.code === 0) {
							this.update_guest_info(res.data)
						} else {
							this.$notify.error({
								title: '错误',
								message: `重启虚拟机失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		start_guest_click() {
			startGuest(this.start_guest).then((res) => {
				if (res.code === 0) {
					this.update_guest_info(res.data)
					this.start_dialog_visiable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `启动虚拟机失败:${res.message}`
					})
				}
			})
		},
		stop_guest_click() {
			this.$confirm('停止当前虚拟机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					stopGuest(this.stop_guest).then((res) => {
						if (res.code === 0) {
							this.update_guest_info(res.data)
							this.stop_dialog_visiable = false
						} else {
							this.$notify.error({
								title: '错误',
								message: `启动虚拟机失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		detach_guest_cd_room_click(guest) {
			detachGuestCdRoom({ guestId: guest.guestId }).then((res) => {
				if (res.code === 0) {
					this.update_guest_info(res.data)
					this.stop_dialog_visiable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `卸载虚拟机光驱失败:${res.message}`
					})
				}
			})
		},
		attach_cd_room_guest_click() {
			attachGuestCdRoom(this.attach_cd_room_guest).then((res) => {
				if (res.code === 0) {
					this.update_guest_info(res.data)
					this.attach_cd_room_dialog_visiable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `挂载虚拟机光驱失败:${res.message}`
					})
				}
			})
		},
		attach_network_click() {
			attachGuestNetwork(this.attach_network_guest).then((res) => {
				if (res.code === 0) {
					this.update_guest_info(res.data.guest)
					this.show_guest_info.networks.push(res.data.network)
					this.attach_network_dialog_visiable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: `虚拟机附加网卡失败:${res.message}`
					})
				}
			})
		},
		detach_network_click(guestNetwork) {
			this.$confirm('卸载当前挂载网卡, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					detachGuestNetwork({ guestId: guestNetwork.guestId, guestNetworkId: guestNetwork.guestNetworkId }).then((res) => {
						if (res.code === 0) {
							this.update_guest_info(res.data)
							let findIndex = this.show_guest_info.networks.findIndex((item) => item.guestNetworkId === guestNetwork.guestNetworkId)
							if (findIndex >= 0) {
								this.show_guest_info.networks.splice(findIndex, 1)
							}
						} else {
							this.$notify.error({
								title: '错误',
								message: `虚拟机附加网卡失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		detach_volume_click(guestVolume) {
			this.$confirm('卸载当前挂载磁盘, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					let attach = guestVolume.attach
					detachGuestDisk({ guestId: attach.guestId, guestDiskId: attach.guestDiskId }).then((res) => {
						if (res.code === 0) {
							this.update_guest_info(res.data)
							let findIndex = this.show_guest_info.volumes.findIndex((v) => v.attach.guestDiskId === attach.guestDiskId)
							if (findIndex >= 0) {
								this.show_guest_info.volumes.splice(findIndex, 1)
							}
						} else {
							this.$notify.error({
								title: '错误',
								message: `卸载磁盘失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		attach_volume_click() {
			attachGuestDisk(this.attach_volume_guest).then((res) => {
				if (res.code === 0) {
					this.update_guest_info(res.data.guest)
					this.show_guest_info.volumes.push(res.data.volume)
					this.attach_volume_dialog_visiable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: ` 挂载磁盘失败:${res.message}`
					})
				}
			})
		},
		modify_guest_click() {
			modifyGuest(this.modify_guest).then((res) => {
				if (res.code === 0) {
					this.update_guest_info(res.data)
					this.modify_guest_dialog_visiable = false
				} else {
					this.$notify.error({
						title: '错误',
						message: ` 修改主机信息失败:${res.message}`
					})
				}
			})
		},
		menu_command_click(data) {
			switch (data.command) {
				case 'info':
					this.show_guest_info_click(data.guest)
					break
				case 'start':
					this.show_start_guest_click(data.guest)
					break
				case 'reboot':
					this.reboot_guest_click(data.guest)
					break
				case 'stop':
					this.show_stop_guest_click(data.guest)
					break
				case 'detach_cd':
					this.detach_guest_cd_room_click(data.guest)
					break
				case 'attach_cd':
					this.show_attach_cd_room_click(data.guest)
					break
				case 'vnc':
					this.vnc_click(data.guest)
					break
				case 'destroy':
					this.destroy_guest(data.guest)
					break
			}
		},
		async show_guest_info_click(guest) {
			this.show_guest_info.guestId = guest.guestId
			this.show_guest_info.current_guest = guest
			await this.load_current_guest_template(guest)
			await this.load_current_guest_host(guest)
			await this.load_current_guest_scheme(guest)
			await this.load_current_guest_volume(guest)
			await this.load_current_guest_network(guest)
			this.show_type = 1
		},
		async show_reinstall_guest_click(guest) {
			this.load_all_attach_volumes()
			this.load_all_template()
			this.load_all_storage()
			this.load_all_snapshot()
			if (this.$refs['reInstallForm']) {
				this.$refs['reInstallForm'].resetFields()
			}
			this.reinstall_guest.guestId = guest.guestId
			this.reinstall_guest.isoTemplateId = ''
			this.reinstall_guest.diskTemplateId = ''
			this.reinstall_guest.snapshotVolumeId = ''
			this.reinstall_guest.volumeId = ''
			this.reinstall_guest.type = 0
			this.show_type = 3
		},
		async show_start_guest_click(guest) {
			await this.load_all_host()
			this.start_guest.guestId = guest.guestId
			this.start_guest.hostId = 0
			this.start_dialog_visiable = true
		},
		async show_attach_network_click(guest) {
			await this.load_all_networks()
			this.attach_network_guest.guestId = guest.guestId
			this.attach_network_guest.networkId = ''
			this.attach_network_dialog_visiable = true
		},
		async show_attach_volume_click(guest) {
			await this.load_all_attach_volumes()
			this.attach_volume_guest.guestId = guest.guestId
			this.attach_volume_guest.volumeId = ''
			this.attach_volume_dialog_visiable = true
		},
		show_stop_guest_click(guest) {
			this.stop_guest.guestId = guest.guestId
			this.stop_guest.force = false
			this.stop_dialog_visiable = true
		},
		show_attach_cd_room_click(guest) {
			this.load_all_template()
			this.attach_cd_room_guest.guestId = guest.guestId
			this.attach_cd_room_guest.templateId = ''
			this.attach_cd_room_dialog_visiable = true
		},
		show_modify_guest_click(guest) {
			this.load_all_schemes()
			this.modify_guest.guestId = guest.guestId
			this.modify_guest.description = guest.description
			this.modify_guest.schemeId = guest.schemeId
			this.modify_guest.busType = guest.busType
			this.modify_guest_dialog_visiable = true
		},
		vnc_click(guest) {
			let { href } = this.$router.resolve({ path: '/Vnc', query: { id: guest.guestId, description: guest.description } })
			window.open(href, '_blank')
		},
		destroy_guest(guest) {
			this.$confirm('删除当前虚拟机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					destroyGuest({ guestId: guest.guestId }).then((res) => {
						if (res.code === 0) {
							let findIndex = this.guests.findIndex((v) => v.guestId === guest.guestId)
							if (findIndex >= 0) {
								this.guests.splice(findIndex, 1)
							}
							if (this.show_type === 1) {
								this.show_type = 0
							}
						} else {
							this.$notify.error({
								title: '错误',
								message: `删除虚拟机失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		batch_start_guest_click() {
			this.$confirm('启动所选虚拟机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					let guestIds = this.select_guests.map((v) => v.guestId).join(',')
					batchStartGuest({ guestIds: guestIds }).then((res) => {
						if (res.code === 0) {
							res.data.filter((guest) => {
								this.update_guest_info(guest)
							})
						} else {
							this.$notify.error({
								title: '错误',
								message: `批量启动虚拟机失败:${res.message}`
							})
						}
					})
				})
				.catch(() => {})
		},
		batch_stop_guest_click() {
			this.$confirm('停止所选虚拟机, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					let guestIds = this.select_guests.map((v) => v.guestId).join(',')
					batchStoptGuest({ guestIds: guestIds }).then((res) => {
						if (res.code === 0) {
							res.data.filter((guest) => {
								this.update_guest_info(guest)
							})
						} else {
							this.$notify.error({
								title: '错误',
								message: `批量停止虚拟机失败:${res.message}`
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
.el-dropdown {
	vertical-align: top;
}
.el-dropdown + .el-dropdown {
	margin-left: 15px;
}
.el-icon-arrow-down {
	font-size: 12px;
}
</style>