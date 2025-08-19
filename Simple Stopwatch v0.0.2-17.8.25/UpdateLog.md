# Dev Log — SimpleStopwatch Improvements
Tanggal: 2025-08-17  
Versi target: v1.1 (informal)

Ringkasan singkat
- Refactor besar: memisahkan tanggung jawab plugin menjadi beberapa kelas (Stopwatch, StopwatchService, StopwatchData, StopwatchCommand, PlayerListener).
- Perbaikan UX: update waktu dikirim ke action bar (bukan chat) untuk menghindari spam.
- Stabilitas & keamanan: ConcurrentHashMap untuk penyimpanan stopwatch, StopwatchData disinkronisasi.
- Lifecycle & resource management: scheduler disimpan dan dibatalkan di onDisable; data pemain dihapus pada PlayerQuit untuk menghindari memory leak.
- Konfigurasi & lokalisasi: tambah `config.yml` untuk interval updater, permission default, dan pesan yang dapat dikustom.
- Permissions: perintah dicek menggunakan permission dari config (default `stopwatch.use`).
- Style & testing readiness: kode dipisah sehingga lebih mudah diuji (StopwatchData siap untuk unit test).

Perubahan berkas utama
- stopwatch/src/main/java/com/aithor/time_core/Stopwatch.java
    - Bootstrapping plugin: load config, register command & listener, start/stop task.
- stopwatch/src/main/java/com/aithor/time_core/StopwatchService.java
    - Logika inti: manajemen instance stopwatch, updater action-bar, format waktu.
- stopwatch/src/main/java/com/aithor/time_core/StopwatchData.java
    - Model stopwatch, synchronized methods, laps immutable view.
- stopwatch/src/main/java/com/aithor/time_core/StopwatchCommand.java
    - Pemrosesan command + permission checks + penggunaan pesan dari config.
- stopwatch/src/main/java/com/aithor/time_core/PlayerListener.java
    - Remove stopwatch data on PlayerQuit.
- stopwatch/src/main/resources/config.yml
    - Konfigurasi default (interval, permissions, messages).
- stopwatch/src/main/resources/plugin.yml
    - Metadata plugin (commands, permission default).

Alasan / Rationale
- Menghindari chat spam: action bar memberikan feedback yang non-intrusif.
- Pemisahan tanggung jawab membuat kode lebih maintainable, mudah diuji, dan memudahkan penambahan fitur (persistence, UI alternatif).
- ConcurrentHashMap + synchronized mencegah race condition bila diakses lintas-thread di masa depan.
- Menghentikan scheduler di onDisable mencegah task tertinggal saat plugin di-reload.

Compatibility & Requirements
- Server: Spigot/Paper yang menyediakan Spigot API (p.spigot().sendMessage untuk action bar).
- api-version: "1.13" (sesuaikan plugin.yml jika perlu target lebih baru).
- Java: sesuaikan dengan environment proyek (kode kompatibel Java 8+).

Konfigurasi (config.yml)
- update-interval-ticks: interval updater (default 20 = 1 detik).
- permission: permission untuk menggunakan commands (default `stopwatch.use`).
- messages.*: pesan yang bisa dikustom (gunakan `%s` untuk placeholder jika diperlukan).
  Contoh action-bar format: `messages.action-bar-format: "⏱ %s"`

Permissions
- Default: `stopwatch.use` (dapat diubah lewat config).
- Pastikan plugin server permission (Vault/permissions plugin) mengatur akses jika ingin non-default.

Instruksi upgrade/installation singkat
1. Backup file JAR dan config lama (jika ada).
2. Copy/replace JAR hasil build ke folder `plugins/`.
3. Letakkan `config.yml` di `plugins/SimpleStopwatch/` (jika sudah ada, merge pesan baru atau hapus agar dibuat ulang).
4. Restart server (lebih aman daripada reload).
5. Test perintah:
    - /sw start — start stopwatch (cek action bar update).
    - /sw lap — catat lap.
    - /sw time — lihat waktu & status.
    - /sw pause /sw resume /sw stop /sw reset.
6. Pantau console untuk error saat enable.

Testing Checklist (QA)
- [ ] Start/stop/pause/resume/reset/lap bekerja sesuai logika.
- [ ] Action bar muncul setiap interval hanya saat stopwatch berjalan.
- [ ] PlayerQuit menghapus data (cek memory usage/heap snapshot bila perlu).
- [ ] onDisable membatalkan task (cek console saat shutdown/reload).
- [ ] Permission enforcement (user tanpa permission tidak dapat menjalankan commands).
- [ ] Config messages dapat disesuaikan dan muncul sesuai format.

Migrasi data / persistence
- Saat ini tidak ada persistence; stopwatches adalah volatile. Jika perlu reconnect-persistent state, rancangkan serialisasi per pemain di folder `plugins/SimpleStopwatch/data/` (future work).

Hal yang sudah saya siapkan di branch / commit
- Branch yang direkomendasikan: `improve-stopwatch-10-10`
- Commit message yang direkomendasikan: "Refactor stopwatch plugin: service + data + command + listener, action bar updates, config & fixes"
- NOTE: saya sudah menyiapkan patch/berkas yang bisa di-commit. Saya belum melakukan push otomatis ke repo karena akses push diperlukan.

Risiko & catatan technical debt
- Action bar API (`p.spigot().sendMessage`) bergantung pada Spigot/Bungee API — pastikan server kompatibel.
- Tidak ada feature untuk persistence state — pemain yang reconnect akan kehilangan stopwatch.
- Pesan config belum mendukung placeholder warna otomatis (& > § mapping) — bisa ditambahkan util untuk translate color codes.

Next / Rencana pengembangan (saran)
- Tambahkan unit tests untuk StopwatchData (pause/resume/elapsed/lap).
- Tambahkan persistence opsional (save saat disconnect, restore on reconnect).
- Tambahkan opsi UI alternatif: scoreboard atau BossBar untuk pengalaman berbeda.
- Tambahkan i18n/messages per-locale.
- Tambahkan metrics/telemetry ringan (opsional) untuk melihat penggunaan.

Contoh entry Changelog singkat yang bisa dimasukkan ke CHANGELOG atau Release Notes
- Added: Action bar updates for active stopwatches (no more chat spam).
- Improved: Refactored codebase into service/data/command/listener modules.
- Fixed: Memory leak on player disconnect & scheduler not cancelled on disable.
- Added: Configurable update interval and message localization.
- Improved: Thread-safety and concurrency handling.

Penutup
- Saya sudah merapikan dan menyiapkan semua berkas utama. Kalau Anda mau, saya bisa:
    - (A) Buat PR/branch di repo Anda langsung (perlukan akses push).
    - (B) Kirim patch/ZIP berkas untuk Anda commit manual.
    - (C) Tambahkan unit tests & CI setup (GitHub Actions) untuk memastikan regresi tidak muncul.

Dibuat oleh: copilot / asisten dev (menyiapkan patch & instruksi).  