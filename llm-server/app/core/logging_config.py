"""
구조화된 로깅 설정
JSON 형식 로그로 검색 및 분석 용이
"""

import logging
import sys
import os
from pythonjsonlogger import jsonlogger


class CustomJsonFormatter(jsonlogger.JsonFormatter):
    """커스텀 JSON 로거 포맷터"""

    def add_fields(self, log_record, record, message_dict):
        """로그 레코드에 추가 필드 포함"""
        super(CustomJsonFormatter, self).add_fields(log_record, record, message_dict)

        # 타임스탬프
        log_record["timestamp"] = self.formatTime(record, self.datefmt)

        # 로그 레벨
        log_record["level"] = record.levelname

        # 환경 정보
        log_record["environment"] = os.getenv("ENVIRONMENT", "development")

        # 요청 ID (미들웨어에서 설정되면 포함)
        if hasattr(record, "request_id"):
            log_record["request_id"] = record.request_id


def setup_logging(log_level: str = None) -> logging.Logger:
    """
    구조화된 JSON 로깅 설정

    Args:
        log_level: 로그 레벨 (DEBUG, INFO, WARNING, ERROR, CRITICAL)
                   None이면 환경변수 LOG_LEVEL 사용 (기본: INFO)

    Returns:
        설정된 루트 로거
    """
    # 로그 레벨 결정
    if log_level is None:
        log_level = os.getenv("LOG_LEVEL", "INFO").upper()

    # 핸들러 생성 (stdout으로 출력)
    log_handler = logging.StreamHandler(sys.stdout)

    # JSON 포맷터 적용
    formatter = CustomJsonFormatter(
        "%(timestamp)s %(level)s %(name)s %(message)s %(pathname)s %(lineno)d %(funcName)s"
    )
    log_handler.setFormatter(formatter)

    # 루트 로거 설정
    root_logger = logging.getLogger()

    # 기존 핸들러 제거 (중복 방지)
    root_logger.handlers.clear()

    root_logger.addHandler(log_handler)
    root_logger.setLevel(log_level)

    # 테스트 모드에서는 간단한 로깅
    if os.getenv("TESTING") == "true":
        # 테스트 중에는 WARNING 이상만 출력
        root_logger.setLevel(logging.WARNING)

    root_logger.info(
        f"Logging configured",
        extra={"log_level": log_level, "format": "JSON"},
    )

    return root_logger


def get_logger(name: str) -> logging.Logger:
    """
    모듈별 로거 가져오기

    Args:
        name: 로거 이름 (보통 __name__ 사용)

    Returns:
        로거 인스턴스
    """
    return logging.getLogger(name)
