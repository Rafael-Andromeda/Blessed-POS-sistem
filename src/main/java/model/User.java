package model;

/**
 * Model untuk entitas pengguna (kasir/admin).
 * Extends BaseModel untuk mewarisi field audit createdAt (inheritance).
 */
public class User extends BaseModel {
    private int idUser;
    private String nama;
    private String username;
    private String password;
    private String role;
    private String status;

    public User() {}

    public User(int idUser, String nama, String username, String password, String role, String status, String createdAt) {
        super(createdAt, null);
        this.idUser = idUser;
        this.nama = nama;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getInitials() {
        if (nama == null || nama.isBlank()) return "U";
        String[] parts = nama.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    @Override
    public String toString() {
        return nama + " (" + role + ")";
    }
}
