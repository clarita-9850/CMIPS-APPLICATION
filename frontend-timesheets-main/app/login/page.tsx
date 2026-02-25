'use client';

'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/contexts/AuthContext';
import LoginCard from '@/components/auth/LoginCard';

export default function LoginPage() {
  const router = useRouter();
  const { isAuthenticated, user } = useAuth();

  React.useEffect(() => {
    if (isAuthenticated) {
      // Redirect based on user role
      const userRole = user?.role?.toUpperCase();
      if (userRole === 'CASE_WORKER') {
        router.push('/my-workspace');
      } else {
        router.push('/');
      }
    }
  }, [isAuthenticated, user, router]);

  return (
    <div className="container" style={{ minHeight: '70vh', display: 'flex', alignItems: 'center' }}>
      <div className="row justify-content-center w-100">
        <div className="col-md-6 col-lg-5">
          <LoginCard title="Welcome Back" subtitle="Sign in with your credentials" redirectPath="/" />
        </div>
      </div>
    </div>
  );
}

