#!/bin/bash

# PostgreSQL 데이터베이스 복원 스크립트
# 사용법: ./scripts/restore-db.sh <백업파일경로>

set -e

if [ $# -eq 0 ]; then
  echo "사용법: $0 <백업파일경로>"
  echo "예시: $0 ./backups/postgres/backup_novel_ai_20250101_120000.sql.gz"
  exit 1
fi

BACKUP_FILE=$1

if [ ! -f "${BACKUP_FILE}" ]; then
  echo "오류: 백업 파일을 찾을 수 없습니다: ${BACKUP_FILE}"
  exit 1
fi

# 환경 변수 로드 (.env 파일이 있는 경우)
if [ -f .env ]; then
  source .env
fi

# 기본값 설정
POSTGRES_CONTAINER=${POSTGRES_CONTAINER:-"novel_ai-postgres-1"}
POSTGRES_USER=${POSTGRES_USER:-"dev"}
POSTGRES_DB=${POSTGRES_DB:-"novel_ai"}

echo "=== PostgreSQL 데이터베이스 복원 시작 ==="
echo "데이터베이스: ${POSTGRES_DB}"
echo "백업 파일: ${BACKUP_FILE}"

# .gz 파일인 경우 압축 해제 후 복원
if [[ "${BACKUP_FILE}" == *.gz ]]; then
  echo "압축 파일 감지, 압축 해제 후 복원..."
  gunzip -c "${BACKUP_FILE}" | docker exec -i ${POSTGRES_CONTAINER} psql -U ${POSTGRES_USER} -d ${POSTGRES_DB}
else
  echo "SQL 파일 복원..."
  cat "${BACKUP_FILE}" | docker exec -i ${POSTGRES_CONTAINER} psql -U ${POSTGRES_USER} -d ${POSTGRES_DB}
fi

echo "=== 복원 완료 ==="
