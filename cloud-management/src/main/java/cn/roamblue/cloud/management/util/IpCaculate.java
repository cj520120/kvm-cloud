package cn.roamblue.cloud.management.util;

import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenjun
 */
public class IpCaculate {

    private final static int MAX_COMPANY_LIST_LEN = 1200;
    private final static String COMPANY_LIST_STRING = "00000C#00000E#000075#000095#0000F0#000102#000103#000130#000142#000143#00014A#000163#000164#000181#000196#000197#0001C7#0001C9#0001E6#0001E7#000216#000217#00024A#00024B#00025F#000278#00027D#00027E#0002A5#0002B3#0002B9#0002BA#0002DC#0002EE#0002FC#0002FD#000331#000332#000342#000347#00034B#00036B#00036C#00037F#000393#00039F#0003A0#0003E3#0003E4#0003FE#00040B#00041F#000423#000427#000428#000438#00044D#00044E#000456#00045A#00046B#00046D#00046E#000480#000496#00049A#00049B#0004BD#0004C0#0004C1#0004DC#0004DD#0004DE#0004E2#0004EA#000500#000501#000502#00051A#000531#000532#00055D#00055E#00055F#000573#000574#000585#00059A#00059B#0005B5#0005DC#0005DD#000625#000628#00062A#000652#000653#00065B#00067C#00068C#0006C1#0006D6#0006D7#00070D#00070E#00074D#00074F#000750#000772#000784#000785#0007B3#0007B4#0007E0#0007E9#0007EB#0007EC#000802#00080E#000820#000821#000874#00087C#00087D#000883#00089A#0008A3#0008A4#0008C2#0008C7#0008E2#0008E3#000911#000912#000918#000943#000944#00097B#00097C#000997#0009B6#0009B7#0009E8#0009E9#000A04#000A27#000A28#000A41#000A42#000A57#000A5E#000A8A#000A8B#000A95#000AB7#000AB8#000AD9#000AE0#000AF3#000AF4#000AF7#000B06#000B0E#000B45#000B46#000B5D#000B5F#000B60#000B85#000B86#000BAC#000BBE#000BBF#000BC5#000BCD#000BDB#000BE1#000BFC#000BFD#000C30#000C31#000C41#000C43#000C85#000C86#000CCE#000CCF#000CDB#000CE5#000CE6#000CF1#000CF7#000CF8#000D0B#000D28#000D29#000D54#000D56#000D57#000D65#000D66#000D88#000D93#000D9D#000DAE#000DBC#000DBD#000DE5#000DEC#000DED#000E07#000E08#000E0C#000E35#000E38#000E39#000E40#000E5C#000E62#000E6A#000E7F#000E83#000E84#000E86#000EB3#000EC0#000EC7#000ED6#000ED7#000EED#000F06#000F20#000F23#000F24#000F34#000F35#000F3D#000F61#000F62#000F66#000F6A#000F8F#000F90#000F9F#000FB5#000FBB#000FC3#000FCB#000FCD#000FDE#000FF7#000FF8#001007#00100B#00100D#001011#001014#001018#00101F#001029#00102F#001040#001045#00104B#001054#001055#00105A#001079#00107B#001083#00108C#0010A6#0010B3#0010DB#0010E3#0010F6#0010FA#0010FF#00110A#001111#00111A#001120#001121#001124#00113F#001143#001150#001158#00115C#00115D#001180#001185#00118B#001192#001193#001195#00119F#0011AE#0011BB#0011BC#0011F9#001200#001201#001217#00121E#001225#001237#00123F#001243#001244#001247#00124B#001262#001279#00127F#001280#001283#00128A#0012A9#0012C9#0012D1#0012D2#0012D9#0012DA#0012EE#0012F0#0012F2#0012FB#001302#00130A#001310#001315#001319#00131A#001320#001321#001346#001349#00135F#001360#001365#001370#001371#001372#001374#001377#00137F#001380#001392#0013A9#0013C3#0013C4#0013CE#0013E8#0013F7#0013FD#001404#00140D#00140E#00141B#00141C#001422#001438#00143E#001451#001469#00146A#00146C#00147C#00149A#0014A7#0014A8#0014A9#0014BF#0014C2#0014C7#0014D5#0014E8#0014F1#0014F2#0014F6#001500#001517#00152A#00152B#00152C#00152F#00153F#001540#001560#001562#001563#001570#001599#00159A#00159B#0015A0#0015A8#0015B9#0015C1#0015C5#0015C6#0015C7#0015DE#0015E8#0015E9#0015F9#0015FA#001601#001620#001626#001632#001635#001646#001647#00164D#00164E#001660#00166B#00166C#00166F#001675#001676#00169C#00169D#0016B5#0016B6#0016B8#0016BC#0016C7#0016C8#0016CA#0016CB#0016DB#0016E0#0016EA#0016EB#0016F0#001700#001708#00170E#00170F#00173F#001742#00174B#001759#00175A#001765#00177C#001783#001784#001794#001795#00179A#0017A4#0017B0#0017C9#0017CB#0017CC#0017D1#0017D5#0017DF#0017E0#0017E2#0017E3#0017E4#0017E5#0017E6#0017E7#0017E8#0017E9#0017EA#0017EB#0017EC#0017EE#0017F2#00180F#001813#001818#001819#00182F#001830#001831#001832#001833#001834#001839#001842#00184D#001868#00186E#001871#001873#001874#001882#00188B#00188D#0018A4#0018AF#0018B0#0018B9#0018BA#0018C0#0018C5#0018DE#0018F8#0018FE#001906#001907#00192C#00192D#00192F#001930#001947#00194F#001955#001956#00195B#00195E#001963#001969#001979#00198F#001992#001999#0019A6#0019A9#0019AA#0019B7#0019B9#0019BB#0019C0#0019C5#0019CB#0019D1#0019D2#0019E1#0019E2#0019E3#0019E7#0019E8#001A16#001A1B#001A1E#001A2F#001A30#001A4B#001A66#001A6C#001A6D#001A70#001A75#001A77#001A80#001A89#001A8A#001A8F#001AA0#001AA1#001AA2#001AAD#001AC1#001ADB#001ADC#001ADE#001AE2#001AE3#001AF0#001B0C#001B0D#001B11#001B21#001B25#001B2A#001B2B#001B2F#001B33#001B52#001B53#001B54#001B59#001B63#001B77#001B78#001B8F#001B90#001B98#001BAF#001BBA#001BC0#001BD4#001BD5#001BD7#001BDD#001BE9#001BED#001BEE#001C0E#001C0F#001C10#001C11#001C12#001C17#001C23#001C35#001C43#001C57#001C58#001C8E#001C9A#001C9C#001CA4#001CB0#001CB1#001CB3#001CBF#001CC0#001CC1#001CC4#001CC5#001CD4#001CD6#001CDF#001CEB#001CF0#001CF6#001CF9#001CFB#001D09#001D0D#001D25#001D28#001D2E#001D3B#001D42#001D45#001D46#001D4C#001D4F#001D6B#001D6E#001D70#001D71#001D73#001D7E#001D98#001DA1#001DA2#001DAF#001DB5#001DBA#001DBE#001DE0#001DE1#001DE5#001DE6#001DE9#001DF6#001DFD#001DFE#001E0B#001E10#001E13#001E14#001E1F#001E2A#001E3A#001E3B#001E45#001E46#001E49#001E4A#001E4F#001E52#001E58#001E5A#001E64#001E65#001E67#001E6B#001E79#001E7A#001E7D#001E7E#001E8D#001EA3#001EA4#001EA8#001EBD#001EBE#001EC1#001EC2#001EC9#001ECA#001EDC#001EE1#001EE2#001EE5#001EF6#001EF7#001F00#001F01#001F0A#001F12#001F26#001F27#001F29#001F33#001F3B#001F3C#001F41#001F46#001F5B#001F5C#001F5D#001F6C#001F6D#001F7E#001F9A#001F9D#001F9E#001FA7#001FC4#001FC9#001FCA#001FCC#001FCD#001FDA#001FDE#001FDF#001FE4#001FF3#002032#002040#002060#002075#00207B#0020A6#0020AF#0020D8#0020DA#002105#002108#002109#002119#00211B#00211C#00211E#002129#002135#002136#002143#00214C#002155#002156#002159#00215A#00215C#00215D#002162#00216A#00216B#002170#002180#002191#00219B#00219E#0021A0#0021A1#0021AA#0021AB#0021AE#0021BA#0021BE#0021D1#0021D2#0021D7#0021D8#0021E1#0021E9#0021FC#0021FE#00220C#00220D#002210#002219#00222D#00223A#00223F#002241#002255#002256#002257#002264#002265#002266#002267#00226B#002275#00227F#002283#002290#002291#002298#0022A1#0022A5#0022A6#0022B0#0022B4#0022BD#0022BE#0022CE#0022FA#0022FB#0022FC#0022FD#002304#002305#00230B#00230D#002312#002314#002315#002326#002332#002333#002334#002339#00233A#00233E#002345#00235D#00235E#002368#002369#00236C#002374#002375#00237D#002395#002399#00239C#0023A2#0023A3#0023AB#0023AC#0023AE#0023AF#0023B4#0023BE#0023C2#0023C6#0023D3#0023D4#0023D6#0023D7#0023DF#0023EA#0023EB#0023ED#0023EE#0023F1#0023F8#002400#002401#002403#002404#002413#002414#002436#002437#002438#002443#002450#002451#002454#00246C#002473#00247C#00247D#00247F#002481#002482#00248D#002490#002491#002492#002493#002495#002497#002498#0024A0#0024A1#0024A5#0024B5#0024BA#0024BE#0024C1#0024C3#0024C4#0024D6#0024D7#0024DC#0024E8#0024E9#0024EF#0024F7#0024F9#002500#00252E#002538#002545#002546#002547#002548#00254B#002564#002566#002567#002568#002583#002584#00259C#00259E#0025B3#0025B4#0025B5#0025BA#0025BC#0025BD#0025C3#0025C4#0025CF#0025D0#0025E7#0025F1#0025F2#002608#00260A#00260B#002636#002637#00263E#002641#002642#00264A#002651#002652#002654#002655#00265A#00265D#00265F#002668#002669#002688#002698#002699#0026B0#0026B9#0026BA#0026BB#0026C6#0026C7#0026CA#0026CB#0026CC#0026F3#003005#003019#00301E#003024#003040#003065#00306E#003071#003078#00307B#003080#003085#003094#003096#0030A3#0030B6#0030BD#0030C1#0030F2#004001#00400B#004027#004043#004096#005004#00500B#00500F#005014#00502A#00503E#005043#005050#005053#005054#005073#005080#00508B#005099#0050A2#0050A7#0050BA#0050BD#0050D1#0050DA#0050E2#0050E3#0050E4#0050F0#006008#006009#00602F#006038#00603E#006047#00605C#006070#006083#00608C#006097#0060B0#0060CF#008021#008039#00805F#00809F#0080A0#0080C8#009004#00900C#009021#009027#00902B#00905F#009069#00906D#00906F#009086#00908E#009092#00909C#0090A6#0090AB#0090B1#0090BF#0090CF#0090D9#0090F2#00A024#00A040#00A077#00A081#00A08E#00A0BF#00A0C5#00A0C6#00A0C9#00A0CA#00A0F8#00AA00#00AA01#00AA02#00B04A#00B064#00B08E#00B0C2#00B0D0#00C04F#00C0BE#00C0F9#00D006#00D058#00D063#00D079#00D088#00D090#00D095#00D096#00D097#00D0B7#00D0BA#00D0BB#00D0BC#00D0C0#00D0D3#00D0D8#00D0E4#00D0F6#00D0FF#00E000#00E003#00E00C#00E014#00E01E#00E02B#00E034#00E04F#00E052#00E064#00E06F#00E08F#00E0A3#00E0B0#00E0B1#00E0DA#00E0F7#00E0F9#00E0FC#00E0FE#02608C#02C08C#080007#080009#080028#080046#08004E";

