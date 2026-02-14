📱 Android Studio Module 3: Amiibo Vault - Offline-First Architecture

Este repositorio contiene una aplicación de catálogo de Amiibos desarrollada con Kotlin y Jetpack Compose. El objetivo principal es demostrar el dominio de arquitecturas offline-first, gestión de estado reactivo, búsqueda local optimizada y manejo de errores de red.

## Tech Stack & Conceptos Clave
* **Lenguaje:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material 3)
* **Arquitectura:** MVVM + Repository Pattern + Single Source of Truth
* **Persistencia Local:** Room Database (SQLite)
* **Networking:** Retrofit + kotlinx.serialization
* **Inyección de Dependencias:** Koin
* **Concurrencia:** Kotlin Coroutines + Flow
* **Gestión de Estado:** StateFlow, `collectAsStateWithLifecycle`
* **Side Effects:** LaunchedEffect, Snackbar con retry
* **Imágenes Asíncronas:** Coil 3

---

## 🎮 Proyecto: Amiibo Vault

Una aplicación completa para explorar la colección de Amiibos de Nintendo, diseñada para funcionar sin conexión a internet mediante un sistema de caché inteligente.

### [SCREENSHOT: Pantalla principal con grid de Amiibos]

---

## ⭐ Características Principales

### **Part 1: Graceful Offline Mode** 🌐
* **Experiencia Offline-First:** La app funciona completamente sin conexión una vez descargados los datos.
* **Manejo Inteligente de Errores:**
   * Con caché disponible: Snackbar no invasivo + Grid visible con datos guardados
   * Sin caché: Pantalla completa de error con iconos contextuales por tipo de fallo
* **Retry Funcional:** Botón "Reintentar" en Snackbar para refrescar datos sin perder la vista actual
* **Errores Tipados:** Distinción clara entre errores de red, parsing, base de datos y desconocidos

### [SCREENSHOT: Snackbar con error + Grid visible]

### **Part 2: Local Search** 🔍
* **Búsqueda en Tiempo Real:** TextField con debounce de 300ms para optimizar consultas
* **Room Query Directo:** Filtrado mediante SQL LIKE (no filtrado en memoria)
* **Clear Button Dinámico:** Icono X que aparece solo cuando hay texto escrito
* **Flow Switching Reactivo:** Alternancia automática entre lista completa y búsqueda usando `flatMapLatest`

### [SCREENSHOT: TextField de búsqueda en acción]

### **Características Adicionales** 🚀
* **Paginación Infinita:** Carga progresiva de datos con infinite scroll
* **Pull-to-Refresh:** Actualización manual de datos con Material 3 PullToRefreshBox
* **Tamaño de Página Configurable:** Selector dropdown (20, 50, 100 items por página)
* **Error de Paginación Inline:** Botón de reintentar al final de la lista sin perder items ya cargados

### [SCREENSHOT: Paginación con error inline]

---

## 🏗️ Implementación Técnica

### **Arquitectura Offline-First**
```
┌─────────────────────────────────────────────────────┐
│                  REPOSITORY                         │
│                                                     │
│   ┌─────────────┐         ┌─────────────┐         │
│   │   REMOTE    │ ──────> │   LOCAL     │ ────>   │
│   │  (Retrofit) │         │   (Room)    │   UI    │
│   └─────────────┘         └─────────────┘         │
│                                                     │
│   1. Fetch from API                                │
│   2. Save to Room DB                               │
│   3. UI observa Flow de Room (única fuente)        │
└─────────────────────────────────────────────────────┘
```

## 🧪 Cómo probar el proyecto

### **Requisitos Previos**
* Android Studio Hedgehog (2023.1.1) o superior
* Kotlin 1.9+
* Gradle 8.0+

### **Instalación**
1. Clonar el repositorio:
```bash
   git clone https://github.com/Anyelo1617/AmiiboVault.git
```

2. Abrir el proyecto en Android Studio

3. Sync Gradle (File → Sync Project with Gradle Files)

4. Ejecutar en emulador o dispositivo físico ▶️

---

## 📦 Estructura del Proyecto
```
app/src/main/java/com/curso/android/module3/amiibo/
├── data/
│   ├── local/
│   │   ├── entity/
│   │   │   ├── AmiiboEntity.kt           # Modelo Room
│   │   │   └── AmiiboDetailEntity.kt
│   │   ├── dao/
│   │   │   └── AmiiboDao.kt              # Queries SQL
│   │   └── db/
│   │       └── AmiiboDatabase.kt
│   └── remote/
│       ├── api/
│       │   └── AmiiboApiService.kt       # Endpoints Retrofit
│       └── model/
│           └── AmiiboDto.kt              # DTOs para JSON
├── domain/
│   └── error/
│       └── AmiiboError.kt                # Errores tipados (sealed class)
├── repository/
│   └── AmiiboRepository.kt               # Single Source of Truth
├── di/
│   └── AppModule.kt                      # Koin DI
└── ui/
    ├── viewmodel/
    │   └── AmiiboViewModel.kt            # State management
    ├── screens/
    │   └── AmiiboListScreen.kt           # UI Compose
    └── theme/
        └── Theme.kt
```

## 📚 API Utilizada
**Amiibo API:** [https://www.amiiboapi.com/](https://www.amiiboapi.com/)

---

**Desarrollado como parte del Módulo 3 de Fundamentos Avanzados de Aplicaciones Móviles**
