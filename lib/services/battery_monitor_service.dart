import 'package:flutter/services.dart';
import '../models/battery_info.dart';

class BatteryMonitorService {
  static const MethodChannel _channel = MethodChannel('battery_monitor');

  /// Starts the Android native Foreground Service for continuous status bar monitoring
  static Future<bool> startMonitoring() async {
    try {
      final result = await _channel.invokeMethod<bool>('startMonitoring');
      return result ?? false;
    } on PlatformException catch (_) {
      return false;
    }
  }

  /// Stops the Android native Foreground Service
  static Future<bool> stopMonitoring() async {
    try {
      final result = await _channel.invokeMethod<bool>('stopMonitoring');
      return result ?? false;
    } on PlatformException catch (_) {
      return false;
    }
  }

  /// Checks whether the Foreground Service is currently running
  static Future<bool> isMonitoring() async {
    try {
      final result = await _channel.invokeMethod<bool>('isMonitoring');
      return result ?? false;
    } on PlatformException catch (_) {
      return false;
    }
  }

  /// Retrieves the current snapshot of battery metrics
  static Future<BatteryInfo?> getBatteryInfo() async {
    try {
      final result = await _channel.invokeMethod<Map<dynamic, dynamic>>('getBatteryInfo');
      if (result != null) {
        return BatteryInfo.fromMap(result);
      }
    } on PlatformException catch (_) {
      return null;
    }
    return null;
  }

  /// Checks if POST_NOTIFICATIONS permission is granted (Android 13+)
  static Future<bool> hasNotificationPermission() async {
    try {
      final result = await _channel.invokeMethod<bool>('hasNotificationPermission');
      return result ?? true;
    } on PlatformException catch (_) {
      return true;
    }
  }

  /// Requests POST_NOTIFICATIONS permission on Android 13+
  static Future<bool> requestNotificationPermission() async {
    try {
      final result = await _channel.invokeMethod<bool>('requestNotificationPermission');
      return result ?? true;
    } on PlatformException catch (_) {
      return false;
    }
  }
}
