import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import LoginPage from './page';
import { login, saveAuthData } from '@/lib/auth';

// Mock next/navigation
const mockPush = jest.fn();
jest.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}));

// Mock auth library
jest.mock('@/lib/auth', () => ({
  login: jest.fn(),
  saveAuthData: jest.fn(),
}));

describe('LoginPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('페이지가 올바르게 렌더링된다', () => {
    render(<LoginPage />);

    expect(screen.getByText('Novel AI')).toBeInTheDocument();
    expect(screen.getByText('로그인하여 프로젝트에 접근하세요')).toBeInTheDocument();
  });

  it('사용자명과 비밀번호 입력 필드가 있다', () => {
    render(<LoginPage />);

    expect(screen.getByLabelText('사용자명')).toBeInTheDocument();
    expect(screen.getByLabelText('비밀번호')).toBeInTheDocument();
  });

  it('로그인 버튼이 있다', () => {
    render(<LoginPage />);

    expect(screen.getByRole('button', { name: '로그인' })).toBeInTheDocument();
  });

  it('회원가입 링크가 있다', () => {
    render(<LoginPage />);

    const signupLink = screen.getByRole('link', { name: '회원가입' });
    expect(signupLink).toBeInTheDocument();
    expect(signupLink).toHaveAttribute('href', '/signup');
  });

  it('사용자 입력이 올바르게 동작한다', async () => {
    const user = userEvent.setup();
    render(<LoginPage />);

    const usernameInput = screen.getByLabelText('사용자명');
    const passwordInput = screen.getByLabelText('비밀번호');

    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'password123');

    expect(usernameInput).toHaveValue('testuser');
    expect(passwordInput).toHaveValue('password123');
  });

  it('로그인 성공 시 홈으로 이동한다', async () => {
    const user = userEvent.setup();
    const mockLoginResponse = {
      token: 'mock-token',
      user: { id: 1, username: 'testuser' },
      refreshToken: 'mock-refresh-token',
    };

    (login as jest.Mock).mockResolvedValue(mockLoginResponse);

    render(<LoginPage />);

    const usernameInput = screen.getByLabelText('사용자명');
    const passwordInput = screen.getByLabelText('비밀번호');
    const submitButton = screen.getByRole('button', { name: '로그인' });

    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(login).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'password123',
      });
      expect(saveAuthData).toHaveBeenCalledWith(
        'mock-token',
        { id: 1, username: 'testuser' },
        'mock-refresh-token'
      );
      expect(mockPush).toHaveBeenCalledWith('/');
    });
  });

  it('로그인 실패 시 에러 메시지를 표시한다', async () => {
    const user = userEvent.setup();
    const errorMessage = '로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.';

    (login as jest.Mock).mockRejectedValue({
      response: {
        data: {
          error: errorMessage,
        },
      },
    });

    render(<LoginPage />);

    const usernameInput = screen.getByLabelText('사용자명');
    const passwordInput = screen.getByLabelText('비밀번호');
    const submitButton = screen.getByRole('button', { name: '로그인' });

    await user.type(usernameInput, 'wronguser');
    await user.type(passwordInput, 'wrongpassword');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('로그인 중에는 버튼이 비활성화된다', async () => {
    const user = userEvent.setup();
    let resolveLogin: any;
    const loginPromise = new Promise((resolve) => {
      resolveLogin = resolve;
    });

    (login as jest.Mock).mockReturnValue(loginPromise);

    render(<LoginPage />);

    const usernameInput = screen.getByLabelText('사용자명');
    const passwordInput = screen.getByLabelText('비밀번호');
    const submitButton = screen.getByRole('button', { name: '로그인' });

    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: '로그인 중...' })).toBeDisabled();
    });

    // 로그인 완료
    resolveLogin({
      token: 'mock-token',
      user: { id: 1, username: 'testuser' },
      refreshToken: 'mock-refresh-token',
    });
  });
});