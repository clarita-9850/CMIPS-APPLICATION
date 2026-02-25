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

// Full California county mapping to themes
// All 58 California counties mapped to appropriate CA.gov themes
const COUNTY_THEME_MAP: Record<string, keyof typeof CA_GOV_THEMES> = {
  // Original CMIPS county codes
  'CTA': 'sacramento',      // County Type A → Sacramento theme (Dark Blue)
  'CTB': 'orangecounty',    // County Type B → Orange County theme (Orange & Brown)
  'CTC': 'shasta',          // County Type C → Shasta theme (Green)

  // Additional county codes (CT1-CT5 for demo purposes)
  'CT1': 'sacramento',
  'CT2': 'orangecounty',
  'CT3': 'shasta',
  'CT4': 'delta',
  'CT5': 'eureka',

  // All 58 California Counties - mapped by region/geography
  // Northern California Counties - Green/Forest themes
  'ALAMEDA': 'oceanside',
  'ALPINE': 'sierra',
  'AMADOR': 'pasorobles',
  'BUTTE': 'shasta',
  'CALAVERAS': 'pasorobles',
  'COLUSA': 'delta',
  'CONTRA COSTA': 'oceanside',
  'DEL NORTE': 'eureka',
  'EL DORADO': 'sierra',
  'FRESNO': 'pasorobles',
  'GLENN': 'shasta',
  'HUMBOLDT': 'eureka',
  'IMPERIAL': 'delta',
  'INYO': 'mono',
  'KERN': 'pasorobles',
  'KINGS': 'delta',
  'LAKE': 'trinity',
  'LASSEN': 'shasta',
  'LOS ANGELES': 'oceanside',
  'MADERA': 'pasorobles',
  'MARIN': 'oceanside',
  'MARIPOSA': 'sierra',
  'MENDOCINO': 'eureka',
  'MERCED': 'delta',
  'MODOC': 'shasta',
  'MONO': 'mono',
  'MONTEREY': 'santacruz',
  'NAPA': 'delta',
  'NEVADA': 'sierra',
  'ORANGE': 'orangecounty',
  'PLACER': 'sacramento',
  'PLUMAS': 'sierra',
  'RIVERSIDE': 'pasorobles',
  'SACRAMENTO': 'sacramento',
  'SAN BENITO': 'santacruz',
  'SAN BERNARDINO': 'pasorobles',
  'SAN DIEGO': 'oceanside',
  'SAN FRANCISCO': 'oceanside',
  'SAN JOAQUIN': 'delta',
  'SAN LUIS OBISPO': 'pasorobles',
  'SAN MATEO': 'oceanside',
  'SANTA BARBARA': 'santabarbara',
  'SANTA CLARA': 'oceanside',
  'SANTA CRUZ': 'santacruz',
  'SHASTA': 'shasta',
  'SIERRA': 'sierra',
  'SISKIYOU': 'shasta',
  'SOLANO': 'delta',
  'SONOMA': 'eureka',
  'STANISLAUS': 'delta',
  'SUTTER': 'sacramento',
  'TEHAMA': 'shasta',
  'TRINITY': 'trinity',
  'TULARE': 'pasorobles',
  'TUOLUMNE': 'sierra',
  'VENTURA': 'santabarbara',
  'YOLO': 'sacramento',
  'YUBA': 'sacramento',

  // Numeric county codes (FIPS codes)
  '01': 'oceanside',      // Alameda
  '02': 'sierra',         // Alpine
  '03': 'pasorobles',     // Amador
  '04': 'shasta',         // Butte
  '05': 'pasorobles',     // Calaveras
  '06': 'delta',          // Colusa
  '07': 'oceanside',      // Contra Costa
  '08': 'eureka',         // Del Norte
  '09': 'sierra',         // El Dorado
  '10': 'pasorobles',     // Fresno
  '11': 'shasta',         // Glenn
  '12': 'eureka',         // Humboldt
  '13': 'delta',          // Imperial
  '14': 'mono',           // Inyo
  '15': 'pasorobles',     // Kern
  '16': 'delta',          // Kings
  '17': 'trinity',        // Lake
  '18': 'shasta',         // Lassen
  '19': 'oceanside',      // Los Angeles
  '20': 'pasorobles',     // Madera
  '21': 'oceanside',      // Marin
  '22': 'sierra',         // Mariposa
  '23': 'eureka',         // Mendocino
  '24': 'delta',          // Merced
  '25': 'shasta',         // Modoc
  '26': 'mono',           // Mono
  '27': 'santacruz',      // Monterey
  '28': 'delta',          // Napa
  '29': 'sierra',         // Nevada
  '30': 'orangecounty',   // Orange
  '31': 'sacramento',     // Placer
  '32': 'sierra',         // Plumas
  '33': 'pasorobles',     // Riverside
  '34': 'sacramento',     // Sacramento
  '35': 'santacruz',      // San Benito
  '36': 'pasorobles',     // San Bernardino
  '37': 'oceanside',      // San Diego
  '38': 'oceanside',      // San Francisco
  '39': 'delta',          // San Joaquin
  '40': 'pasorobles',     // San Luis Obispo
  '41': 'oceanside',      // San Mateo
  '42': 'santabarbara',   // Santa Barbara
  '43': 'oceanside',      // Santa Clara
  '44': 'santacruz',      // Santa Cruz
  '45': 'shasta',         // Shasta
  '46': 'sierra',         // Sierra
  '47': 'shasta',         // Siskiyou
  '48': 'delta',          // Solano
  '49': 'eureka',         // Sonoma
  '50': 'delta',          // Stanislaus
  '51': 'sacramento',     // Sutter
  '52': 'shasta',         // Tehama
  '53': 'trinity',        // Trinity
  '54': 'pasorobles',     // Tulare
  '55': 'sierra',         // Tuolumne
  '56': 'santabarbara',   // Ventura
  '57': 'sacramento',     // Yolo
  '58': 'sacramento',     // Yuba
};

