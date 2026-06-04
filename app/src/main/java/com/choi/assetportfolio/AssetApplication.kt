/**
 * 애플리케이션의 전역 진입점 클래스입니다.
 * 앱 시작 시 Supabase 클라이언트를 설정된 URL 및 API Key를 통해 초기화하고,
 * 전역에서 공유할 수 있도록 SupabaseClient 인스턴스를 보관하는 역할을 수행합니다.
 * 보안 유출 방지를 위해 API Key와 URL은 local.properties로부터 빌드 시점에 BuildConfig를 통해 안전하게 주입받습니다.
 */
package com.choi.assetportfolio

import android.app.Application
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

import com.choi.assetportfolio.core.util.AppLogger
import com.choi.assetportfolio.BuildConfig

class AssetApplication : Application() {
    // 앱 전역에서 접근 가능한 SupabaseClient 인스턴스
    lateinit var supabase: SupabaseClient

    override fun onCreate() {
        super.onCreate()
        AppLogger.d("AssetApplication onCreate - 시작")
        
        try {
            supabase = createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_KEY
            ) {
                install(Auth)
                install(Postgrest)
            }
            AppLogger.d("Supabase Client 초기화 성공. URL: ${BuildConfig.SUPABASE_URL}")
        } catch (e: Exception) {
            AppLogger.e("Supabase Client 초기화 실패", error = e)
        }
    }
}
