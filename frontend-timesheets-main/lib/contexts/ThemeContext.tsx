'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useAuth } from './AuthContext';

// CA.gov Template Theme Files Available
export const CA_GOV_THEMES = {
  oceanside: 'colortheme-oceanside',
  delta: 'colortheme-delta',
  eureka: 'colortheme-eureka',
  mono: 'colortheme-mono',
  orangecounty: 'colortheme-orangecounty',
  pasorobles: 'colortheme-pasorobles',
  sacramento: 'colortheme-sacramento',
  santabarbara: 'colortheme-santabarbara',
  santacruz: 'colortheme-santacruz',
  shasta: 'colortheme-shasta',
  sierra: 'colortheme-sierra',
  trinity: 'colortheme-trinity',
} as const;

export interface CountyTheme {
  value: string;
  label: string;
  themeFile: keyof typeof CA_GOV_THEMES;
}

// Map all 58 California counties to CA.gov template themes
// Available themes: oceanside, delta, eureka, mono, orangecounty, pasorobles, 
//                   sacramento, santabarbara, santacruz, shasta, sierra, trinity
const counties: CountyTheme[] = [
  // Major Metropolitan Areas
  { value: 'los-angeles', label: 'Los Angeles County', themeFile: 'oceanside' },
  { value: 'orange', label: 'Orange County', themeFile: 'orangecounty' },
  { value: 'san-diego', label: 'San Diego County', themeFile: 'santabarbara' },
  { value: 'riverside', label: 'Riverside County', themeFile: 'pasorobles' },
  { value: 'san-bernardino', label: 'San Bernardino County', themeFile: 'sierra' },
  { value: 'santa-clara', label: 'Santa Clara County', themeFile: 'santacruz' },
  { value: 'alameda', label: 'Alameda County', themeFile: 'delta' },
  { value: 'sacramento', label: 'Sacramento County', themeFile: 'sacramento' },
  { value: 'contra-costa', label: 'Contra Costa County', themeFile: 'delta' },
  { value: 'fresno', label: 'Fresno County', themeFile: 'eureka' },
  
  // Bay Area
  { value: 'san-francisco', label: 'San Francisco County', themeFile: 'oceanside' },
  { value: 'san-mateo', label: 'San Mateo County', themeFile: 'santacruz' },
  { value: 'marin', label: 'Marin County', themeFile: 'santabarbara' },
  { value: 'sonoma', label: 'Sonoma County', themeFile: 'santabarbara' },
  { value: 'napa', label: 'Napa County', themeFile: 'pasorobles' },
  { value: 'solano', label: 'Solano County', themeFile: 'delta' },
  
  // Central Valley
  { value: 'kern', label: 'Kern County', themeFile: 'eureka' },
  { value: 'tulare', label: 'Tulare County', themeFile: 'eureka' },
  { value: 'kings', label: 'Kings County', themeFile: 'eureka' },
  { value: 'madera', label: 'Madera County', themeFile: 'sierra' },
  { value: 'merced', label: 'Merced County', themeFile: 'eureka' },
  { value: 'stanislaus', label: 'Stanislaus County', themeFile: 'delta' },
  { value: 'san-joaquin', label: 'San Joaquin County', themeFile: 'delta' },
  
  // Central Coast
  { value: 'santa-barbara', label: 'Santa Barbara County', themeFile: 'santabarbara' },
  { value: 'san-luis-obispo', label: 'San Luis Obispo County', themeFile: 'pasorobles' },
  { value: 'monterey', label: 'Monterey County', themeFile: 'santabarbara' },
  { value: 'ventura', label: 'Ventura County', themeFile: 'santabarbara' },
  
  // Northern California
  { value: 'shasta', label: 'Shasta County', themeFile: 'shasta' },
  { value: 'butte', label: 'Butte County', themeFile: 'sierra' },
  { value: 'yuba', label: 'Yuba County', themeFile: 'sierra' },
  { value: 'sutter', label: 'Sutter County', themeFile: 'sierra' },
  { value: 'placer', label: 'Placer County', themeFile: 'sierra' },
  { value: 'el-dorado', label: 'El Dorado County', themeFile: 'sierra' },
  { value: 'yolo', label: 'Yolo County', themeFile: 'delta' },
  
  // Sierra Nevada
  { value: 'nevada', label: 'Nevada County', themeFile: 'sierra' },
  { value: 'plumas', label: 'Plumas County', themeFile: 'sierra' },
  { value: 'sierra', label: 'Sierra County', themeFile: 'sierra' },
  { value: 'alpine', label: 'Alpine County', themeFile: 'sierra' },
  { value: 'amador', label: 'Amador County', themeFile: 'sierra' },
  { value: 'calaveras', label: 'Calaveras County', themeFile: 'sierra' },
  { value: 'tuolumne', label: 'Tuolumne County', themeFile: 'sierra' },
  { value: 'mariposa', label: 'Mariposa County', themeFile: 'sierra' },
  
  // North Coast
  { value: 'humboldt', label: 'Humboldt County', themeFile: 'eureka' },
  { value: 'mendocino', label: 'Mendocino County', themeFile: 'eureka' },
  { value: 'lake', label: 'Lake County', themeFile: 'eureka' },
  { value: 'del-norte', label: 'Del Norte County', themeFile: 'eureka' },
  { value: 'siskiyou', label: 'Siskiyou County', themeFile: 'trinity' },
  { value: 'modoc', label: 'Modoc County', themeFile: 'trinity' },
  { value: 'lassen', label: 'Lassen County', themeFile: 'trinity' },
  { value: 'trinity', label: 'Trinity County', themeFile: 'trinity' },
  { value: 'tehama', label: 'Tehama County', themeFile: 'shasta' },
  { value: 'glenn', label: 'Glenn County', themeFile: 'shasta' },
  { value: 'colusa', label: 'Colusa County', themeFile: 'delta' },
  
  // Inland Empire / Desert
  { value: 'imperial', label: 'Imperial County', themeFile: 'pasorobles' },
  { value: 'inyo', label: 'Inyo County', themeFile: 'sierra' },
  { value: 'mono', label: 'Mono County', themeFile: 'mono' },
  
  // Additional Counties (to complete 58)
  { value: 'san-benito', label: 'San Benito County', themeFile: 'santabarbara' },
  { value: 'santa-cruz', label: 'Santa Cruz County', themeFile: 'santacruz' },
];

