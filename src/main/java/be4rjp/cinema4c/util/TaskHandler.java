package be4rjp.cinema4c.util;

import be4rjp.cinema4c.Cinema4C;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TaskHandler<T> extends BukkitRunnable {
    
    
    public static <U> CompletableFuture<U> supplySync(Supplier<U> supplier){
        CompletableFuture<U> completableFuture = new CompletableFuture<>();
        new TaskHandler<>(completableFuture, supplier, false).runTask(Cinema4C.getPlugin());
        
        return completableFuture;
    }
    
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier){
        CompletableFuture<U> completableFuture = new CompletableFuture<>();
        new TaskHandler<>(completableFuture, supplier, true).runTaskAsynchronously(Cinema4C.getPlugin());
        
        return completableFuture;
    }
    
    public static void runSync(Runnable runnable){
        Bukkit.getScheduler().runTask(Cinema4C.getPlugin(), runnable);
    }
    
    public static void runAsync(Runnable runnable){
        Bukkit.getScheduler().runTaskAsynchronously(Cinema4C.getPlugin(), runnable);
    }
    
    
    private final CompletableFuture<T> completableFuture;
    private final Supplier<T> supplier;
    private final boolean isAsync;
    
    private TaskHandler(CompletableFuture<T> completableFuture, Supplier<T> supplier, boolean isAsync){
        this.completableFuture = completableFuture;
        this.supplier = supplier;
        this.isAsync = isAsync;
    }
    
    @Override
    public void run() {
        T result = supplier.get();
        
        Runnable runnable = () -> completableFuture.complete(result);
        if(isAsync){
            TaskHandler.runSync(runnable);
        }else{
            TaskHandler.runAsync(runnable);
        }
    }
}
