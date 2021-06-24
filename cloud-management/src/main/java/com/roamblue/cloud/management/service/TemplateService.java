package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.TemplateInfo;
import com.roamblue.cloud.management.bean.TemplateRefInfo;

import java.util.List;

public interface TemplateService {
    /**
     * 获取模版列表
     *
     * @return
     */
    List<TemplateInfo> listTemplates();

    /**
     * 搜索模版
     *
     * @param clusterId
     * @return
     */
    List<TemplateInfo> search(int clusterId);

    /**
     * 获取集群的模版列表
     *
     * @return
     */
    List<TemplateInfo> listTemplateByClusterId(int clusterId);

    /**
     * 根据ID获取模版
     *
     * @param id
     * @return
     */
    TemplateInfo findTemplateById(int id);

    /**
     * 创建模版
     *
     * @return
     */
    TemplateInfo createTemplate(int clusterId, int osCategoryId, String name, String type, String uri);

    /**
     * 销毁模版
     *
     * @return
     */
    void destroyTemplateById(int id);

    /**
     * 获取模版下载信息
     *
     * @param id
     * @return
     */
    List<TemplateRefInfo> listTemplateRefByTemplateId(int id);

}
