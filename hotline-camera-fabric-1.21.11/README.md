# Hotline Camera — Fabric 1.21.11

A client-side Minecraft mod that adds a fourth F5 camera mode: overhead / top-down.

## F5 order
1. First person
2. Third person (back)
3. Third person (front)
4. Overhead (camera 14 blocks above player)
5. First person

The mod changes only the client camera. It does not change player movement or aim controls; Minecraft's default camera-relative controls may feel unusual when looking straight down. Camera clipping and indoor behavior have not been polished.

## Build on GitHub
1. Create a new GitHub repository (main branch).
2. Extract this ZIP and upload **its contents**, including `.github/workflows/build.yml`.
3. Commit the files. Open **Actions → Build Hotline Camera**.
4. Download the artifact from a successful run; extract the `.jar` and add it to Modrinth.

Requires Fabric Loader and Java 21. No local Gradle install is needed for GitHub Actions.

**Status:** Source project only. Not compiled or play-tested. If compilation fails, share the failing Actions step log for a patch. This template targets official Mojang mappings and Fabric Loom 1.14.10; upstream changes may require adjustments.
