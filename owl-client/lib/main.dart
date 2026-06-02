import 'package:flutter/material.dart';
import 'core/utils/storage.dart';
import 'presentation/app.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Storage.init();
  runApp(const VexOwlApp());
}
