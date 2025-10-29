# Novel AI - 디자인 시스템 가이드

> 프로젝트 전반에 걸쳐 일관된 디자인 경험을 제공하기 위한 디자인 시스템 문서
> 마지막 업데이트: 2025-10-29

---

## 📖 목차

1. [디자인 원칙](#디자인-원칙)
2. [색상 팔레트](#색상-팔레트)
3. [타이포그래피](#타이포그래피)
4. [간격 시스템](#간격-시스템)
5. [컴포넌트 라이브러리](#컴포넌트-라이브러리)
6. [다크 모드](#다크-모드)
7. [접근성](#접근성)

---

## 🎨 디자인 원칙

### 1. 일관성 (Consistency)
- 모든 페이지와 컴포넌트에서 동일한 패턴과 스타일을 사용합니다.
- 디자인 토큰을 통해 색상, 간격, 타이포그래피를 통일합니다.

### 2. 접근성 (Accessibility)
- WCAG 2.1 AA 수준 이상의 접근성을 목표로 합니다.
- 키보드 네비게이션, 스크린 리더 지원, 충분한 색상 대비를 보장합니다.

### 3. 반응성 (Responsiveness)
- 모바일, 태블릿, 데스크톱 모든 화면 크기에서 최적의 경험을 제공합니다.
- Mobile-first 접근 방식을 사용합니다.

### 4. 사용자 경험 (User Experience)
- 직관적이고 명확한 인터페이스를 제공합니다.
- 로딩 상태, 에러 메시지, 성공 알림을 명확히 표시합니다.

---

## 🎨 색상 팔레트

### Primary (파란색) - 주요 액션 및 브랜드 색상
```css
--color-primary-50: #eff6ff;   /* 매우 밝음 */
--color-primary-100: #dbeafe;
--color-primary-200: #bfdbfe;
--color-primary-300: #93c5fd;
--color-primary-400: #60a5fa;
--color-primary-500: #3b82f6;  /* 기본 */
--color-primary-600: #2563eb;
--color-primary-700: #1d4ed8;
--color-primary-800: #1e40af;
--color-primary-900: #1e3a8a;  /* 매우 어두움 */
```

**사용 예시:**
- 기본 버튼, 링크, 포커스 링
- `bg-primary-500`, `text-primary-600`, `border-primary-500`

### Secondary (보라색) - 보조 액션
```css
--color-secondary-50: #faf5ff;
--color-secondary-100: #f3e8ff;
--color-secondary-200: #e9d5ff;
--color-secondary-300: #d8b4fe;
--color-secondary-400: #c084fc;
--color-secondary-500: #a855f7;  /* 기본 */
--color-secondary-600: #9333ea;
--color-secondary-700: #7e22ce;
--color-secondary-800: #6b21a8;
--color-secondary-900: #581c87;
```

**사용 예시:**
- 보조 버튼, 배지, 하이라이트
- `bg-secondary-500`, `text-secondary-600`

### Success (초록색) - 성공 상태
```css
--color-success-500: #22c55e;  /* 기본 */
--color-success-600: #16a34a;
```

**사용 예시:**
- 성공 메시지, 체크마크, 저장 완료 알림
- `bg-success-600`, `text-success-500`

### Warning (노란색) - 경고 상태
```css
--color-warning-500: #eab308;  /* 기본 */
--color-warning-600: #ca8a04;
```

**사용 예시:**
- 경고 메시지, 주의 필요 상태
- `bg-warning-500`, `text-warning-600`

### Danger (빨간색) - 에러 및 삭제
```css
--color-danger-500: #ef4444;  /* 기본 */
--color-danger-600: #dc2626;
```

**사용 예시:**
- 에러 메시지, 삭제 버튼, 필수 필드 표시
- `bg-danger-600`, `text-danger-500`

### Neutral (회색) - 텍스트 및 배경
```css
/* Light Mode */
background: white
text: gray-900
border: gray-300

/* Dark Mode */
background: gray-800
text: gray-100
border: gray-600
```

---

## 📝 타이포그래피

### 폰트 패밀리
```css
font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
  Helvetica, Arial, 'Apple Color Emoji', 'Segoe UI Emoji', sans-serif;
```

### 폰트 크기

| 크기명 | Tailwind Class | 픽셀 크기 | 사용 예시 |
|--------|---------------|----------|----------|
| XS | `text-xs` | 12px | 캡션, 작은 레이블 |
| SM | `text-sm` | 14px | 본문 보조, 헬퍼 텍스트 |
| Base | `text-base` | 16px | 기본 본문 |
| LG | `text-lg` | 18px | 강조 본문 |
| XL | `text-xl` | 20px | 소제목 |
| 2XL | `text-2xl` | 24px | 카드 제목 |
| 3XL | `text-3xl` | 30px | 섹션 제목 |
| 4XL | `text-4xl` | 36px | 페이지 제목 |

### 폰트 굵기

| 굵기명 | Tailwind Class | 값 | 사용 예시 |
|--------|---------------|-----|----------|
| Normal | `font-normal` | 400 | 기본 본문 |
| Medium | `font-medium` | 500 | 버튼, 레이블 |
| Semibold | `font-semibold` | 600 | 소제목 |
| Bold | `font-bold` | 700 | 제목, 강조 |

### 행간 (Line Height)

| Tailwind Class | 값 | 사용 예시 |
|---------------|-----|----------|
| `leading-tight` | 1.25 | 제목 |
| `leading-normal` | 1.5 | 기본 본문 |
| `leading-relaxed` | 1.625 | 긴 본문, 가독성 중요 |

---

## 📏 간격 시스템

### 기본 간격 (Spacing Scale)

Tailwind CSS의 기본 4px 단위 스케일을 사용합니다:

| 값 | 픽셀 | Tailwind Class |
|----|------|---------------|
| 0 | 0px | `p-0`, `m-0` |
| 1 | 4px | `p-1`, `m-1` |
| 2 | 8px | `p-2`, `m-2` |
| 3 | 12px | `p-3`, `m-3` |
| 4 | 16px | `p-4`, `m-4` |
| 6 | 24px | `p-6`, `m-6` |
| 8 | 32px | `p-8`, `m-8` |
| 12 | 48px | `p-12`, `m-12` |
| 16 | 64px | `p-16`, `m-16` |

### 컴포넌트별 권장 간격

- **Card 패딩**: `p-6` (24px)
- **Button 패딩**: `px-4 py-2` (16px 8px)
- **Input 패딩**: `px-4 py-2` (16px 8px)
- **섹션 간격**: `mb-8` (32px)
- **요소 간격**: `gap-4` (16px)

---

## 🧩 컴포넌트 라이브러리

### Button

```tsx
import Button from '@/components/ui/Button';

// 기본 사용
<Button>클릭</Button>

// Variant 및 Size
<Button variant="primary" size="md">Primary</Button>
<Button variant="secondary" size="sm">Secondary</Button>
<Button variant="danger" size="lg">Delete</Button>

// 로딩 상태
<Button loading>저장 중...</Button>

// 비활성화
<Button disabled>비활성화</Button>
```

**Props:**
- `variant`: `'primary' | 'secondary' | 'success' | 'warning' | 'danger'`
- `size`: `'sm' | 'md' | 'lg'`
- `loading`: `boolean` - 로딩 스피너 표시
- `disabled`: `boolean` - 버튼 비활성화
- `type`: `'button' | 'submit' | 'reset'`

---

### Input

```tsx
import Input from '@/components/ui/Input';

// 기본 사용
<Input placeholder="이름을 입력하세요" />

// Label 및 Helper Text
<Input
  label="이메일"
  placeholder="example@email.com"
  helperText="이메일 주소를 입력하세요"
  required
/>

// 에러 상태
<Input
  label="비밀번호"
  type="password"
  error="비밀번호는 최소 6자 이상이어야 합니다"
/>

// 아이콘 포함
<Input
  icon={<SearchIcon />}
  placeholder="검색..."
/>

// Variant
<Input variant="filled" />
<Input variant="outlined" />
```

**Props:**
- `label`: `string` - 레이블 텍스트
- `error`: `string` - 에러 메시지
- `helperText`: `string` - 도움말 텍스트
- `icon`: `React.ReactNode` - 왼쪽 아이콘
- `variant`: `'default' | 'filled' | 'outlined'`
- `fullWidth`: `boolean` - 전체 너비

---

### Select

```tsx
import Select from '@/components/ui/Select';

// 기본 사용
<Select
  options={[
    { value: '1', label: '옵션 1' },
    { value: '2', label: '옵션 2' },
  ]}
/>

// Label 및 Placeholder
<Select
  label="카테고리"
  placeholder="선택하세요"
  options={options}
  required
/>

// 에러 상태
<Select
  label="국가"
  options={countries}
  error="국가를 선택해주세요"
/>
```

**Props:**
- `label`: `string` - 레이블 텍스트
- `options`: `Array<{ value: string; label: string; disabled?: boolean }>` - 옵션 목록
- `placeholder`: `string` - 플레이스홀더
- `error`: `string` - 에러 메시지
- `helperText`: `string` - 도움말 텍스트
- `fullWidth`: `boolean` - 전체 너비

---

### Modal

```tsx
import Modal from '@/components/ui/Modal';

const [isOpen, setIsOpen] = useState(false);

<Modal
  isOpen={isOpen}
  onClose={() => setIsOpen(false)}
  title="모달 제목"
  footer={
    <div className="flex justify-end gap-3">
      <Button variant="secondary" onClick={() => setIsOpen(false)}>
        취소
      </Button>
      <Button variant="primary" onClick={handleSave}>
        저장
      </Button>
    </div>
  }
>
  <p>모달 내용</p>
</Modal>
```

**Props:**
- `isOpen`: `boolean` - 모달 열림/닫힘 상태
- `onClose`: `() => void` - 닫기 콜백
- `title`: `string` - 모달 제목
- `children`: `React.ReactNode` - 모달 본문
- `footer`: `React.ReactNode` - 푸터 영역
- `size`: `'sm' | 'md' | 'lg' | 'xl' | 'full'` - 모달 크기
- `closeOnOverlayClick`: `boolean` - 오버레이 클릭 시 닫기 (기본: true)
- `closeOnEscape`: `boolean` - ESC 키로 닫기 (기본: true)
- `showCloseButton`: `boolean` - X 버튼 표시 (기본: true)

---

### Card

```tsx
import Card from '@/components/Card';

// 기본 사용
<Card>
  <p>카드 내용</p>
</Card>

// 제목 포함
<Card title="카드 제목">
  <p>카드 내용</p>
</Card>

// 클릭 가능한 카드
<Card onClick={handleClick} className="cursor-pointer hover:shadow-lg">
  <p>클릭 가능</p>
</Card>
```

**Props:**
- `title`: `string` - 카드 제목
- `children`: `React.ReactNode` - 카드 내용
- `className`: `string` - 추가 CSS 클래스

---

## 🌓 다크 모드

### 다크 모드 토글

프로젝트는 클래스 기반 다크 모드를 사용합니다. `dark` 클래스가 HTML 또는 body에 추가되면 다크 모드가 활성화됩니다.

```tsx
// layout.tsx에서 관리
<body className={isDarkMode ? 'dark' : ''}>
```

### 다크 모드 스타일 작성

모든 컴포넌트는 다크 모드를 지원해야 합니다:

```tsx
// 배경색
<div className="bg-white dark:bg-gray-800">

// 텍스트 색상
<p className="text-gray-900 dark:text-gray-100">

// 보더 색상
<div className="border-gray-300 dark:border-gray-600">
```

### 다크 모드 색상 가이드

| 요소 | Light Mode | Dark Mode |
|------|-----------|-----------|
| 페이지 배경 | `bg-gray-50` | `bg-gray-900` |
| 카드 배경 | `bg-white` | `bg-gray-800` |
| 본문 텍스트 | `text-gray-900` | `text-gray-100` |
| 보조 텍스트 | `text-gray-600` | `text-gray-400` |
| 보더 | `border-gray-300` | `border-gray-600` |
| 입력 필드 배경 | `bg-white` | `bg-gray-700` |

---

## ♿ 접근성

### 키보드 네비게이션

- 모든 인터랙티브 요소는 `Tab` 키로 접근 가능해야 합니다.
- 포커스 상태는 명확히 표시되어야 합니다 (`focus:ring-2 focus:ring-blue-500`).
- 모달, 드롭다운 등은 포커스 트랩을 구현해야 합니다.
- ESC 키로 모달/드롭다운 닫기를 지원합니다.

### ARIA 속성

모든 컴포넌트는 적절한 ARIA 속성을 사용합니다:

```tsx
// 버튼
<button aria-label="메뉴 열기">

// 입력 필드
<input
  aria-invalid={!!error}
  aria-describedby="input-error"
/>

// 모달
<div role="dialog" aria-modal="true" aria-labelledby="modal-title">

// 로딩 상태
<div role="status" aria-live="polite">
```

### 색상 대비

- 텍스트와 배경 간 최소 4.5:1 대비율 (WCAG AA)
- 큰 텍스트(18px 이상)는 3:1 대비율
- 인터랙티브 요소의 포커스 링은 명확히 보여야 함

### 스크린 리더 지원

```tsx
// 스크린 리더 전용 텍스트
<span className="sr-only">메뉴 열기</span>

// 로딩 상태
<div role="status" aria-live="polite">
  <span className="sr-only">로딩 중...</span>
</div>
```

---

## 🎯 사용 지침

### 1. 새 컴포넌트 생성 시

1. `src/components/ui/` 폴더에 생성
2. TypeScript와 React.forwardRef 사용
3. 적절한 ARIA 속성 추가
4. 다크 모드 지원
5. Props 인터페이스 정의

### 2. 색상 사용 시

- 하드코딩된 색상 값 대신 Tailwind 클래스 사용
- 의미에 맞는 색상 선택 (primary, success, danger 등)
- 다크 모드 대응 색상 함께 정의

### 3. 간격 사용 시

- 일관된 간격 단위 사용 (4의 배수)
- `gap-4`, `p-6`, `mb-8` 등 Tailwind 유틸리티 활용

### 4. 접근성 체크리스트

- [ ] 키보드로 모든 기능 접근 가능
- [ ] 포커스 상태 명확히 표시
- [ ] ARIA 속성 적절히 사용
- [ ] 색상 대비 4.5:1 이상
- [ ] 스크린 리더 테스트 완료

---

## 📚 참고 자료

- [Tailwind CSS v4 문서](https://tailwindcss.com/docs)
- [WCAG 2.1 가이드라인](https://www.w3.org/WAI/WCAG21/quickref/)
- [React 접근성 가이드](https://react.dev/learn/accessibility)
- [MDN ARIA 가이드](https://developer.mozilla.org/ko/docs/Web/Accessibility/ARIA)

---

**이 문서는 프로젝트 진행에 따라 지속적으로 업데이트됩니다.**
