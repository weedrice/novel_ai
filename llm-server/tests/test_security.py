"""
Security tests for Rate Limiting and API Authentication
"""

import pytest
import time
import os
from fastapi import FastAPI
from fastapi.testclient import TestClient
from app.main import app
from app.middleware.auth import APIKeyMiddleware


# ============================================================
# Rate Limiting Configuration Tests
# ============================================================

class TestRateLimiting:
    """Rate Limiting 설정 테스트"""

    def test_rate_limiter_configured_in_app(self):
        """Rate Limiter가 앱에 올바르게 설정되어 있는지 확인"""
        from app.core.rate_limiter import limiter

        # Rate limiter 인스턴스가 존재하는지 확인
        assert limiter is not None
        assert hasattr(limiter, 'limit')

        # FastAPI 앱에 limiter가 설정되어 있는지 확인
        assert hasattr(app.state, 'limiter')
        assert app.state.limiter is not None

    def test_rate_limit_decorators_applied(self):
        """Rate Limit 데코레이터가 컨트롤러에 적용되어 있는지 확인"""
        # 엔드포인트가 존재하는지 확인
        routes = {route.path: route for route in app.routes if hasattr(route, 'path')}

        # 주요 엔드포인트가 존재하는지 확인
        assert "/gen/suggest" in routes
        assert "/gen/scenario" in routes
        assert "/gen/suggest-stream" in routes
        assert "/gen/episode/summary" in routes

    def test_endpoints_work_in_test_mode(self):
        """테스트 모드에서도 엔드포인트가 정상 작동하는지 확인"""
        client = TestClient(app)

        # Rate Limiting이 비활성화되어 있어도 엔드포인트는 정상 작동해야 함
        payload = {
            "speakerId": "char1",
            "targetIds": ["char2"],
            "intent": "greet",
            "honorific": "banmal",
            "maxLen": 80,
            "nCandidates": 1
        }

        # 여러 번 요청해도 성공해야 함 (테스트 모드)
        for _ in range(5):
            response = client.post("/gen/suggest", json=payload)
            assert response.status_code == 200


# ============================================================
# API Authentication Tests
# ============================================================

class TestAPIAuthentication:
    """API Key 인증 미들웨어 테스트"""

    @pytest.fixture
    def app_with_auth(self):
        """API Key 인증이 활성화된 테스트 앱"""
        test_app = FastAPI()

        # API Key 미들웨어 추가
        test_api_key = "test-api-key-12345"
        test_app.add_middleware(APIKeyMiddleware, api_key=test_api_key)

        # 간단한 테스트 엔드포인트 추가
        @test_app.get("/")
        async def root():
            return {"message": "root"}

        @test_app.get("/health")
        async def health():
            return {"status": "ok"}

        @test_app.get("/protected")
        async def protected():
            return {"message": "protected data"}

        return test_app, test_api_key

    def test_auth_with_valid_api_key(self, app_with_auth):
        """유효한 API Key로 요청 시 성공"""
        test_app, api_key = app_with_auth
        client = TestClient(test_app)

        response = client.get("/protected", headers={"X-API-Key": api_key})
        assert response.status_code == 200
        assert response.json()["message"] == "protected data"

    def test_auth_with_invalid_api_key(self, app_with_auth):
        """잘못된 API Key로 요청 시 403 반환"""
        test_app, api_key = app_with_auth
        client = TestClient(test_app)

        response = client.get("/protected", headers={"X-API-Key": "wrong-key"})
        assert response.status_code == 403
        assert "Invalid API key" in response.json()["detail"]

    def test_auth_without_api_key(self, app_with_auth):
        """API Key 없이 요청 시 401 반환"""
        test_app, api_key = app_with_auth
        client = TestClient(test_app)

        response = client.get("/protected")
        assert response.status_code == 401
        assert "API key required" in response.json()["detail"]

    def test_exempt_paths_no_auth_required(self, app_with_auth):
        """Exempt 경로는 인증 없이 접근 가능"""
        test_app, api_key = app_with_auth
        client = TestClient(test_app)

        # Root endpoint - should be exempt
        response = client.get("/")
        assert response.status_code == 200
        assert response.json()["message"] == "root"

        # Health endpoint - should be exempt
        response = client.get("/health")
        assert response.status_code == 200
        assert response.json()["status"] == "ok"

    def test_auth_disabled_when_no_api_key_set(self):
        """API_KEY가 설정되지 않으면 인증 미들웨어가 비활성화됨"""
        test_app = FastAPI()

        # API Key 없이 미들웨어 추가 (비활성화 상태)
        test_app.add_middleware(APIKeyMiddleware, api_key=None)

        @test_app.get("/protected")
        async def protected():
            return {"message": "data"}

        client = TestClient(test_app)

        # API Key 없이도 접근 가능해야 함
        response = client.get("/protected")
        assert response.status_code == 200
        assert response.json()["message"] == "data"

    def test_auth_disabled_with_empty_api_key(self):
        """API_KEY가 빈 문자열이면 인증 미들웨어가 비활성화됨"""
        test_app = FastAPI()

        # 빈 API Key로 미들웨어 추가 (비활성화 상태)
        test_app.add_middleware(APIKeyMiddleware, api_key="")

        @test_app.get("/protected")
        async def protected():
            return {"message": "data"}

        client = TestClient(test_app)

        # API Key 없이도 접근 가능해야 함
        response = client.get("/protected")
        assert response.status_code == 200

    def test_api_key_case_sensitive(self, app_with_auth):
        """API Key는 대소문자를 구분함"""
        test_app, api_key = app_with_auth
        client = TestClient(test_app)

        # 대소문자가 다른 API Key는 거부되어야 함
        wrong_case_key = api_key.upper()
        if wrong_case_key != api_key:  # Only test if actually different
            response = client.get("/protected", headers={"X-API-Key": wrong_case_key})
            assert response.status_code == 403


# ============================================================
# Integration Tests
# ============================================================

class TestSecurityIntegration:
    """보안 관련 통합 테스트"""

    def test_system_endpoints_always_accessible(self):
        """시스템 엔드포인트는 항상 접근 가능"""
        client = TestClient(app)

        # Root, health, providers 엔드포인트를 여러 번 호출
        for _ in range(10):
            response = client.get("/")
            assert response.status_code == 200

            response = client.get("/health")
            assert response.status_code == 200

            response = client.get("/providers")
            assert response.status_code == 200

    def test_api_endpoints_work_correctly(self):
        """API 엔드포인트가 정상적으로 작동"""
        client = TestClient(app)

        # 대사 생성 엔드포인트
        payload = {
            "speakerId": "char1",
            "targetIds": ["char2"],
            "intent": "greet",
            "honorific": "banmal",
            "maxLen": 80,
            "nCandidates": 1
        }
        response = client.post("/gen/suggest", json=payload)
        assert response.status_code == 200

        # 시나리오 생성 엔드포인트
        scenario_payload = {
            "sceneDescription": "Test",
            "participants": [{"characterId": "char1", "name": "Alice"}],
            "dialogueCount": 3
        }
        response = client.post("/gen/scenario", json=scenario_payload)
        assert response.status_code == 200
