package ebu6304.model;

public final class Applicant {
    private final String id;
    private final String name;
    private final String email;
    private final String skills;
    private final String cvPath;

    public Applicant(String id, String name, String email, String skills, String cvPath) {
        if (id == null) throw new IllegalArgumentException("id");
        if (name == null) throw new IllegalArgumentException("name");
        if (email == null) throw new IllegalArgumentException("email");
        this.id = id;
        this.name = name;
        this.email = email;
        this.skills = skills == null ? "" : skills;
        this.cvPath = cvPath == null ? "" : cvPath;
    }

    public String id() { return id; }
    public String name() { return name; }
    public String email() { return email; }
    public String skills() { return skills; }
    public String cvPath() { return cvPath; }

    public Applicant withProfile(String name, String email, String skills, String cvPath) {
        return new Applicant(id, name, email, skills, cvPath);
    }
}
