import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vex_owl_admin/app/app.dart';

void main() {
  testWidgets('App smoke test', (WidgetTester tester) async {
    await tester.pumpWidget(
      const ProviderScope(
        child: VexOwlApp(),
      ),
    );
    await tester.pumpAndSettle();
    expect(find.text('Vex-Owl 管理后台'), findsAny);
  });
}