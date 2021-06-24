package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.TemplateInfo;

import java.util.List;

public interface TemplateUiService {
    /**
     * 获取模版列表
     *
     * @return
     */
    ResultUtil<List<TemplateInfo>> listTemplates();

    /**
     * 搜索模版
     *
     * @param clusterId
     * @return
     */
    ResultUtil<List<TemplateInfo>> search(int clusterId);

    /**
     * 根据ID获取模版信息
     *
     * @param id
     * @return
     */
    ResultUtil<TemplateInfo> findTemplateById(int id);

    /**
     * 创建模版信息
     *
     * @param clusterId
     * @param osCategoryId
     * @param name
     * @param type
     * @param uri
     * @return
     */
    ResultUtil<TemplateInfo> createTemplate(int clusterId, int osCategoryId, String name, String type, String uri);

    /**
     * 销毁模版信息
     *
     * @param id
     * @return
     */
    ResultUtil<Void> destroyTemplate(int id);
}
