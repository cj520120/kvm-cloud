package com.roamblue.cloud.common.agent;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandResultModel implements Serializable {
    @SerializedName("enable")
    private boolean enable;
    private String name;
    @SerializedName("success-response")
    private boolean response;
}
