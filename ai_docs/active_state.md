# Active State & Session Context

## 1. Immediate Execution Boundary
- **Completed**: Refactored `FinancialDashboardViewModel` to use a declarative UDF pipeline (`combine` operator) for `uiState`. Resolved the syntax errors in `DashboardScreen.kt` and type alignment issues (`List<Asset>` vs `List<DashboardAsset>`).
- **Rollback Executed**: Stripped out the day-by-day forward-fill algorithms and forex API fetch integration to restore compilation and baseline operational stability. Reverted both `AssetTrendCardView` and `ProfitPerformanceCardView` to a basic discrete Bar Chart format.

## 2. Technical Context at Hand
- **Target Component**: `DashboardScreen.kt`, `FinancialDashboardViewModel.kt`
- **Current Blockers**: Although compiling successfully, charts are still showing rendering or display anomalies on specific data points/ranges due to the lack of proper data interpolation or sizing bounds in the layout.

## 3. Next Things to Do (다음 세션 작업 예정 사항)
1. **차트 시각화 및 데이터 보정 재정립**:
   - 현재 단순 Bar Chart 형태로 강등(Rollback)되어 데이터가 비연속적으로 표현되고 있으므로, 안정적인 날짜 보간(Interpolation) 알고리즘 혹은 빈 거래일에 대한 가중치 누적(Forward-Fill) 대안 설계를 재검토합니다.
   - 단일 데이터셋이나 빈 리스트가 전달될 경우에도 Canvas가 깨지거나 비정상 배치되지 않도록 방어 코드(Min/Max/Range 예외 처리 및 Zero-division 방지)를 확실하게 보완합니다.
2. **환율 변동 및 외화 자산 매끄러운 통합**:
   - `exchangeRate` 변환 로직 및 환율 정보 갱신(Forex API)을 Clean Architecture 규칙을 준수하며 오프라인 로컬 저장소 캐시 또는 Repository 단에 완전 은닉하여 ViewModel로 전달하는 방법을 수립합니다.
3. **MVI/UDF 디버깅 및 디바이스 검증**:
   - 실제 기기/에뮬레이터 환경에서 탭 전환(전체 계좌 <-> 개별 계좌) 시 수집된 데이터 포인트 갯수 변동에 따라 차트 영역이 제대로 Recomposition되는지 UI 렌더링 생명주기를 분석합니다.