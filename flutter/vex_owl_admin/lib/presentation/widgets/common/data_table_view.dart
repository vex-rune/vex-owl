import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';

class DataTableView extends StatelessWidget {
  final List<DataColumn> columns;
  final List<DataRow> rows;

  const DataTableView({
    super.key,
    required this.columns,
    required this.rows,
  });

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Card(
        child: SizedBox(
          width: double.infinity,
          child: DataTable(
            headingRowHeight: 48,
            dataRowMinHeight: 48,
            dataRowMaxHeight: 56,
            horizontalMargin: 24,
            columnSpacing: 24,
            columns: columns,
            rows: rows.isEmpty
                ? [
                    DataRow(
                      cells: [
                        DataCell(
                          Center(
                            child: Padding(
                              padding: const EdgeInsets.all(24),
                              child: Text(
                                '暂无数据',
                                style: TextStyle(color: AppColors.textTertiary),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ]
                : rows,
          ),
        ),
      ),
    );
  }
}