package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.OsCategoryInfo;

import java.util.List;

public interface OsCategoryService {
    OsCategoryInfo findOsCategoryById(int id);

    List<OsCategoryInfo> listAllOsCategory();
}