interface ThemeContextType {
  selectedCounty: string;
  counties: CountyTheme[];
  setSelectedCounty: (county: string) => void;
  currentThemeFile: string | null;
  // Removed setSelectedCounty from public API - county is now auto-set from JWT
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export function ThemeProvider({ children }: { children: ReactNode }) {
  const { user, isAuthenticated } = useAuth();
  const [selectedCounty, setSelectedCountyState] = useState<string>(counties[0].value);
  const [currentThemeFile, setCurrentThemeFile] = useState<string | null>(null);

  // Function to load/unload theme CSS dynamically
  const loadTheme = (themeFile: keyof typeof CA_GOV_THEMES) => {
    if (typeof document === 'undefined') return;

    const themeFileName = `${CA_GOV_THEMES[themeFile]}.min.css`;
    const themeId = 'cagov-dynamic-theme';
    const defaultThemeId = 'cagov-default-theme';
    const themePath = `/cagov/css/${themeFileName}`;

    console.log('🎨 loadTheme: Starting to load theme:', themeFile, 'File:', themePath);

    // Remove existing dynamic theme if any
    const existingTheme = document.getElementById(themeId);
    if (existingTheme) {
      console.log('🎨 loadTheme: Removing existing dynamic theme');
      existingTheme.remove();
    }

    // Also remove/hide default theme from layout.tsx
    const defaultTheme = document.getElementById(defaultThemeId);
    if (defaultTheme) {
      console.log('🎨 loadTheme: Removing default theme');
      defaultTheme.remove();
    }

    // Create new link element for the theme
    const link = document.createElement('link');
    link.id = themeId;
    link.rel = 'stylesheet';
    link.href = themePath;
    link.onload = () => {
      setCurrentThemeFile(themeFile);
      console.log(`🎨 Theme loaded successfully: ${themeFile} (${themePath})`);
      // Force a repaint to ensure theme is applied
      document.body.style.opacity = '0.99';
      setTimeout(() => {
        document.body.style.opacity = '1';
      }, 10);
    };
    link.onerror = (e) => {
      console.error(`❌ Failed to load theme: ${themeFile} (${themePath})`, e);
    };

    // Append to head
    document.head.appendChild(link);
    console.log('🎨 loadTheme: Link element appended to head');
  };

  const setSelectedCounty = (county: string) => {
    const countyData = counties.find((c) => c.value === county);
    if (countyData) {
      setSelectedCountyState(county);
      loadTheme(countyData.themeFile);
      
      // Save to localStorage
      if (typeof window !== 'undefined') {
        localStorage.setItem('selectedCounty', county);
      }
    }
  };

  // Helper function to normalize county name to theme value format
  const normalizeCountyName = (countyName: string | undefined): string | null => {
    if (!countyName) return null;

    // Handle county codes (CT1-CT5) from Keycloak
    const countyCodeMap: Record<string, string> = {
      'CT1': 'orange',
      'CT2': 'sacramento',
      'CT3': 'riverside',
      'CT4': 'los-angeles',
      'CT5': 'alameda',
    };

    const upperCounty = countyName.trim().toUpperCase();
    if (countyCodeMap[upperCounty]) {
      return countyCodeMap[upperCounty];
    }

    // Remove "County" suffix if present
    let cleaned = countyName.trim().replace(/\s+County\s*$/i, '');

    // Convert to lowercase and replace spaces with hyphens
    const normalized = cleaned
      .toLowerCase()
      .trim()
      .replace(/\s+/g, '-')
      .replace(/[^a-z0-9-]/g, '');

    // Check if normalized value exists in counties array
    const found = counties.find(c => c.value === normalized);
    if (found) {
      return normalized;
    }

    // Try case-insensitive match on label
    const foundByLabel = counties.find(c =>
      c.label.toLowerCase().replace(/\s+County\s*$/, '') === cleaned.toLowerCase()
    );
    if (foundByLabel) {
      return foundByLabel.value;
    }

    return null;
  };

  // Initialize theme based on user's county from JWT token
  useEffect(() => {
    if (typeof window === 'undefined') return;

    console.log('🎨 ThemeContext: useEffect triggered', {
      isAuthenticated,
      user: user ? { username: user.username, countyId: user.countyId, role: user.role } : null
    });

    // If user is authenticated and has countyId, use that
    if (isAuthenticated && user?.countyId) {
      console.log('🎨 ThemeContext: User authenticated with countyId:', user.countyId);
      const normalizedCounty = normalizeCountyName(user.countyId);
      console.log('🎨 ThemeContext: Normalized county:', normalizedCounty);

      if (normalizedCounty) {
        const countyData = counties.find((c) => c.value === normalizedCounty);
        if (countyData) {
          console.log('🎨 ThemeContext: Applying theme for county:', countyData.label, 'Theme:', countyData.themeFile);
          console.log('🎨 ThemeContext: Theme CSS file will be:', `/cagov/css/${CA_GOV_THEMES[countyData.themeFile]}.min.css`);
          setSelectedCountyState(normalizedCounty);
          loadTheme(countyData.themeFile);
          // Save to localStorage for consistency
          localStorage.setItem('selectedCounty', normalizedCounty);
          return;
        } else {
          console.warn('🎨 ThemeContext: County data not found for normalized value:', normalizedCounty);
        }
      } else {
        console.warn('🎨 ThemeContext: Could not normalize countyId:', user.countyId);
      }
    } else if (!isAuthenticated) {
      console.log('🎨 ThemeContext: User not authenticated, clearing saved county and using default theme');
      // Clear saved county on logout so next login gets fresh theme
      localStorage.removeItem('selectedCounty');
    } else if (isAuthenticated && !user?.countyId) {
      console.warn('🎨 ThemeContext: User authenticated but no countyId in JWT token');
      console.warn('🎨 ThemeContext: Full user object:', JSON.stringify(user, null, 2));
    }

    // Fallback: Load saved county preference or use default
    const savedCounty = localStorage.getItem('selectedCounty');
    const countyToUse = savedCounty || counties[0].value;
    const countyData = counties.find((c) => c.value === countyToUse);

    if (countyData) {
      console.log('🎨 ThemeContext: Using fallback theme:', countyData.label, 'Theme:', countyData.themeFile);
      setSelectedCountyState(countyToUse);
      loadTheme(countyData.themeFile);
    }
  }, [isAuthenticated, user]);

  return (
    <ThemeContext.Provider
      value={{
        selectedCounty,
        counties,
        setSelectedCounty,
        currentThemeFile,
      }}
    >
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const context = useContext(ThemeContext);
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
}
