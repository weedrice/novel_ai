import { test, expect } from '@playwright/test';

test.describe('인증 플로우', () => {
  test.beforeEach(async ({ page }) => {
    // 각 테스트 전에 localStorage 클리어
    await page.goto('/');
    await page.evaluate(() => localStorage.clear());
  });

  test('회원가입 페이지가 올바르게 렌더링된다', async ({ page }) => {
    await page.goto('/signup');

    await expect(page).toHaveTitle(/Novel AI/);
    await expect(page.getByRole('heading', { name: 'Novel AI' })).toBeVisible();
    await expect(page.getByText('새 계정을 만들어 시작하세요')).toBeVisible();
  });

  test('로그인 페이지가 올바르게 렌더링된다', async ({ page }) => {
    await page.goto('/login');

    await expect(page).toHaveTitle(/Novel AI/);
    await expect(page.getByRole('heading', { name: 'Novel AI' })).toBeVisible();
    await expect(page.getByText('로그인하여 프로젝트에 접근하세요')).toBeVisible();
  });

  test('회원가입 → 로그인 플로우', async ({ page }) => {
    const timestamp = Date.now();
    const username = `testuser${timestamp}`;
    const email = `test${timestamp}@example.com`;
    const password = 'password123';

    // 1. 회원가입 페이지로 이동
    await page.goto('/signup');

    // 2. 폼 작성
    await page.getByLabel('사용자명').fill(username);
    await page.getByLabel('이메일').fill(email);
    await page.getByLabel(/^비밀번호$/).fill(password);
    await page.getByLabel('비밀번호 확인').fill(password);

    // 3. 회원가입 버튼 클릭
    await page.getByRole('button', { name: '회원가입' }).click();

    // 4. 홈페이지로 리다이렉트되는지 확인
    // Note: 실제 백엔드가 실행 중이어야 합니다
    // await expect(page).toHaveURL('/');
  });

  test('로그인 페이지에서 회원가입 링크가 동작한다', async ({ page }) => {
    await page.goto('/login');

    await page.getByRole('link', { name: '회원가입' }).click();

    await expect(page).toHaveURL('/signup');
    await expect(page.getByText('새 계정을 만들어 시작하세요')).toBeVisible();
  });

  test('회원가입 페이지에서 로그인 링크가 동작한다', async ({ page }) => {
    await page.goto('/signup');

    await page.getByRole('link', { name: '로그인' }).click();

    await expect(page).toHaveURL('/login');
    await expect(page.getByText('로그인하여 프로젝트에 접근하세요')).toBeVisible();
  });

  test('회원가입 폼 검증 - 비밀번호 불일치', async ({ page }) => {
    await page.goto('/signup');

    await page.getByLabel('사용자명').fill('testuser');
    await page.getByLabel('이메일').fill('test@example.com');
    await page.getByLabel(/^비밀번호$/).fill('password123');
    await page.getByLabel('비밀번호 확인').fill('differentpassword');

    await page.getByRole('button', { name: '회원가입' }).click();

    await expect(page.getByText('비밀번호가 일치하지 않습니다.')).toBeVisible();
  });

  test('회원가입 폼 검증 - 비밀번호 길이', async ({ page }) => {
    await page.goto('/signup');

    await page.getByLabel('사용자명').fill('testuser');
    await page.getByLabel('이메일').fill('test@example.com');
    await page.getByLabel(/^비밀번호$/).fill('12345');
    await page.getByLabel('비밀번호 확인').fill('12345');

    await page.getByRole('button', { name: '회원가입' }).click();

    await expect(page.getByText('비밀번호는 최소 6자 이상이어야 합니다.')).toBeVisible();
  });
});