package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.StorageOperate;
import cn.chenjun.cloud.agent.operate.annotation.DispatchBind;
import cn.chenjun.cloud.agent.util.DomainXmlUtil;
import cn.chenjun.cloud.agent.util.StorageUtil;
import cn.chenjun.cloud.agent.util.TemplateUtil;
import cn.chenjun.cloud.common.bean.StorageCreateRequest;
import cn.chenjun.cloud.common.bean.StorageDestroyRequest;
import cn.chenjun.cloud.common.bean.StorageInfo;
import cn.chenjun.cloud.common.bean.StorageInfoRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.hubspot.jinjava.Jinjava;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.Secret;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class StorageOperateImpl implements StorageOperate {


    @DispatchBind(command = Constant.Command.STORAGE_INFO)
    @Override
    public StorageInfo getStorageInfo(Connect connect, StorageInfoRequest request) throws Exception {
        StoragePool storagePool = StorageUtil.findStorage(connect, request.getName(), false);
        if (storagePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在:" + request.getName());
        }
        StoragePoolInfo storagePoolInfo = storagePool.getInfo();
        return StorageInfo.builder().name(request.getName())
                .state(storagePoolInfo.state.toString())
                .capacity(storagePoolInfo.capacity)
                .allocation(storagePoolInfo.allocation)
                .available(storagePoolInfo.available)
                .build();
    }

    @DispatchBind(command = Constant.Command.BATCH_STORAGE_INFO)
    @Override
    public List<StorageInfo> batchStorageInfo(Connect connect, List<StorageInfoRequest> batchRequest) throws Exception {
        List<StorageInfo> list = new ArrayList<>();
        for (StorageInfoRequest request : batchRequest) {
            StoragePool storagePool = StorageUtil.findStorage(connect, request.getName(), false);
            StorageInfo model = null;
            if (storagePool != null) {
                StoragePoolInfo storagePoolInfo = storagePool.getInfo();
                model = StorageInfo.builder().name(request.getName())
                        .state(storagePoolInfo.state.toString())
                        .capacity(storagePoolInfo.capacity)
                        .allocation(storagePoolInfo.allocation)
                        .available(storagePoolInfo.available)
                        .build();
            }
            list.add(model);

        }
        return list;
    }

    @DispatchBind(command = Constant.Command.STORAGE_CREATE)
    @Override
    public StorageInfo create(Connect connect, StorageCreateRequest request) throws Exception {
        synchronized (request.getName().intern()) {
            StoragePool storagePool = StorageUtil.findStorage(connect, request.getName(), true);
            if (storagePool == null) {
                String createTemplateXml;
                Map<String, Object> templateRenderMap = new HashMap<>();
                Jinjava jinjava = TemplateUtil.create();
                switch (request.getType()) {
                    case Constant.StorageType.NFS: {
                        String nfsUri = request.getParam().get("uri").toString();
                        String nfsPath = request.getParam().get("path").toString();
                        FileUtil.mkdir(request.getMountPath());
                        createTemplateXml = ResourceUtil.readUtf8Str("tpl/nfs_storage.xml");
                        templateRenderMap.put("name", request.getName());
                        templateRenderMap.put("uri", nfsUri);
                        templateRenderMap.put("path", nfsPath);
                        templateRenderMap.put("mount", request.getMountPath());

                    }
                    break;
                    case Constant.StorageType.GLUSTERFS: {
                        String glusterfsUri = request.getParam().get("uri").toString();
                        String volume = request.getParam().get("volume").toString();
                        createTemplateXml = ResourceUtil.readUtf8Str("tpl/glusterfs_storage.xml");
                        Map<String, Object> map = new HashMap<>(4);
                        templateRenderMap.put("name", request.getName());
                        templateRenderMap.put("hostList", DomainXmlUtil.parseUrlList(glusterfsUri, "24007"));
                        templateRenderMap.put("volume", volume);
                        templateRenderMap.put("mount", request.getMountPath());
                    }
                    break;
                    case Constant.StorageType.CEPH_RBD: {
                        List<Map<String, String>> hostList = DomainXmlUtil.parseUrlList(request.getParam().get("uri").toString(), "6789");
                        String pool = request.getParam().get("pool").toString();
                        String username = request.getParam().get("username").toString();
                        String secretValue = request.getParam().get("secret").toString();
                        boolean hasSecret = Arrays.asList(connect.listSecrets()).contains(request.getName());
                        Secret secret;
                        if (!hasSecret) {
                            Map<String, String> secretMap = new HashMap<>();
                            secretMap.put("id", request.getName());
                            secretMap.put("type", "ceph");
                            secretMap.put("username", username);
                            String xml = ResourceUtil.readUtf8Str("tpl/ceph_rbd_secret.xml");
                            xml = jinjava.render(xml, secretMap);
                            secret = connect.secretDefineXML(xml);
                            log.info("创建 secret:{} xml={}", request.getName(), xml);
                        } else {
                            secret = connect.secretLookupByUUIDString(request.getName());
                        }
                        secret.setValue(Base64.decode(secretValue));
                        log.info("update secret[{}] value:{}", request.getName(), secretValue);
                        createTemplateXml = ResourceUtil.readUtf8Str("tpl/ceph_rbd_storage.xml");
                        templateRenderMap.put("name", request.getName());
                        templateRenderMap.put("pool", pool);
                        templateRenderMap.put("username", username);
                        templateRenderMap.put("hostList", hostList);

                    }
                    break;
                    default:
                        throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的存储池类型:" + request.getType());
                }
                createTemplateXml = jinjava.render(createTemplateXml, templateRenderMap);
                log.info("create {} storage {}", request.getType(), createTemplateXml);
                storagePool = connect.storagePoolDefineXML(createTemplateXml, 0);
                storagePool.setAutostart(1);
                if (storagePool.isActive() == 0) {
                    storagePool.create(0);
                }
            }
            StoragePoolInfo storagePoolInfo = storagePool.getInfo();
            return StorageInfo.builder().name(request.getName())
                    .state(storagePoolInfo.state.toString())
                    .capacity(storagePoolInfo.capacity)
                    .allocation(storagePoolInfo.allocation)
                    .available(storagePoolInfo.available)
                    .build();
        }
    }

    @DispatchBind(command = Constant.Command.STORAGE_DESTROY)
    @Override
    public Void destroy(Connect connect, StorageDestroyRequest request) throws Exception {
        synchronized (request.getName().intern()) {
            StoragePool storagePool = StorageUtil.findStorage(connect, request.getName(), true);
            if (storagePool != null) {
                storagePool.destroy();
                storagePool.undefine();
            }
            if (Objects.equals(request.getType(), Constant.StorageType.CEPH_RBD)) {
                try {
                    connect.secretLookupByUUIDString(request.getName()).undefine();
                } catch (Exception err) {

                }
            }
            return null;
        }

    }
}
