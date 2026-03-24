package cn.chenjun.cloud.management.util;

import cn.chenjun.cloud.common.util.MapUtil;
import cn.hutool.core.io.FileUtil;
import com.github.stephenc.javaisotools.iso9660.ISO9660File;
import com.github.stephenc.javaisotools.iso9660.ISO9660RootDirectory;
import com.github.stephenc.javaisotools.iso9660.impl.CreateISO;
import com.github.stephenc.javaisotools.iso9660.impl.ISO9660Config;
import com.github.stephenc.javaisotools.iso9660.impl.ISOImageFileHandler;
import com.github.stephenc.javaisotools.joliet.impl.JolietConfig;
import com.github.stephenc.javaisotools.sabre.impl.ByteArrayDataReference;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class CloudInitIsoGenerator {

    private static final String VOLUME_ID = "CIDATA";


    @SneakyThrows
    public String generateCloudInitImage(String userData, String metaData, String vendorData, String networkConfig) {

        ISO9660RootDirectory root = new ISO9660RootDirectory();
        Map<String, String> dataReferencesMap = MapUtil.of("user-data", userData, "meta-data", metaData, "vendor-data", vendorData, "network-config", networkConfig);

        for (Map.Entry<String, String> entry : dataReferencesMap.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (ObjectUtils.isEmpty(value)) {
                continue;
            }
            root.addFile(new ISO9660File(new ByteArrayDataReference(value.getBytes(StandardCharsets.UTF_8)), name, System.currentTimeMillis()));
        }
        File outfile = File.createTempFile(UUID.randomUUID().toString(), ".iso");
        try {
            CreateISO iso = new CreateISO(new ISOImageFileHandler(outfile), root);
            ISO9660Config iso9660Config = new ISO9660Config();
            iso9660Config.setVolumeID(VOLUME_ID);
            iso9660Config.setVolumeSetID(VOLUME_ID);

            JolietConfig jolietConfig = new JolietConfig();
            jolietConfig.setVolumeID(VOLUME_ID);
            jolietConfig.setVolumeSetID(VOLUME_ID);
            iso.process(iso9660Config, null, jolietConfig, null);
            byte[] isoBuffer = FileUtil.readBytes(outfile);
            return Base64.getEncoder().encodeToString(isoBuffer);
        } finally {
            outfile.deleteOnExit();
        }
    }
}