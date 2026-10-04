LIVE CAMERA / Живая камера — Minecraft 1.21.1, клиентский мод.
  fabric/  — Fabric (Loader 0.16+, Fabric API 0.105+)
  forge/   — Forge 52.0.x (Mixin + official mappings)

Сборка:
 1. Java 21.
 2. В обе папки (fabric/ и forge/) положи gradle wrapper (gradle/, gradlew, gradlew.bat)
    из шаблона fabric-example-mod / Forge MDK 1.21.1,
    либо в каждой папке: gradle wrapper --gradle-version 8.8
 3. ./build-all.sh  -> jars/livecamera-fabric-1.0.0.jar и jars/livecamera-forge-1.0.0.jar

K — вкл/выкл мод. Настройки камеры — LiveCameraState.java (блок SETTINGS) в каждой папке.
