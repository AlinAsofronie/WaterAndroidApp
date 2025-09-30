package com.example.assetregisterapp.config

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseConfig {
    // Your Supabase project configuration
    private const val SUPABASE_URL = "https://ydudfioxgxyglzuckcnp.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlkdWRmaW94Z3h5Z2x6dWNrY25wIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTkxNzU2MzksImV4cCI6MjA3NDc1MTYzOX0.yyDyF4rhWZogA9y1smP8ehKD0GJhRSVt0FqgITSi28g"
    
    val client: SupabaseClient? by lazy {
        try {
            createSupabaseClient(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_ANON_KEY
            ) {
                install(Postgrest)
                install(Realtime)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}