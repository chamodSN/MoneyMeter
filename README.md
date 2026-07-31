# MoneyMeter

MoneyMeter is an Android expense tracker built with Kotlin and the AndroidX stack. It helps users record income and expenses, review monthly spending patterns, set a budget, secure the app with a 4-digit PIN, and back up or restore data locally.

## Highlights

- Track income and expense transactions with category, amount, description, and date.
- Switch between expense and income categories when adding or editing transactions.
- View spending analytics with pie charts and category breakdowns.
- Set a monthly budget and monitor how much remains.
- Choose a preferred currency for formatting values across the app.
- Protect access with a PIN setup and verification flow.
- Back up and restore app data from local JSON files.
- Use onboarding screens to introduce the app on first launch.

## Screenshots

### Launch and Onboarding

| Splash | Onboarding 1 | Onboarding 2 | Onboarding 3 |
| --- | --- | --- | --- |
| ![Splash Screen](UI/Splash%20Screen.jpg) | ![Onboard 1](UI/Onboard%201.jpg) | ![Onboard 2](UI/Onboard%202.jpg) | ![Onboard 3](UI/Onboard%203.jpg) |

### App Security

| Set PIN | Enter PIN |
| --- | --- |
| ![Set Pin](UI/Set%20Pin.jpg) | ![Enter Pin](UI/Enter%20Pin.jpg) |

### Main App

| All Transactions | Add / Edit Transaction | Analytics | Settings |
| --- | --- | --- | --- |
| ![All Transactions Window](UI/All%20Transactions%20Window.jpg) | ![Insert Transaction Window](UI/Insert%20Transaction%20Window.jpg) | ![Analysis Window](UI/Analysis%20Window.jpg) | ![Settings Window](UI/Settings%20Window.jpg) |

## Tech Stack

- Kotlin
- AndroidX AppCompat and Material Components
- Navigation Component
- ViewBinding
- Room persistence library
- ViewModel and LiveData
- ViewPager2 for onboarding
- Lottie animations
- MPAndroidChart for analytics charts
- Gson for backup and restore serialization

## App Flow

1. The app opens with a splash animation.
2. First-time users are shown a three-step onboarding flow.
3. If no PIN has been created yet, the app redirects to PIN setup.
4. Returning users enter their PIN before reaching the main app.
5. The main area uses bottom navigation with Transactions, Analytics, and Settings.

## Main Features

### Transactions

The Transactions screen is the primary working area. It shows a list of saved transactions and supports:

- adding a new transaction from a bottom sheet
- editing an existing transaction
- deleting a transaction with confirmation
- switching between expense and income categories

Expense categories include food, transport, bills, entertainment, shopping, health, education, and other. Income categories include salary, business, investment, rental, and other.

### Analytics

The Analytics screen summarizes the financial picture with:

- separate pie charts for expenses and income
- category total lists for both transaction types
- total monthly expense and income figures
- a monthly budget progress indicator

If a monthly budget is configured, the app shows how much remains or how far over budget the user is.

### Settings

The Settings screen lets users manage the app’s core preferences:

- set or update the monthly budget
- choose a preferred display currency
- create a backup of stored data
- restore from an existing backup file
- change the app PIN

## Data Storage

MoneyMeter stores its data locally on the device.

- Transaction records are stored in a Room database named `money_meter_database`.
- App preferences such as first-launch state, monthly budget, selected currency, and PIN state are stored in SharedPreferences.
- Backups are exported as JSON files in the app-specific external files directory under a `backups` folder.

## Project Structure

- `app/src/main/java/com/example/moneymeter/data` contains Room entities, DAO, repository, and preferences.
- `app/src/main/java/com/example/moneymeter/ui` contains the activities, fragments, and adapters that make up the user interface.
- `app/src/main/java/com/example/moneymeter/util` contains validation, currency formatting, and backup helpers.
- `app/src/main/res/layout` contains the XML layouts for screens and dialogs.
- `UI/` contains the screenshots used in this README.

## Requirements

- Android Studio Hedgehog or newer is recommended.
- JDK 11.
- Android SDK 35 for the current compile and target configuration.

## Getting Started

### Open the project

Open the root folder in Android Studio and let Gradle sync complete.

### Run on an emulator or device

Use the standard Android Studio Run action, or from a terminal in the project root run:

```bash
./gradlew installDebug
```

On Windows PowerShell:

```powershell
.\gradlew.bat installDebug
```

### Build the app

```bash
./gradlew assembleDebug
```

On Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

## Notes

- The app launches through a custom splash screen and Lottie animation rather than the default Android splash experience.
- The onboarding flow only appears on the first launch unless the app preferences are reset.
- Backup and restore are designed for local device storage and are not cloud synced.

## Screenshot Credits

All screenshots in this README are taken from the `UI/` folder in the repository.
