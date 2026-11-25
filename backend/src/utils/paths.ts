import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

/** Folder web pelanggan (tigarasa-web), sejajar dengan folder backend. Berlaku dari src/ maupun dist/. */
export const WEB_DIR = path.resolve(__dirname, '../../../tigarasa-web');

/** Folder foto menu hasil upload admin (disajikan sebagai /uploads/menu/...) */
export const MENU_UPLOAD_DIR = path.join(WEB_DIR, 'uploads', 'menu');
