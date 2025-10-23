import type { Metadata } from 'next';
import './globals.css';
import { ProjectProvider } from '@/contexts/ProjectContext';
import Navbar from '@/components/Navbar';

export const metadata: Metadata = {
  title: 'Novel AI',
  description: 'AI-powered character dialogue and story creation system',
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
      <body>
        <ProjectProvider>
          <Navbar />
          {children}
        </ProjectProvider>
      </body>
    </html>
  );
}
