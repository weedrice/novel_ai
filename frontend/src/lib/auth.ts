import apiClient from './api';

export interface User {
  id: number;
  username: string;
  email: string;
  role: string;
}

export interface LoginResponse {
  token: string; // Access Token
  refreshToken: string; // Refresh Token
  user: User;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * JWT 토큰 디코딩 (페이로드만 추출)
 */
const decodeJWT = (token: string): any => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('JWT 디코딩 실패:', error);
    return null;
  }
};

/**
 * JWT 토큰 만료 여부 확인
 */
export const isTokenExpired = (token: string): boolean => {
  try {
    const payload = decodeJWT(token);
    if (!payload || !payload.exp) {
      return true; // 페이로드나 만료 시간이 없으면 만료로 간주
    }

    // exp는 Unix timestamp (초 단위)
    const currentTime = Math.floor(Date.now() / 1000);
    return payload.exp < currentTime;
  } catch (error) {
    console.error('토큰 만료 체크 실패:', error);
    return true; // 에러 시 만료로 간주
  }
};

/**
 * JWT 토큰 만료 시간 가져오기 (Unix timestamp)
 */
export const getTokenExpiryTime = (token: string): number | null => {
  try {
    const payload = decodeJWT(token);
    return payload?.exp || null;
  } catch (error) {
    console.error('토큰 만료 시간 추출 실패:', error);
    return null;
  }
};

/**
 * 저장된 토큰의 유효성 검증
 */
export const validateStoredToken = (): boolean => {
  if (typeof window === 'undefined') return false;

  const token = localStorage.getItem('token');
  if (!token) return false;

  // 토큰 만료 확인
  if (isTokenExpired(token)) {
    console.warn('저장된 토큰이 만료되었습니다. 로그아웃합니다.');
    logout();
    return false;
  }

  return true;
};

/**
 * 회원가입
 */
export const signup = async (data: SignupRequest): Promise<LoginResponse> => {
  const response = await apiClient.post<LoginResponse>('/auth/signup', data);
  return response.data;
};

/**
 * 로그인
 */
export const login = async (data: LoginRequest): Promise<LoginResponse> => {
  const response = await apiClient.post<LoginResponse>('/auth/login', data);
  return response.data;
};

/**
 * 로그아웃
 */
export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
  window.location.href = '/login';
};

/**
 * 현재 로그인된 사용자 정보 가져오기
 */
export const getCurrentUser = (): User | null => {
  if (typeof window === 'undefined') return null;

  const userStr = localStorage.getItem('user');
  if (!userStr) return null;

  try {
    return JSON.parse(userStr) as User;
  } catch {
    return null;
  }
};

/**
 * 로그인 여부 확인 (토큰 만료 체크 포함)
 */
export const isAuthenticated = (): boolean => {
  if (typeof window === 'undefined') return false;

  const token = localStorage.getItem('token');
  if (!token) return false;

  // 토큰 만료 확인
  if (isTokenExpired(token)) {
    console.warn('토큰이 만료되어 로그아웃 처리합니다.');
    logout();
    return false;
  }

  return true;
};

/**
 * 토큰 및 사용자 정보 저장
 */
export const saveAuthData = (token: string, user: User, refreshToken?: string) => {
  localStorage.setItem('token', token);
  localStorage.setItem('user', JSON.stringify(user));
  if (refreshToken) {
    localStorage.setItem('refreshToken', refreshToken);
  }
};

/**
 * Refresh Token 가져오기
 */
export const getRefreshToken = (): string | null => {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('refreshToken');
};

/**
 * Access Token 갱신
 */
export const refreshAccessToken = async (): Promise<string | null> => {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    console.warn('Refresh Token not found');
    return null;
  }

  try {
    const response = await apiClient.post('/auth/refresh', { refreshToken });
    const { token } = response.data;

    // 새 Access Token 저장
    localStorage.setItem('token', token);
    console.log('Access Token refreshed successfully');

    return token;
  } catch (error: any) {
    console.error('Failed to refresh access token:', error);
    // Refresh Token도 만료된 경우 로그아웃
    if (error.response?.status === 401) {
      logout();
    }
    return null;
  }
};