package com.baidu.rim.guofeng007.okhttpdns;

import android.text.TextUtils;

import java.net.InetAddress;
import java.util.LinkedList;

public class IPUtils {


    public static int ipToInt(String ipAddress) throws Exception {
        int result = 0;
        String[] ipAddressInArray = ipAddress.split("\\.");
        for (int i = 3; i >= 0; i--) {
            int ip = Integer.parseInt(ipAddressInArray[3 - i]);
            // left shifting 24,16,8,0 and bitwise OR
            // 1. 192 << 24
            // 1. 168 << 16
            // 1. 1 << 8
            // 1. 2 << 0
            result |= ip << (i * 8);

        }
        return result;
    }

    public static byte[] intToBytes(int ipAddress) {
        byte[] result = new byte[4];
        result[3] = (byte) (ipAddress & 0xFF);
        result[2] = (byte) ((ipAddress >> 8) & 0xFF);
        result[1] = (byte) ((ipAddress >> 16) & 0xFF);
        result[0] = (byte) ((ipAddress >> 24) & 0xFF);
        return result;
    }


    public static int bytesToInt(byte[] i) {
        return ((i[3] << 24) & 0xFF) |
                ((i[2] << 16) & 0xFF) |
                ((i[1] << 8) & 0xFF) |
                ((i[0] & 0xFF));

    }


    public static String intToIp(int i) {

        return ((i >> 24) & 0xFF) +
                "." + ((i >> 16) & 0xFF) +
                "." + ((i >> 8) & 0xFF) +
                "." + (i & 0xFF);

    }

    public static LinkedList<InetAddress> strToInetAddress(String host, String ipList){
        if(TextUtils.isEmpty(ipList)){
            return null;
        }
        LinkedList<InetAddress> list = new LinkedList<>();
        String[] splits = ipList.split(";");
        for (String split : splits) {
            split = split.trim();
            String[] split1 = split.split("\\.");
            if(split1.length != 4){
                // not valid ip
                continue;
            }
            int ipInt = 0;
            try {
                ipInt = ipToInt(split);
                byte[] ipBytes = intToBytes(ipInt);
                InetAddress byAddress = InetAddress.getByAddress(host, ipBytes);
                list.add(byAddress);
            } catch (Exception e) {
                // e.printStackTrace();
                // continue next
            }
        }
        return list;
    }

}