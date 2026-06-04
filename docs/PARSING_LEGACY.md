# 📥 PARSING_LEGACY.md: Data Ingestion Lessons Learned

## 1. Resilient Parsing Strategies
- **Index-based Extraction:** Never rely on column names (Headers change). Use index positions (e.g., `iloc[:, 2]`).
- **Date Conversion:** Handle Excel Serial Dates (e.g., 46104) to `YYYY-MM-DD` string.
- **Row Explosion:** Split single rows containing both BUY/SELL into two independent transactions.

## 2. Error Handling & Defense
- **Ticker Protection:** Force string format for tickers starting with '0' (e.g., 005930).
- **Non-blocking Parsing:** Complete parsing even with missing tickers; log them as 'Ticker Missing' instead of throwing errors.
- **Null Safety:** Use `(val or '').strip()` to prevent `AttributeError` on NoneTypes.