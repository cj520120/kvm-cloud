package com.roamblue.cloud.management.bean;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ApiModel("系统类型")
public class OsCategoryInfo {

    private int id;
    private String categoryName;
    private String networkDriver;
    private String diskDriver;
}
