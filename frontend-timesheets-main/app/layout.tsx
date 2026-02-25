import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import { ThemeProvider } from '@/lib/contexts/ThemeContext';
import { AuthProvider } from '@/lib/contexts/AuthContext';
import Header from '@/components/structure/Header';
import Footer from '@/components/structure/Footer';
import ConditionalHeader from '@/components/structure/ConditionalHeader';
import ConditionalFooter from '@/components/structure/ConditionalFooter';
import '@/styles/globals.css';
import Script from 'next/script';
import ClientProviders from './providers';
import I18nProvider from './i18n-provider';

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'Timesheet Reporting System',
  description: 'California State Timesheet Reporting System',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <head>
        <link rel="stylesheet" href="/cagov/css/cagov.core.min.css" />
        {/* Default theme - will be replaced dynamically by ThemeContext */}
        <link rel="stylesheet" href="/cagov/css/colortheme-oceanside.min.css" id="cagov-default-theme" />
      </head>
      <body className={inter.className}>
        <I18nProvider>
          <ClientProviders>
            <AuthProvider>
              <ThemeProvider>
                <ConditionalHeader />
                <main id="main-content" className="container" style={{ minHeight: 'calc(100vh - 300px)', padding: '2rem 0' }}>
                  {children}
                </main>
                <ConditionalFooter />
              </ThemeProvider>
            </AuthProvider>
          </ClientProviders>
        </I18nProvider>
        <Script src="/cagov/js/cagov.core.min.js" strategy="afterInteractive" />
        <Script
          id="disable-cagov-pagination"
          strategy="afterInteractive"
          dangerouslySetInnerHTML={{
            __html: `
              // Disable CA.gov pagination component completely
              window.addEventListener('load', function() {
                // Prevent pagination component from being defined
                if (window.customElements && window.customElements.get('cagov-pagination')) {
                  // Component already defined, remove all instances
                  document.querySelectorAll('cagov-pagination, .cagov-pagination, nav[aria-label="Pagination"]').forEach(el => {
                    el.remove();
                  });
                }
                // Override the define function to prevent pagination component
                const originalDefine = window.customElements.define;
                window.customElements.define = function(name, constructor, options) {
                  if (name === 'cagov-pagination') {
                    console.log('CA.gov pagination component disabled');
                    return; // Don't register the pagination component
                  }
                  return originalDefine.call(this, name, constructor, options);
                };
                // Remove any existing pagination elements
                setInterval(function() {
                  document.querySelectorAll('cagov-pagination, .cagov-pagination, nav[aria-label="Pagination"].cagov-pagination').forEach(el => {
                    el.remove();
                  });
                }, 100);
              });
            `,
          }}
        />
      </body>
    </html>
  );
}

