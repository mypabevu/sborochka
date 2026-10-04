LIVE CAMERA (Живая камера) — Fabric, Minecraft 1.21.1, только клиент.

Сборка:
  1. Нужна Java 21.
  2. Скопируй папку gradle/ + gradlew + gradlew.bat из любого шаблона Fabric (fabric-example-mod),
     либо выполни: gradle wrapper --gradle-version 8.10
  3. ./gradlew build   ->  build/libs/livecamera-1.0.0.jar  кидай в mods/
  Для теста из IDE: ./gradlew runClient

Управление: F5 — третье лицо, K — вкл/выкл мод (меняется в настройках управления).
Все параметры камеры — в LiveCameraState.java (блок SETTINGS).
