package be4rjp.cinema4c.util;

import be4rjp.cinema4c.Cinema4C;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import world.chiyogami.chiyogamilib.scheduler.WorldThreadRunnable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TaskHandler{
    
    public static <U> CompletableFuture<U> supplySync(Supplier<U> supplier){
        CompletableFuture<U> completableFuture = new CompletableFuture<>();
        new BukkitTaskHandler<>(completableFuture, supplier, false).runTask(Cinema4C.getPlugin());
        
        return completableFuture;
    }
    
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier){
        CompletableFuture<U> completableFuture = new CompletableFuture<>();
        new BukkitTaskHandler<>(completableFuture, supplier, true).runTaskAsynchronously(Cinema4C.getPlugin());
        
        return completableFuture;
    }
    
    public static <U> CompletableFuture<U> supplyWorldSync(World world, Supplier<U> supplier){
        CompletableFuture<U> completableFuture = new CompletableFuture<>();
        new WorldTaskHandler<>(completableFuture, supplier, world).runTask(Cinema4C.getPlugin());
        
        return completableFuture;
    }
    
    public static void runSync(Runnable runnable){
        Bukkit.getScheduler().runTask(Cinema4C.getPlugin(), runnable);
    }
    
    public static void runAsync(Runnable runnable){
        Bukkit.getScheduler().runTaskAsynchronously(Cinema4C.getPlugin(), runnable);
    }
    
    public static void runAsyncImmediately(Runnable runnable){
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }
    
    public static void runWorldSync(World world, Runnable runnable){
        new WorldThreadRunnable(world){
            @Override
            public void run() {
                runnable.run();
            }
        }.runTask(Cinema4C.getPlugin());
    }
    
    
    private static class BukkitTaskHandler<T> extends BukkitRunnable{
        private final CompletableFuture<T> completableFuture;
        private final Supplier<T> supplier;
        private final boolean isAsync;
        
        private BukkitTaskHandler(CompletableFuture<T> completableFuture, Supplier<T> supplier, boolean isAsync){
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
    
    
    private static class WorldTaskHandler<T> extends WorldThreadRunnable{
        private final CompletableFuture<T> completableFuture;
        private final Supplier<T> supplier;
        
        private WorldTaskHandler(CompletableFuture<T> completableFuture, Supplier<T> supplier, World world){
            super(world);
            this.completableFuture = completableFuture;
            this.supplier = supplier;
        }
        
        @Override
        public void run() {
            T result = supplier.get();
            completableFuture.complete(result);
        }
    }
    
}
