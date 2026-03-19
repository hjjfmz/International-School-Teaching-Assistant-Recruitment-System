package ebu6304.ui;

public enum Role {
    TA,
    MO,
    ADMIN;

    public String displayKey() {
        switch (this) {
            case TA: return "role.ta";
            case MO: return "role.mo";
            case ADMIN: return "role.admin";
            default: return "role.ta";
        }
    }

    public String authRole() {
        switch (this) {
            case TA: return "TA";
            case MO: return "MO";
            case ADMIN: return "Admin";
            default: return "TA";
        }
    }
}
