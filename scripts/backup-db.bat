@echo off
REM PostgreSQL 데이터베이스 백업 스크립트 (Windows)
REM 사용법: scripts\backup-db.bat

setlocal EnableDelayedExpansion

REM 환경 변수 설정
set POSTGRES_CONTAINER=novel_ai-postgres-1
set POSTGRES_USER=dev
set POSTGRES_DB=novel_ai
set BACKUP_DIR=backups\postgres

REM 타임스탬프 생성
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I
set TIMESTAMP=%datetime:~0,8%_%datetime:~8,6%
set BACKUP_FILE=%BACKUP_DIR%\backup_%POSTGRES_DB%_%TIMESTAMP%.sql

REM 백업 디렉토리 생성
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

echo === PostgreSQL 데이터베이스 백업 시작 ===
echo 데이터베이스: %POSTGRES_DB%
echo 백업 파일: %BACKUP_FILE%

REM pg_dump를 사용한 백업
docker exec -t %POSTGRES_CONTAINER% pg_dump -U %POSTGRES_USER% -d %POSTGRES_DB% > "%BACKUP_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo 백업 완료: %BACKUP_FILE%
    echo === 백업 완료 ===
) else (
    echo 백업 실패!
    exit /b 1
)

endlocal
