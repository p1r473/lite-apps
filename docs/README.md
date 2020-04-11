---
title: "Hermit Library: Lite Apps, Fonts, Styles, Scriptlets, Blocklists"
description: "Repository of Hermit app-related data, served from https://lite-apps.chimbori.com/"
---

# Lite App Manifests

This repo contains manifest files for [Hermit](https://hermit.chimbori.com). Manifests are zipped up into `.hermit` files, and can be used to set up a new Lite App in Hermit.

[![CircleCI](https://circleci.com/gh/chimbori/lite-apps/tree/master.svg?style=svg)](https://circleci.com/gh/chimbori/lite-apps/tree/master)

## Submitting New Lite Apps

We welcome new additions to this Library, and enhancements to existing ones (e.g. adding new Bookmarks or Integrations). Please see the [step by step instructions](CONTRIBUTING.md) and an overview of the automated tools and tests we’ve made available.

# Syntax

Hermit Lite Apps are `.zip` files, with the extension `.hermit`. Each zip file contains multiple files that define the Lite App, how it should be installed, and default settings to be used. Only two files are required, all others are optional.

- `manifest.json` : The basic metadata about a Lite App is contained in a `manifest.json` file. This follows the [W3C Web App Manifest](https://www.w3.org/TR/appmanifest/) format with additional vendor-specific fields for Hermit that are not yet a part of the W3C standard.

- `icons/favicon.png`: The icon to be used for this site when importing the Lite App. Ensure that icons are large enough (our automated tests require that the size be exactly 300×300px).

## Directory Structure

    Lite App.hermit
    - manifest.json    (required)
    + icons
      - favicon.png    (required)

## manifest.json

    {
      "manifest_version": 1,
      "name": "Lite App Example",
      "start_url": "https://example.com/",
      "theme_color": "#ff0000",
      "secondary_color": "#00ff00",
      "icon": "favicon.png",
      "settings": {
        "block_malware":  true | false,
        "do_not_track":  true | false,
        "third_party_cookies":  true | false,
        "load_images":  true | false,
        "open_links": "in_app" | "browser",
        "preferred_view": "accelerated",
        "save_data":  true | false,
        "scroll_to_top":  true | false,
        "text_zoom":  true | false,
        "user_agent": "desktop" | ""
      },
      "bookmarks": [
        {
          "url": "https://example.com/top-level-navigation",
          "name": "Top Level"
        },
        {
          "url": "https://example.com/another-top-level-navigation",
          "name": "Another Top Level"
        }
      ],
      "search": [
        {
          "url": "https://example.com/search?q=%s",
          "name": "Integrated Search, use %s as a search query placeholder."
        }
      ],
      "share": [
        {
          "url": "https://example.com/share?u=%u&t=%t",
          "name": "Share from the native Android dialog to any Lite App"
        }
      ],
      "feeds": [
        {
          "url": "https://example.com/rss.xml",
          "name": "RSS feed of all new content"
        },
        {
          "url": "https://example.com/atom.xml",
          "name": "Atom feeds are supported too."
        }
      ]
    }


### Required Fields

- `manifest_version`: Integer, must be `2` (the current version). Lite Apps may require Hermit to be upgraded if it is too old to support an older version of the manifest.
- `name`: The name of the Lite App, shown on the home screen & at the top of the app.
- `manifest_url`: The URL where this Lite App Manifest will be hosted. Typically, this should be `https://lite-apps.chimbori.com/lite-apps/v3/YOUR_APP_NAME.hermit`. This must be explicitly specified for every Lite App, although it is not present when you create your own Lite App in Hermit and export it.
- `start_url`: The URL for the home page of the Lite App.
- `theme_color`: A hex-formatted color used as the theme color for the app.
- `secondary_color`: A hex-formatted color used for the navigation bar and in other places in the app.
- `icon`: The default is `favicon.png`. When users set custom icons or monograms, the exported `manifest.json` contains the filename of the icon chosen by the user.

### Optional Fields

- `settings`: A vendor-specific addition to the W3C Web Manifest format, where Hermit settings are saved. See details below.
- `bookmarks`: A list of bookmarks shown in the left sidebar in every Hermit Lite App.
- `search`: Search can be integrated into any Lite App. [See details on how to configure this](https://hermit.chimbori.com/help/integrations).
- `share`: Share text from any Android app directly (natively) into a Hermit Lite App. [See details on how to configure this](https://hermit.chimbori.com/help/integrations).
- `feeds`: RSS or Atom feed URLs that Hermit will check regularly and notify the user about.

### Settings

- `block_malware`: Whether or not to block ads and malware. Boolean, `true` \| `false`
- `do_not_track`: Whether to send the [Do Not Track HTTP header](https://donottrack.us/). Boolean, `true` \| `false`
- `load_images`: Image loading can be disabled, e.g. on slow networks. Boolean, `true` \| `false`
- `open_links`: Choose where external links should be opened: `"in_app"` opens them inside the Lite App. `"browser"` uses the system default browser.
- `preferred_view`: `"accelerated"` will load fast Accelerated Mobile Pages instead of slow regular ones. `"original`" loads the original pages.
- `save_data`: Whether to send the [Save Data client hint](https://httpwg.org/http-extensions/client-hints.html#the-save-data-hint) on every request. Boolean, `true` \| `false`
- `scroll_to_top`: Whether to show the Scroll to Top button in the Hermit UI. Boolean, `true` \| `false`
- `pull_to_refresh`: Whether swiping down in the Lite App should refresh the page. Boolean, `true` \| `false`
- `text_zoom`: A percentage number between `0` to `200`, in steps of `20`. The default is `100`.
- `user_agent`: `"desktop"` reports the user agent of this browser as a desktop user agent, `""` to use the default mobile user agent.

## Questions?

Email us at [hello@chimbori.com](mailto:hello@chimbori.com) with your questions; we’ll be happy to answer. Be sure to include a link to your work-in-progress source code.
