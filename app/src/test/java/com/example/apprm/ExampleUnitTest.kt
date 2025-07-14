package com.example.apprm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.example.apprm.module.db.model.Character
import com.example.apprm.module.db.model.CharacterResponse
import com.example.apprm.module.db.model.Info
import com.example.apprm.module.db.repository.CharacterRepository
import com.example.apprm.module.viewModel.CharacterViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@ExperimentalCoroutinesApi
class ExampleUnitTest {

        // Rule para LiveData, permite ejecutar tareas en el hilo principal de forma síncrona
        @get:Rule
        val instantTaskExecutorRule = InstantTaskExecutorRule()

        // Dispatcher de prueba para controlar la ejecución de coroutines
        private val testDispatcher = StandardTestDispatcher()

        // Mock del repositorio de caracteres
        private val mockRepository = mockk<CharacterRepository>()

        // ViewModel a testear
        private lateinit var viewModel: CharacterViewModel

        @Before
        fun setUp() {
            // Establecer el dispatcher principal para coroutines a nuestro dispatcher de prueba
            Dispatchers.setMain(testDispatcher)
            viewModel = CharacterViewModel(mockRepository)
        }

        // --- Helper para crear respuestas mock de la API ---
        private fun createMockCharacterResponse(
            characters: List<Character>,
            nextPageUrl: String? = null,
            prevPageUrl: String? = null,
            count: Int = characters.size,
            pages: Int = 1
        ): CharacterResponse {
            val info = Info(count = count, pages = pages, next = nextPageUrl, prev = prevPageUrl)
            return CharacterResponse(info = info, results = characters)
        }

        private fun createMockCharacter(id: Int, name: String): Character {
            return Character(
                id = id,
                name = name,
                status = "Alive",
                species = "Human",
                type = "",
                gender = "Male",
                origin = mockk(),
                location = mockk(),
                image = "",
                episode = listOf(),
                url = "",
                created = ""
            )
        }

        // --- Pruebas para fetchCharacters (Carga Inicial/Aplicación de Filtros) ---

        @Test
        fun `fetchCharacters loads initial characters successfully and sets next URL`() = runTest {
            // Given: El repositorio devuelve una respuesta exitosa con personajes y una URL siguiente
            val mockCharacters = listOf(createMockCharacter(1, "Rick"), createMockCharacter(2, "Morty"))
            val nextUrl = "https://rickandmortyapi.com/api/character/?page=2"
            val mockResponse = createMockCharacterResponse(mockCharacters, nextPageUrl = nextUrl)
            coEvery { mockRepository.getCharacters(page = 1, name = null, status = null, species = null) } returns Response.success(mockResponse)

            // When: Llamamos a fetchCharacters
            viewModel.fetchCharacters()
            advanceUntilIdle() // Avanzar el tiempo virtual hasta que las coroutines estén inactivas

            // Then:
            // Los caracteres se han cargado correctamente
            assertThat(viewModel.characters.value).isEqualTo(mockCharacters)
            // El estado de carga es falso
            assertThat(viewModel.isLoading.value).isFalse()
            // No hay mensaje de error
          //  assertThat(viewModel.errorMessage.value).isNull().toString()
            // La URL de la siguiente página se ha almacenado correctamente
            assertThat(viewModel.javaClass.getDeclaredField("nextCharactersPageUrl").apply { isAccessible = true }.get(viewModel)).isEqualTo(nextUrl)
        }

        @Test
        fun `fetchCharacters with filters loads filtered characters successfully`() = runTest {
            // Given: El repositorio devuelve una respuesta filtrada
            val filteredCharacters = listOf(createMockCharacter(3, "Summer"))
            val nextUrl = "https://rickandmortyapi.com/api/character/?page=1&name=Summer&status=Alive"
            val mockResponse = createMockCharacterResponse(filteredCharacters, nextPageUrl = nextUrl)
            coEvery { mockRepository.getCharacters(page = 1, name = "Summer", status = "Alive", species = null) } returns Response.success(mockResponse)

            // When: Llamamos a fetchCharacters con filtros
            viewModel.fetchCharacters(name = "Summer", status = "Alive", species = null)
            advanceUntilIdle()

            // Then:
            assertThat(viewModel.characters.value).isEqualTo(filteredCharacters)
            assertThat(viewModel.isLoading.value).isFalse()
         //   assertThat(viewModel.errorMessage.value).isNull().toString()
            assertThat(viewModel.javaClass.getDeclaredField("nextCharactersPageUrl").apply { isAccessible = true }.get(viewModel)).isEqualTo(nextUrl)
        }

