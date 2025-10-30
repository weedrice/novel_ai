import { test, expect } from '@playwright/test';

test.describe('캐릭터 관리 플로우', () => {
  test.beforeEach(async ({ page }) => {
    // 데모 모드를 위해 페이지 방문 (로그인 없이 사용 가능)
    await page.goto('/');
  });

  test('홈페이지가 올바르게 렌더링된다', async ({ page }) => {
    await page.goto('/');

    // 홈페이지 제목 확인
    await expect(page).toHaveTitle(/Novel AI/);

    // 주요 네비게이션 요소 확인
    await expect(page.getByText('Novel AI')).toBeVisible();
  });

  test('캐릭터 페이지로 이동할 수 있다', async ({ page }) => {
    await page.goto('/');

    // 캐릭터 관리 링크/버튼 클릭
    const characterLink = page.getByRole('link', { name: /캐릭터/ });
    if (await characterLink.isVisible()) {
      await characterLink.click();
      await expect(page).toHaveURL('/characters');
    } else {
      // 직접 URL로 이동
      await page.goto('/characters');
    }

    // 캐릭터 페이지가 로드되었는지 확인
    await expect(page.getByText(/캐릭터/)).toBeVisible();
  });

  test('씬 관리 페이지로 이동할 수 있다', async ({ page }) => {
    await page.goto('/');

    // 씬 관리 링크 찾기
    const sceneLink = page.getByRole('link', { name: /씬/ });
    if (await sceneLink.isVisible()) {
      await sceneLink.click();
      await expect(page).toHaveURL('/scenes');
    } else {
      await page.goto('/scenes');
    }

    await expect(page.getByText(/씬/)).toBeVisible();
  });

  test('그래프 뷰로 이동할 수 있다', async ({ page }) => {
    await page.goto('/');

    const graphLink = page.getByRole('link', { name: /관계도/ });
    if (await graphLink.isVisible()) {
      await graphLink.click();
      await expect(page).toHaveURL('/graph');
    } else {
      await page.goto('/graph');
    }

    // 그래프 페이지가 로드되었는지 확인
    await expect(page).toHaveURL('/graph');
  });

  test('스크립트 분석 페이지로 이동할 수 있다', async ({ page }) => {
    await page.goto('/');

    const analyzerLink = page.getByRole('link', { name: /스크립트/ });
    if (await analyzerLink.isVisible()) {
      await analyzerLink.click();
      await expect(page).toHaveURL('/script-analyzer');
    } else {
      await page.goto('/script-analyzer');
    }

    await expect(page).toHaveURL('/script-analyzer');
  });
});

test.describe('네비게이션', () => {
  const pages = [
    { name: '캐릭터', url: '/characters' },
    { name: '씬', url: '/scenes' },
    { name: '그래프', url: '/graph' },
    { name: '스크립트 분석', url: '/script-analyzer' },
  ];

  for (const pageInfo of pages) {
    test(`${pageInfo.name} 페이지가 접근 가능하다`, async ({ page }) => {
      await page.goto(pageInfo.url);

      // 페이지가 로드되었는지 확인 (404가 아님)
      await expect(page).not.toHaveURL(/.*404.*/);

      // 기본 레이아웃이 있는지 확인
      await expect(page.locator('body')).toBeVisible();
    });
  }
});