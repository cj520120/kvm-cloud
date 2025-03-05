package cn.chenjun.cloud.management.servcie.convert.impl;

import cn.chenjun.cloud.management.servcie.convert.bean.VCpuTune;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class VCpuTuneConvert extends BaseJsonConvert<List<VCpuTune>> {
    public static final VCpuTuneConvert Default = new VCpuTuneConvert();

    protected VCpuTuneConvert() {
        super(new TypeToken<List<VCpuTune>>() {
        }.getType());
    }
}
