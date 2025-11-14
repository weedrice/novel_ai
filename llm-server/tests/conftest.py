"""
Pytest configuration and fixtures
"""

import os

# IMPORTANT: Set TESTING env var BEFORE any app imports
# This must be at module level to ensure it's set before app.core.rate_limiter is imported
os.environ["TESTING"] = "true"

import pytest
from unittest.mock import Mock, AsyncMock
from fastapi.testclient import TestClient


@pytest.fixture(scope="session", autouse=True)
def set_test_env():
    """Ensure test environment variables are set"""
    # Already set at module level, but keep this to clean up after tests
    yield
    os.environ.pop("TESTING", None)
