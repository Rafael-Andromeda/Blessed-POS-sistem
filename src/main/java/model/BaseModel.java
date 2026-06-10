package model;

/**
 * Abstract class sebagai base untuk semua model entity.
 * Menyediakan field audit (createdAt, updatedAt) yang digunakan bersama
 * oleh seluruh model dalam aplikasi melalui inheritance.
 */
public abstract class BaseModel {

    protected String createdAt;
    protected String updatedAt;

    public BaseModel() {}

    public BaseModel(String createdAt, String updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Representasi string dari entity untuk logging/debugging.
     * Setiap subclass wajib mengimplementasikan method ini (polymorphism).
     */
    @Override
    public abstract String toString();
}