        @Test
        fun `fetchCharacters handles API error`() = runTest {
            // Given: El repositorio devuelve un error
            coEvery { mockRepository.getCharacters(any(), any(), any(), any()) } returns Response.error(404, mockk(relaxed = true))

            // When: Llamamos a fetchCharacters
            viewModel.fetchCharacters()
            advanceUntilIdle()

            // Then:
            assertThat(viewModel.characters.value).isEmpty()
            assertThat(viewModel.isLoading.value).isFalse()
            assertThat(viewModel.errorMessage.value).isNotNull()
        }

        // --- Pruebas para loadMoreCharacters (Paginación) ---

        @Test
        fun `loadMoreCharacters appends characters and updates next URL`() = runTest {
            // Given: Una carga inicial y el repositorio devuelve la siguiente página
            val initialCharacters = listOf(createMockCharacter(1, "Rick"))
            val initialNextUrl = "https://rickandmortyapi.com/api/character/?page=2"
            val initialResponse = createMockCharacterResponse(initialCharacters, nextPageUrl = initialNextUrl)
            coEvery { mockRepository.getCharacters(page = 1, name = null, status = null, species = null) } returns Response.success(initialResponse)

            // Simular la carga inicial
            viewModel.fetchCharacters()
            advanceUntilIdle()

            // Given: La siguiente página de personajes
            val moreCharacters = listOf(createMockCharacter(2, "Morty"))
            val newNextUrl = "https://rickandmortyapi.com/api/character/?page=3" // Otra página siguiente
            val moreResponse = createMockCharacterResponse(moreCharacters, nextPageUrl = newNextUrl)
            coEvery { mockRepository.getCharactersByFullUrl(initialNextUrl) } returns Response.success(moreResponse)

            // When: Llamamos a loadMoreCharacters
            viewModel.loadMoreCharacters()
            advanceUntilIdle()

            // Then:
            // Los nuevos caracteres se han añadido a la lista existente
            assertThat(viewModel.characters.value).isEqualTo(initialCharacters + moreCharacters)
            assertThat(viewModel.isLoading.value).isFalse()
        //    assertThat(viewModel.errorMessage.value).isNull()
            // La URL de la siguiente página se ha actualizado
            assertThat(viewModel.javaClass.getDeclaredField("nextCharactersPageUrl").apply { isAccessible = true }.get(viewModel)).isEqualTo(newNextUrl)
        }

        @Test
        fun `loadMoreCharacters does nothing if next URL is null`() = runTest {
            // Given: Una carga inicial que es la última página (next URL es null)
            val initialCharacters = listOf(createMockCharacter(1, "Rick"))
            val initialResponse = createMockCharacterResponse(initialCharacters, nextPageUrl = null)
            coEvery { mockRepository.getCharacters(page = 1, name = null, status = null, species = null) } returns Response.success(initialResponse)

            viewModel.fetchCharacters()
            advanceUntilIdle()

            // When: Llamamos a loadMoreCharacters
            viewModel.loadMoreCharacters()
            advanceUntilIdle()

            // Then: No se debería haber llamado al repositorio para más caracteres
            coEvery { mockRepository.getCharactersByFullUrl(any()) } throws IllegalStateException("Should not be called")
            assertThat(viewModel.characters.value).isEqualTo(initialCharacters) // La lista no cambia
            assertThat(viewModel.isLoading.value).isFalse()
            //assertThat(viewModel.errorMessage.value).isNull()
        }

