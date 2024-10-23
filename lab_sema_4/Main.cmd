@echo off
kotlinc -d src -cp "libs/Jama.jar;libs/libGrafo.jar" src/Main.kt && rmdir /s /q src\META-INF && kotlin -cp "src;libs/Jama.jar;libs/libGrafo.jar" MainKt