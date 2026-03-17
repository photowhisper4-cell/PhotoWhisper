package uk.ac.tees.mad.photowhisper.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.storage.Storage


object SupabaseClient {
    private const val SUPABASE_URL = "https://pevvxosgopiucdvkyqed.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_MAbgGtFxDKJL3tadNUv3aQ_fkG_ZHrD"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Storage)
    }
}