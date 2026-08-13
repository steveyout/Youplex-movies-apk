package com.example.cinestream.data.adblock

import android.net.Uri
import android.util.Log

object AdBlocker {

    // Known ad networks, popups, trackers, gambling redirectors, and overlay ad domains
    private val knownAdDomains = setOf(
        "popads.net", "popcash.net", "propellerads.com", "adsterra.com", "exoclick.com",
        "juicyads.com", "monetag.com", "adcash.com", "trafficjunky.com", "hilltopads.com",
        "clickadu.com", "revenuehits.com", "a-ads.com", "mgid.com", "taboola.com",
        "outbrain.com", "criteo.com", "doubleclick.net", "googlesyndication.com",
        "googleadservices.com", "pagead2.googlesyndication.com", "g.doubleclick.net",
        "adnxs.com", "rubiconproject.com", "openx.net", "pubmatic.com", "adform.net",
        "adservice.google.com", "histats.com", "statcounter.com", "mc.yandex.ru",
        "clksite.com", "activerevenue.com", "zeropark.com", "richads.com", "bidvertiser.com",
        "smartadserver.com", "adtrue.com", "infolinks.com", "media.net", "sovrn.com",
        "yieldmo.com", "indexww.com", "casalemedia.com", "conversantservices.com",
        "bet365.com", "1xbet.com", "22bet.com", "parimatch.com", "stake.com", "mostbet.com"
    )

    private val adKeywords = setOf(
        "popcash", "popads", "adsterra", "exoclick", "juicyads",
        "propellerads", "monetag", "adcash", "trafficjunky", "hilltopads",
        "clickadu", "doubleclick", "googlesyndication", "googleadservices",
        "ad-delivery", "adform", "adsystem", "adnxs", "rubiconproject",
        "outbrain", "taboola", "criteo", "bet365", "1xbet", "casino",
        "poker", "gambling", "popunder", "popup", "redirect", "clickout",
        "telemetry", "statcounter", "histats", "yandex", "mc.yandex",
        "adservice", "counter", "pixel", "pagead", "banner", "sponsor"
    )

    fun shouldBlockUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val lower = url.lowercase()

        // Block custom URI schemes used for app redirects
        if (lower.startsWith("intent:") ||
            lower.startsWith("market:") ||
            lower.startsWith("whatsapp:") ||
            lower.startsWith("tg:") ||
            lower.startsWith("tbopen:") ||
            lower.startsWith("alipays:")
        ) {
            Log.d("AdBlocker", "Blocked custom URI redirect: $url")
            return true
        }

        try {
            val uri = Uri.parse(url)
            val host = uri.host?.lowercase() ?: ""

            if (host.isNotEmpty()) {
                // Check known exact ad domains and subdomains
                if (knownAdDomains.any { domain -> host == domain || host.endsWith(".$domain") }) {
                    Log.d("AdBlocker", "Blocked known ad network domain: $host")
                    return true
                }

                // Check keyword matches in hostname
                if (adKeywords.any { keyword -> host.contains(keyword) }) {
                    Log.d("AdBlocker", "Blocked ad host keyword match: $host for url: $url")
                    return true
                }
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }

        // Check path keywords for ad tags
        if (lower.contains("/ad/") ||
            lower.contains("/ads/") ||
            lower.contains("/popunder/") ||
            lower.contains("/popup/") ||
            lower.contains("/banner/") ||
            lower.contains("click.php") ||
            lower.contains("redirect.php")
        ) {
            return true
        }

        return false
    }

