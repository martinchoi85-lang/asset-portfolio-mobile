package com.choi.assetportfolio.core.util

import android.util.Log

/**
 * 전역 디버그 로거.
 * 기능:
 * 1. IS_ENABLED 플래그를 통해 프로덕션에서 코드 수정 없이 로깅을 일괄 비활성화 가능.
 * 2. 호출한 클래스 및 라인 번호를 자동 추출하여 상태 파악을 용이하게 함.
 * 3. 데이터 객체(data)를 받아 가독성 있게 출력.
 */
object AppLogger {
    // 개발 완료 시 false로 변경하면 모든 로그가 출력되지 않음.
    private const val IS_ENABLED = true
    private const val DEFAULT_TAG = "AppDebug"

    fun d(message: String, data: Any? = null, tag: String = DEFAULT_TAG) {
        if (!IS_ENABLED) return

        val stackTrace = Thread.currentThread().stackTrace
        // 0: VMStack, 1: Thread, 2: AppLogger.d, 3: Caller
        val caller = if (stackTrace.size >= 4) stackTrace[3] else null

        val callerInfo = caller?.let {
            val className = it.className.substringAfterLast(".")
            "[$className::${it.methodName}:${it.lineNumber}]"
        } ?: "[Unknown]"

        val dataString = if (data != null) {
            "\n  ↳ Data: $data"
        } else {
            ""
        }

        Log.d(tag, "$callerInfo $message$dataString")
    }
    
    fun e(message: String, error: Throwable? = null, tag: String = DEFAULT_TAG) {
        if (!IS_ENABLED) return

        val stackTrace = Thread.currentThread().stackTrace
        val caller = if (stackTrace.size >= 4) stackTrace[3] else null

        val callerInfo = caller?.let {
            val className = it.className.substringAfterLast(".")
            "[$className::${it.methodName}:${it.lineNumber}]"
        } ?: "[Unknown]"

        Log.e(tag, "$callerInfo $message", error)
    }
}
