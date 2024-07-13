package com.github.paicoding.forum.test.basic;

import cn.hutool.core.lang.Snowflake;
import com.github.paicoding.forum.core.util.IpUtil;
import org.junit.Test;

import java.net.SocketException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class IpTest {

    @Test
    public void test() throws SocketException {
        String ip = IpUtil.getLocalIp4Address();
        System.out.println(ip);
        System.out.println(IpUtil.getLocationByIp("121.40.134.96").toRegionStr());
    }

    @Test
    public void snowFlake() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.NOVEMBER, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date date = calendar.getTime();

        Snowflake snowflake = new Snowflake(date, 0, 0, true);
        int i = 0;
        while (true) {
            Long id = snowflake.nextId();
            System.out.println(id);
            ++i;
            if (i > 100) {
                break;
            }
        }
    }
}
