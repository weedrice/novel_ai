import type { Metadata } from 'next';
import './globals.css';
import { ProjectProvider } from '@/contexts/ProjectContext';
import { ThemeProvider } from '@/contexts/ThemeContext';
import { ToastProvider } from '@/contexts/ToastContext';
import Navbar from '@/components/Navbar';
import GlobalKeyboardShortcuts from '@/components/GlobalKeyboardShortcuts';
import AuthValidator from '@/components/AuthValidator';

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
    <html lang="ko" suppressHydrationWarning>
      <head>
        <meta charSet="utf-8" />
      </head>
      <body className="bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100 transition-colors duration-200" suppressHydrationWarning>
        <ThemeProvider>
          <ToastProvider>
            <ProjectProvider>
              <AuthValidator />
              <Navbar />
              <GlobalKeyboardShortcuts />
              {children}
            </ProjectProvider>
          </ToastProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}
