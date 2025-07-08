package com.example.onlineshop.Helper

import android.content.Context

object FavoriteManager {
    fun getFavoriteIds(context: Context, userEmail: String): MutableSet<String> {
        val tinyDB = TinyDB(context)
        return tinyDB.getListString("favorite_$userEmail").toMutableSet()
    }

    fun addFavorite(context: Context, userEmail: String, productId: String) {
        val tinyDB = TinyDB(context)
        val set = getFavoriteIds(context, userEmail)
        set.add(productId)
        tinyDB.putListString("favorite_$userEmail", ArrayList(set))
    }

    fun removeFavorite(context: Context, userEmail: String, productId: String) {
        val tinyDB = TinyDB(context)
        val set = getFavoriteIds(context, userEmail)
        set.remove(productId)
        tinyDB.putListString("favorite_$userEmail", ArrayList(set))
    }

    fun isFavorite(context: Context, userEmail: String, productId: String): Boolean {
        return getFavoriteIds(context, userEmail).contains(productId)
    }
}