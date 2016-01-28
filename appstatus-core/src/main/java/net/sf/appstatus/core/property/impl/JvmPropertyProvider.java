package net.sf.appstatus.core.property.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * Provides JVM informations :
 * <ul>
 * <li>name</li>
 * <li>vendor</li>
 * <li>version</li>
 * <li>uptime</li>
 * <li>start time</li>
 * <li>heap memory</li>
 * <li><non heap memory/li>
 * </ul>
 *
 * @author Nicolas Richeton
 *
 */
public class JvmPropertyProvider extends AbstractPropertyProvider {

    private static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public String getCategory() {
        return "JVM";
    }

    public Map<String, String> getProperties() {
        Map<String, String> map = new TreeMap<String, String>();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        List<String> aList = runtimeMXBean.getInputArguments();
        String parameters = "";
        for (int i = 0; i < aList.size(); i++) {
            parameters = parameters + " " + aList.get(i);
        }
        map.put("params", parameters);

        map.put("name", runtimeMXBean.getVmName());
        map.put("vendor", runtimeMXBean.getVmVendor());
        map.put("version", runtimeMXBean.getVmVersion());
        map.put("specification", runtimeMXBean.getSpecVersion());
        map.put("uptime", DurationFormatUtils.formatDurationHMS(runtimeMXBean.getUptime()));

        Date date = new Date(runtimeMXBean.getStartTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        map.put("start time", sdf.format(date));

        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

        MemoryUsage heap = memory.getHeapMemoryUsage();
        map.put("memory. (heap)", readableFileSize(heap.getUsed()) + "/" + readableFileSize(heap.getCommitted()) + " min:" + readableFileSize(heap.getInit()) + " max:" + readableFileSize(heap.getMax()));
        MemoryUsage nonheap = memory.getNonHeapMemoryUsage();
        map.put("memory (non heap)", readableFileSize(nonheap.getUsed()) + "/" + readableFileSize(nonheap.getCommitted()) + " min:" + readableFileSize(nonheap.getInit()) + " max:" + readableFileSize(nonheap.getMax()));

        return map;
    }

}
