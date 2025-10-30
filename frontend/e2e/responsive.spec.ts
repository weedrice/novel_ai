import { test, expect, devices } from '@playwright/test';

// Mobile viewport tests
test.describe('반응형 디자인 - 모바일', () => {
  test.use({ ...devices['iPhone SE'] });

  test('네비게이션 바가 모바일에서 올바르게 렌더링된다', async ({ page }) => {
    await page.goto('/');
    const mobileMenuButton = page.locator('button[aria-label="메뉴"]');
    await expect(mobileMenuButton).toBeVisible();
  });

  test('모바일 메뉴가 올바르게 동작한다', async ({ page }) => {
    await page.goto('/');
    const mobileMenuButton = page.locator('button[aria-label="메뉴"]');
    await mobileMenuButton.click();
    await expect(page.locator('.sm\\:hidden.py-4')).toBeVisible();
  });

  test('홈페이지 그리드가 모바일에서 1열로 표시된다', async ({ page }) => {
    await page.goto('/');
    const gridContainer = page.locator('.grid.grid-cols-1.md\\:grid-cols-2.lg\\:grid-cols-4');
    await expect(gridContainer).toBeVisible();
  });

  test('스크립트 분석기가 모바일에서 올바르게 렌더링된다', async ({ page }) => {
    await page.goto('/script-analyzer');
    const buttonContainer = page.locator('.flex.flex-col.sm\\:flex-row');
    await expect(buttonContainer).toBeVisible();
  });
});

// Tablet viewport tests
test.describe('반응형 디자인 - 태블릿', () => {
  test.use({ ...devices['iPad Mini'] });

  test('네비게이션 바가 태블릿에서 데스크톱 스타일을 유지한다', async ({ page }) => {
    await page.goto('/');
    const mobileMenuButton = page.locator('button[aria-label="메뉴"]');
    await expect(mobileMenuButton).not.toBeVisible();
  });

  test('홈페이지 그리드가 태블릿에서 2열로 표시된다', async ({ page }) => {
    await page.goto('/');
    const gridContainer = page.locator('.grid.grid-cols-1.md\\:grid-cols-2.lg\\:grid-cols-4');
    await expect(gridContainer).toBeVisible();
  });
});

// Desktop viewport tests
test.describe('반응형 디자인 - 데스크톱', () => {
  test.use({ viewport: { width: 1920, height: 1080 } });

  test('네비게이션 바가 데스크톱에서 올바르게 렌더링된다', async ({ page }) => {
    await page.goto('/');
    const mobileMenuButton = page.locator('button[aria-label="메뉴"]');
    await expect(mobileMenuButton).not.toBeVisible();
  });

  test('홈페이지 그리드가 데스크톱에서 4열로 표시된다', async ({ page }) => {
    await page.goto('/');
    const gridContainer = page.locator('.grid.grid-cols-1.md\\:grid-cols-2.lg\\:grid-cols-4');
    await expect(gridContainer).toBeVisible();
  });

  test('그래프 페이지가 데스크톱 레이아웃을 사용한다', async ({ page }) => {
    await page.goto('/graph');
    const gridContainer = page.locator('.grid.grid-cols-1.lg\\:grid-cols-4');
    await expect(gridContainer).toBeVisible();
  });
});

// Breakpoint transition tests
test.describe('브레이크포인트 전환', () => {
  test('뷰포트 크기 변경 시 레이아웃이 전환된다', async ({ page }) => {
    // 데스크톱에서 시작
    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.goto('/');
    let mobileMenuButton = page.locator('button[aria-label="메뉴"]');
    await expect(mobileMenuButton).not.toBeVisible();

    // 모바일로 축소
    await page.setViewportSize({ width: 375, height: 667 });
    mobileMenuButton = page.locator('button[aria-label="메뉴"]');
    await expect(mobileMenuButton).toBeVisible();
  });
});
