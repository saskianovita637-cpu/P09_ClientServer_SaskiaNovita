package akun;

import koneksi.KoneksiDB;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Akses data akun aplikasi pada tabel pengguna.
 */
public final class AkunDAO {

    private static final String SQL_CARI =
            "SELECT username, nama_lengkap, peran, aktif, password_hash "
            + "FROM pengguna WHERE username = ?";

    /**
     * Memeriksa username dan kata sandi.
     * Mengembalikan Sesi jika berhasil, atau null jika ditolak.
     * Alasan penolakan sengaja tidak dibedakan agar tidak membocorkan informasi akun.
     */
    public Sesi masuk(String username, char[] password)
            throws SQLException, GeneralSecurityException {
        boolean ditemukan = false;
        String user = null;
        String nama = null;
        String peran = null;
        String hash = null;
        boolean aktif = false;

        try (Connection con = KoneksiDB.getConnection();
                PreparedStatement ps = con.prepareStatement(SQL_CARI)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ditemukan = true;
                    user = rs.getString("username");
                    nama = rs.getString("nama_lengkap");
                    peran = rs.getString("peran");
                    aktif = rs.getInt("aktif") == 1;
                    hash = rs.getString("password_hash");
                }
            }
        }

        if (!ditemukan) {
            PasswordUtil.hitungTanpaHasil(password);
            return null;
        }
        boolean sandiBenar = PasswordUtil.cocok(password, hash);
        if (!sandiBenar || !aktif) {
            return null;
        }
        if (!"admin".equals(peran) && !"petugas".equals(peran)) {
            return null;
        }
        return new Sesi(user, nama, peran);
    }
}