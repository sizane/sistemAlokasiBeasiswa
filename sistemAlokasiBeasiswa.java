package sistemAlokasiBeasiswa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class sistemAlokasiBeasiswa {

    private static final String FILE_PATH = "etc\\bis.csv"; 

    // Class representasi data Mahasiswa
    static class Mahasiswa implements Comparable<Mahasiswa> {
        String nama;
        long penghasilan;
        long besaranUkt;
        double rasioUkt;
        int golUkt;
        int tanggungan;
        double ipk;
        int levelJabatan; // (0, 1, 2, 3, 4)
        
        double skorFinansial;
        double skorAkademik;
        double totalSkor;
        double rasioBeban; 

        public Mahasiswa(String nama, long penghasilan, long besaranUkt, int golUkt, 
                         int tanggungan, int levelJabatan, double ipk, 
                         String p1, String p2, String p3) {
            this.nama = nama;
            this.penghasilan = penghasilan;
            this.besaranUkt = besaranUkt;
            this.golUkt = golUkt; // Langsung di-assign sebagai int
            this.tanggungan = tanggungan;
            this.levelJabatan = levelJabatan; // Langsung di-assign sebagai int
            this.ipk = ipk;
            
            // Perhitungan Rasio UKT = Besaran UKT (Semester) / Penghasilan Orang Tua (Bulan)
            this.rasioUkt = (penghasilan > 0) ? (double) besaranUkt / penghasilan : 0;
            
            // Perhitungan Tambahan: Rasio Beban Finansial
            this.rasioBeban = (tanggungan > 0) ? (this.rasioUkt / tanggungan) : 0;

            // 1. Hitung Komponen Finansial (Maks 60 Poin)
            this.skorFinansial = hitungSkorRasioUkt(this.rasioUkt) + 
                                 hitungSkorGolUkt(this.golUkt) + 
                                 hitungSkorTanggungan(this.tanggungan);
            
            // 2. Hitung Komponen Akademik & Non-Akademik (Maks 40 Poin)
            this.skorAkademik = hitungSkorIpk(this.ipk) + 
                                hitungSkorOrganisasi(this.levelJabatan) + 
                                hitungSkorPrestasiAkumulasi(p1, p2, p3);
            
            // Total Skor Akhir (Gabungan 60/40)
            this.totalSkor = this.skorFinansial + this.skorAkademik;
        }

        private int hitungSkorRasioUkt(double rasio) {
            if (rasio > 0.6) return 35;
            if (rasio > 0.4) return 27;
            if (rasio > 0.25) return 18;
            if (rasio >= 0.1) return 10;
            return 0;
        }

        private int hitungSkorGolUkt(int gol) {
            if (gol == 1 || gol == 2) return 5; 
            if (gol == 3 || gol == 4) return 15;
            if (gol == 5 || gol == 6) return 10;
            return 0; 
        }

        private int hitungSkorTanggungan(int jml) {
            if (jml >= 3) return 10;
            if (jml == 2) return 5;
            return 0; 
        }

        private int hitungSkorIpk(double ipk) {
            if (ipk >= 3.5) return 20;
            if (ipk >= 3.0) return 12;
            if (ipk >= 2.5) return 5;
            return 0;
        }

        private int hitungSkorOrganisasi(int level) {
            if (level == 4) return 12; // Top Leader
            if (level == 3) return 9;  // Core Administrator
            if (level == 2) return 6;  // Staff / Officer
            if (level == 1) return 3;  // Passive / Member
            return 0;                  // 0 = Tidak berorganisasi
        }

        private int hitungSkorPrestasiAkumulasi(String p1, String p2, String p3) {
            List<Integer> skorDasar = new ArrayList<>();
            skorDasar.add(getSkorSinglePrestasi(p1));
            skorDasar.add(getSkorSinglePrestasi(p2));
            skorDasar.add(getSkorSinglePrestasi(p3));

            // Urutkan dari prestasi dengan nilai tertinggi
            Collections.sort(skorDasar, Collections.reverseOrder());

            double total = (skorDasar.get(0) * 1.0) + (skorDasar.get(1) * 0.3) + (skorDasar.get(2) * 0.1);
            
            // Pembulatan ke integer terdekat dengan limit cap 8 poin
            int hasilBulat = (int) Math.round(total);
            return Math.min(hasilBulat, 8);
        }

        private int getSkorSinglePrestasi(String pStr) {
            if (pStr == null || pStr.trim().isEmpty() || pStr.equalsIgnoreCase("tidak ada prestasi")) {
                return 0;
            }
            pStr = pStr.toLowerCase();
            
            // Urutan "harapan" dan "finalis" didahulukan agar tidak tertimpa deteksi angka 1, 2, atau 3
            if (pStr.contains("internasional")) {
                if (pStr.contains("harapan")) return 5;
                if (pStr.contains("finalis") || pStr.contains("peserta")) return 4;
                if (pStr.contains("1")) return 8;
                if (pStr.contains("2")) return 7;
                if (pStr.contains("3")) return 6;
                return 4; // Fallback
            } else if (pStr.contains("nasional")) {
                if (pStr.contains("harapan")) return 4;
                if (pStr.contains("finalis") || pStr.contains("peserta")) return 3;
                if (pStr.contains("1")) return 7;
                if (pStr.contains("2")) return 6;
                if (pStr.contains("3")) return 5;
                return 3;
            } else if (pStr.contains("provinsi")) {
                if (pStr.contains("harapan")) return 2;
                if (pStr.contains("finalis") || pStr.contains("peserta")) return 1;
                if (pStr.contains("1")) return 5;
                if (pStr.contains("2")) return 4;
                if (pStr.contains("3")) return 3;
                return 1;
            }
            return 0;
        }

        @Override
        public int compareTo(Mahasiswa other) {
            if (Double.compare(this.totalSkor, other.totalSkor) != 0) {
                return Double.compare(this.totalSkor, other.totalSkor);
            }
            return Double.compare(this.rasioBeban, other.rasioBeban);
        }

        @Override
        public String toString() {
            return String.format("%-14s | Skor Akhir: %5.1f | Finansial (60%%): %4.1f | Akademik (40%%): %4.1f | Rasio Beban: %5.4f", 
                    nama, totalSkor, skorFinansial, skorAkademik, rasioBeban);
        }
    }

    // Struktur Data Max Heap
    static class MaxHeap {
        private List<Mahasiswa> heap;

        public MaxHeap() {
            this.heap = new ArrayList<>();
        }

        public void insert(Mahasiswa m) {
            heap.add(m);
            heapifyUp(heap.size() - 1);
        }

        public Mahasiswa extractMax() {
            if (heap.isEmpty()) return null;
            
            Mahasiswa max = heap.get(0);
            Mahasiswa lastNode = heap.remove(heap.size() - 1);
            
            if (!heap.isEmpty()) {
                heap.set(0, lastNode);
                heapifyDown(0);
            }
            return max;
        }

        public boolean isEmpty() {
            return heap.isEmpty();
        }

        private void heapifyUp(int index) {
            int parent = (index - 1) / 2;
            while (index > 0 && heap.get(index).compareTo(heap.get(parent)) > 0) {
                swap(index, parent);
                index = parent;
                parent = (index - 1) / 2;
            }
        }

        private void heapifyDown(int index) {
            int maxIndex = index;
            int left = 2 * index + 1;
            int right = 2 * index + 2;

            if (left < heap.size() && heap.get(left).compareTo(heap.get(maxIndex)) > 0) {
                maxIndex = left;
            }
            if (right < heap.size() && heap.get(right).compareTo(heap.get(maxIndex)) > 0) {
                maxIndex = right;
            }

            if (index != maxIndex) {
                swap(index, maxIndex);
                heapifyDown(maxIndex);
            }
        }

        private void swap(int i, int j) {
            Mahasiswa temp = heap.get(i);
            heap.set(i, heap.get(j));
            heap.set(j, temp);
        }
    }

    public static void main(String[] args) {
        MaxHeap heapAlokasi = new MaxHeap();
        
        System.out.println("Executing Max Heap Allocation Engine using '" + FILE_PATH + "'...");

        String line = "";
        String csvSplitBy = ","; 

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            
            br.readLine(); // Lewati baris header CSV

            // Gunakan -1 pada split untuk menangani field kosong (null) yang menempel di belakang seperti ",,"
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(csvSplitBy, -1);
                if (data.length < 7) continue;

                try {
                    String nama = data[0].trim();
                    long penghasilan = Long.parseLong(data[1].trim());
                    long besaranUkt = Long.parseLong(data[2].trim());
                    
                    // Format Integer untuk Golongan UKT, Tanggungan, dan Organisasi
                    int golUkt = Integer.parseInt(data[3].trim());
                    int tanggungan = Integer.parseInt(data[4].trim());
                    int levelJabatan = Integer.parseInt(data[5].trim()); 
                    
                    double ipk = Double.parseDouble(data[6].trim());
                    
                    // Deteksi kolom prestasi & handle "null" sebagai "Tidak ada prestasi"
                    String p1 = (data.length > 7 && !data[7].trim().isEmpty()) ? data[7].trim() : "Tidak ada prestasi";
                    String p2 = (data.length > 8 && !data[8].trim().isEmpty()) ? data[8].trim() : "Tidak ada prestasi";
                    String p3 = (data.length > 9 && !data[9].trim().isEmpty()) ? data[9].trim() : "Tidak ada prestasi";

                    // Instansiasi objek Mahasiswa dan masukkan ke Max Heap
                    Mahasiswa m = new Mahasiswa(nama, penghasilan, besaranUkt, golUkt, tanggungan, levelJabatan, ipk, p1, p2, p3);
                    heapAlokasi.insert(m);

                } catch (Exception e) {
                    System.out.println("Skip baris bermasalah pada data: " + data[0] + " | Error: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Gagal membaca file! Pastikan file '" + FILE_PATH + "' diletakkan di root folder proyek.");
            e.printStackTrace();
            return;
        }

        // Tampilkan Hasil Pemrosesan Urutan Prioritas Penerima
        System.out.println("\n=================================== HASIL SELEKSI ALOKASI BEASISWA (MAX HEAP) ===================================");
        int rank = 1;
        while (!heapAlokasi.isEmpty()) {
            System.out.println(String.format("%-2d. %s", rank, heapAlokasi.extractMax()));
            rank++;
        }
    }
}