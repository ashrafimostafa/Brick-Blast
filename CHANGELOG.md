# Changelog

## v1.5.1

- Fix pause buffering that skipped brick lowering and allowed bypassing challenge pressure ([#7](https://github.com/ashrafimostafa/Brick-Blast/issues/7))
- Update app launcher and F-Droid store icon

## v1.5.0

- Punchier game audio: machine-gun ball launch chatter and varied Crack/Blam/Pop brick destroy sounds
- Richer brick smash particle effects with multiple explosion styles
- Settings toggle for Rich Explosions
- Fix brick HP display above 100
- Fix equipped board color pack applying on the first game start
- Keep skip-round button above the system navigation bar
- Stop bricks from advancing behind the launcher in late rounds

## v1.4.0

- Coin shop with wallet balance on the main menu; coins earned shown on game over and victory
- Coins banked when saving and quitting (fixes double-count on continue)
- Board color packs: six premium themes with animated effects and Block World voxel style
- Skip to next round button with ball recall animation
- Achievements menu with 54 milestones, share, and improved unlock popup
- Fix brick HP numbers and rounded corners disappearing in later rounds
- Improved rendering performance during large ball counts; removed CRIT popup text

## v1.3.0

- Persian (Farsi) UI with Vazir font for menus, settings, and in-game text
- Fix language switch in Settings (persists correctly and applies on restart)
- Separate save slots per game mode (Classic, Hardcore, Challenge, Time Attack)
- Full board restore when continuing a saved game
- Smoother gameplay (Choreographer game loop, rendering optimizations)
- Harder difficulty curve (brick HP, ball pickups, lose line)
- Performance improvements for high ball counts

## v1.2.0

- F-Droid reproducible build support (developer-signed fdroid APK on GitHub Releases)
- Separate fdroid and store release artifacts in CI
- F-Droid metadata and category updates

## v1.1.1

- Fix ad in CI

## v1.1.0

- Add save-me option with ad reward

## v1.0.1

- Fix release R8 crash

## v1.0.0

- Initial release of Brick Blast
- Classic mode, challenge mode, time attack, and hardcore mode
- Dark theme support
- Accessibility improvements
- Performance optimizations
