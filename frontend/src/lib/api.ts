import axios from 'axios';
import { env } from './env';

// 브라우저 환경 체크 유틸리티
const isBrowser = typeof window !== 'undefined';

// Axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: env.API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터: JWT 토큰 자동 추가 (브라우저 환경에서만)
apiClient.interceptors.request.use(
  (config) => {
    if (isBrowser) {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터: 401 에러 시 Refresh Token으로 자동 갱신 시도
let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });

  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // 브라우저 환경에서만 토큰 갱신 처리
    if (!isBrowser) {
      return Promise.reject(error);
    }

    // 401 에러이고, 아직 재시도하지 않은 요청인 경우
    if (error.response?.status === 401 && !originalRequest._retry) {
      // /auth/refresh 요청이 실패한 경우는 바로 로그아웃
      if (originalRequest.url?.includes('/auth/refresh')) {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');

        if (!window.location.pathname.startsWith('/login') &&
            !window.location.pathname.startsWith('/signup')) {
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }

      // Refresh Token이 처리 중인 경우 대기열에 추가
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return apiClient(originalRequest);
          })
          .catch((err) => {
            return Promise.reject(err);
          });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');

      if (!refreshToken) {
        // Refresh Token이 없으면 로그아웃
        isRefreshing = false;
        localStorage.removeItem('token');
        localStorage.removeItem('user');

        if (!window.location.pathname.startsWith('/login') &&
            !window.location.pathname.startsWith('/signup')) {
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }

      try {
        // Refresh Token으로 새 Access Token 발급
        const response = await apiClient.post('/auth/refresh', { refreshToken });
        const { token } = response.data;

        // 새 토큰 저장
        localStorage.setItem('token', token);

        // 대기 중인 요청들 처리
        processQueue(null, token);

        // 원래 요청 재시도
        originalRequest.headers.Authorization = `Bearer ${token}`;
        isRefreshing = false;

        return apiClient(originalRequest);
      } catch (refreshError) {
        // Refresh Token도 만료된 경우 로그아웃
        processQueue(refreshError, null);
        isRefreshing = false;

        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');

        if (!window.location.pathname.startsWith('/login') &&
            !window.location.pathname.startsWith('/signup')) {
          window.location.href = '/login';
        }

        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;