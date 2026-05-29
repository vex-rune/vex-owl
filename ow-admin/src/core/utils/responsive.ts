import { AppConstants } from '../constants/appConstants';

export type ScreenSize = 'mobile' | 'tablet' | 'desktop';

export function getScreenSize(): ScreenSize {
  const width = window.innerWidth;
  if (width < AppConstants.mobileBreakpoint) {
    return 'mobile';
  } else if (width < AppConstants.tabletBreakpoint) {
    return 'tablet';
  }
  return 'desktop';
}

export function isMobile(): boolean {
  return getScreenSize() === 'mobile';
}

export function isTablet(): boolean {
  return getScreenSize() === 'tablet';
}

export function isDesktop(): boolean {
  return getScreenSize() === 'desktop';
}