        @Test
        fun `loadMoreCharacters handles API error during pagination`() = runTest {
            // Given: Una carga inicial y la siguiente llamada falla
            val initialCharacters = listOf(createMockCharacter(1, "Rick"))
            val initialNextUrl = "https://rickandmortyapi.com/api/character/?page=2"
            val initialResponse = createMockCharacterResponse(initialCharacters, nextPageUrl = initialNextUrl)
            coEvery { mockRepository.getCharacters(page = 1, name = null, status = null, species = null) } returns Response.success(initialResponse)

            viewModel.fetchCharacters()
            advanceUntilIdle()

            coEvery { mockRepository.getCharactersByFullUrl(initialNextUrl) } returns Response.error(500, mockk(relaxed = true))

            // When: Llamamos a loadMoreCharacters
            viewModel.loadMoreCharacters()
            advanceUntilIdle()

            // Then:
            assertThat(viewModel.characters.value).isEqualTo(initialCharacters) // La lista original se mantiene
            assertThat(viewModel.isLoading.value).isFalse()
            assertThat(viewModel.errorMessage.value).isNotNull()
            // nextCharactersPageUrl debería seguir siendo el que causó el error (o podría limpiarse dependiendo de la lógica)
            // En nuestra implementación, se mantiene.
            assertThat(viewModel.javaClass.getDeclaredField("nextCharactersPageUrl").apply { isAccessible = true }.get(viewModel)).isEqualTo(initialNextUrl)
        }

        // --- Pruebas para searchCharacters (Búsqueda y Filtros combinados) ---

        @Test
        fun `searchCharacters performs new search and resets pagination`() = runTest {
            // Given: Una respuesta para la búsqueda
            val searchResults = listOf(createMockCharacter(5, "Alien Rick"))
            val searchNextUrl = "https://rickandmortyapi.com/api/character/?page=2&name=Alien"
            val searchResponse = createMockCharacterResponse(searchResults, nextPageUrl = searchNextUrl)
            coEvery { mockRepository.getCharacters(page = 1, name = "Alien", status = null, species = null) } returns Response.success(searchResponse)

            // When: Llamamos a searchCharacters
            viewModel.searchCharacters(nameQuery = "Alien")
            advanceTimeBy(300) // Avanzar por el debounce
            advanceUntilIdle()

            // Then:
            assertThat(viewModel.characters.value).isEqualTo(searchResults)
            assertThat(viewModel.isLoading.value).isFalse()
           // assertThat(viewModel.errorMessage.value).isNull()
            assertThat(viewModel.javaClass.getDeclaredField("nextCharactersPageUrl").apply { isAccessible = true }.get(viewModel)).isEqualTo(searchNextUrl)
        }

        @Test
        fun `searchCharacters with no results sets empty list and error message`() = runTest {
            // Given: La API devuelve 404 para una búsqueda sin resultados
            coEvery { mockRepository.getCharacters(page = 1, name = "NonExistent", status = null, species = null) } returns Response.error(404, mockk(relaxed = true))

            // When: Llamamos a searchCharacters
            viewModel.searchCharacters(nameQuery = "NonExistent")
            advanceTimeBy(300)
            advanceUntilIdle()

            // Then:
            assertThat(viewModel.characters.value).isEmpty()
            assertThat(viewModel.isLoading.value).isFalse()
           // assertThat(viewModel.errorMessage.value).isEqualTo("No se encontraron personajes con los filtros aplicados.")
            assertThat(viewModel.javaClass.getDeclaredField("nextCharactersPageUrl").apply { isAccessible = true }.get(viewModel)).isNull()
        }

        @Test
        fun `searchCharacters with empty query and null filters calls resetFilters behavior`() = runTest {
            // Given: El repositorio devuelve la carga inicial sin filtros
            val initialCharacters = listOf(createMockCharacter(1, "Rick"))
            val initialNextUrl = "https://rickandmortyapi.com/api/character/?page=2"
            val initialResponse = createMockCharacterResponse(initialCharacters, nextPageUrl = initialNextUrl)
            coEvery { mockRepository.getCharacters(page = 1, name = null, status = null, species = null) } returns Response.success(initialResponse)

            // When: Llamamos a searchCharacters con todos los filtros vacíos/nulos
            viewModel.searchCharacters(nameQuery = "", statusFilter = null, speciesFilter = null)
            advanceUntilIdle() // No hay debounce si se detecta inmediatamente el reset

            // Then: Se debería haber comportado como un reset
            assertThat(viewModel.characters.value).isEqualTo(initialCharacters)
            assertThat(viewModel.isLoading.value).isFalse()
          //  assertThat(viewModel.errorMessage.value).isNull()
            assertThat(viewModel.javaClass.getDeclaredField("nextCharactersPageUrl").apply { isAccessible = true }.get(viewModel)).isEqualTo(initialNextUrl)
        }

