/**
 * Service type manager with predefined service configurations.
 */
class ServiceManager {
    private HashTable services;

    public ServiceManager() {
        services = new HashTable(20);
        initializeServices();
    }

    private void initializeServices() {
        // Normalize service names - support both with and without underscores
        services.put("paint", new Service("paint", 70, 60, 50, 85, 90));
        services.put("webdev", new Service("webdev", 95, 75, 85, 80, 90));
        services.put("web_dev", new Service("web_dev", 95, 75, 85, 80, 90));
        services.put("graphic_design", new Service("graphic_design", 75, 85, 95, 70, 85));
        services.put("graphicdesign", new Service("graphicdesign", 75, 85, 95, 70, 85));
        services.put("data_entry", new Service("data_entry", 50, 50, 30, 95, 95));
        services.put("dataentry", new Service("dataentry", 50, 50, 30, 95, 95));
        services.put("tutoring", new Service("tutoring", 80, 95, 70, 90, 75));
        services.put("cleaning", new Service("cleaning", 40, 60, 40, 90, 85));
        services.put("writing", new Service("writing", 70, 85, 90, 80, 95));
        services.put("photography", new Service("photography", 85, 80, 90, 75, 90));
        services.put("plumbing", new Service("plumbing", 85, 65, 60, 90, 85));
        services.put("electrical", new Service("electrical", 90, 65, 70, 95, 95));
    }

    private String normalizeServiceName(String serviceName) {
        // Normalize service names
        if (serviceName.equals("web_dev")) return "webdev";
        if (serviceName.equals("graphic_design")) return "graphicdesign";
        if (serviceName.equals("data_entry")) return "dataentry";
        return serviceName;
    }

    public boolean isValidService(String serviceName) {
        String normalized = normalizeServiceName(serviceName);
        return services.containsKey(serviceName) || services.containsKey(normalized);
    }

    public Service getService(String serviceName) {
        Service s = (Service) services.get(serviceName);
        if (s != null) return s;
        String normalized = normalizeServiceName(serviceName);
        return (Service) services.get(normalized);
    }
}

