package cn.roamblue.cloud.management.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ClassName: ApplicaionConfig
 * @Create by: chenjun
 * @Date: 2021/7/30 上午11:51
 */
@Data
@Component
@ConfigurationProperties(prefix = "roamblue.cloud")
public class ApplicaionConfig {
	/**
	 * 管理端时区
	 */
	private String timeZone="Asia/Shanghai";
	/**
	 * VM 停止超时时间（秒)
	 */
    private int stopTimeout = 180;
    /**
     * 主机检测时间间隔（秒)
     */
    private int hostCheckInterval=10;
    /**
     * 主机监控数据清理周期（秒)
     */
    private int vmStatsCleanInterval=600;
    /**
     * 主机监控数据过期周期(天)
     */
    private int vmStatsExpireDay=1;
    /**
     * 主机存储持检测周期(秒)
     */
    private int hostStorageCheckInterval=10; 
    /**
     * 管理端心跳间隔(秒)
     */
    private int managerKeepInterval=10; 
    /**
     * Route检测间隔(秒)
     */
    private int routeCheckInterval=5; 
    /**
     * Console(VNC)检测间隔(秒)
     */
    private int consoleCheckInterval = 5;
    /**
     * 模版下载检测间隔(秒)
     */
    private int templateCheckInterval = 5;
    /**
     * 虚拟机运行状态检测间隔(秒)
     */
    private int vmStatusCheckInterval = 10;
    /**
     * 虚拟机销毁检测间隔(秒)
     */
    private int vmDestroyCheckInterval = 60;
    /**
     * 虚拟机销毁清理时长(秒)
     */
    private int vmDestroyExpireSeconds = 1800;
    /**
     * VM状态数据统计时间间隔(秒)
     */
    private int vmStatsCheckInterval = 5;
    /**
     * 磁盘数据检测时间间隔（秒)
     */
    private int volumeCheckInterval = 600;
    /**
     * 磁盘销毁检测时间间隔（秒)
     */
    private int volumeDestroyCheckInterval = 60;
    /**
     * 磁盘销毁清理时长(秒)
     */
    private int volumeDestroyExpireSeconds = 1800;
    /**
     * 默认CPU数量
     */
    private int systemCpu = 1;
    /**
     * 默认内存
     */
    private int systemMemory = 512;
    /**
     * 默认每个套接字核心数
     */
    private int systemCpuCore = 1;
    /**
     * 默认CPU套接字数量
     */  
    private int systemCpuSocket=1;    
    /**
     * 默认CPU频率
     */
    private int systemCpuSpeed=1000; 
    /**
     * 默认CPU超线程数
     */
    private int systemCpuThread=1;
    /**
     * JWT 密码
     */
    private String jwtPassword="#$1fa)&*WS09";
    /**
     * ISSUser
     */
    private String jwtIssuer="Roamblue Cloud Management";
}
