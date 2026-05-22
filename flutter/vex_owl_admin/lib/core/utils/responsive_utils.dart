import 'package:flutter/material.dart';
import '../constants/app_constants.dart';

enum ScreenSize { mobile, tablet, desktop }

class ResponsiveUtils {
  ResponsiveUtils._();

  static ScreenSize getScreenSize(BuildContext context) {
    final width = MediaQuery.of(context).size.width;
    if (width < AppConstants.mobileBreakpoint) {
      return ScreenSize.mobile;
    } else if (width < AppConstants.tabletBreakpoint) {
      return ScreenSize.tablet;
    } else {
      return ScreenSize.desktop;
    }
  }

  static bool isMobile(BuildContext context) {
    return getScreenSize(context) == ScreenSize.mobile;
  }

  static bool isTablet(BuildContext context) {
    return getScreenSize(context) == ScreenSize.tablet;
  }

  static bool isDesktop(BuildContext context) {
    return getScreenSize(context) == ScreenSize.desktop;
  }

  static double getContentWidth(BuildContext context) {
    final width = MediaQuery.of(context).size.width;
    final screenSize = getScreenSize(context);

    switch (screenSize) {
      case ScreenSize.mobile:
        return width;
      case ScreenSize.tablet:
        return width - AppConstants.sidebarCollapsedWidth;
      case ScreenSize.desktop:
        return width - AppConstants.sidebarWidth;
    }
  }
}