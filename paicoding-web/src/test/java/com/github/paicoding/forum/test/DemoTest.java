package com.github.paicoding.forum.test;

import com.github.paicoding.forum.core.util.DateUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class DemoTest {

    @Test
    public void testTime() {
        long now = System.currentTimeMillis();
        LocalDateTime local = DateUtil.time2LocalTime(now);
        System.out.println(local);

        System.out.println(DateUtil.time2utc(now));
        System.out.println("over");
    }

    public static void scan(int maxX, int maxY, BiConsumer<Integer, Integer> consumer) {
        for (int i = 0; i < maxX; i++) {
            for (int j = 0; j < maxY; j++) {
                consumer.accept(i, j);
            }
        }
    }

    public static <T> T scanReturn(int x, int y, ScanProcess<T> func) {
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                ImmutablePair<Boolean, T> ans = func.accept(i, j);
                if (ans != null && ans.left) {
                    return ans.right;
                }
            }
        }
        return null;
    }

    @FunctionalInterface
    public interface ScanProcess<T> {
        ImmutablePair<Boolean, T> accept(int i, int j);
    }

    public static <T> T scanReturn(int x, int y, ScanFunc<T> func) {
        Ans<T> ans = new Ans<>();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                func.accept(i, j, ans);
                if (ans.tag) {
                    return ans.ans;
                }
            }
        }
        return null;
    }

    public interface ScanFunc<T> {
        void accept(int i, int j, Ans<T> ans);
    }

    public static class Ans<T> {
        private T ans;
        private boolean tag = false;

        public Ans<T> setAns(T ans) {
            tag = true;
            this.ans = ans;
            return this;
        }

        public T getAns() {
            return ans;
        }
    }

    @Test
    public void testScan() {
        int[][] cells = new int[][]{{1, 2, 3, 4}, {11, 12, 13, 14}, {21, 22, 23, 24}};
        scan(cells.length, cells[0].length, (i, j) -> {
            System.out.println(cells[i][j]);
        });

        String ans = scanReturn(cells.length, cells[0].length, (i, j) -> cells[i][j] % 2 == 0 ?
                ImmutablePair.of(true, "\"index:\" " + i + " + \"_\" " + j + ";") :
                null);
        System.out.println(ans);
    }

    @Test
    public void getEven() {
        int[][] cells = new int[][]{{1, 2, 3, 4}, {11, 12, 13, 14}, {21, 22, 23, 24}};
        String ans = scanReturn(cells.length, cells[0].length, (i, j, a) -> {
            if ((cells[i][j] & 1) == 0) {
                a.setAns(i + "_" + j);
            }
        });
        System.out.println(ans);
    }

    public int tt(List<Integer> ans) {
        try {
            System.out.println("tt 开始执行!!!");
            Thread.sleep(2000);
            System.out.println("tt 结束了!");
            ans.add(10);
            return 10;
        } catch (Exception e) {
            e.printStackTrace();
            ans.add(-1);
            return -1;
        }
    }

    @Test
    public void futureTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        List<Integer> list = new ArrayList<>();
        Future<Integer> ans = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return tt(list);
            }
        });
        try {
            ans.get(1, SECONDS);
        } catch (TimeoutException e) {
            System.out.println("超时异常了！");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread.sleep(3000);
        System.out.println("结束了!!!" + list);

    }
}