// Default theme if county is not found
const DEFAULT_THEME: keyof typeof CA_GOV_THEMES = 'oceanside';

interface CountyThemeContextType {
  currentCounty: string | null;
  currentTheme: keyof typeof CA_GOV_THEMES | null;
  themeLoaded: boolean;
  setManualTheme: (theme: keyof typeof CA_GOV_THEMES) => void;
  availableThemes: typeof CA_GOV_THEMES;
}

const CountyThemeContext = createContext<CountyThemeContextType | undefined>(undefined);

export function CountyThemeProvider({ children }: { children: ReactNode }) {
  const { user, token } = useAuth();
  const [currentCounty, setCurrentCounty] = useState<string | null>(null);
  const [currentTheme, setCurrentTheme] = useState<keyof typeof CA_GOV_THEMES | null>(null);
  const [themeLoaded, setThemeLoaded] = useState(false);

  // Extract county from JWT token or user object
  const extractCounty = (): string | null => {
    if (typeof window === 'undefined') return null;

    // DEBUG: Check for manual override in localStorage (for testing)
    const manualCounty = localStorage.getItem('manualCounty');
    if (manualCounty) {
      console.log('🔧 Using manual county override:', manualCounty);
      return manualCounty.toUpperCase();
    }

    // Try to get county from user object first
    if (user?.county) {
      console.log('✅ County found in user object:', user.county);
      return user.county.toUpperCase();
    }

    // Try to get countyId from user object
    if (user?.countyId) {
      console.log('✅ CountyId found in user object:', user.countyId);
      return user.countyId.toUpperCase();
    }

    // Try to get county from JWT token
    if (token) {
      try {
        const decoded = JSON.parse(atob(token.split('.')[1]));
        console.log('🔍 JWT decoded, checking for county. Available fields:', Object.keys(decoded));

        // Method 1: Check for groups array (Keycloak groups)
        let county = null;

        // Check direct 'groups' claim (array of group names/paths)
        if (decoded.groups && Array.isArray(decoded.groups)) {
          console.log('🔍 Found groups array:', decoded.groups);
          // Look for county codes in group names (CTA, CTB, CTC, CT1-CT5, county names, or numeric codes)
          const countyGroup = decoded.groups.find((g: string) => {
            const upper = g.toUpperCase();
            return /^(CTA|CTB|CTC|CT[1-5])$/i.test(upper) ||
                   /\/(CTA|CTB|CTC|CT[1-5])$/i.test(g) ||
                   COUNTY_THEME_MAP[upper] !== undefined;
          });
          if (countyGroup) {
            // Extract county code from group path
            const match = countyGroup.match(/(CTA|CTB|CTC|CT[1-5])$/i);
            if (match) {
              county = match[1].toUpperCase();
            } else {
              county = countyGroup.toUpperCase();
            }
            if (county && (COUNTY_THEME_MAP[county] || ['CTA', 'CTB', 'CTC'].includes(county))) {
              console.log('✅ County found in groups array:', county);
              return county;
            }
          }
        }

        // Check realm_access.groups
        if (decoded.realm_access?.groups && Array.isArray(decoded.realm_access.groups)) {
          console.log('🔍 Found realm_access.groups:', decoded.realm_access.groups);
          const countyGroup = decoded.realm_access.groups.find((g: string) => {
            const upper = g.toUpperCase();
            return /^(CTA|CTB|CTC|CT[1-5])$/i.test(upper) ||
                   /\/(CTA|CTB|CTC|CT[1-5])$/i.test(g) ||
                   COUNTY_THEME_MAP[upper] !== undefined;
          });
          if (countyGroup) {
            const match = countyGroup.match(/(CTA|CTB|CTC|CT[1-5])$/i);
            if (match) {
              county = match[1].toUpperCase();
            } else {
              county = countyGroup.toUpperCase();
            }
            if (county && (COUNTY_THEME_MAP[county] || ['CTA', 'CTB', 'CTC'].includes(county))) {
              console.log('✅ County found in realm_access.groups:', county);
              return county;
            }
          }
        }

        // Method 2: Check other possible fields for county
        county = decoded.county ||
                 decoded.countyId ||
                 decoded.county_id ||
                 decoded.location ||
                 decoded.userCounty ||
                 decoded.attributes?.county?.[0] ||
                 decoded.attributes?.countyId?.[0] ||
                 decoded.attributes?.county_id?.[0] ||
                 decoded.attributes?.location?.[0];

        if (county) {
          console.log('✅ County found in JWT token attributes:', county);
          return county.toUpperCase();
        } else {
          console.log('⚠️ No county found in JWT token.');
        }
      } catch (error) {
        console.error('❌ Error decoding JWT token:', error);
      }
    } else {
      console.log('⚠️ No JWT token available');
    }

    // Try to get from localStorage as fallback
    const storedCounty = localStorage.getItem('userCounty');
    if (storedCounty) {
      console.log('✅ County found in localStorage:', storedCounty);
      return storedCounty.toUpperCase();
    }

    console.log('❌ No county found in any source');
    return null;
  };

  // Function to load/unload theme CSS dynamically
  const loadTheme = (themeFile: keyof typeof CA_GOV_THEMES) => {
    if (typeof document === 'undefined') return;

    const themeFileName = `${CA_GOV_THEMES[themeFile]}.min.css`;
    const themeId = 'cagov-dynamic-theme';
    const defaultThemeId = 'cagov-default-theme';

    // Remove existing dynamic theme if any
    const existingTheme = document.getElementById(themeId);
    if (existingTheme) {
      existingTheme.remove();
    }

    // Also remove/hide default theme
    const defaultTheme = document.getElementById(defaultThemeId);
    if (defaultTheme) {
      defaultTheme.remove();
    }

    // Create new link element for the theme
    const link = document.createElement('link');
    link.id = themeId;
    link.rel = 'stylesheet';
    link.href = `/cagov/css/${themeFileName}`;
    link.onload = () => {
      setCurrentTheme(themeFile);
      setThemeLoaded(true);
      console.log(`✅ County theme loaded: ${themeFile} for county: ${currentCounty}`);
      console.log(`📄 Theme file loaded: /cagov/css/${themeFileName}`);

      // Verify CSS variables are available
      const rootStyles = getComputedStyle(document.documentElement);
      const p2Color = rootStyles.getPropertyValue('--color-p2').trim();
      console.log(`🎨 Current --color-p2 value: ${p2Color || 'NOT SET'}`);
    };
    link.onerror = () => {
      console.error(`❌ Failed to load theme: ${themeFile}`);
      // Fallback to default theme
      if (themeFile !== DEFAULT_THEME) {
        loadTheme(DEFAULT_THEME);
      }
    };

    // Append to head
    document.head.appendChild(link);
  };

  // Function to manually set theme (for admin/testing)
  const setManualTheme = (theme: keyof typeof CA_GOV_THEMES) => {
    loadTheme(theme);
  };

  // Update theme when user or county changes
  useEffect(() => {
    if (typeof window === 'undefined') return;

    // Check if user is admin - admins should use default theme
    const isAdmin = user?.role === 'ADMIN' || user?.roles?.includes('ADMIN');

    if (isAdmin) {
      // Admin users always use default theme (Oceanside)
      console.log('Admin user detected - using default theme:', DEFAULT_THEME);
      loadTheme(DEFAULT_THEME);
      setCurrentCounty(null);
      setCurrentTheme(DEFAULT_THEME);
      setThemeLoaded(true);
      return;
    }

    const county = extractCounty();

    if (county) {
      setCurrentCounty(county);

      // Map county to theme
      const theme = COUNTY_THEME_MAP[county] || DEFAULT_THEME;

      console.log(`🗺️ County mapping: ${county} → ${theme}`);
      console.log(`📋 Available county mappings:`, Object.keys(COUNTY_THEME_MAP).length, 'counties');

      // Store county in localStorage for persistence
      localStorage.setItem('userCounty', county);

      // Load the theme
      loadTheme(theme);
    } else {
      // No county found, use default theme
      console.log('⚠️ No county found, using default theme:', DEFAULT_THEME);
      console.log('💡 TIP: To test county themes, set localStorage.setItem("manualCounty", "CTA") or any county name/code');
      loadTheme(DEFAULT_THEME);
      setCurrentCounty(null);
      setCurrentTheme(DEFAULT_THEME);
      setThemeLoaded(true);
    }
  }, [user, token]);

  return (
    <CountyThemeContext.Provider
      value={{
        currentCounty,
        currentTheme,
        themeLoaded,
        setManualTheme,
        availableThemes: CA_GOV_THEMES,
      }}
    >
      {children}
    </CountyThemeContext.Provider>
  );
}

export function useCountyTheme() {
  const context = useContext(CountyThemeContext);
  if (context === undefined) {
    throw new Error('useCountyTheme must be used within a CountyThemeProvider');
  }
  return context;
}
