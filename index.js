// ✅ PALING ATAS – TIDAK BOLEH ADA APA PUN DI ATAS INI
import 'react-native-url-polyfill/auto'
import { Buffer } from 'buffer'
global.Buffer = Buffer

// Baru import yang lain
import { AppRegistry } from 'react-native';
import App from './src/App';
import { name as appName } from './app.json';

AppRegistry.registerComponent(appName, () => App);
