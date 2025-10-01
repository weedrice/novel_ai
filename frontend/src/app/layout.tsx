import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Character Tone Assistant',
  description: 'AI-powered character dialogue tone assistance system',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
