import java.sql.*;
import java.util.*;
import java.util.Date;

// Interface untuk operasi CRUD
interface CRUDOperations {
    void create(Barang barang) throws SQLException;
    List<Barang> read() throws SQLException;
    void update(String id, Barang barang) throws SQLException;
    void delete(String id) throws SQLException;
}

// Superclass (untuk inheritance)
class Koleksi {
    private String id, nama, jenis;

    public Koleksi(String id, String nama, String jenis) {
        this.id = id;
        this.nama = nama;
        this.jenis = jenis;
    }

    // Getter
    public String getId() { return id; }
    public String getNama() { return nama; }
    public String getJenis() { return jenis; }

    // Method manipulasi String
    public String formatNamaJenis() {
        return "Nama: " + nama.toUpperCase() + ", Jenis: " + jenis.toLowerCase();
    }
}

// Subclass Barang (inheritance dari Koleksi)
class Barang extends Koleksi {
    private String asal, kondisi;
    private Date waktuTemuan;
    private int jumlah;
    private double berat;

    public Barang(String id, String nama, String jenis, String asal, Date waktuTemuan, String kondisi, int jumlah, double berat) {
        super(id, nama, jenis); // Memanggil konstruktor superclass
        this.asal = asal;
        this.waktuTemuan = waktuTemuan;
        this.kondisi = kondisi;
        this.jumlah = jumlah;
        this.berat = berat;
    }

    // Getter
    public String getAsal() { return asal; }
    public Date getWaktuTemuan() { return waktuTemuan; }
    public String getKondisi() { return kondisi; }
    public int getJumlah() { return jumlah; }
    public double getBerat() { return berat; }

    // Perhitungan matematis (menghitung total berat barang)
    public double getTotalBerat() {
        return jumlah * berat;
    }

    // Manipulasi Date (mengubah Date ke String format khusus)
    public String formatTanggal() {
        return new java.text.SimpleDateFormat("dd/MM/yyyy").format(waktuTemuan);
    }
}

// Implementasi interface CRUDOperations
public class MuseumManager implements CRUDOperations {
    private static final String URL = "jdbc:postgresql://localhost:5432/JDBC";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Ardra16!";
    private Connection connection;
    //collaction framework menggunakan hashmap untuk mengnyimpan data untuk sementara
    private Map<String, Barang> barangMap = new HashMap<>();

