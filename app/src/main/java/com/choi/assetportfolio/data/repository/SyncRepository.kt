package com.choi.assetportfolio.data.repository

import com.choi.assetportfolio.core.session.SessionManager
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.choi.assetportfolio.core.util.AppLogger

/**
 * Room(로컬 캐시)과 Supabase(원격 DB) 간의 데이터 동기화를 관리합니다.
 * Dirty Flag 전략을 사용하여 오프라인에서 생성된 데이터를 추후 동기화하거나
 * 백그라운드 Fetch 실패 시 포그라운드에서 재시도합니다.
 */
class SyncRepository(
    private val postgrest: Postgrest
) {
    // 앱 전역 캐시 오염 상태 (초기에는 앱 구동 시 강제 동기화를 위해 true로 가정)
    private val _isLocalCacheDirty = MutableStateFlow(true)
    val isLocalCacheDirty = _isLocalCacheDirty.asStateFlow()

    fun setCacheDirty(isDirty: Boolean) {
        _isLocalCacheDirty.value = isDirty
        if (isDirty) {
            AppLogger.d("SyncEngine: Local Cache Dirty Marked", data = "백그라운드 동기화 실패 또는 명시적 오염 처리")
        } else {
            AppLogger.d("SyncEngine: Local Cache Cleaned", data = "최종 동기화 완료 및 캐시 최신화")
        }
    }

    /**
     * 포그라운드 진입 시 호출되어, Dirty Flag가 켜져있으면 주어진 동기화(Refresh) 작업을 즉시 수행합니다.
     */
    suspend fun syncOnForegroundIfNeeded(refreshAction: suspend () -> Unit) = withContext(Dispatchers.IO) {
        if (_isLocalCacheDirty.value) {
            AppLogger.d("SyncEngine: Local Cache Dirty Detected - Initiating Remote Fetch")
            try {
                refreshAction()
                setCacheDirty(false) // 성공 시 플래그 초기화
            } catch (e: Exception) {
                AppLogger.e("SyncEngine: Foreground Sync Failed - Retaining Dirty Flag", e)
                setCacheDirty(true)
            }
        } else {
            AppLogger.d("SyncEngine: Cache is clean. Skipping remote fetch.")
        }
    }
}