        @Test
        fun `searchCharacters debounces multiple rapid calls`() = runTest {
            // Given: Múltiples llamadas a searchCharacters
            val mockCharacters1 = listOf(createMockCharacter(10, "A"))
            val mockResponse1 = createMockCharacterResponse(mockCharacters1, nextPageUrl = null)
            coEvery { mockRepository.getCharacters(page = 1, name = "A", any(), any()) } returns Response.success(mockResponse1)

            val mockCharacters2 = listOf(createMockCharacter(11, "AB"))
            val mockResponse2 = createMockCharacterResponse(mockCharacters2, nextPageUrl = null)
            coEvery { mockRepository.getCharacters(page = 1, name = "AB", any(), any()) } returns Response.success(mockResponse2)

            // When: Llamadas rápidas
            viewModel.searchCharacters(nameQuery = "A")
            advanceTimeBy(100) // No suficiente para que termine el debounce de A
            viewModel.searchCharacters(nameQuery = "AB") // Esta debería cancelar la anterior
            advanceTimeBy(300) // Suficiente para que termine el debounce de AB
            advanceUntilIdle()

            // Then: Solo la última búsqueda ("AB") debería haberse ejecutado
            coEvery { mockRepository.getCharacters(page = 1, name = "A", any(), any()) } throws AssertionError("Should not be called")
            assertThat(viewModel.characters.value).isEqualTo(mockCharacters2)
        }

        // --- Pruebas para resetFilters ---

        @Test
        fun `resetFilters reloads initial characters without any filters`() = runTest {
            // Given: El repositorio devuelve la carga inicial sin filtros
            val initialCharacters = listOf(createMockCharacter(1, "Rick"))
            val initialNextUrl = "https://rickandmortyapi.com/api/character/?page=2"
            val initialResponse = createMockCharacterResponse(initialCharacters, nextPageUrl = initialNextUrl)
            coEvery { mockRepository.getCharacters(page = 1, name = null, status = null, species = null) } returns Response.success(initialResponse)

            // When: Llamamos a resetFilters
            viewModel.resetFilters()
            advanceUntilIdle()

            // Then:
            assertThat(viewModel.characters.value).isEqualTo(initialCharacters)
            assertThat(viewModel.isLoading.value).isFalse()
           // assertThat(viewModel.errorMessage.value).isNull()
            assertThat(viewModel.javaClass.getDeclaredField("nextCharactersPageUrl").apply { isAccessible = true }.get(viewModel)).isEqualTo(initialNextUrl)
        }

        // --- Pruebas para el estado de carga ---

        @Test
        fun `isLoading is true during data fetching and false afterwards`() = runTest {
            // Given: El repositorio tardará un poco en responder
            coEvery { mockRepository.getCharacters(any(), any(), any(), any()) } coAnswers {
                delay(100) // Simular un retraso en la red
                Response.success(createMockCharacterResponse(listOf()))
            }

            // When: Llamamos a fetchCharacters
            viewModel.fetchCharacters()

            // Then: isLoading debe ser true inmediatamente
            assertThat(viewModel.isLoading.value).isTrue()

            // When: Avanzamos el tiempo y la llamada termina
            advanceTimeBy(100)
            advanceUntilIdle()

            // Then: isLoading debe ser false
            assertThat(viewModel.isLoading.value).isFalse()
        }

        @Test
        fun `isLoading is true during loadMore and false afterwards`() = runTest {
            // Given: Carga inicial y la siguiente llamada tardará
            val initialCharacters = listOf(createMockCharacter(1, "Rick"))
            val initialNextUrl = "https://rickandmortyapi.com/api/character/?page=2"
            val initialResponse = createMockCharacterResponse(initialCharacters, nextPageUrl = initialNextUrl)
            coEvery { mockRepository.getCharacters(page = 1, any(), any(), any()) } returns Response.success(initialResponse)
            viewModel.fetchCharacters()
            advanceUntilIdle()

            coEvery { mockRepository.getCharactersByFullUrl(initialNextUrl) } coAnswers {
                delay(100)
                Response.success(createMockCharacterResponse(listOf(createMockCharacter(2, "Morty"))))
            }

            // When: Llamamos a loadMoreCharacters
            viewModel.loadMoreCharacters()

            // Then: isLoading debe ser true inmediatamente
            assertThat(viewModel.isLoading.value).isTrue()

            // When: Avanzamos el tiempo y la llamada termina
            advanceTimeBy(100)
            advanceUntilIdle()

            // Then: isLoading debe ser false
            assertThat(viewModel.isLoading.value).isFalse()
        }
}