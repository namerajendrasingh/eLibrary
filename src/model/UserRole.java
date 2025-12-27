package model;
public enum UserRole {
    ADMIN("ADMIN"),
    STAFF("STAFF"),
    GUEST("GUEST");
    
    private final String value;
    
    UserRole(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static boolean isValid(String role) {
        if (role == null) return false;
        for (UserRole r : values()) {
            if (r.value.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }
    
    public static String[] getValidRoles() {
        UserRole[] values = values();
        String[] roles = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            roles[i] = values[i].getValue();
        }
        return roles;
    }
}

