package akun;

import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Alat bantu untuk membuat perintah SQL akun aplikasi baru.
 * Program ini TIDAK menyimpan ke database. SQL hasilnya dijalankan
 * oleh pengelola database melalui tab SQL phpMyAdmin.
 */
public final class BuatAkun {

    private BuatAkun() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tampilkan();
            }
        });
    }

    private static void tampilkan() {
        JTextField txtUsername = new JTextField(20);
        JTextField txtNama = new JTextField(20);
        JComboBox<String> cmbPeran = new JComboBox<String>(new String[]{"petugas", "admin"});
        JPasswordField txtSandi = new JPasswordField(20);
        JPasswordField txtUlang = new JPasswordField(20);

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 4));
        panel.add(new JLabel("Username (3-30 karakter: huruf kecil, angka, garis bawah)"));
        panel.add(txtUsername);
        panel.add(new JLabel("Nama lengkap"));
        panel.add(txtNama);
        panel.add(new JLabel("Peran"));
        panel.add(cmbPeran);
        panel.add(new JLabel("Kata sandi (minimal 8 karakter)"));
        panel.add(txtSandi);
        panel.add(new JLabel("Ulangi kata sandi"));
        panel.add(txtUlang);

        while (true) {
            int pilihan = JOptionPane.showConfirmDialog(null, panel,
                    "Buat Akun Aplikasi - P09", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (pilihan != JOptionPane.OK_OPTION) {
                return;
            }
            String username = txtUsername.getText().trim();
            String nama = txtNama.getText().trim();
            String peran = (String) cmbPeran.getSelectedItem();
            char[] sandi = txtSandi.getPassword();
            char[] ulang = txtUlang.getPassword();
            try {
                String salah = periksa(username, nama, sandi, ulang);
                if (salah != null) {
                    JOptionPane.showMessageDialog(null, salah, "Periksa isian",
                            JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                String hash = PasswordUtil.buatHash(sandi);
                String sql = buatSql(username, nama, peran, hash);
                salinKeClipboard(sql);
                JTextArea area = new JTextArea(sql, 6, 60);
                area.setEditable(false);
                area.setLineWrap(true);
                JOptionPane.showMessageDialog(null, new JScrollPane(area),
                        "SQL akun (sudah disalin ke clipboard)",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            } catch (GeneralSecurityException e) {
                JOptionPane.showMessageDialog(null,
                        "Hash gagal dibuat: " + e.getMessage(), "Kesalahan",
                        JOptionPane.ERROR_MESSAGE);
                return;
            } finally {
                Arrays.fill(sandi, '\0');
                Arrays.fill(ulang, '\0');
            }
        }
    }

    /** Mengembalikan pesan kesalahan, atau null jika semua isian benar. */
    static String periksa(String username, String nama, char[] sandi, char[] ulang) {
        if (!username.matches("[a-z0-9_]{3,30}")) {
            return "Username harus 3-30 karakter: huruf kecil, angka, atau garis bawah.";
        }
        if (nama.isEmpty() || nama.length() > 100) {
            return "Nama lengkap wajib diisi, maksimal 100 karakter.";
        }
        if (sandi.length < 8 || sandi.length > 128) {
            return "Kata sandi harus 8-128 karakter.";
        }
        if (!Arrays.equals(sandi, ulang)) {
            return "Kata sandi dan ulangan kata sandi tidak sama.";
        }
        return null;
    }

    /** Menyusun perintah INSERT yang siap dijalankan di phpMyAdmin. */
    static String buatSql(String username, String nama, String peran, String hash) {
        return "INSERT INTO pengguna (username, password_hash, nama_lengkap, peran, aktif)\n"
                + "VALUES ('" + username + "', '" + hash + "', '"
                + teksSql(nama) + "', '" + peran + "', 1);";
    }

    /** Menggandakan backslash dan tanda petik agar teks aman di dalam '...'. */
    static String teksSql(String teks) {
        return teks.replace("\\", "\\\\").replace("'", "''");
    }

    private static void salinKeClipboard(String teks) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(teks), null);
        } catch (RuntimeException e) {
            // Clipboard tidak tersedia; mahasiswa dapat menyalin dari kotak teks.
        }
    }
}