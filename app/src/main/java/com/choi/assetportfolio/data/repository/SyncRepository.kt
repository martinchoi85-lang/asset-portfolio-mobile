package com.choi.assetportfolio.data.repository

import com.choi.assetportfolio.core.session.SessionManager
import com.choi.assetportfolio.data.local.entity.LocalTransactionEntity
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.choi.assetportfolio.core.util.AppLogger

/**
 * Room(로컬 캐시)과 Supabase(원격 DB) 간의 데이터 동기화를 관리합니다.
 * Dirty Flag 전략을 사용하여 오프라인에서 생성된 데이터를 추후 동기화합니다.
 */
class SyncRepository(
    private val postgrest: Postgrest
    // private val localDao: TransactionDao // Room DAO (가정)
) {
    /**
     * Dirty Flag가 설정된 트랜잭션들을 서버로 업로드합니다.
     */
    suspend fun syncTransactions() = withContext(Dispatchers.IO) {
        AppLogger.d("syncTransactions - 동기화 시작")
        // 1. 세션 체크 (세션 만료 시 SessionManager에서 IllegalStateException 발생)
        val userId = try {
            SessionManager.requireUserId()
        } catch (e: IllegalStateException) {
            AppLogger.e("syncTransactions - 세션이 없어 동기화 중단", e)
            return@withContext
        }

        AppLogger.d("syncTransactions - 사용자 세션 확인 완료", data = "userId=$userId")

        // 2. 로컬에서 isDirty=true인 항목 조회 (가상 코드)
        // val dirtyTransactions = localDao.getDirtyTransactions()
        // if (dirtyTransactions.isEmpty()) return@withContext

        // 3. Supabase로 일괄 업로드 (Upsert)
        // try {
        //     postgrest.from("transactions").upsert(dirtyTransactions) {
        //         // user_id를 포함하여 데이터 격리 보장
        //         filter { eq("user_id", userId) }
        //     }
        //     // 성공 시 로컬의 Dirty Flag 해제
        //     localDao.markAsSynced(dirtyTransactions.map { it.localId })
        // } catch (e: Exception) {
        //     // 실패 시 에러 로그 기록 및 다음 주기 재시도
        //     localDao.updateSyncError(dirtyTransactions.map { it.localId }, e.localizedMessage)
        // }
    }
}
