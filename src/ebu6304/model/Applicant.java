package ebu6304.model;

public final class Applicant {
    private final String id;
    private final String name;
    private final String email;
    private final String skills;
    private final String cvPath;
    private final String description;

    public Applicant(String id, String name, String email, String skills, String cvPath) {
        this(id, name, email, skills, cvPath, "");
    }

    public Applicant(String id, String name, String email, String skills, String cvPath, String description) {
        if (id == null) throw new IllegalArgumentException("id");
        if (name == null) throw new IllegalArgumentException("name");
        if (email == null) throw new IllegalArgumentException("email");
        this.id = id;
        this.name = name;
        this.email = email;
        this.skills = skills == null ? "" : skills;
        this.cvPath = cvPath == null ? "" : cvPath;
        this.description = description == null ? "" : description;
    }

    public String id() { return id; }
    public String name() { return name; }
    public String email() { return email; }
    public String skills() { return skills; }
    public String cvPath() { return cvPath; }
    public String description() { return description; }

    public Applicant withProfile(String name, String email, String skills, String cvPath) {
        return new Applicant(id, name, email, skills, cvPath, this.description);
    }

    public Applicant withProfile(String name, String email, String skills, String cvPath, String description) {
        return new Applicant(id, name, email, skills, cvPath, description);
    }
}