    public static List<String> parseIpRange(String ipfrom, String ipto) {
        List<String> ips = new ArrayList<String>();
        String[] ipfromd = ipfrom.split("\\.");
        String[] iptod = ipto.split("\\.");
        int[] intIpf = new int[4];
        int[] intIpt = new int[4];
        for (int i = 0; i < 4; i++) {
            intIpf[i] = Integer.parseInt(ipfromd[i]);
            intIpt[i] = Integer.parseInt(iptod[i]);
        }
        for (int i = intIpf[0]; i <= intIpt[0]; i++) {
            for (int j = (i == intIpf[0] ? intIpf[1] : 0); j <= (i == intIpt[0] ? intIpt[1]
                    : 255); j++) {
                for (int k = (j == intIpf[1] ? intIpf[2] : 0); k <= (j == intIpt[1] ? intIpt[2]
                        : 255); k++) {
                    for (int l = (k == intIpf[2] ? intIpf[3] : 0); l <= (k == intIpt[2] ? intIpt[3]
                            : 255); l++) {
                        ips.add(i + "." + j + "." + k + "." + l);
                    }
                }
            }
        }
        return ips;
    }

    /**
     * 根据掩码位数计算掩码
     *
     * @param maskIndex 掩码位
     * @return 子网掩码
     */
    public static String getNetMask(String maskIndex) {
        StringBuilder mask = new StringBuilder();
        Integer inetMask = 0;
        try {
            inetMask = Integer.parseInt(maskIndex);
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
            return null;
        }
        if (inetMask > 32) {
            return null;
        }
        // 子网掩码为1占了几个字节
        int num1 = inetMask / 8;
        // 子网掩码的补位位数
        int num2 = inetMask % 8;
        int[] array = new int[4];
        for (int i = 0; i < num1; i++) {
            array[i] = 255;
        }
        for (int i = num1; i < 4; i++) {
            array[i] = 0;
        }
        for (int i = 0; i < num2; num2--) {
            array[num1] += 1 << 8 - num2;
        }
        for (int i = 0; i < 4; i++) {
            if (i == 3) {
                mask.append(array[i]);
            } else {
                mask.append(array[i] + ".");
            }
        }
        return mask.toString();
    }

