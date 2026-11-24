// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.data.repository

/**
 * Authentication contract. v1 = stub only.
 * v2 will implement Google Sign-In + Firebase Auth.
 */
interface AuthRepository {
    suspend fun signInWithGoogle(): Result<Unit>
    suspend fun signOut()
    fun isSignedIn(): Boolean
}