    val adBlockJavaScript: String = """
        (function() {
            // 1. Strict override of window.open to suppress popups and popunders
            window.open = function() {
                console.log('AdBlocker: Suppressed window.open call');
                if (window.AndroidAdBlock) {
                    window.AndroidAdBlock.onAdBlocked();
                }
                return null;
            };

            // 2. Prevent target="_blank" redirects on links
            document.addEventListener('click', function(e) {
                var target = e.target;
                while (target && target !== document) {
                    if (target.tagName === 'A') {
                        if (target.getAttribute('target') === '_blank') {
                            target.removeAttribute('target');
                        }
                        var href = target.getAttribute('href') || '';
                        if (href.indexOf('http') === 0 && href.indexOf(window.location.hostname) === -1) {
                            var isAdLink = ['bet', 'casino', 'poker', 'pop', 'click', 'redirect', '1xbet', 'ad'].some(function(k) {
                                return href.toLowerCase().indexOf(k) !== -1;
                            });
                            if (isAdLink) {
                                e.preventDefault();
                                e.stopPropagation();
                                console.log('AdBlocker: Intercepted external ad link click:', href);
                                if (window.AndroidAdBlock) {
                                    window.AndroidAdBlock.onAdBlocked();
                                }
                                return false;
                            }
                        }
                    }
                    target = target.parentNode;
                }
            }, true);

            // 3. Remove overlay ads, invisible click-jacking overlays, and known ad elements without removing player/audio controls
            function cleanAds() {
                var adSelectors = [
                    'div[class*="ad-box"]', 'div[class*="ad-container"]', 'div[class*="ad-wrapper"]',
                    'div[id*="ad-box"]', 'div[id*="ad-container"]', 'div[class*="popunder"]', 'div[class*="popup-banner"]',
                    'iframe[src*="adsterra"]', 'iframe[src*="popads"]', 'div[style*="z-index: 2147483647"]',
                    'div[style*="z-index: 999999"]', 'div[style*="z-index:999999"]',
                    '.popunder', '#popunder', '.ad-overlay', '#ad-overlay', '.ad-banner', '.adsbygoogle',
                    'a[href*="bet"]', 'a[href*="casino"]', 'iframe[src*="1xbet"]'
                ];

                adSelectors.forEach(function(selector) {
                    try {
                        var elements = document.querySelectorAll(selector);
                        elements.forEach(function(el) {
                            // Ensure we never hide or delete video/audio player or control elements
                            var tag = el.tagName ? el.tagName.toLowerCase() : '';
                            var id = el.id ? el.id.toLowerCase() : '';
                            var cls = el.className ? (typeof el.className === 'string' ? el.className.toLowerCase() : '') : '';

                            var isPlayerOrAudio = tag === 'video' || tag === 'audio' ||
                                id.indexOf('player') !== -1 || cls.indexOf('player') !== -1 ||
                                id.indexOf('control') !== -1 || cls.indexOf('control') !== -1 ||
                                id.indexOf('audio') !== -1 || cls.indexOf('audio') !== -1 ||
                                id.indexOf('jw') !== -1 || cls.indexOf('jw') !== -1 ||
                                id.indexOf('vjs') !== -1 || cls.indexOf('vjs') !== -1 ||
                                el.querySelector('video') || el.querySelector('audio');

                            if (!isPlayerOrAudio) {
                                el.style.display = 'none';
                                if (el.parentNode) {
                                    el.parentNode.removeChild(el);
                                }
                            }
                        });
                    } catch(e) {}
                });

                // Ensure pointer events are active on video elements
                var videos = document.getElementsByTagName('video');
                for (var i = 0; i < videos.length; i++) {
                    videos[i].style.pointerEvents = 'auto';
                }
            }

            // 4. Unmute and enable sound for all video/audio embeds
            function unmuteAndEnableSound() {
                try {
                    var mediaElements = document.querySelectorAll('video, audio');
                    for (var i = 0; i < mediaElements.length; i++) {
                        var media = mediaElements[i];
                        media.muted = false;
                        media.defaultMuted = false;
                        if (media.volume < 0.1) {
                            media.volume = 1.0;
                        }
                    }
                } catch(e) {}
            }

            // Clean ads and force sound periodically
            cleanAds();
            unmuteAndEnableSound();
            setInterval(cleanAds, 800);
            setInterval(unmuteAndEnableSound, 800);

            // User gesture triggers sound unmute on touch or click
            window.addEventListener('click', unmuteAndEnableSound, true);
            window.addEventListener('touchstart', unmuteAndEnableSound, true);

            if (window.AndroidAdBlock) {
                window.AndroidAdBlock.onAdBlocked();
            }
        })();
    """.trimIndent()
}

