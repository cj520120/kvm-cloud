package cn.chenjun.cloud.management.util;

import cn.chenjun.cloud.management.servcie.bean.SubnetNetwork;

import java.util.ArrayList;
import java.util.List;

public class SubnetCalculator {


    /**
     * 传入 子网 + 掩码，计算所有子网信息
     */
    public static SubnetNetwork calculate(String subnet, String mask) {
        try {
            long ipLong = ipToLong(subnet);
            long maskLong = ipToLong(mask);

            long network = ipLong & maskLong;             // 子网地址
            long broadcast = network | (~maskLong & 0xFFFFFFFFL); // 广播地址
            long gateway = network + 1;                 // 网关地址
            long firstIp = network + 1;                   // 起始可用IP
            long lastIp = broadcast - 1;                  // 结束可用IP
            long hostCount = (broadcast - network - 1);  // 可用主机数
            SubnetNetwork result = SubnetNetwork.builder()
                    .subnet(longToIp(network))
                    .mask(longToIp(maskLong))
                    .firstIp(longToIp(firstIp))
                    .lastIp(longToIp(lastIp))
                    .broadcast(longToIp(broadcast))
                    .gateway(longToIp(gateway))
                    .hostCount(hostCount)
                    .build();
            System.out.println("===== 子网计算结果 =====");
            System.out.println(result);
            return result;

        } catch (Exception e) {
            System.out.println("IP 格式错误，请检查输入！");
            throw new RuntimeException(e);
        }
    }

    // IP字符串 转 长整型
    public static long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        long res = 0;
        for (String part : parts) {
            res = (res << 8) | Integer.parseInt(part);
        }
        return res & 0xFFFFFFFFL;
    }

    // 长整型 转 IP字符串
    public static String longToIp(long val) {
        return ((val >> 24) & 0xFF) + "." +
                ((val >> 16) & 0xFF) + "." +
                ((val >> 8) & 0xFF) + "." +
                (val & 0xFF);
    }

    public static List<String> listRangeIps(String startIp, String lastIp) {
        List<String> ipList = new ArrayList<>();

        long start = ipToLong(startIp);
        long end = ipToLong(lastIp);

        // 确保顺序正确
        if (start > end) {
            long temp = start;
            start = end;
            end = temp;
        }

        for (long ip = start; ip <= end; ip++) {
            ipList.add(longToIp(ip));
        }

        return ipList;
    }
}