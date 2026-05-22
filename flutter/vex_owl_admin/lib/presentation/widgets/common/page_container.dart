import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';

class PageContainer extends StatelessWidget {
  final String title;
  final List<Widget>? actions;
  final Widget child;

  const PageContainer({
    super.key,
    required this.title,
    this.actions,
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
          decoration: const BoxDecoration(
            color: AppColors.bgPrimary,
            border: Border(
              bottom: BorderSide(color: AppColors.border),
            ),
          ),
          child: Row(
            children: [
              Text(
                title,
                style: const TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w600,
                  color: AppColors.textPrimary,
                ),
              ),
              const Spacer(),
              if (actions != null) ...actions!,
            ],
          ),
        ),
        Expanded(
          child: Container(
            color: AppColors.bgSecondary,
            child: child,
          ),
        ),
      ],
    );
  }
}