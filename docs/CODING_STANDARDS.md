# CODING_STANDARDS.md

# Raachi Memory v1.0 Coding Standards

## General

-   Follow Clean Architecture.
-   Follow MVVM.
-   Keep code simple and readable.
-   Avoid unnecessary abstractions.

## Kotlin

-   Use Kotlin only.
-   Prefer immutable data classes.
-   Use meaningful names.
-   Document public APIs with KDoc.

## Jetpack Compose

-   No business logic inside Composables.
-   One screen = one ViewModel.
-   UI observes StateFlow only.
-   Create reusable composables.

## Architecture

UI → ViewModel → UseCase → Repository → Room

UI never accesses Room directly.

## Coroutines

-   Use viewModelScope.
-   No GlobalScope.
-   Use Dispatchers.IO for database work.

## Dependency Injection

-   Use Hilt.
-   Constructor injection preferred.

## Database

-   Room is the single source of truth.
-   Migrations must preserve user data.

## Git

-   One feature per commit.
-   Meaningful commit messages.

## Testing

-   Test every completed roadmap phase.
-   Fix warnings before starting the next phase.

## Rule

Follow: - ARCHITECTURE.md - DATABASE.md - UI_GUIDELINES.md - ROADMAP.md

These documents take precedence over generated code.
