## Características

* **Lista de Personajes**: Explora una lista paginada de personajes de Rick y Morty.
* **Búsqueda de Personajes**: Busca personajes por nombre.
* **Detalle del Personaje**:
    * Ver información detallada del personaje (estado, especie, tipo, género, origen, última ubicación).
    * Marcar/desmarcar personajes como favoritos.
    * Lista de episodios en los que aparece el personaje.
* **Mapa de Ubicación**: Un botón "Ver en Mapa" que muestra la ubicación del personaje en un mapa de Google Maps (mediante geocodificación del nombre de la ubicación).
* **Mis Favoritos**: Un botón en la pantalla principal que te permite ver una lista de todos tus personajes favoritos guardados.
* **Autenticación Biométrica**: Acceso seguro a la sección de "Mis Favoritos" mediante huella dactilar o reconocimiento facial.

## Tecnologías Utilizadas

* **Kotlin**: Lenguaje de programación principal.
* **Android Jetpack Compose**: (Asumiendo que se usa para alguna UI o planeado, si no, es XML nativo)
* **Android Architecture Components**:
    * **LiveData**: Para observar cambios en los datos.
    * **ViewModel**: Para gestionar la lógica de negocio y el estado de la UI.
    * **Room Persistence Library**: Para la base de datos local de personajes favoritos.
    * **Lifecycle**: Para manejar el ciclo de vida de los componentes.
* **Kotlin Coroutines & Flow**: Para operaciones asíncronas y reactividad en el flujo de datos (especialmente para la base de datos).
* **Retrofit**: Para realizar solicitudes HTTP a la API de Rick and Morty.
* **Glide**: Para la carga y visualización eficiente de imágenes.
* **Google Maps API**: Para la visualización de mapas y geocodificación de ubicaciones.
* **Android Biometric API**: Para la autenticación biométrica (huella dactilar/facial).
* **View Binding**: Para acceder a las vistas del layout de forma segura.


## Cómo Ejecutar el Proyecto

1.  **Clona este repositorio** (o descarga el código fuente).
2.  Abre el proyecto en **Android Studio**.
3.  Asegúrate de haber configurado tu **clave de API de Google Maps** como se describe en la sección [Configuración del Proyecto](#configuración-del-proyecto).
4.  **Sincroniza el proyecto** con los archivos Gradle (File > Sync Project with Gradle Files).
5.  **Ejecuta la aplicación** en un emulador de Android (API 23 o superior, preferiblemente) o en un dispositivo físico.
    * Para la autenticación biométrica, asegúrate de que el emulador/dispositivo tenga una huella dactilar o un patrón/PIN configurado en la seguridad del dispositivo. Puedes configurar una huella dactilar virtual en un emulador yendo a `Extended Controls` (el botón de los tres puntos `...` en la barra lateral del emulador) -> `Fingerprint` -> `Touch the sensor` (varias veces para enrollar).

## Estructura del Proyecto

├── app
│   ├── build.gradle.kts           // Configuración del módulo de la aplicación
│   └── src
│       └── main
│           ├── AndroidManifest.xml // Permisos y declaración de componentes
│           ├── java
│           │   └── com
│           │       └── apprm
C
│           │               ├── apiService       // Clases de la API (Retrofit, interfaces de servicio)
│           │               │   └── ApiService.kt
│           │               │   └── ClientApi.kt
│           │               │   └── ApiEndPoint.kt
│           │               ├── data         // Clases de modelo (Character, Episode, Location, FavoriteCharacter, etc.)
│           │               ├── db           // Clases de la base de datos Room (AppDatabase, DAOs)
│           │               |    └── model
│           │               |    └── repository // Clases del repositorio
│           │               │         └── CharacterRepository.kt
│           │               |    └── dao
│           │               │         └── FavoriteCharacterDao.kt
│           │               │   ├── AppDatabase.kt
│           │               │   └── FavoriteCharacter.kt
│           │               └── ui           // Clases de la interfaz de usuario (Activities, ViewModels, Adapters)
│           │                    └── adapter
│           │                           ├── EpisodeAdapter.kt
│           │                           ├── CharacterAdapter.kt
│           │                           ├── FavoriteCharacterAdapter.kt
│           │                   ├── CharacterUI.kt
│           │                   ├── MainViewModelFactory.kt
│           │                   ├── CharacterDetailUI.kt
│           │                   ├── CharacterDetailViewModelFactory.kt
│           │                   ├── FavoriteCharactersUI.kt
│           │                   ├── FavoriteCharactersViewModelFactory.kt
│           │                   ├── CharacterMapUI.kt
│           │                   └── CharacterMapViewModelFactory.kt
│           │               └── viewModel  // Clases de la interfaz de usuario (Activities, ViewModels, Adapters)
│           │                      ├── CharacterMapViewModel.kt
│           │                      ├── FavoriteCharactersViewModel.kt
│           │                      ├── CharacterDetailViewModel.kt
│           │                      ├── LocationDetailViewModel.kt
│           │                      ├── CharacterViewModel.kt
│           └── res
│               ├── drawable       // Recursos gráficos
│               ├── layout         // Archivos de diseño XML (.xml para actividades, ítems de RecyclerView)
│               │   ├── view_location_detail.xml
│               │   ├── view_character_detail.xml
│               │   ├── view_character.xml
│               │   ├── view_favorite_characters.xml
│               │   ├── view_character_map.xml
│               │   ├── item_character.xml
│               │   ├── item_episode.xml
│               │   └── item_favorite_character.xml
│               ├── mipmap         // Iconos de la aplicación
│               └── values         // Recursos de valores (cadenas, colores, estilos, temas)
└── build.gradle.kts           // Configuración global de Gradle
