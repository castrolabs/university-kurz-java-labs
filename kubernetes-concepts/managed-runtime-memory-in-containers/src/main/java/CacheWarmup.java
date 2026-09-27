import com.sun.management.HotSpotDiagnosticMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/** The app running in the Pod: loads 200 MiB of data into an in-memory cache, then serves it. */
public class CacheWarmup {

    public static void main(String[] args) throws Exception {
        var diagnostics = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        System.out.println("max heap MiB: " + Runtime.getRuntime().maxMemory() / (1024 * 1024));
        System.out.println("gc: " + ManagementFactory.getGarbageCollectorMXBeans().stream()
                .map(GarbageCollectorMXBean::getName).toList());
        System.out.println("exit on OOM: " + diagnostics.getVMOption("ExitOnOutOfMemoryError").getValue());

        List<byte[]> cache = new ArrayList<>();
        for (int i = 0; i < 200 * 16; i++) {
            cache.add(new byte[64 * 1024]);   // 200 MiB in 64 KiB entries
        }
        System.out.println("cache warm: " + cache.size() / 16 + " MiB");
        Thread.sleep(Long.MAX_VALUE);
    }
}
