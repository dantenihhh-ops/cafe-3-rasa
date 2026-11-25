package com.cafe3rasa.kasir.data

/**
 * Penyimpanan lokal sederhana (key-value) yang tetap ada walau aplikasi ditutup.
 * - Android : SharedPreferences
 * - Desktop : java.util.prefs.Preferences
 *
 * Dipakai untuk menyimpan sesi login (token, data user) dan alamat server.
 */
expect fun storageGet(key: String): String?

/** Simpan nilai. Isi null untuk menghapus key. */
expect fun storagePut(key: String, value: String?)

/** Alamat server bawaan per platform (bisa diganti dari layar login). */
expect val defaultServerUrl: String
