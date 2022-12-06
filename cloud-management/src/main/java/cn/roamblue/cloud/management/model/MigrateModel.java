package cn.roamblue.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MigrateModel {
    private VolumeModel source;
    private VolumeModel migrate;
}
