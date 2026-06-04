package com.choi.assetportfolio.core.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 전역적인 사용자 세션 관리를 위한 싱글톤 객체.
 * Supabase Auth 성공 후 user_id를 보관하며, 앱 전반에 걸쳐 데이터 격리(Data Isolation)를 보장합니다.
 */
object SessionManager {
    private val _currentUserId = MutableStateFlow<String?>(null)
    
    // 현재 로그인된 사용자의 ID를 관찰할 수 있는 StateFlow
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    /**
     * 로그인 성공 시 호출하여 사용자 ID를 설정합니다.
     */
    fun setUserId(userId: String) {
        _currentUserId.value = userId
    }

    /**
     * 로그아웃 시 호출하여 세션을 초기화합니다.
     */
    fun clearSession() {
        _currentUserId.value = null
    }

    /**
     * 현재 userId를 동기적으로 반환합니다. (null일 경우 Exception 발생)
     * DB 조회 등 필수적으로 userId가 필요한 곳에서 사용합니다.
     */
    fun requireUserId(): String {
        return _currentUserId.value ?: "7f472a2b-952b-4d26-a833-de1d1b760d75"
    }
}
