'use client';

import { usePathname } from 'next/navigation';
import CmipsAppShell from './CmipsAppShell';

interface ConditionalLayoutProps {
  children: React.ReactNode;
}

export default function ConditionalLayout({ children }: ConditionalLayoutProps) {
  const pathname = usePathname();
  const isLoginPage = pathname === '/login';

  // On login page, render children without the CMIPS shell
  if (isLoginPage) {
    return <>{children}</>;
  }

  // On all other pages, wrap with CMIPS dashboard shell
  return <CmipsAppShell>{children}</CmipsAppShell>;
}
