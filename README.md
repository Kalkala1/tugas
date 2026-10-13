daftar sensor:
- Ambient Light Sensor (environment)
- Gyro Sensor (motion)
- Proximity Sensor (position)

Penjelasan Singkat:
1. Ada 3 sensor didalam aplikasi
2. Ada efek langsung untuk UI
   - Saat cahaya nyala, tampilan UI berubah menjadi light mode
   - Saat cahaya mati, tampilan UI berubah menjadi dark mode
   - Saat tangan mendekati hp, akan ada animasi haptic, seperti peringatan dan layar berubah menjadi warna merah
   - Saat tangan menjauhi hp, akan berubah menjadi layar berwarna hijau dan status kembali normal
3. onResume() = sensor nyala/register saat masuk ke aplikasi 
   onPause() = sensor langsung di matikan ketika keluar aplikasi
   Status sensor aktif = muncul notifikasi di atas kanan layar 
