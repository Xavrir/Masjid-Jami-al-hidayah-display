(function () {
    const PREFIX = 'masjid-cache:';

    function buildKey(key) {
        return PREFIX + key;
    }

    function get(key, ttlMs) {
        try {
            const raw = localStorage.getItem(buildKey(key));
            if (!raw) return null;

            const parsed = JSON.parse(raw);
            if (!parsed || typeof parsed.savedAt !== 'number') {
                localStorage.removeItem(buildKey(key));
                return null;
            }

            if (ttlMs > 0 && Date.now() - parsed.savedAt > ttlMs) {
                localStorage.removeItem(buildKey(key));
                return null;
            }

            return parsed.value;
        } catch (error) {
            console.warn('Cache read failed:', error);
            return null;
        }
    }

    function set(key, value) {
        try {
            localStorage.setItem(buildKey(key), JSON.stringify({
                savedAt: Date.now(),
                value: value
            }));
        } catch (error) {
            console.warn('Cache write failed:', error);
        }
    }

    function remove(key) {
        localStorage.removeItem(buildKey(key));
    }

    window.pageCache = {
        get,
        set,
        remove
    };
})();
