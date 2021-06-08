package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.TemplateInfo;
import com.roamblue.cloud.management.bean.TemplateRefInfo;

import java.util.List;

public interface TemplateService {
    /**
     * @return
     */
    List<TemplateInfo> listTemplates();

    List<TemplateInfo> search(int clusterId);

    /**
     * @return
     */
    List<TemplateInfo> listTemplateByClusterId(int clusterId);

    /**
     * 根据ID获取
     *
     * @param id
     * @return
     */
    TemplateInfo findTemplateById(int id);

    /**
     * 创建
     *
     * @return
     */
    TemplateInfo createTemplate(int clusterId, int osCategoryId, String name, String type, String uri);

    /**
     * 销毁
     *
     * @return
     */
    void destroyTemplateById(int id);


    List<TemplateRefInfo> listTemplateRefByTemplateId(int id);

}
