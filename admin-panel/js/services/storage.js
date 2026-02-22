// services/storage.js

/**
 * Upload image to Supabase Storage
 * Returns public URL
 */
async function uploadPoster(file) {
    const ext = file.name.split('.').pop()
    const fileName = `${Date.now()}-${Math.random().toString(36).slice(2)}.${ext}`

    const { error } = await supabase.storage
        .from('mosque-media')
        .upload(fileName, file)

    if (error) throw error

    const { data } = supabase.storage
        .from('mosque-media')
        .getPublicUrl(fileName)

    return data.publicUrl
}

window.uploadPoster = uploadPoster
