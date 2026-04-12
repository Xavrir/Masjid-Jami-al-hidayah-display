import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';
import { createClient } from '@supabase/supabase-js';

const BUCKET = 'banners';
const CACHE_CONTROL = '31536000';
const DOT_ENV_PATH = path.join(process.cwd(), 'scripts', '.env');
const shouldExecute = process.argv.includes('--yes');
const dryRun = process.argv.includes('--dry-run') || !shouldExecute;

loadDotEnv(DOT_ENV_PATH);

const supabaseUrl = process.env.SUPABASE_URL;
const serviceRoleKey = process.env.SUPABASE_SERVICE_ROLE_KEY;

if (!supabaseUrl || !serviceRoleKey) {
    console.error('Missing SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY.');
    console.error('Copy scripts/.env.example to scripts/.env and fill in both values.');
    process.exit(1);
}

const supabase = createClient(supabaseUrl, serviceRoleKey, {
    auth: {
        persistSession: false,
        autoRefreshToken: false
    }
});

async function main() {
    const files = await listBucketFiles();

    if (files.length === 0) {
        console.log(`No files found in bucket ${BUCKET}.`);
        return;
    }

    console.log('This script downloads and re-uploads each file once to update cache headers.');
    if (dryRun) {
        console.log('Dry run mode enabled. Pass --yes to execute the updates.');
    }
    console.log(`Updating cache headers for ${files.length} files in bucket ${BUCKET}...`);

    let updated = 0;
    let failed = 0;

    for (const filePath of files) {
        console.log(`- ${filePath}`);

        if (dryRun) {
            continue;
        }

        try {
            const { data: downloadData, error: downloadError } = await supabase.storage
                .from(BUCKET)
                .download(filePath);

            if (downloadError) {
                throw new Error(`Download failed for ${filePath}: ${downloadError.message}`);
            }

            const contentType = downloadData.type || guessContentType(filePath);
            const { error: updateError } = await supabase.storage
                .from(BUCKET)
                .update(filePath, downloadData, {
                    cacheControl: CACHE_CONTROL,
                    contentType
                });

            if (updateError) {
                throw new Error(`Update failed for ${filePath}: ${updateError.message}`);
            }

            updated += 1;
        } catch (error) {
            failed += 1;
            console.error(`  ! ${error.message}`);
        }
    }

    if (dryRun) {
        console.log(`Dry run complete. ${files.length} files would be updated.`);
        return;
    }

    console.log(`Done. Updated cache headers for ${updated} files. Failed: ${failed}.`);
}

async function listBucketFiles(prefix = '') {
    const files = [];
    let offset = 0;
    const limit = 100;

    while (true) {
        const { data, error } = await supabase.storage
            .from(BUCKET)
            .list(prefix, {
                limit,
                offset,
                sortBy: { column: 'name', order: 'asc' }
            });

        if (error) {
            throw new Error(`List failed for ${prefix || '<root>'}: ${error.message}`);
        }

        const items = data || [];
        if (items.length === 0) {
            break;
        }

        for (const item of items) {
            const itemPath = prefix ? `${prefix}/${item.name}` : item.name;
            if (item.id === null) {
                files.push(...await listBucketFiles(itemPath));
                continue;
            }

            files.push(itemPath);
        }

        if (items.length < limit) {
            break;
        }

        offset += limit;
    }

    return files;
}

function loadDotEnv(filePath) {
    if (!fs.existsSync(filePath)) {
        return;
    }

    const content = fs.readFileSync(filePath, 'utf8');
    for (const line of content.split(/\r?\n/)) {
        const trimmed = line.trim();
        if (!trimmed || trimmed.startsWith('#')) {
            continue;
        }

        const separatorIndex = trimmed.indexOf('=');
        if (separatorIndex === -1) {
            continue;
        }

        const key = trimmed.slice(0, separatorIndex).trim();
        const value = trimmed.slice(separatorIndex + 1).trim();
        if (!process.env[key]) {
            process.env[key] = stripQuotes(value);
        }
    }
}

function stripQuotes(value) {
    if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'"))) {
        return value.slice(1, -1);
    }

    return value;
}

function guessContentType(filePath) {
    const ext = path.extname(filePath).toLowerCase();
    if (ext === '.jpg' || ext === '.jpeg') return 'image/jpeg';
    if (ext === '.png') return 'image/png';
    if (ext === '.webp') return 'image/webp';
    if (ext === '.gif') return 'image/gif';
    if (ext === '.mp4') return 'video/mp4';
    if (ext === '.webm') return 'video/webm';
    if (ext === '.mov') return 'video/quicktime';
    return 'application/octet-stream';
}

main().catch((error) => {
    console.error(error.message);
    process.exit(1);
});
