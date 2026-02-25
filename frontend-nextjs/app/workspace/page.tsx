'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

/**
 * /workspace redirects to the main dashboard (/my-workspace).
 * All users land on My Workspace as the unified main dashboard.
 */
export default function WorkspaceRedirectPage() {
  const router = useRouter();
  useEffect(() => {
    router.replace('/my-workspace');
  }, [router]);
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#153554] mx-auto"></div>
        <p className="mt-4 text-gray-600">Redirecting to My Workspace...</p>
      </div>
    </div>
  );
}
