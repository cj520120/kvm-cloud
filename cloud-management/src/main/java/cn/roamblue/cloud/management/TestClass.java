package cn.roamblue.cloud.management;

import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.data.mapper.TemplateMapper;
import cn.roamblue.cloud.management.data.mapper.TemplateVolumeMapper;
import cn.roamblue.cloud.management.data.mapper.VolumeMapper;
import cn.roamblue.cloud.management.operate.bean.MigrateVolumeOperate;
import cn.roamblue.cloud.management.task.OperateTask;
import cn.roamblue.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class TestClass {
    @Autowired
    private OperateTask operateTask;
    @Autowired
    private VolumeMapper volumeMapper;
    @Autowired
    private TemplateMapper templateMapper;
    @Autowired
    private TemplateVolumeMapper templateVolumeMapper;

    @PostConstruct
    public void test() {
        String volumeName=UUID.randomUUID().toString();
//       创建存储池
//        CreateStorageOperate operate= CreateStorageOperate.builder().taskId(UUID.randomUUID().toString()).storageId(1).build()
//       创建磁盘
//        VolumeEntity volume=VolumeEntity.builder()
//                .clusterId(1)
//                .storageId(1)
//                .name(volumeName)
//                .path("/mnt/TEST_NFS"+"/"+volumeName)
//                .type(Constant.VolumeType.RAW)
//                .allocation(1024*1024*1024)
//                .capacity(0)
//                .templateId(0)
//                .status(cn.roamblue.cloud.management.util.Constant.VolumeStatus.CREATING)
//                .build();
//        volumeMapper.insert(volume);
//        CreateVolumeOperate operate =CreateVolumeOperate.builder().volumeId(1).taskId(UUID.randomUUID().toString()).build(
        // 克隆磁盘
//        VolumeEntity volume=this.volumeMapper.selectById(1);
//        volume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.CLONE);
//        this.volumeMapper.updateById(volume);
//        CloneVolumeOperate operate=CloneVolumeOperate.builder().taskId(UUID.randomUUID().toString()).sourceVolumeId(1).targetStorageId(1).targetName(volumeName).targetPath("/mnt/TEST_NFS"+"/"+volumeName).targetType(Constant.VolumeType.VDI).build();

        //下载模版
//        TemplateEntity entity= TemplateEntity.builder()
//                .uri("http://192.168.1.2:9000/soft/CentOS-7-x86_64-Minimal-2003.iso")
//                .type(Constant.TemplateType.ISO  )
//                .name("CentOS-7-x86_64-Minimal-2003.iso")
//                .status(cn.roamblue.cloud.management.util.Constant.TemplateStatus.DOWNLOAD)
//                .build();
//        this.templateMapper.delete(new QueryWrapper<>());
//        this.templateMapper.insert(entity);
//        TemplateVolumeEntity templateVolume = TemplateVolumeEntity.builder()
//                .storageId(1)
//                .clusterId(1)
//                .templateId(entity.getTemplateId())
//                .name(volumeName)
//                .path("/mnt/TEST_NFS"+"/"+volumeName)
//                .type(cn.roamblue.cloud.common.util.Constant.VolumeType.RAW)
//                .status(Constant.TemplateStatus.DOWNLOAD)
//                .build();
//        this.templateVolumeMapper.delete(new QueryWrapper<>());
//        this.templateVolumeMapper.insert(templateVolume);
//        DownloadTemplateOperate operate=DownloadTemplateOperate.builder().taskId(UUID.randomUUID().toString()).templateVolumeId(templateVolume.getTemplateId()).build();
//
// 迁移磁盘
//        VolumeEntity volume=this.volumeMapper.selectOne(new QueryWrapper<VolumeEntity>().last("limit 0,1"));
//        volume.setStatus(Constant.VolumeStatus.MIGRATE);
//        this.volumeMapper.updateById(volume);
//        MigrateVolumeOperate operate= MigrateVolumeOperate.builder()
//                .taskId(UUID.randomUUID().toString())
//                .sourceVolumeId(volume.getVolumeId())
//                .targetStorageId(1)
//                .targetName(volumeName)
//                .targetPath("/mnt/TEST_NFS"+"/"+volumeName)
//                .targetType(cn.roamblue.cloud.common.util.Constant.VolumeType.RAW)
//                .build();
//        operateTask.addTask(operate);
    }
}
