import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Scans all Java source files for feature flag constants and generates a comprehensive manifest.
 */
public class FlagScanner {
    public static void main(String[] args) throws Exception {
        Map<String, Set<String>> serviceFlags = new TreeMap<>();
        
        // Scan all Java files
        Files.walk(Paths.get("c:/Users/ivars/Downloads/telecom_microservices"))
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(path -> {
                try {
                    scanFile(path, serviceFlags);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        
        // Print all unique flags grouped by service
        System.out.println("=== ALL DISCOVERED FEATURE FLAGS ===");
        serviceFlags.forEach((service, flags) -> {
            System.out.println("\n" + service + " (" + flags.size() + " flags):");
            flags.stream().sorted().forEach(flag -> System.out.println("  - " + flag));
        });
        
        // Count total
        int total = serviceFlags.values().stream().mapToInt(Set::size).sum();
        System.out.println("\n=== TOTAL: " + total + " unique flags ===");
    }
    
    private static void scanFile(Path path, Map<String, Set<String>> serviceFlags) throws IOException {
        String content = new String(Files.readAllBytes(path));
        
        // Look for string patterns: "service_flag_name", "ENABLE_...", "..." = "..."
        Pattern p1 = Pattern.compile("\"([a-z_]{3,})\"");
        Matcher m = p1.matcher(content);
        
        Set<String> flags = new HashSet<>();
        while (m.find()) {
            String candidate = m.group(1);
            // Match service_feature or similar patterns
            if ((candidate.contains("_enable") || candidate.contains("_") && candidate.length() > 5)
                && !candidate.contains("com.telecom") && !candidate.contains("http")) {
                flags.add(candidate);
            }
        }
        
        // Extract service name from path
        String pathStr = path.toString().toLowerCase();
        String service = "unknown";
        if (pathStr.contains("auth-service")) service = "auth-service";
        else if (pathStr.contains("billing")) service = "billing-invoicing";
        else if (pathStr.contains("shopping-cart")) service = "shopping-cart";
        else if (pathStr.contains("product-catalog")) service = "product-catalog";
        else if (pathStr.contains("customer-management")) service = "customer-management";
        else if (pathStr.contains("inventory-management")) service = "inventory-management";
        else if (pathStr.contains("order-management")) service = "order-management";
        else if (pathStr.contains("payment-processing")) service = "payment-processing";
        else if (pathStr.contains("service-provisioning")) service = "service-provisioning";
        else if (pathStr.contains("notification")) service = "notification-service";
        
        if (!flags.isEmpty()) {
            serviceFlags.computeIfAbsent(service, k -> new TreeSet<>()).addAll(flags);
        }
    }
}
