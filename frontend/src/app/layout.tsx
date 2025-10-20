import type { Metadata } from 'next';
import './globals.css';

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
      <head>
        <meta charSet="utf-8" />
      </head>
      <body>{children}</body>
    </html>
  );
}
