package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.TemplateInfo;

import java.util.List;

public interface TemplateUiService {
    ResultUtil<List<TemplateInfo>> listTemplates();

    ResultUtil<List<TemplateInfo>> search(int clusterId);

    ResultUtil<TemplateInfo> findTemplateById(int id);

    ResultUtil<TemplateInfo> createTemplate(int clusterId, int osCategoryId, String name, String type, String uri);


    ResultUtil<Void> destroyTemplate(int id);
}
