#!/bin/bash

# PostgreSQL 데이터베이스 백업 스크립트
# 사용법: ./scripts/backup-db.sh

set -e

# 환경 변수 로드 (.env 파일이 있는 경우)
if [ -f .env ]; then
  source .env
fi

# 기본값 설정
POSTGRES_CONTAINER=${POSTGRES_CONTAINER:-"novel_ai-postgres-1"}
POSTGRES_USER=${POSTGRES_USER:-"dev"}
POSTGRES_DB=${POSTGRES_DB:-"novel_ai"}
BACKUP_DIR="./backups/postgres"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/backup_${POSTGRES_DB}_${TIMESTAMP}.sql"

# 백업 디렉토리 생성
mkdir -p "${BACKUP_DIR}"

echo "=== PostgreSQL 데이터베이스 백업 시작 ==="
echo "데이터베이스: ${POSTGRES_DB}"
echo "백업 파일: ${BACKUP_FILE}"

# pg_dump를 사용한 백업
docker exec -t ${POSTGRES_CONTAINER} pg_dump -U ${POSTGRES_USER} -d ${POSTGRES_DB} > "${BACKUP_FILE}"

# 백업 파일 압축
gzip "${BACKUP_FILE}"
echo "백업 완료: ${BACKUP_FILE}.gz"

# 7일 이상 된 백업 파일 삭제 (옵션)
find "${BACKUP_DIR}" -name "backup_*.sql.gz" -mtime +7 -delete
echo "7일 이상 된 백업 파일 삭제 완료"

echo "=== 백업 완료 ==="