    /**
     * 根据网段计算起始IP 网段格式:x.x.x.x/x
     * 一个网段0一般为网络地址,255一般为广播地址.
     * 起始IP计算:网段与掩码相与之后加一的IP地址
     *
     * @param segment 网段
     * @return 起始IP
     */
    public static String getStartIp(String segment) {
        StringBuffer startIp = new StringBuffer();
        if (segment == null) {
            return null;
        }
        String[] arr = segment.split("/");
        String ip = arr[0];
        String maskIndex = arr[1];
        String mask = IpCaculate.getNetMask(maskIndex);
        if (4 != ip.split("\\.").length || mask == null) {
            return null;
        }
        int[] ipArray = new int[4];
        int[] netMaskArray = new int[4];
        for (int i = 0; i < 4; i++) {
            try {
                ipArray[i] = Integer.parseInt(ip.split("\\.")[i]);
                netMaskArray[i] = Integer.parseInt(mask.split("\\.")[i]);
                if (ipArray[i] > 255 || ipArray[i] < 0 || netMaskArray[i] > 255 || netMaskArray[i] < 0) {
                    return null;
                }
                ipArray[i] = ipArray[i] & netMaskArray[i];
                if (i == 3) {
                    startIp.append(ipArray[i] + 1);
                } else {
                    startIp.append(ipArray[i] + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
            }
        }
        return startIp.toString();
    }

    public static String getBroadcastAddr(String subnet) {
        String broadcast = "";
        String[] addresses = subnet.split("/")[0].split("\\.");
        String[] masks = getNetMask(subnet.split("/")[1]).split("\\.");
        for (int i = 0; i < 4; i++) {
            int opmaskSegement = ~Integer.parseInt(masks[i]) & 0xFF;
            int netSegment = Integer.parseInt(addresses[i]) & Integer.parseInt(masks[i]);
            broadcast += (opmaskSegement | netSegment) + ".";
        }
        return broadcast.substring(0, broadcast.length() - 1);
    }

    /**
     * 根据网段计算结束IP
     *
     * @param segment
     * @return 结束IP
     */
    public static String getEndIp(String segment) {
        StringBuffer endIp = new StringBuffer();
        String startIp = getStartIp(segment);
        if (segment == null) {
            return null;
        }
        String[] arr = segment.split("/");
        String maskIndex = arr[1];
        //实际需要的IP个数
        int hostNumber = 0;
        int[] startIpArray = new int[4];
        try {
            hostNumber = 1 << 32 - (Integer.parseInt(maskIndex));
            for (int i = 0; i < 4; i++) {
                startIpArray[i] = Integer.parseInt(startIp.split("\\.")[i]);
                if (i == 3) {
                    startIpArray[i] = startIpArray[i] - 1;
                    break;
                }
            }
            startIpArray[3] = startIpArray[3] + (hostNumber - 1);
        } catch (NumberFormatException e) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "网段错误");
        }

        if (startIpArray[3] > 255) {
            int k = startIpArray[3] / 256;
            startIpArray[3] = startIpArray[3] % 256;
            startIpArray[2] = startIpArray[2] + k;
        }
        if (startIpArray[2] > 255) {
            int j = startIpArray[2] / 256;
            startIpArray[2] = startIpArray[2] % 256;
            startIpArray[1] = startIpArray[1] + j;
            if (startIpArray[1] > 255) {
                int k = startIpArray[1] / 256;
                startIpArray[1] = startIpArray[1] % 256;
                startIpArray[0] = startIpArray[0] + k;
            }
        }
        for (int i = 0; i < 4; i++) {
            if (i == 3) {
                startIpArray[i] = startIpArray[i] - 1;
            }
            if ("".equals(endIp.toString()) || endIp.length() == 0) {
                endIp.append(startIpArray[i]);
            } else {
                endIp.append("." + startIpArray[i]);
            }
        }
        return endIp.toString();
    }


    public static String getMacAddrWithFormat(String split) {
        String mac = getCompanyMacAddrPart() + getRandomMacAddrPart();

        String outMac = "";
        for (int i = 0; i < mac.length(); ) {
            outMac += mac.charAt(i++);

            if (0 == i % 2 && i < mac.length()) {
                outMac += split;
            }
        }
        return outMac;
    }

    private static String getCompanyMacAddrPart() {
        String[] compListArray = COMPANY_LIST_STRING.split("#");
        int index = (int) Math.round(Math.random() * (compListArray.length - 1) + 0);
        return compListArray[index];
    }

    private static String getRandomMacAddrPart() {
        String baseMacSeed = "0123456789ABCDEF";
        String wapsMacAddr = "";
        for (int i = 0; i < 3; i++) {
            wapsMacAddr += getRandomByteStr(baseMacSeed);
        }
        return wapsMacAddr;
    }

    private static String getRandomByteStr(String macSeed) {
        int h = (int) Math.round(Math.random() * 15 + 0);
        int l = (int) Math.round(Math.random() * 15 + 0);
        String byteStr = String.format("%c%c", macSeed.charAt(h),
                macSeed.charAt(l));
        return byteStr;
    }
}
