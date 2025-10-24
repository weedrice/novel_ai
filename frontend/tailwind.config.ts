import type { Config } from 'tailwindcss'

/**
 * Tailwind CSS v4 Config
 * v4에서는 대부분의 설정을 CSS에서 @theme 지시자로 관리합니다.
 * 이 파일은 content 경로 등 필수 설정만 포함합니다.
 */
const config: Config = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
}

export default config