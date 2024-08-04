import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class Demo {
    @Test
    public void test(){
        List<CompletableFuture<Integer>> FutureTaskList = new ArrayList<>();

        FutureTaskList.add( CompletableFuture.supplyAsync(() -> {
            Function<Integer,Integer> temp = (x) -> {
                return x + 1;
            };
            // 并行执行一些操作
            return 1;
        }));
        FutureTaskList.add( CompletableFuture.supplyAsync(() -> {
            return 2;
        }));
        CompletableFuture<Integer>[] array = FutureTaskList.stream().toArray(size -> new CompletableFuture[size]);
        CompletableFuture<Void> allFuture = CompletableFuture.allOf(array);
        allFuture.thenRun(()->{
            try {
                allFuture.get(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        }).join();
        Integer now = FutureTaskList.get(0).getNow(100);
        System.out.println(now);
    }
}
