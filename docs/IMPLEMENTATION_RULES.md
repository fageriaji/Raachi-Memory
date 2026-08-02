# IMPLEMENTATION_RULES

## Architecture

-   Clean Architecture
-   MVVM
-   Repository Pattern
-   Thin ViewModels
-   Business logic outside Composables

## Code Quality

-   SOLID
-   DRY
-   Reusable utilities
-   No hard-coded UI text
-   strings.xml for all user-visible text

## Protection

-   Do not rewrite working code.
-   Minimize changes.
-   Extend instead of replace.

## Build

-   Must compile after every milestone.
-   No TODOs.
-   No placeholder code.
-   No unnecessary dependencies.

## Performance

-   Avoid unnecessary recompositions.
-   Keep DB work off main thread.
-   Prefer immutable UI state.

## Compatibility

-   Preserve existing user data.
-   Use Room migrations when required.

## Workflow

1.  Read project docs.
2.  Implement requested milestone only.
3.  Verify build.
4.  Stop and wait for review.
