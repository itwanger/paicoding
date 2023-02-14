package com.github.paicoding.forum.test;

import com.github.paicoding.forum.core.util.DateUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.function.BiConsumer;

/**
 * @author YiHui
 * @date 2022/8/6
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
}
