package tampilan;

import akun.AkunDAO;
import akun.Sesi;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 * Jendela login aplikasi Inventori Laboratorium (Praktikum 9).
 */
public class FormLogin extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int BATAS_GAGAL = 3;
    private static final int DETIK_JEDA = 10;

    private final JTextField txtUsername = new JTextField(22);
    private final JPasswordField txtPassword = new JPasswordField(22);
    private final JCheckBox chkTampil = new JCheckBox("Tampilkan kata sandi");
    private final JButton btnMasuk = new JButton("Masuk");
    private final JLabel lblStatus = new JLabel("Masukkan akun aplikasi Anda.");
    private final char karakterSamaran;
    private int jumlahGagal = 0;
    private int sisaDetik = 0;
    private Timer timerJeda;

    public FormLogin() {
        super("Inventori Laboratorium | Login P09");
        karakterSamaran = txtPassword.getEchoChar();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        susunTampilan();
        btnMasuk.addActionListener(e -> login());
        chkTampil.addActionListener(e -> txtPassword.setEchoChar(
                chkTampil.isSelected() ? (char) 0 : karakterSamaran));
        getRootPane().setDefaultButton(btnMasuk);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                txtUsername.requestFocusInWindow();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (timerJeda != null) {
                    timerJeda.stop();
                }
            }
        });
        pack();
        setMinimumSize(new Dimension(460, 340));
        setLocationRelativeTo(null);
    }

    private void susunTampilan() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));
        panel.setBackground(new Color(238, 243, 249));
        setContentPane(panel);

        JLabel judul = new JLabel("INVENTORI LABORATORIUM");
        judul.setFont(new Font("SansSerif", Font.BOLD, 22));
        judul.setForeground(new Color(24, 49, 83));
        JLabel subjudul = new JLabel("Silakan masuk untuk melanjutkan");

        tambah(panel, judul, 0, 12);
        tambah(panel, subjudul, 1, 16);
        tambah(panel, new JLabel("Nama pengguna"), 2, 2);
        tambah(panel, txtUsername, 3, 10);
        tambah(panel, new JLabel("Kata sandi"), 4, 2);
        tambah(panel, txtPassword, 5, 4);
        chkTampil.setOpaque(false);
        tambah(panel, chkTampil, 6, 10);
        tambah(panel, btnMasuk, 7, 10);
        tambah(panel, lblStatus, 8, 0);
    }

    private void tambah(JPanel panel, java.awt.Component komponen, int baris, int jarakBawah) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = baris;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, jarakBawah, 0);
        panel.add(komponen, g);
    }

    private void aturInput(boolean aktif) {
        txtUsername.setEnabled(aktif);
        txtPassword.setEnabled(aktif);
        chkTampil.setEnabled(aktif);
        btnMasuk.setEnabled(aktif);
    }

    private void login() {
        if (!btnMasuk.isEnabled()) {
            return;
        }
        final String username = txtUsername.getText().trim();
        final char[] password = txtPassword.getPassword();
        txtPassword.setText("");

        if (username.isEmpty() || password.length == 0) {
            Arrays.fill(password, '\0');
            lblStatus.setText("Nama pengguna dan kata sandi wajib diisi.");
            return;
        }
        if (username.length() > 30 || password.length > 128) {
            Arrays.fill(password, '\0');
            lblStatus.setText("Nama pengguna maksimal 30 karakter, kata sandi maksimal 128.");
            return;
        }

        aturInput(false);
        lblStatus.setText("Memeriksa akun... (dapat memakan 1-3 detik)");
        new SwingWorker<Sesi, Void>() {
            @Override
            protected Sesi doInBackground() throws Exception {
                try {
                    return new AkunDAO().masuk(username, password);
                } finally {
                    Arrays.fill(password, '\0');
                }
            }

            @Override
            protected void done() {
                if (!isDisplayable()) {
                    return;
                }
                try {
                    Sesi sesi = get();
                    if (sesi != null) {
                        new FormBarang(sesi).setVisible(true);
                        dispose();
                        return;
                    }
                    jumlahGagal++;
                    if (jumlahGagal >= BATAS_GAGAL) {
                        mulaiJeda();
                        return;
                    }
                    lblStatus.setText("Login ditolak. Periksa akun dan kata sandi. (percobaan "
                            + jumlahGagal + " dari " + BATAS_GAGAL + ")");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    lblStatus.setText("Pemeriksaan akun terhenti. Coba lagi.");
                } catch (ExecutionException e) {
                    tampilkanGagalSistem(e.getCause());
                }
                aturInput(true);
                txtPassword.requestFocusInWindow();
            }
        }.execute();
    }

    private void mulaiJeda() {
        sisaDetik = DETIK_JEDA;
        lblStatus.setText("Terlalu banyak percobaan gagal. Tunggu " + sisaDetik + " detik.");
        timerJeda = new Timer(1000, e -> {
            sisaDetik--;
            if (sisaDetik > 0) {
                lblStatus.setText("Terlalu banyak percobaan gagal. Tunggu " + sisaDetik + " detik.");
            } else {
                ((Timer) e.getSource()).stop();
                jumlahGagal = 0;
                aturInput(true);
                lblStatus.setText("Silakan coba login kembali.");
                txtPassword.requestFocusInWindow();
            }
        });
        timerJeda.start();
    }

    private void tampilkanGagalSistem(Throwable sebab) {
        String pesan = "Login tidak dapat diproses karena masalah sistem.";
        if (sebab instanceof SQLException) {
            SQLException e = (SQLException) sebab;
            pesan += "\nSQLState: " + e.getSQLState() + "; kode: " + e.getErrorCode()
                    + "\nPesan: " + e.getMessage()
                    + "\n\nJalankan koneksi/UjiKoneksi.java untuk memeriksa koneksi.";
        } else if (sebab != null) {
            pesan += "\nJenis gangguan: " + sebab.getClass().getSimpleName();
        }
        lblStatus.setText("Login gagal diproses. Periksa koneksi database.");
        JOptionPane.showMessageDialog(this, pesan, "Informasi Login", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormLogin().setVisible(true));
    }
}