    // Konstruktor untuk koneksi database
    public MuseumManager() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try {
            MuseumManager manager = new MuseumManager(); // Objek utama
            Scanner scanner = new Scanner(System.in);
            int pilihan = 0;
            do {
                // Perulangan (looping untuk menu)
                System.out.println("\n=== Sistem Manajemen Barang Museum ===");
                System.out.println("1. Tambah Barang");
                System.out.println("2. Lihat Semua Barang");
                System.out.println("3. Edit Barang");
                System.out.println("4. Hapus Barang");
                System.out.println("5. Keluar");
                System.out.print("Masukkan pilihan: ");
                try {
                    pilihan = scanner.nextInt();
                    scanner.nextLine();
                } catch (InputMismatchException e) {
                    System.out.println("Input tidak valid. Harap masukkan angka."); // Exception handling
                    scanner.nextLine(); // Membersihkan buffer
                    continue;
                }

                // Percabangan (switch-case)
                switch (pilihan) {
                    case 1:
                        manager.tambahBarang(scanner);
                        break;
                    case 2:
                        manager.lihatSemuaBarang();
                        break;
                    case 3:
                        manager.editBarang(scanner);
                        break;
                    case 4:
                        manager.hapusBarang(scanner);
                        break;
                    case 5:
                        System.out.println("Keluar dari sistem.");
                        break;
                    default:
                        System.out.println("Pilihan tidak valid.");
                        break;
                }
            } while (pilihan != 5);
        } catch (SQLException e) {
            e.printStackTrace(); // Exception handling untuk database
        }
    }

    @Override
    public void create(Barang barang) throws SQLException {
        String query = "INSERT INTO barang (id, nama, jenis, asal, waktu_temuan, kondisi, jumlah, berat) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, barang.getId());
            ps.setString(2, barang.getNama());
            ps.setString(3, barang.getJenis());
            ps.setString(4, barang.getAsal());
            ps.setDate(5, new java.sql.Date(barang.getWaktuTemuan().getTime()));
            ps.setString(6, barang.getKondisi());
            ps.setInt(7, barang.getJumlah());
            ps.setDouble(8, barang.getBerat());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Barang> read() throws SQLException {
        List<Barang> barangList = new ArrayList<>(); // Collection Framework
        String query = "SELECT * FROM barang";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                Barang barang = new Barang(
                    rs.getString("id"),
                    rs.getString("nama"),
                    rs.getString("jenis"),
                    rs.getString("asal"),
                    rs.getDate("waktu_temuan"),
                    rs.getString("kondisi"),
                    rs.getInt("jumlah"),
                    rs.getDouble("berat")
                );
                barangList.add(barang);
            }
        }
        return barangList;
    }

    @Override
    public void update(String id, Barang barang) throws SQLException {
        String query = "UPDATE barang SET nama = ?, jenis = ?, asal = ?, waktu_temuan = ?, kondisi = ?, jumlah = ?, berat = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, barang.getNama());
            ps.setString(2, barang.getJenis());
            ps.setString(3, barang.getAsal());
            ps.setDate(4, new java.sql.Date(barang.getWaktuTemuan().getTime()));
            ps.setString(5, barang.getKondisi());
            ps.setInt(6, barang.getJumlah());
            ps.setDouble(7, barang.getBerat());
            ps.setString(8, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Barang dengan ID " + id + " tidak ditemukan."); // Exception handling
            }
        }
    }

    @Override
    public void delete(String id) throws SQLException {
        String query = "DELETE FROM barang WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Barang dengan ID " + id + " tidak ditemukan."); // Exception handling
            }
        }
    }

    private void tambahBarang(Scanner scanner) {
        try {
            System.out.print("Masukkan ID barang: ");
            String id = scanner.nextLine();
            //ini untuk melihat apakah id sudah digunakan atau belum didalam hashmap
            if (barangMap.containsKey(id)) {
                System.out.println("Barang dengan ID ini sudah ada. Silakan gunakan ID yang berbeda.");
                return; // Keluar dari metode jika ID duplikat
            }
            System.out.print("Masukkan nama barang: ");
            String namaBarang = scanner.nextLine();
            System.out.print("Masukkan jenis barang: ");
            String jenisBarang = scanner.nextLine();
            System.out.print("Masukkan asal barang: ");
            String asalBarang = scanner.nextLine();
            System.out.print("Masukkan waktu barang ditemukan (yyyy-MM-dd): ");
            Date waktuTemuan = java.sql.Date.valueOf(scanner.nextLine());
            System.out.print("Masukkan kondisi barang: ");
            String kondisiBarang = scanner.nextLine();
            System.out.print("Masukkan jumlah barang: ");
            int jumlahBarang = scanner.nextInt();
            System.out.print("Masukkan berat barang (kg): ");
            double beratBarang = scanner.nextDouble();

            Barang barang = new Barang(id, namaBarang, jenisBarang, asalBarang, waktuTemuan, kondisiBarang, jumlahBarang, beratBarang);
            create(barang);
            System.out.println("Barang berhasil ditambahkan.");
        } catch (InputMismatchException e) {
            System.out.println("Input tidak valid. Harap masukkan data yang benar.");
            scanner.nextLine();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void lihatSemuaBarang() {
        try {
            List<Barang> barangList = read();
            System.out.println("\nData Barang Museum:");
            for (Barang barang : barangList) {
                System.out.println(barang.formatNamaJenis() + ", Total Berat: " + barang.getTotalBerat() + " kg, Tanggal Temuan: " + barang.formatTanggal());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editBarang(Scanner scanner) {
        try {
            System.out.print("Masukkan ID barang yang ingin diedit: ");
            String id = scanner.nextLine();
            System.out.print("Masukkan nama barang baru: ");
            String namaBarang = scanner.nextLine();
            System.out.print("Masukkan jenis barang baru: ");
            String jenisBarang = scanner.nextLine();
            System.out.print("Masukkan asal barang baru: ");
            String asalBarang = scanner.nextLine();
            System.out.print("Masukkan waktu barang ditemukan baru (yyyy-MM-dd): ");
            Date waktuTemuan = java.sql.Date.valueOf(scanner.nextLine());
            System.out.print("Masukkan kondisi barang baru: ");
            String kondisiBarang = scanner.nextLine();
            System.out.print("Masukkan jumlah barang baru: ");
            int jumlahBarang = scanner.nextInt();
            System.out.print("Masukkan berat barang baru (kg): ");
            double beratBarang = scanner.nextDouble();

            Barang barang = new Barang(id, namaBarang, jenisBarang, asalBarang, waktuTemuan, kondisiBarang, jumlahBarang, beratBarang);
            update(id, barang);
            System.out.println("Barang berhasil diperbarui.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void hapusBarang(Scanner scanner) {
        try {
            System.out.print("Masukkan ID barang yang ingin dihapus: ");
            String id = scanner.nextLine();
            delete(id);
            System.out.println("Barang berhasil dihapus.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
