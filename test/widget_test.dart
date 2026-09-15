import 'package:flutter_test/flutter_test.dart';
import 'package:android_temp_indicator/main.dart';

void main() {
  testWidgets('Dashboard smoke test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const BatteryMonitorApp());

    // Verify app bar title
    expect(find.text('Battery Temperature'), findsOneWidget);
  });
}
