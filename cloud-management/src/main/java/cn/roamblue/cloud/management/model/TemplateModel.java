package cn.roamblue.cloud.management.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateModel {
    private int templateId;
    private String name;
    private String uri;
    private int templateType;
    private String volumeType;
    private int status;
    private Date createTime;
}
