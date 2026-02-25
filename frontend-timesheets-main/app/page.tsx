'use client';

import React, { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import Banner from '@/components/patterns/Banner';
import CardGrid, { type CardItem } from '@/components/patterns/CardGrid';
import ProgressTracker, { type ProgressStep } from '@/components/patterns/ProgressTracker';
import Breadcrumb from '@/components/Breadcrumb';
import { useAuth } from '@/lib/contexts/AuthContext';

export default function HomePage() {
  const { t } = useTranslation();
  const router = useRouter();
  const { isAuthenticated, loading, user } = useAuth();
  const [showDashboard, setShowDashboard] = useState(false);

  const isCaseWorker = user?.role?.toUpperCase() === 'CASE_WORKER';
  
  // Check for dashboard query parameter on client side
  useEffect(() => {
    if (typeof window !== 'undefined') {
      const params = new URLSearchParams(window.location.search);
      setShowDashboard(params.get('dashboard') === 'true');
    }
  }, []);
  
  // If case worker clicked Dashboard button, show supervisor content
  const shouldShowSupervisorContent = useMemo(() => {
    return isCaseWorker && showDashboard;
  }, [isCaseWorker, showDashboard]);

  const bannerProps = {
    titleText: t('banner.title'),
    bodyText: t('banner.body'),
    buttonText: t('banner.buttonText'),
    buttonHref: '/dashboard',
  };

  const cardItems: CardItem[] = [
    {
      type: 'icon',
      icon: '📊',
      titleText: t('cards.title1'),
      bodyText: t('cards.body'),
      buttonText: 'View Reports',
      buttonHref: '/dashboard',
    },
    {
      type: 'icon',
      icon: '⚙️',
      titleText: t('cards.title2'),
      bodyText: t('cards.body'),
      buttonText: 'Manage Jobs',
      buttonHref: '/batch-jobs',
    },
    {
      type: 'icon',
      icon: '📈',
      titleText: t('cards.title3'),
      bodyText: t('cards.body'),
      buttonText: 'View Visualization',
      buttonHref: '/visualization',
    },
  ];

  const progressSteps: ProgressStep[] = [
    {
      title: t('progress.steps.step1.title'),
      body: t('progress.steps.step1.body'),
    },
    {
      title: t('progress.steps.step2.title'),
      body: t('progress.steps.step2.body'),
    },
    {
      title: t('progress.steps.step3.title'),
      body: t('progress.steps.step3.body'),
    },
  ];

  // For case workers, use demo-screens style content
  const caseWorkerCardItems: CardItem[] = [
    {
      type: 'icon',
      icon: 'ca-gov-icon-code',
      titleText: t('cards.swTitle1'),
      bodyText: t('cards.swBody'),
      buttonText: 'Learn More',
      buttonHref: '#',
    },
    {
      type: 'icon',
      icon: 'ca-gov-icon-accessibility',
      titleText: t('cards.swTitle2'),
      bodyText: t('cards.swBody'),
      buttonText: 'Learn More',
      buttonHref: '#',
    },
    {
      type: 'icon',
      icon: 'ca-gov-icon-capitol',
      titleText: t('cards.swTitle3'),
      bodyText: t('cards.swBody'),
      buttonText: 'Learn More',
      buttonHref: '#',
    },
  ];

  const caseWorkerBannerProps = {
    titleText: t('banner.swTitle'),
    bodyText: t('banner.swBody'),
    buttonText: t('banner.swButtonText'),
    buttonHref: '#',
  };

  const caseWorkerProgressSteps: ProgressStep[] = [
    {
      title: t('progress.steps.step1.title'),
      body: t('progress.steps.step1.swBody'),
    },
    {
      title: t('progress.steps.step2.title'),
      body: t('progress.steps.step2.swBody'),
    },
    {
      title: t('progress.steps.step3.title'),
      body: t('progress.steps.step3.swBody'),
    },
  ];

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      router.replace('/login');
    }
    // Removed redirect - case workers can access / directly (SW-Home)
    // Dashboard button in header will redirect to /dashboard for timesheet-frontend content
  }, [loading, isAuthenticated, router]);

  if (loading || !isAuthenticated) {
    return (
      <div className="container py-5 text-center">
        <p>Loading...</p>
      </div>
    );
  }

  // Show demo-screens style home page for case workers (unless Dashboard button was clicked),
  // timesheet-frontend style for supervisors or when Dashboard button is clicked
  const displayBanner = shouldShowSupervisorContent ? bannerProps : (isCaseWorker ? caseWorkerBannerProps : bannerProps);
  const displayCards = shouldShowSupervisorContent ? cardItems : (isCaseWorker ? caseWorkerCardItems : cardItems);
  const displayProgressSteps = shouldShowSupervisorContent ? progressSteps : (isCaseWorker ? caseWorkerProgressSteps : progressSteps);
  const displayGetStarted = shouldShowSupervisorContent ? t('progress.getStarted') : (isCaseWorker ? t('progress.swGetStarted') : t('progress.getStarted'));
  const displayDescription = shouldShowSupervisorContent ? t('progress.description') : (isCaseWorker ? t('progress.swDescription') : t('progress.description'));

  // Only apply gray background for demo-screens content (case workers viewing SW-Home)
  const cardGridWrapperStyle = !shouldShowSupervisorContent && isCaseWorker 
    ? { background: '#cccccc' } 
    : {};

  return (
    <>
      <Breadcrumb path={['Home']} currentPage="" />
      <Banner {...displayBanner} />
      <div style={cardGridWrapperStyle}>
        <div className="container" style={{ paddingTop: '2rem', paddingBottom: '2rem' }}>
          <CardGrid contentArr={displayCards} />
        </div>
      </div>
      <div className="container mb-2">
        <h2 className="text-center mb-4">{displayGetStarted}</h2>
        <p className="text-center mb-5">{displayDescription}</p>
        <ProgressTracker type="horizontal" currentStep={1} contentArr={displayProgressSteps} />
      </div>
    </>
  );
}

