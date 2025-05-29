package gyullectric.gyullectric.domain;

public enum Priority {
    HIGH("긴급"),
    MEDIUM("보통"),
    LOW("낮음");

    private final String displayName;

    Priority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
