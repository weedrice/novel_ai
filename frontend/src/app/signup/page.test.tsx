import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import SignupPage from './page';
import { signup, saveAuthData } from '@/lib/auth';

// Mock next/navigation
const mockPush = jest.fn();
jest.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}));

// Mock auth library
jest.mock('@/lib/auth', () => ({
  signup: jest.fn(),
  saveAuthData: jest.fn(),
}));

describe('SignupPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('페이지가 올바르게 렌더링된다', () => {
    render(<SignupPage />);

    expect(screen.getByText('Novel AI')).toBeInTheDocument();
    expect(screen.getByText('새 계정을 만들어 시작하세요')).toBeInTheDocument();
  });

  it('모든 입력 필드가 있다', () => {
    render(<SignupPage />);

    expect(screen.getByLabelText('사용자명')).toBeInTheDocument();
    expect(screen.getByLabelText('이메일')).toBeInTheDocument();
    expect(screen.getByLabelText(/^비밀번호$/)).toBeInTheDocument();
    expect(screen.getByLabelText('비밀번호 확인')).toBeInTheDocument();
  });

  it('회원가입 버튼이 있다', () => {
    render(<SignupPage />);

    expect(screen.getByRole('button', { name: '회원가입' })).toBeInTheDocument();
  });

  it('로그인 링크가 있다', () => {
    render(<SignupPage />);

    const loginLink = screen.getByRole('link', { name: '로그인' });
    expect(loginLink).toBeInTheDocument();
    expect(loginLink).toHaveAttribute('href', '/login');
  });

  it('사용자 입력이 올바르게 동작한다', async () => {
    const user = userEvent.setup();
    render(<SignupPage />);

    const usernameInput = screen.getByLabelText('사용자명');
    const emailInput = screen.getByLabelText('이메일');
    const passwordInput = screen.getByLabelText(/^비밀번호$/);
    const confirmPasswordInput = screen.getByLabelText('비밀번호 확인');

    await user.type(usernameInput, 'testuser');
    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    await user.type(confirmPasswordInput, 'password123');

    expect(usernameInput).toHaveValue('testuser');
    expect(emailInput).toHaveValue('test@example.com');
    expect(passwordInput).toHaveValue('password123');
    expect(confirmPasswordInput).toHaveValue('password123');
  });

  it('비밀번호 불일치 시 에러 메시지를 표시한다', async () => {
    const user = userEvent.setup();
    render(<SignupPage />);

    const usernameInput = screen.getByLabelText('사용자명');
    const emailInput = screen.getByLabelText('이메일');
    const passwordInput = screen.getByLabelText(/^비밀번호$/);
    const confirmPasswordInput = screen.getByLabelText('비밀번호 확인');
    const submitButton = screen.getByRole('button', { name: '회원가입' });

    await user.type(usernameInput, 'testuser');
    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    await user.type(confirmPasswordInput, 'differentpassword');
    await user.click(submitButton);

    expect(screen.getByText('비밀번호가 일치하지 않습니다.')).toBeInTheDocument();
    expect(signup).not.toHaveBeenCalled();
  });

  it('비밀번호가 6자 미만일 때 에러 메시지를 표시한다', async () => {
    const user = userEvent.setup();
    render(<SignupPage />);

    const usernameInput = screen.getByLabelText('사용자명');
    const emailInput = screen.getByLabelText('이메일');
    const passwordInput = screen.getByLabelText(/^비밀번호$/);
    const confirmPasswordInput = screen.getByLabelText('비밀번호 확인');
    const submitButton = screen.getByRole('button', { name: '회원가입' });

    await user.type(usernameInput, 'testuser');
    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, '12345');
    await user.type(confirmPasswordInput, '12345');
    await user.click(submitButton);

    expect(screen.getByText('비밀번호는 최소 6자 이상이어야 합니다.')).toBeInTheDocument();
    expect(signup).not.toHaveBeenCalled();
  });

  it('회원가입 성공 시 홈으로 이동한다', async () => {
    const user = userEvent.setup();
    const mockSignupResponse = {
      token: 'mock-token',
      user: { id: 1, username: 'testuser', email: 'test@example.com' },
      refreshToken: 'mock-refresh-token',
    };

    (signup as jest.Mock).mockResolvedValue(mockSignupResponse);

    render(<SignupPage />);

    const usernameInput = screen.getByLabelText('사용자명');
    const emailInput = screen.getByLabelText('이메일');
    const passwordInput = screen.getByLabelText(/^비밀번호$/);
    const confirmPasswordInput = screen.getByLabelText('비밀번호 확인');
    const submitButton = screen.getByRole('button', { name: '회원가입' });

    await user.type(usernameInput, 'testuser');
    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    await user.type(confirmPasswordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(signup).toHaveBeenCalledWith({
        username: 'testuser',
        email: 'test@example.com',
        password: 'password123',
      });
      expect(saveAuthData).toHaveBeenCalledWith(
        'mock-token',
        { id: 1, username: 'testuser', email: 'test@example.com' },
        'mock-refresh-token'
      );
      expect(mockPush).toHaveBeenCalledWith('/');
    });
  });

  it('회원가입 실패 시 에러 메시지를 표시한다', async () => {
    const user = userEvent.setup();
    const errorMessage = '이미 사용 중인 사용자명입니다.';

    (signup as jest.Mock).mockRejectedValue({
      response: {
        data: {
          error: errorMessage,
        },
      },
    });

    render(<SignupPage />);

    const usernameInput = screen.getByLabelText('사용자명');
    const emailInput = screen.getByLabelText('이메일');
    const passwordInput = screen.getByLabelText(/^비밀번호$/);
    const confirmPasswordInput = screen.getByLabelText('비밀번호 확인');
    const submitButton = screen.getByRole('button', { name: '회원가입' });

    await user.type(usernameInput, 'existinguser');
    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    await user.type(confirmPasswordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('회원가입 중에는 버튼이 비활성화된다', async () => {
    const user = userEvent.setup();
    let resolveSignup: any;
    const signupPromise = new Promise((resolve) => {
      resolveSignup = resolve;
    });

    (signup as jest.Mock).mockReturnValue(signupPromise);

    render(<SignupPage />);

    const usernameInput = screen.getByLabelText('사용자명');
    const emailInput = screen.getByLabelText('이메일');
    const passwordInput = screen.getByLabelText(/^비밀번호$/);
    const confirmPasswordInput = screen.getByLabelText('비밀번호 확인');
    const submitButton = screen.getByRole('button', { name: '회원가입' });

    await user.type(usernameInput, 'testuser');
    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    await user.type(confirmPasswordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: '가입 중...' })).toBeDisabled();
    });

    // 회원가입 완료
    resolveSignup({
      token: 'mock-token',
      user: { id: 1, username: 'testuser', email: 'test@example.com' },
      refreshToken: 'mock-refresh-token',
    });
  });
});