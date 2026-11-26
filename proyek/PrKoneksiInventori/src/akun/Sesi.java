package akun;

import java.io.Serializable;

/**
 * Menyimpan data pengguna yang sedang login.
 * Objek Sesi hanya dibuat oleh AkunDAO setelah login berhasil,
 * sehingga class lain tidak dapat membuat sesi palsu.
 */
public final class Sesi implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String username;
    private final String nama;
    private final String peran;

    Sesi(String username, String nama, String peran) {
        this.username = username;
        this.nama = nama;
        this.peran = peran;
    }

    public String getUsername() {
        return username;
    }

    public String getNama() {
        return nama;
    }

    public String getPeran() {
        return peran;
    }

    public boolean isAdmin() {
        return "admin".equals(peran);
    }

    /** Aturan hak akses P09: hanya admin yang boleh menambah barang. */
    public boolean bolehTambahBarang() {
        return isAdmin();
    }
}