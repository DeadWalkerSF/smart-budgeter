# Smart Budgeter

Black-and-gold Android budgeting app scaffold built with:

- Kotlin
- Jetpack Compose
- Room
- MVVM
- WorkManager

## Included

- `Home` dashboard with budget overview, burn-rate warning, quick-add suggestions, charts, recurring expenses, vault goals, and export actions
- `History` tab with date and category filters backed by Room paging
- `Add Transaction` flow in a `ModalBottomSheet`
- Room tables for transactions, categories, recurring expenses, and vault goals
- Real-time budget calculations for:
  - total spent vs monthly budget
  - daily allowance
  - burn-rate check before the 15th
- WorkManager automation for recurring monthly expenses
- Overspend notifications when a single transaction crosses 20% of the total budget
- Biometric unlock gate
- CSV and PDF monthly export helpers

## Notes

- The workspace did not include Gradle or an Android wrapper, so the project files are scaffolded but `gradlew` was not generated in this environment.
- Open the project in Android Studio to sync dependencies, generate the wrapper if you want it committed, and run on a device or emulator.

## Package

- `com.deadwalkersf.smartbudgeter`
