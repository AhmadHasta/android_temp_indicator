import 'dart:async';
import 'package:flutter/material.dart';
import 'models/battery_info.dart';
import 'services/battery_monitor_service.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const BatteryMonitorApp());
}

class BatteryMonitorApp extends StatelessWidget {
  const BatteryMonitorApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Battery Temperature Monitor',
      debugShowCheckedModeBanner: false,
      themeMode: ThemeMode.dark,
      darkTheme: ThemeData(
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF0F172A), // Slate 900
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFF38BDF8), // Sky 400
          secondary: Color(0xFF818CF8),
          surface: Color(0xFF1E293B), // Slate 800
        ),
        cardTheme: CardThemeData(
          color: const Color(0xFF1E293B),
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
            side: const BorderSide(color: Color(0xFF334155), width: 1),
          ),
        ),
        useMaterial3: true,
      ),
      theme: ThemeData(
        brightness: Brightness.light,
        scaffoldBackgroundColor: const Color(0xFFF8FAFC),
        colorScheme: const ColorScheme.light(
          primary: Color(0xFF0284C7),
          surface: Colors.white,
        ),
        cardTheme: CardThemeData(
          color: Colors.white,
          elevation: 1,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
            side: const BorderSide(color: Color(0xFFE2E8F0), width: 1),
          ),
        ),
        useMaterial3: true,
      ),
      home: const DashboardScreen(),
    );
  }
}

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen>
    with WidgetsBindingObserver {
  BatteryInfo? _batteryInfo;
  bool _isMonitoring = false;
  bool _hasNotificationPermission = true;
  bool _isLoading = true;
  Timer? _pollingTimer;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _initializeData();
  }

  @override
  void dispose() {
    _stopPolling();
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      // Resume live polling while user is actively viewing the screen
      _startPolling();
      _fetchCurrentState();
    } else if (state == AppLifecycleState.paused ||
        state == AppLifecycleState.detached) {
      // Stop Flutter polling immediately when app is minimized/closed to save battery and CPU
      _stopPolling();
    }
  }

  Future<void> _initializeData() async {
    setState(() => _isLoading = true);
    await _checkPermissions();
    await _fetchCurrentState();
    _startPolling();
    if (mounted) {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _checkPermissions() async {
    final hasPerm = await BatteryMonitorService.hasNotificationPermission();
    if (mounted) {
      setState(() {
        _hasNotificationPermission = hasPerm;
      });
    }
  }

  Future<void> _requestNotificationPermission() async {
    final granted =
        await BatteryMonitorService.requestNotificationPermission();
    if (mounted) {
      setState(() {
        _hasNotificationPermission = granted;
      });
      if (granted && !_isMonitoring) {
        _toggleMonitoring();
      }
    }
  }

  Future<void> _fetchCurrentState() async {
    final isMonitoring = await BatteryMonitorService.isMonitoring();
    final info = await BatteryMonitorService.getBatteryInfo();
    if (mounted) {
      setState(() {
        _isMonitoring = isMonitoring;
        if (info != null) {
          _batteryInfo = info;
        }
      });
    }
  }

  void _startPolling() {
    _stopPolling();
    _pollingTimer = Timer.periodic(const Duration(seconds: 3), (timer) {
      _fetchCurrentState();
    });
  }

  void _stopPolling() {
    _pollingTimer?.cancel();
    _pollingTimer = null;
  }

  Future<void> _toggleMonitoring() async {
    if (!_hasNotificationPermission) {
      await _requestNotificationPermission();
      return;
    }

    if (_isMonitoring) {
      final success = await BatteryMonitorService.stopMonitoring();
      if (success && mounted) {
        setState(() => _isMonitoring = false);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Monitoring dihentikan.'),
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
    } else {
      final success = await BatteryMonitorService.startMonitoring();
      if (success && mounted) {
        setState(() => _isMonitoring = true);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text(
                'Monitoring aktif! Suhu sekarang muncul di status bar.'),
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
    }
    await _fetchCurrentState();
  }

  Color _getStatusColor(TemperatureStatus status) {
    switch (status) {
      case TemperatureStatus.cool:
        return const Color(0xFF06B6D4); // Cyan 500
      case TemperatureStatus.warm:
        return const Color(0xFFF59E0B); // Amber 500
      case TemperatureStatus.hot:
        return const Color(0xFFF97316); // Orange 500
      case TemperatureStatus.veryHot:
        return const Color(0xFFEF4444); // Red 500
    }
  }

  IconData _getStatusIcon(TemperatureStatus status) {
    switch (status) {
      case TemperatureStatus.cool:
        return Icons.ac_unit_rounded;
      case TemperatureStatus.warm:
        return Icons.wb_sunny_rounded;
      case TemperatureStatus.hot:
        return Icons.local_fire_department_rounded;
      case TemperatureStatus.veryHot:
        return Icons.warning_amber_rounded;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'Battery Temperature',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
        ),
        centerTitle: false,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded),
            tooltip: 'Perbarui Data',
            onPressed: _fetchCurrentState,
          ),
          IconButton(
            icon: const Icon(Icons.info_outline_rounded),
            tooltip: 'Tips HyperOS',
            onPressed: () => _showHyperOSTipsDialog(context),
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _fetchCurrentState,
              child: ListView(
                padding: const EdgeInsets.symmetric(
                    horizontal: 16.0, vertical: 12.0),
                children: [
                  if (!_hasNotificationPermission) _buildPermissionWarning(),
                  _buildStatusIndicatorBanner(),
                  const SizedBox(height: 16),
                  _buildTemperatureHeroCard(),
                  const SizedBox(height: 16),
                  _buildStatsGrid(),
                  const SizedBox(height: 20),
                  _buildMonitoringControlCard(),
                  const SizedBox(height: 16),
                  _buildStatusBarPreviewCard(),
                  const SizedBox(height: 16),
                  _buildOptimizationGuideCard(),
                  const SizedBox(height: 24),
                ],
              ),
            ),
    );
  }

  Widget _buildPermissionWarning() {
    return Card(
      color: const Color(0xFF7C2D12), // Amber/Red dark
      margin: const EdgeInsets.only(bottom: 16),
      child: Padding(
        padding: const EdgeInsets.all(14.0),
        child: Row(
          children: [
            const Icon(Icons.notifications_off_rounded,
                color: Color(0xFFFDBA74)),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: const [
                  Text(
                    'Izin Notifikasi Dibutuhkan',
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                  ),
                  SizedBox(height: 2),
                  Text(
                    'Diperlukan agar indikator suhu dapat tampil di status bar.',
                    style: TextStyle(color: Color(0xFFFED7AA), fontSize: 12),
                  ),
                ],
              ),
            ),
            ElevatedButton(
              onPressed: _requestNotificationPermission,
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFFF97316),
                foregroundColor: Colors.white,
                padding:
                    const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              ),
              child: const Text('Izinkan', style: TextStyle(fontSize: 12)),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatusIndicatorBanner() {
    final statusColor =
        _isMonitoring ? const Color(0xFF10B981) : const Color(0xFF64748B);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      decoration: BoxDecoration(
        color: statusColor.withAlpha(25),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: statusColor.withAlpha(80)),
      ),
      child: Row(
        children: [
          Container(
            width: 10,
            height: 10,
            decoration: BoxDecoration(
              color: statusColor,
              shape: BoxShape.circle,
              boxShadow: _isMonitoring
                  ? [
                      BoxShadow(
                        color: statusColor.withAlpha(150),
                        blurRadius: 6,
                        spreadRadius: 2,
                      )
                    ]
                  : null,
            ),
          ),
          const SizedBox(width: 10),
          Text(
            _isMonitoring
                ? 'Status Bar Monitoring: AKTIF'
                : 'Status Bar Monitoring: NONAKTIF',
            style: TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: statusColor,
            ),
          ),
          const Spacer(),
          Text(
            _isMonitoring ? 'Berjalan' : 'Berhenti',
            style: TextStyle(
              fontSize: 12,
              color: statusColor.withAlpha(200),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTemperatureHeroCard() {
    final info = _batteryInfo;
    final temp = info?.temperature ?? 0.0;
    final status = info?.status ?? TemperatureStatus.cool;
    final statusColor = _getStatusColor(status);

    return Card(
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 24.0, horizontal: 20.0),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'SUHU BATERAI',
                  style: TextStyle(
                    fontSize: 12,
                    letterSpacing: 1.2,
                    fontWeight: FontWeight.w700,
                    color: Color(0xFF94A3B8),
                  ),
                ),
                Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: statusColor.withAlpha(30),
                    borderRadius: BorderRadius.circular(20),
                    border: Border.all(color: statusColor.withAlpha(90)),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(_getStatusIcon(status),
                          size: 14, color: statusColor),
                      const SizedBox(width: 5),
                      Text(
                        status.label,
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.bold,
                          color: statusColor,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 14),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  temp > 0 ? temp.toStringAsFixed(1) : '--.-',
                  style: TextStyle(
                    fontSize: 60,
                    fontWeight: FontWeight.w900,
                    letterSpacing: -1.5,
                    color: statusColor,
                  ),
                ),
                const Padding(
                  padding: EdgeInsets.only(top: 8.0),
                  child: Text(
                    '°C',
                    style: TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                      color: Color(0xFF94A3B8),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 6),
            Text(
              status.description,
              style: const TextStyle(
                fontSize: 13,
                color: Color(0xFF94A3B8),
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatsGrid() {
    final info = _batteryInfo;
    final percentage = info?.percentage ?? 0;
    final isCharging = info?.isCharging ?? false;
    final voltage = info?.voltage;
    final health = info?.health ?? 'Unknown';
    final chargingType = info?.chargingType ?? 'Discharging';

    return GridView.count(
      crossAxisCount: 2,
      crossAxisSpacing: 12,
      mainAxisSpacing: 12,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      childAspectRatio: 1.6,
      children: [
        _buildStatCard(
          title: 'Persentase',
          value: '$percentage%',
          subtitle: isCharging ? 'Sedang diisi' : 'Penggunaan baterai',
          icon: isCharging ? Icons.battery_charging_full : Icons.battery_std,
          accentColor: const Color(0xFF38BDF8),
        ),
        _buildStatCard(
          title: 'Status Pengisian',
          value: isCharging ? 'Mengisi Daya' : 'Tidak Mengisi',
          subtitle: chargingType,
          icon: isCharging ? Icons.bolt_rounded : Icons.power_off_rounded,
          accentColor: isCharging
              ? const Color(0xFF10B981)
              : const Color(0xFF64748B),
        ),
        _buildStatCard(
          title: 'Tegangan (Voltage)',
          value: voltage != null
              ? '${voltage.toStringAsFixed(2)} V'
              : 'Tidak tersedia',
          subtitle: 'Kondisi cell baterai',
          icon: Icons.electric_meter_rounded,
          accentColor: const Color(0xFFA855F7),
        ),
        _buildStatCard(
          title: 'Kesehatan Baterai',
          value: health,
          subtitle: 'Laporan sensor Android',
          icon: Icons.health_and_safety_rounded,
          accentColor: health == 'Good'
              ? const Color(0xFF10B981)
              : const Color(0xFFEF4444),
        ),
      ],
    );
  }

  Widget _buildStatCard({
    required String title,
    required String value,
    required String subtitle,
    required IconData icon,
    required Color accentColor,
  }) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.w600,
                    color: Color(0xFF94A3B8),
                  ),
                ),
                Icon(icon, size: 18, color: accentColor),
              ],
            ),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  value,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 2),
                Text(
                  subtitle,
                  style: const TextStyle(
                    fontSize: 10,
                    color: Color(0xFF64748B),
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMonitoringControlCard() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: _isMonitoring
                        ? const Color(0xFF10B981).withAlpha(30)
                        : const Color(0xFF64748B).withAlpha(30),
                    shape: BoxShape.circle,
                  ),
                  child: Icon(
                    _isMonitoring
                        ? Icons.visibility_rounded
                        : Icons.visibility_off_rounded,
                    color: _isMonitoring
                        ? const Color(0xFF10B981)
                        : const Color(0xFF94A3B8),
                  ),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        _isMonitoring
                            ? 'Foreground Service Aktif'
                            : 'Monitoring Siap Dijalankan',
                        style: const TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        _isMonitoring
                            ? 'Suhu tampil di status bar. Aplikasi dapat ditutup.'
                            : 'Nyalakan untuk menampilkan suhu di status bar.',
                        style: const TextStyle(
                          fontSize: 12,
                          color: Color(0xFF94A3B8),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              height: 48,
              child: ElevatedButton.icon(
                onPressed: _toggleMonitoring,
                icon: Icon(
                  _isMonitoring
                      ? Icons.stop_circle_outlined
                      : Icons.play_arrow_rounded,
                ),
                label: Text(
                  _isMonitoring ? 'Stop Monitoring' : 'Start Monitoring',
                  style: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                style: ElevatedButton.styleFrom(
                  backgroundColor: _isMonitoring
                      ? const Color(0xFFEF4444)
                      : const Color(0xFF0284C7),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatusBarPreviewCard() {
    final temp = _batteryInfo?.temperature.round() ?? 38;
    final percentage = _batteryInfo?.percentage ?? 72;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'PREVIEW STATUS BAR',
              style: TextStyle(
                fontSize: 11,
                letterSpacing: 1.1,
                fontWeight: FontWeight.w700,
                color: Color(0xFF94A3B8),
              ),
            ),
            const SizedBox(height: 10),
            Container(
              padding:
                  const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
              decoration: BoxDecoration(
                color: const Color(0xFF0B1329),
                borderRadius: BorderRadius.circular(10),
                border: Border.all(color: const Color(0xFF1E293B)),
              ),
              child: Row(
                children: [
                  const Text(
                    '12:30',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: Colors.white70,
                    ),
                  ),
                  const SizedBox(width: 6),
                  // Simulated status bar temperature icon (beside clock)
                  Container(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 4, vertical: 1),
                    decoration: BoxDecoration(
                      color: Colors.white24,
                      borderRadius: BorderRadius.circular(3),
                    ),
                    child: Text(
                      '$temp°',
                      style: const TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.w900,
                        color: Colors.white,
                      ),
                    ),
                  ),
                  const Spacer(),
                  const Icon(Icons.signal_cellular_4_bar_rounded,
                      size: 14, color: Colors.white70),
                  const SizedBox(width: 4),
                  const Icon(Icons.wifi_rounded,
                      size: 14, color: Colors.white70),
                  const SizedBox(width: 6),
                  Text(
                    '$percentage%',
                    style: const TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                      color: Colors.white70,
                    ),
                  ),
                  const SizedBox(width: 2),
                  const Icon(Icons.battery_5_bar_rounded,
                      size: 14, color: Colors.white70),
                ],
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              'Indikator suhu tampil di samping jam (area notifikasi status bar) via ongoing notification small icon tanpa memerlukan overlay window.',
              style: TextStyle(fontSize: 11, color: Color(0xFF64748B)),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildOptimizationGuideCard() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14.0),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Icon(Icons.lightbulb_outline_rounded,
                color: Color(0xFFFBBF24), size: 20),
            const SizedBox(width: 10),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Catatan untuk Xiaomi / Poco (HyperOS)',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 4),
                  const Text(
                    'Agar monitoring tetap berjalan saat layar mati di HyperOS, atur Battery Saver aplikasi ke "No restrictions" dan beri izin "Autostart".',
                    style: TextStyle(fontSize: 11, color: Color(0xFF94A3B8)),
                  ),
                  const SizedBox(height: 6),
                  InkWell(
                    onTap: () => _showHyperOSTipsDialog(context),
                    child: const Text(
                      'Lihat petunjuk lengkap →',
                      style: TextStyle(
                        fontSize: 11,
                        color: Color(0xFF38BDF8),
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showHyperOSTipsDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Row(
          children: [
            Icon(Icons.phone_android_rounded, color: Color(0xFF38BDF8)),
            SizedBox(width: 8),
            Text('Tips Xiaomi / HyperOS', style: TextStyle(fontSize: 17)),
          ],
        ),
        content: const SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                'Sistem HyperOS / MIUI memiliki manajemen baterai yang agresif. Ikuti langkah berikut agar status bar monitor tidak tertutup otomatis:',
                style: TextStyle(fontSize: 13),
              ),
              SizedBox(height: 12),
              Text(
                '1. Battery Saver:\nBuka App Info > Battery saver > Pilih "No restrictions" (Tidak ada batasan).',
                style: TextStyle(fontSize: 12, color: Color(0xFF94A3B8)),
              ),
              SizedBox(height: 8),
              Text(
                '2. Autostart:\nBuka App Info > Aktifkan toggle "Autostart".',
                style: TextStyle(fontSize: 12, color: Color(0xFF94A3B8)),
              ),
              SizedBox(height: 8),
              Text(
                '3. Lock di Recent Apps:\nBuka Recent Apps > Tahan aplikasi Battery Monitor > Ketuk ikon Gembok (Lock).',
                style: TextStyle(fontSize: 12, color: Color(0xFF94A3B8)),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(),
            child: const Text('Mengerti'),
          ),
        ],
      ),
    );
  }
}
