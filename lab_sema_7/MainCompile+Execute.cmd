@echo off
@echo kotlinc -d src -cp "libs/libGrafo.jar" src/Main.kt + rmdir /s /q src\META-INF + kotlin -cp "src;libs/libGrafo.jar" MainKt
kotlinc -d src -cp "libs/libGrafo.jar" src/Main.kt && rmdir /s /q src\META-INF && kotlin -cp "src;libs/libGrafo.jar" MainKt