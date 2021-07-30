package ir.limoo.driver.entity;

public enum ConversationType {
    PUBLIC("public"),
    PRIVATE("private"),
    DIRECT("direct");

    public final String label;

    private ConversationType(String label) {
        this.label = label;
    }

    public static ConversationType valueOfLabel(String label) {
        for (ConversationType e : values()) {
            if (e.label.equals(label)) {
                return e;
            }
        }
        return null;
    }
}
