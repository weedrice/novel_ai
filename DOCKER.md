# Docker 및 배포 가이드

## 목차
- [환경 설정](#환경-설정)
- [로컬 개발 환경](#로컬-개발-환경)
- [프로덕션 배포](#프로덕션-배포)
- [CI/CD 파이프라인](#cicd-파이프라인)
- [문제 해결](#문제-해결)

---

## 환경 설정

### 사전 요구 사항
- Docker 20.10 이상
- Docker Compose v2.0 이상
- (선택) Node.js 20, Java 21, Python 3.11 (로컬 개발 시)

### 환경 변수 설정

1. **루트 .env 파일 생성**
```bash
cp .env.example .env
```

2. **환경 변수 편집**
`.env` 파일을 열어 다음 항목을 설정하세요:

```bash
# 필수: LLM API 키 (최소 하나 이상)
OPENAI_API_KEY=your-openai-api-key-here
# 또는
ANTHROPIC_API_KEY=your-anthropic-api-key-here
# 또는
GOOGLE_API_KEY=your-google-api-key-here

# 선택: 포트 변경이 필요한 경우
FRONTEND_PORT=3001
API_SERVER_PORT=8080
LLM_SERVER_PORT=8000

# 선택: 데이터베이스 설정 변경
POSTGRES_DB=novel_ai
POSTGRES_USER=dev
POSTGRES_PASSWORD=dev1234

# 중요: 프로덕션 환경에서는 JWT_SECRET을 반드시 변경하세요
# 생성 명령: openssl rand -base64 64
JWT_SECRET=your-production-jwt-secret-here
```

3. **LLM 서버 환경 변수 확인**
```bash
# llm-server/.env 파일은 이미 존재하거나 자동으로 생성됩니다
# 루트 .env와 동일한 LLM API 키를 사용합니다
```

---

## 로컬 개발 환경

### 전체 스택 실행 (권장)

```bash
# 모든 서비스 빌드 및 실행
docker-compose up --build

# 백그라운드 실행
docker-compose up --build -d

# 로그 확인
docker-compose logs -f

# 특정 서비스 로그만 확인
docker-compose logs -f api-server
docker-compose logs -f frontend
docker-compose logs -f llm-server

# 종료
docker-compose down

# 종료 + 볼륨 삭제 (데이터베이스 초기화)
docker-compose down -v
```

### 서비스 접속 URL
- **Frontend**: http://localhost:3001
- **API Server**: http://localhost:8080
  - Health Check: http://localhost:8080/health
  - Swagger UI: http://localhost:8080/swagger-ui.html (향후 추가 예정)
- **LLM Server**: http://localhost:8000
  - Health Check: http://localhost:8000/health
  - API Docs: http://localhost:8000/docs
- **PostgreSQL**: localhost:5432
  - Database: `novel_ai`
  - User: `dev`
  - Password: `dev1234`
- **Neo4j Browser**: http://localhost:7474
  - User: `neo4j`
  - Password: `password`

### 개별 서비스 재시작

```bash
# 특정 서비스만 재시작
docker-compose restart api-server
docker-compose restart frontend
docker-compose restart llm-server

# 특정 서비스만 재빌드 및 재시작
docker-compose up --build -d api-server
```

### 개발 중 코드 변경 시

```bash
# 변경된 서비스만 재빌드
docker-compose build api-server
docker-compose up -d api-server

# 또는 전체 재빌드
docker-compose up --build
```

---

## 프로덕션 배포

### 프로덕션 환경 변수 설정

1. **프로덕션 .env 파일 생성**
```bash
cp .env.example .env.prod
```

2. **중요 설정 변경**
```bash
# .env.prod 파일에서 다음 항목을 반드시 변경하세요

# 프로덕션 JWT 시크릿 (보안 강화)
JWT_SECRET=$(openssl rand -base64 64)

# 프로덕션 데이터베이스 비밀번호
POSTGRES_PASSWORD=your-strong-production-password

# Neo4j 비밀번호 변경
NEO4J_AUTH=neo4j/your-strong-neo4j-password

# LLM API 키 (실제 프로덕션 키 사용)
OPENAI_API_KEY=your-production-openai-key
ANTHROPIC_API_KEY=your-production-anthropic-key
GOOGLE_API_KEY=your-production-google-key
```

3. **프로덕션 프로필로 실행**
```bash
# Spring Boot 프로덕션 프로필 활성화
export SPRING_PROFILES_ACTIVE=prod

# 프로덕션 환경 변수 파일 사용
docker-compose --env-file .env.prod up -d
```

### Docker 이미지 빌드 및 푸시

```bash
# GitHub Container Registry에 로그인
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# 이미지 빌드 및 태그
docker build -t ghcr.io/your-org/novel-ai/api-server:latest ./api-server
docker build -t ghcr.io/your-org/novel-ai/frontend:latest ./frontend
docker build -t ghcr.io/your-org/novel-ai/llm-server:latest ./llm-server

# 이미지 푸시
docker push ghcr.io/your-org/novel-ai/api-server:latest
docker push ghcr.io/your-org/novel-ai/frontend:latest
docker push ghcr.io/your-org/novel-ai/llm-server:latest
```

### 프로덕션 배포 체크리스트

- [ ] `.env.prod` 파일에서 모든 시크릿 변경 완료
- [ ] `SPRING_PROFILES_ACTIVE=prod` 환경 변수 설정
- [ ] PostgreSQL 데이터 볼륨 백업 설정
- [ ] HTTPS/TLS 인증서 설정 (Nginx, Traefik 등)
- [ ] 방화벽 및 보안 그룹 설정
- [ ] 모니터링 및 로깅 설정
- [ ] 자동 백업 스크립트 구성

---

## CI/CD 파이프라인

### GitHub Actions 워크플로우

프로젝트에는 두 가지 워크플로우가 설정되어 있습니다:

#### 1. CI 파이프라인 (`.github/workflows/ci.yml`)
- **트리거**: `main`, `develop` 브랜치에 push 또는 PR
- **작업**:
  - Backend 빌드 및 테스트 (Gradle)
  - Frontend 빌드 및 테스트 (npm)
  - LLM 서버 테스트 (pytest)
  - Docker 이미지 빌드 및 GitHub Container Registry에 푸시

#### 2. 배포 파이프라인 (`.github/workflows/deploy.yml`)
- **트리거**: Release 생성 또는 수동 실행
- **작업**:
  - 멀티 플랫폼 Docker 이미지 빌드 (linux/amd64, linux/arm64)
  - GitHub Container Registry에 푸시
  - 배포 환경 선택 (staging, production)

### GitHub Secrets 설정

GitHub 리포지토리 설정에서 다음 시크릿을 추가하세요:

```
OPENAI_API_KEY
ANTHROPIC_API_KEY
GOOGLE_API_KEY
JWT_SECRET_PROD
POSTGRES_PASSWORD_PROD
```

**설정 방법**:
1. GitHub 리포지토리 → Settings → Secrets and variables → Actions
2. "New repository secret" 클릭하여 추가

### 자동 배포 트리거

```bash
# Release 생성으로 배포 트리거
git tag v1.0.0
git push origin v1.0.0
# GitHub에서 Release 생성

# 또는 수동으로 GitHub Actions 탭에서 "Deploy to Production" 워크플로우 실행
```

---

## 문제 해결

### 일반적인 문제

#### 1. 포트 충돌 에러
```bash
Error: Bind for 0.0.0.0:8080 failed: port is already allocated
```

**해결 방법**:
```bash
# .env 파일에서 포트 변경
FRONTEND_PORT=3002
API_SERVER_PORT=8081
LLM_SERVER_PORT=8001

# 또는 포트를 사용하는 프로세스 종료
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

#### 2. Docker 빌드 실패
```bash
# Docker 캐시 삭제 후 재빌드
docker-compose build --no-cache

# 미사용 이미지 정리
docker system prune -a
```

#### 3. 데이터베이스 연결 실패
```bash
# PostgreSQL 컨테이너 로그 확인
docker-compose logs postgres

# PostgreSQL 서비스가 healthy 상태인지 확인
docker-compose ps

# 데이터베이스 재시작
docker-compose restart postgres
```

#### 4. LLM API 키 오류
```bash
# .env 파일에서 API 키 확인
cat .env | grep API_KEY

# LLM 서버 로그 확인
docker-compose logs llm-server

# 환경 변수가 올바르게 전달되었는지 확인
docker-compose exec llm-server env | grep API_KEY
```

#### 5. Frontend가 API 서버에 연결 실패
```bash
# CORS 설정 확인
# api-server/src/main/resources/application.properties의 FRONTEND_ORIGIN 확인

# 네트워크 확인
docker-compose exec frontend ping api-server

# API 서버 health check
curl http://localhost:8080/health
```

### 로그 확인

```bash
# 모든 서비스 로그
docker-compose logs

# 최근 100줄만 확인
docker-compose logs --tail=100

# 실시간 로그 스트리밍
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f api-server
```

### 컨테이너 내부 접속

```bash
# API 서버 컨테이너 접속
docker-compose exec api-server /bin/bash

# Frontend 컨테이너 접속
docker-compose exec frontend /bin/sh

# LLM 서버 컨테이너 접속
docker-compose exec llm-server /bin/bash

# PostgreSQL 접속
docker-compose exec postgres psql -U dev -d novel_ai
```

### 데이터베이스 백업 및 복원

```bash
# PostgreSQL 백업
docker-compose exec postgres pg_dump -U dev novel_ai > backup.sql

# PostgreSQL 복원
docker-compose exec -T postgres psql -U dev -d novel_ai < backup.sql

# Neo4j 백업 (선택적)
docker-compose exec neo4j neo4j-admin dump --database=neo4j --to=/backups/neo4j-backup.dump
```

---

## 성능 최적화

### Docker 이미지 크기 최적화
- 각 Dockerfile은 이미 multi-stage build를 사용하여 최적화되어 있습니다
- 추가 최적화가 필요한 경우 `.dockerignore` 파일 확인

### 컨테이너 리소스 제한
```yaml
# docker-compose.yml에 추가 (필요시)
services:
  api-server:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
```

### 데이터베이스 성능 튜닝
- `application-prod.properties`에서 HikariCP 연결 풀 설정 조정
- PostgreSQL 설정 튜닝 (향후 추가 예정)

---

## 참고 자료

- [Docker 공식 문서](https://docs.docker.com/)
- [Docker Compose 문서](https://docs.docker.com/compose/)
- [GitHub Actions 문서](https://docs.github.com/en/actions)
- [Spring Boot Docker 가이드](https://spring.io/guides/topicals/spring-boot-docker/)
- [Next.js Docker 배포](https://nextjs.org/docs/deployment#docker-image)
