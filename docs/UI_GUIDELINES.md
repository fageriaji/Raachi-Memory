# Raachi Memory UI Guidelines

## 1. Brand Character

Raachi Memory should feel calm, capable, private, and friendly. It is an everyday utility, not a marketing page. Screens prioritize clear hierarchy, quick scanning, and confident actions.

## 2. Brand Assets

- Use the approved Raachi Memory RM circuit logo.
- Preserve the logo's navy/indigo and orange colors.
- Do not recreate or approximate the logo with unrelated icons.
- Keep sufficient clear space around the mark.
- Use the appropriate asset for adaptive launcher, round launcher, Android themed icon, splash, onboarding, and About contexts.
- The profile-led drawer intentionally does not repeat the full logo or wordmark.

## 3. Typography

- Font family: bundled Nunito.
- Use one consistent primary-screen title size across Reminders, Ledger, Expenses, Activity, Profile, Settings, and About.
- Dashboard app name is centered in the top bar.
- Compact cards use title/body styles, not hero typography.
- Avoid negative letter spacing and viewport-scaled font sizes.
- Allow names and user-generated text to wrap or ellipsize without overlapping actions.

## 4. Color Roles

Core brand roles:

- Primary navy/indigo: application identity, primary actions, greeting, selection, and focus.
- Orange: brand accent and supporting highlights.
- Green: completed, returned, successful, and positive states.
- Red/coral: money amounts or destructive/overdue states where context requires.
- Purple: Ledger module identity. Use a lighter purple token in dark theme.
- Teal: item-ledger identity and selected expense accents.
- Pink/blue/green category accents: reminder recognition.

Rules:

- Do not use brown-dominant surfaces in dark mode.
- Do not use one hue for the entire application.
- Maintain readable contrast in both themes.
- Do not encode state by color alone; pair color with iconography or text.
- Destructive actions require a clear red role and confirmation when data loss is significant.

## 5. Navigation

### Primary Top Bars

- Dashboard: hamburger, centered app name, profile action.
- Other primary screens: hamburger and centered module name.
- Use the same title style and top-bar height across primary modules.

### Editors and Secondary Screens

- Use a back icon for editor, crop, setup, and detail flows.
- Do not show the bottom navigation inside focused editor flows.
- Keep Save or confirmation actions visible and unambiguous.

### Drawer

- Width: 72% of screen width, capped at 300dp.
- Header: centered 100dp circular profile image and user name below.
- User name: maximum two lines with ellipsis.
- Main group: Home, Reminders, Ledger, Expenses, Activity.
- Secondary group: Profile, Backup & Restore, Settings, About.
- Use distinct colored Material icons for destination recognition.
- Footer displays application version and copyright/brand text.

### Bottom Navigation

- Keep exactly four primary destinations: Home, Reminders, Ledger, Expenses.
- Use the same destination color identities as the drawer.
- Do not add Activity, Profile, Settings, or About to the bottom bar.

## 6. Cards and Lists

- Use cards for individual repeated records and compact summaries.
- Do not place cards inside cards.
- Keep corner radii restrained and consistent with Material 3 theme shapes.
- Reminder and ledger list cards must remain compact enough to scan several records on screen.
- Delete actions belong in the upper-right available action area of reminder and ledger cards.
- Prevent dynamic actions, badges, and long names from changing card width or overlapping content.

### Reminder Cards

- Use full-color category emoji or category visual, not outline-only substitutes.
- Preserve category color identity across Dashboard and Reminder list.
- Show title, next time/date, repeat description, and relevant actions.
- Interval editors expose an "Allow during quiet hours" switch; it is off by default and does not appear for other repeat types.
- Completed state uses green or muted-success treatment.

### Ledger Cards

- Ledger module identity: purple.
- Money identity: coral (`#F95738` family).
- Item identity: teal (`#43AA8B` family).
- Use lighter accessible ledger purple in dark mode.
- WhatsApp and Mark Returned actions must remain visually distinct.

### Expense Overview

- Use the approved two-row overview composition rather than a large Dashboard expense card.
- Avoid brown surfaces.
- Clearly separate total balance, account balances, period summaries, and transaction history.
- Debits, credits, and transfers must remain distinguishable by label and icon as well as color.

## 7. Controls

- Use icons for familiar actions such as back, menu, search, delete, edit, calendar, and contacts.
- Provide content descriptions and tooltips where an icon is not self-explanatory.
- Use segmented controls or tabs for list modes and filters.
- Use switches for sound, biometric, and other binary settings.
- Use native Android date/time pickers for date and time entry.
- Place Quiet Hours under Settings > Reminder Preferences, show start/end controls only while enabled, and reject identical start/end times.
- Maintain at least 48dp touch targets.
- Use equal-width paired buttons where a two-choice security flow requires Back and Continue/Confirm.

## 8. Forms and Validation

- Keep 12-16dp vertical separation between related fields and actions.
- Display field errors near the affected control.
- Do not clear user input after validation failure.
- Keep primary actions reachable above system navigation and the keyboard.
- Restrict new reminders to future date/time and explain invalid scheduling clearly.
- Format recovery codes without moving the cursor unexpectedly.
- Use the Android contact picker from the name/contact affordance and populate both name and number.

## 9. App Lock Screens

- Use centered branding and a clear security icon.
- Keep passcode fields, error text, and buttons separated vertically.
- Use full-width primary actions.
- When two navigation actions are needed, use two equal-width buttons instead of a text-only Back action.
- Never display or imply a universal recovery password.

## 10. Themes and System UI

- Support System, Light, and Dark modes.
- Status/navigation bar icon appearance must follow the active app theme, not only the system theme.
- Logos must remain visible on dark surfaces.
- Dark theme should use layered dark neutrals rather than near-black everywhere.
- Review every domain color for dark-theme contrast.

## 11. Accessibility and Responsiveness

- Check layouts at increased font scale.
- Provide TalkBack labels for meaningful actions and images.
- Keep text contrast and touch targets compliant with Android accessibility guidance.
- Avoid horizontal clipping on narrow phones.
- Use stable constraints for top bars, bottom navigation, avatar sizes, filters, and action rows.
- Respect edge-to-edge system insets and display cutouts.

## 12. Reference Screens

The images under `docs/screens` are historical visual references for content, color intent, and module composition. Current Compose implementation and this guide take precedence when a reference image conflicts with implemented navigation, branding, accessibility, or feature behavior.
