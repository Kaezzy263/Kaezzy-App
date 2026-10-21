package com.kaezzy.app.core.copy

object KaezzyMicrocopy {
    // Auth / App Lock
    const val PIN_ENTER = "Sini masuk dulu, brok. Ketik PIN rahasia lu."
    const val BIOMETRIC_PROMPT = "Tempel sidik jari lu biar cepet, brok."
    fun pinWrong(remainingAttempts: Int) =
        "Waduh, PIN-nya salah brok. Coba inget-inget lagi, jangan panik (Sisa $remainingAttempts percobaan)."
    fun lockoutActive(seconds: Int) =
        "Tahan dulu brok, lu udah salah 5 kali. Tunggu $seconds detik lagi baru coba lagi ya."
    const val FORGOT_PIN = "Lupa PIN?"
    const val RECOVERY_PASSPHRASE_HINT = "Masukkan Recovery Passphrase lu saat pertama kali bikin PIN."

    // Greetings
    const val GREETING_MORNING = "Pagi, brok! Udah siap ngelibas hari ini?"
    const val GREETING_AFTERNOON = "Siang, brok! Jangan lupa istirahat bentar habis fokus."
    const val GREETING_EVENING = "Sore, brok! Masih ada sisa tenaga buat nyicil dikit?"
    const val GREETING_NIGHT = "Malam, brok! Sebelum rebahan, cek dulu yuk hari ini udah ngapain aja."

    fun greetingForHour(hour: Int): String {
        return when (hour) {
            in 4..10 -> GREETING_MORNING
            in 11..14 -> GREETING_AFTERNOON
            in 15..18 -> GREETING_EVENING
            else -> GREETING_NIGHT
        }
    }

    // States
    const val LOADING = "Bentar ya brok, gw rapiin datanya dulu..."
    const val EMPTY_HOME_TASKS = "Meja bersih nih, brok. Emang lagi santai atau ada tugas yang belum lu catet?"
    const val EMPTY_TASKS = "Gokil, nggak ada tanggungan tugas aktif! Mau nambah target baru atau santai dulu?"
    const val EMPTY_MONEY = "Dompet masih senyap nih, brok. Catet pemasukan atau pengeluaran pertama lu di sini."
    const val EMPTY_DAILY_LOG = "Brok, hari ini ngapain aja lu? Sini spill biar besok nggak lupa."
    const val EMPTY_NOTES = "Ada ide lewat di kepala? Tulis di sini sekarang sebelum hilang ditelan bumi, brok."
    const val EMPTY_SKILLS = "Belum ada skill yang dicatet nih. Masukin materi atau kodingan yang lagi lu pelajari, yuk!"

    // Validations
    const val VALIDATION_TITLE_EMPTY = "Judulnya jangan dikosongin dong brok, nanti lu bingung sendiri ini apaan."
    const val VALIDATION_AMOUNT_INVALID = "Nominalnya harus lebih dari Rp 0 ya, brok. Masa transaksi angin?"
    const val VALIDATION_SAVINGS_EXCEEDED = "Waduh, isi tabungan di target ini nggak cukup buat ditarik segitu, brok."
    const val VALIDATION_SAVINGS_DELETE_PREVENTED = "Brok, dana tabungan ini udah pernah lu tarik sebagian. Hapus atau sesuaikan dulu transaksi penarikannya ya biar saldonya nggak minus."

    // Success
    fun taskDoneSuccess(xp: Int) = "NAHH, GITU DONG! Satu beban kelar, brok! (+$xp XP)"
    const val MISSION_DONE_SUCCESS = "Mantap jiwa! Satu misi harian tumbang. Lanjut terus, brok!"
    fun levelUpSuccess(level: Int) = "RESMI NAIK LEVEL $level! Kelihatan nih hasil konsisten lu, brok. Pertahanin!"
    fun savingsAllocationSuccess(amountFormatted: String) = "Cakep! Rp $amountFormatted udah diamanin ke tabungan. Dikit-dikit lama-lama jadi bukit, brok."
    const val BACKUP_SUCCESS = "Sip, file backup udah aman tersimpan di HP lu, brok!"

    // Supportive
    const val SUPPORTIVE_MANY_TASKS = "Waduh, jadwal lu mulai padet nih. Nggak usah panik, kita urutin dari yang paling mepet satu-satu, yuk."
    const val SUPPORTIVE_OVERDUE = "Ada tugas yang kelewat jamnya nih, brok. Santai, belum terlambat buat diberesin sekarang atau atur ulang jadwalnya."
    const val SUPPORTIVE_GOAL_NOT_REACHED = "Belum nyampe target? Nggak apa-apa banget, brok. Namanya juga proses. Kita cek yuk apa yang bisa diperbaiki buat langkah berikutnya."
    const val SUPPORTIVE_STREAK_RESET = "Streak kemarin putus ya, brok? Nggak masalah, istirahat itu wajar. Yuk kita mulai hitungan baru hari ini, gw temenin!"

    // Confirmations
    const val CONFIRM_DELETE_ITEM = "Yakin mau hapus ini, brok? Kalau udah dihapus nggak bisa dibalikin lagi nih."
    const val CONFIRM_DELETE_MONEY_TX = "Kalau transaksi ini dihapus, perhitungan saldo lu bakal ikut disesuaikan lagi. Yakin mau hapus, brok?"
    const val CONFIRM_RESET_ALL_DATA = "PERINGATAN KERAS, BROK: Ini bakal ngehapus SEMUA tugas, uang, catatan, dan level lu sampai bersih. Pastiin lu udah bikin Backup dulu. Ketik 'HAPUS' di bawah kalau lu beneran yakin."

    // Errors
    const val ERROR_DB_SAVE = "Waduh, maaf brok, data lu gagal kesimpen karena ada gangguan di penyimpanan lokal HP. Coba cek sisa memori HP lu lalu klik simpan lagi ya."
    const val ERROR_BACKUP_INVALID = "File backup-nya nggak bisa dibaca atau formatnya nggak cocok, brok. Tenang, data lama lu di aplikasi masih utuh dan nggak keusik sama sekali."
}
