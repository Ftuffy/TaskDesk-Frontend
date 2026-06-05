==== TASKDESK - APLIKASI MANAJEMEN TUGAS ====
================================================================
NAMA APLIKASI
-------------
TaskDesk

DESKRIPSI APLIKASI
------------------
TaskDesk adalah aplikasi manajemen tugas berbasis Android yang
dirancang untuk membantu pengguna mengorganisir dan mengelola
tugas sehari-hari dengan mudah dan efisien.

Fitur utama aplikasi:
- Manajemen tugas (tambah, edit, hapus, selesaikan tugas)
- Kategorisasi tugas (School & Work)
- Filter tugas berdasarkan kategori dan periode (Harian, Mingguan, Bulanan)
- Notifikasi pengingat deadline tugas
- Halaman profil pengguna
- Asisten virtual "Kaia" berbasis AI (Gemini API)
- Autentikasi pengguna (Register & Login)
- Integrasi dengan REST API Laravel

TEKNOLOGI YANG DIGUNAKAN
-------------------------
- Bahasa       : Kotlin
- UI Framework : XML Layout (View-based)
- Backend API  : Laravel (REST API)
- AI           : Google Gemini API
- Database     : MySQL (via Laravel)
- Autentikasi  : Laravel Sanctum
- Networking   : Retrofit + OkHttp
- Lainnya      : Coroutines, ViewBinding, SharedPreferences

===== ANGGOTA KELOMPOK ==== 
================================================================

No  Nama                                    NIM/No. Absen
--  ----                                    -------------
1.  Diraja Kredo Saujana                    (8)
2.  Fatih Daffa Dzaki Al Huda               (12)
3.  Mahendra Brian Pramudya Admaja          (20)
4.  Sahlan Sahara Qolbi                     (28)

===== PEMBAGIAN TUGAS =====
================================================================

1. Diraja Kredo Saujana (8)
   - Membuat UI/UX Design di Figma
   - Membuat tampilan (layout) di Android Studio
   - [tambahkan tugas lainnya]

2. Fatih Daffa Dzaki Al Huda (12)
   - Membuat visual dan logic di Android Studio
   - Menyambungkan API ke project Android Studio

3. Mahendra Brian Pramudya Admaja (20)
   - Membuat Api Serve
   - Membuat DataBase

4. Sahlan Sahara Qolbi (28)
   - Membuat Api Serve
   - Membuat DataBase

===== LINK PENTING =====
================================================================

Repository Source Code (Frontend - Android)
--------------------------------------------
https://github.com/Ftuffy/TaskDesk-Frontend

Repository Source Code (Backend - Laravel API)
-----------------------------------------------
https://github.com/Geyeeem/manajement-Tugas_-BE-.git

Link APK
---------
[isi link APK setelah diupload ke Google Drive]


===== CARA MENJALANKAN APLIKASI =====
================================================================

BACKEND (Laravel API):
1. Clone repository backend
2. Jalankan: php artisan serve
3. Jalankan Ngrok: ngrok http 8000
4. Update BASE_URL di RetrofitClient.kt dengan URL Ngrok

ANDROID:
1. Clone repository frontend
2. Buka project di Android Studio
3. Tambahkan GEMINI_API_KEY di local.properties
4. Build & Run aplikasi

===== Kelompok TaskDesk - 2026 =====
================================================================
