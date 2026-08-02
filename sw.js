const CACHE_NAME = 'reflex7-v1.1.0-r2';
const CORE_ASSETS = [
    './',
    './index.html',
    './style.css',
    './script.js',
    './bg.png',
    './manifest.webmanifest',
    './assets/icon.svg',
    './assets/icon-maskable.svg',
    './offline.html'
];

self.addEventListener('install', (event) => {
    event.waitUntil(caches.open(CACHE_NAME).then((cache) => cache.addAll(CORE_ASSETS)));
    self.skipWaiting();
});

self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys()
            .then((keys) => Promise.all(keys.filter((key) => key.startsWith('reflex7-') && key !== CACHE_NAME).map((key) => caches.delete(key))))
            .then(() => self.clients.claim())
    );
});

self.addEventListener('fetch', (event) => {
    const requestUrl = new URL(event.request.url);
    if (event.request.method !== 'GET' || requestUrl.origin !== self.location.origin) return;

    if (event.request.mode === 'navigate') {
        event.respondWith(
            fetch(event.request)
                .then((response) => response)
                .catch(() => caches.match('./index.html').then((response) => response || caches.match('./offline.html')))
        );
        return;
    }

    event.respondWith(
        caches.match(event.request).then((cached) => {
            const network = fetch(event.request).then((response) => {
                if (response.ok) caches.open(CACHE_NAME).then((cache) => cache.put(event.request, response.clone()));
                return response;
            });
            return cached || network;
        })
    );
});
