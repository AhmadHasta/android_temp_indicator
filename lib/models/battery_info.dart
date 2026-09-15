enum TemperatureStatus {
  cool,
  warm,
  hot,
  veryHot;

  String get label {
    switch (this) {
      case TemperatureStatus.cool:
        return 'COOL';
      case TemperatureStatus.warm:
        return 'WARM';
      case TemperatureStatus.hot:
        return 'HOT';
      case TemperatureStatus.veryHot:
        return 'VERY HOT';
    }
  }

  String get description {
    switch (this) {
      case TemperatureStatus.cool:
        return '< 35°C — Optimal & safe';
      case TemperatureStatus.warm:
        return '35°C – 39.9°C — Normal usage';
      case TemperatureStatus.hot:
        return '40°C – 44.9°C — Heavy load / Fast charging';
      case TemperatureStatus.veryHot:
        return '≥ 45°C — High heat, cool down recommended';
    }
  }
}

class BatteryInfo {
  final double temperature;
  final int percentage;
  final bool isCharging;
  final double? voltage;
  final String health;
  final String chargingType;

  const BatteryInfo({
    required this.temperature,
    required this.percentage,
    required this.isCharging,
    this.voltage,
    this.health = 'Unknown',
    this.chargingType = 'Discharging',
  });

  factory BatteryInfo.fromMap(Map<dynamic, dynamic> map) {
    return BatteryInfo(
      temperature: (map['temperature'] as num?)?.toDouble() ?? 0.0,
      percentage: (map['percentage'] as num?)?.toInt() ?? 0,
      isCharging: map['isCharging'] as bool? ?? false,
      voltage: (map['voltage'] as num?)?.toDouble(),
      health: map['health'] as String? ?? 'Unknown',
      chargingType: map['chargingType'] as String? ?? 'Discharging',
    );
  }

  TemperatureStatus get status {
    if (temperature < 35.0) {
      return TemperatureStatus.cool;
    } else if (temperature < 40.0) {
      return TemperatureStatus.warm;
    } else if (temperature < 45.0) {
      return TemperatureStatus.hot;
    } else {
      return TemperatureStatus.veryHot;
    }
  }
}
