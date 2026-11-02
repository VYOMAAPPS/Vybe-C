# VYBE – local files mode

This build plays music from the phone's own storage (MediaStore) instead of an online backend.
The UI is untouched: Home, Search, Library, Player, Settings are the original screens, fed by
the phone's songs through the same database.

## How it works
- `app/.../localbackend/` – scanner (MediaStore), database sync, local data source. Everything
  local lives here.
- Songs get ids like `local_123`; MusicService plays them from their `content://` URI.
- Search opens in "Library/local" mode by default. Home "Quick picks" falls back to your songs.
- `innertube/.../InnerTube.kt` and `utils/InnerTubeXPlayer.kt` are now offline stubs: every
  online call fails fast and the UI treats it as "no online data".

## Switching to your own API later
1. `app/build.gradle.kts`: set `LOCAL_BACKEND_ENABLED` to `false`.
2. Put your API calls in `InnerTube.kt` (browse/search/next...) and the stream URL lookup in
   `InnerTubeXPlayer.playerResponseForPlayback`.
3. Delete `app/.../localbackend/` and the hooks marked `LOCAL_BACKEND`
   (`grep -rn LOCAL_BACKEND app/src`): MusicService (data source), MainActivity (permission),
   HomeViewModel (fallback), SearchScreen (default source), AndroidManifest (audio permission).

## Removed from the previous developer's code
- The `innertubex` library from the `VYBEGroup` JitPack org (dependency removed).
- Remote player-config download (raw.githubusercontent ZemerTeam), PO-token WebView code.
- Discord application id, updater repo, Listen Together server, share links -> neutral placeholders.
- Replace `YOUR_GITHUB_USERNAME` with your own GitHub username (About/Discord/Updater links).

## License
This project is based on Metrolist and is licensed under GPL-3.0. The `LICENSE` file and the
copyright/credit notices must stay, and if you distribute the app you must offer its source.
