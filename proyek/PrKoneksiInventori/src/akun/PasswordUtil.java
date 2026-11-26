package akun;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Membuat dan memeriksa hash kata sandi dengan PBKDF2-HMAC-SHA256.
 * Format yang disimpan di kolom password_hash:
 * pbkdf2-sha256:600000:SALT_BASE64:HASH_BASE64
 */
public final class PasswordUtil {

    public static final String FORMAT = "pbkdf2-sha256";
    public static final int ITERASI = 600000;
    private static final String ALGORITMA = "PBKDF2WithHmacSHA256";
    private static final int PANJANG_SALT = 16;
    private static final int PANJANG_HASH = 32;
    private static final SecureRandom ACAK = new SecureRandom();

    private PasswordUtil() {
    }

    /** Membuat hash baru dengan salt acak. */
    public static String buatHash(char[] password) throws GeneralSecurityException {
        byte[] salt = new byte[PANJANG_SALT];
        ACAK.nextBytes(salt);
        byte[] hash = turunkan(password, salt);
        try {
            Base64.Encoder b64 = Base64.getEncoder();
            return FORMAT + ":" + ITERASI + ":"
                    + b64.encodeToString(salt) + ":" + b64.encodeToString(hash);
        } finally {
            Arrays.fill(hash, (byte) 0);
        }
    }

    /**
     * Mengembalikan true jika password sesuai dengan hash tersimpan.
     * Hash yang rusak/belum diatur tetap diproses selama waktu yang sama,
     * lalu menghasilkan false.
     */
    public static boolean cocok(char[] password, String tersimpan)
            throws GeneralSecurityException {
        byte[] salt = null;
        byte[] harapan = null;
        if (tersimpan != null) {
            String[] bagian = tersimpan.split(":", -1);
            if (bagian.length == 4 && FORMAT.equals(bagian[0])
                    && Integer.toString(ITERASI).equals(bagian[1])) {
                try {
                    salt = Base64.getDecoder().decode(bagian[2]);
                    harapan = Base64.getDecoder().decode(bagian[3]);
                } catch (IllegalArgumentException e) {
                    salt = null;
                    harapan = null;
                }
            }
        }
        boolean formatBenar = salt != null && harapan != null
                && salt.length == PANJANG_SALT && harapan.length == PANJANG_HASH;
        if (!formatBenar) {
            hitungTanpaHasil(password);
            return false;
        }
        byte[] hasil = turunkan(password, salt);
        try {
            return MessageDigest.isEqual(harapan, hasil);
        } finally {
            Arrays.fill(hasil, (byte) 0);
        }
    }

    /**
     * Menjalankan perhitungan hash tanpa memakai hasilnya.
     * Dipakai saat username tidak ditemukan agar waktu respons login sama.
     */
    static void hitungTanpaHasil(char[] password) throws GeneralSecurityException {
        byte[] hasil = turunkan(password, new byte[PANJANG_SALT]);
        Arrays.fill(hasil, (byte) 0);
    }

    private static byte[] turunkan(char[] password, byte[] salt)
            throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERASI, PANJANG_HASH * 8);
        try {
            return SecretKeyFactory.getInstance(ALGORITMA).generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}