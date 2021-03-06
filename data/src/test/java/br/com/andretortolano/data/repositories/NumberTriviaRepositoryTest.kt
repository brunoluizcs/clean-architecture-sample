package br.com.andretortolano.data.repositories

import br.com.andretortolano.data.exceptions.NoConnectivityException
import br.com.andretortolano.data.exceptions.NotFoundException
import br.com.andretortolano.data.models.NumberTriviaModel
import br.com.andretortolano.data.sources.local.LocalSource
import br.com.andretortolano.data.sources.remote.RemoteSource
import br.com.andretortolano.domain.entity.EntityResult
import br.com.andretortolano.domain.entity.ErrorEntity
import br.com.andretortolano.domain.entity.NumberTriviaEntity
import br.com.andretortolano.domain.gateway.NumberTriviaGateway
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

internal class NumberTriviaRepositoryTest {

    @MockK
    lateinit var remoteSource: RemoteSource

    @MockK
    lateinit var localSource: LocalSource

    @InjectMockKs
    lateinit var repository: NumberTriviaRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `class SHOULD implement NumberTriviaGateway from domain`() {
        assertThat(repository).isInstanceOf(NumberTriviaGateway::class.java)
    }

    @Test
    fun `getNumberTrivia SHOULD always get from remote`() {
        // Given
        val number = 1L
        val numberTrivia = NumberTriviaModel(number, "trivia")
        every { remoteSource.getConcreteNumberTrivia(number) } returns numberTrivia
        every { localSource.saveNumberTrivia(numberTrivia) } just Runs
        // When
        val result = repository.getNumberTrivia(number)
        // Then
        (result as EntityResult.Success<NumberTriviaEntity>).also {
            assertThat(it.data.number).isEqualTo(numberTrivia.number)
            assertThat(it.data.trivia).isEqualTo(numberTrivia.trivia)
        }
    }

    @Test
    fun `getNumberTrivia SHOULD get from local WHEN remote throws NoConnectivityException`() {
        // Given
        val number = 1L
        val numberTrivia = NumberTriviaModel(number, "trivia")
        every { localSource.getNumberTrivia(number) } returns numberTrivia
        every { remoteSource.getConcreteNumberTrivia(number) } throws NoConnectivityException()
        // When
        val result = repository.getNumberTrivia(number)
        // Then
        (result as EntityResult.Success<NumberTriviaEntity>).also {
            assertThat(it.data.number).isEqualTo(numberTrivia.number)
            assertThat(it.data.trivia).isEqualTo(numberTrivia.trivia)
        }
    }

    @Test
    fun `getNumberTrivia SHOULD return ErrorEntity_NoConnectivity WHEN remote throws NoConnectivityException AND local model is null`() {
        // Given
        val number = 1L
        every { localSource.getNumberTrivia(number) } returns null
        every { remoteSource.getConcreteNumberTrivia(number) } throws NoConnectivityException()
        // When
        val result = repository.getNumberTrivia(number)
        // Then
        (result as EntityResult.Error<NumberTriviaEntity>).also {
            assertThat(it.error).isInstanceOf(ErrorEntity.NoConnectivity::class.java)
        }
    }

    @Test
    fun `getNumberTrivia SHOULD return ErrorEntity_NotFound WHEN remote throws NotFoundException`() {
        // Given
        val number = 1L
        every { remoteSource.getConcreteNumberTrivia(number) } throws NotFoundException()
        // When
        val result = repository.getNumberTrivia(number)
        // Then
        (result as EntityResult.Error<NumberTriviaEntity>).also {
            assertThat(it.error).isInstanceOf(ErrorEntity.NotFound::class.java)
        }
    }

    @Test
    fun `getNumberTrivia SHOULD save to local WHEN getting from remote`() {
        // Given
        val number = 1L
        val expectedNumberTrivia = NumberTriviaModel(number, "trivia")
        every { remoteSource.getConcreteNumberTrivia(number) } returns expectedNumberTrivia
        every { localSource.saveNumberTrivia(expectedNumberTrivia) } just Runs
        // When
        repository.getNumberTrivia(number)
        // Then
        verify(exactly = 1) {
            localSource.saveNumberTrivia(expectedNumberTrivia)
        }
    }

    @Test
    fun `getRandomNumberTrivia SHOULD get from remote`() {
        // Given
        val number = 1337L
        val randomNumberTrivia = NumberTriviaModel(number, "trivia")
        every { remoteSource.getRandomNumberTrivia() } returns randomNumberTrivia
        every { localSource.saveNumberTrivia(randomNumberTrivia) } just Runs
        // When
        val result = repository.getRandomNumberTrivia()
        // Then
        (result as EntityResult.Success<NumberTriviaEntity>).also {
            assertThat(it.data.number).isEqualTo(randomNumberTrivia.number)
            assertThat(it.data.trivia).isEqualTo(randomNumberTrivia.trivia)
        }
    }

    @Test
    fun `getRandomNumberTrivia SHOULD save to local WHEN getting from remote`() {
        // Given
        val number = 1337L
        val randomNumberTrivia = NumberTriviaModel(number, "trivia")
        every { remoteSource.getRandomNumberTrivia() } returns randomNumberTrivia
        every { localSource.saveNumberTrivia(randomNumberTrivia) } just Runs
        // When
        val result = repository.getRandomNumberTrivia()
        // Then
        verify(exactly = 1) {
            localSource.saveNumberTrivia(randomNumberTrivia)
        }
    }

    @Test
    fun `getRandomNumberTrivia SHOULD return ErrorEntity_NoConnectivity WHEN remote throws NoConnectivityException`() {
        // Given
        every { remoteSource.getRandomNumberTrivia() } throws NoConnectivityException()
        // When
        val result = repository.getRandomNumberTrivia()
        // Then
        (result as EntityResult.Error).also {
            assertThat(it.error).isInstanceOf(ErrorEntity.NoConnectivity::class.java)
        }
    }

    @Test
    fun `getRandomNumberTrivia SHOULD return ErrorEntity_NotFound WHEN remote throws NotFoundException`() {
        // Given
        every { remoteSource.getRandomNumberTrivia() } throws NotFoundException()
        // When
        val result = repository.getRandomNumberTrivia()
        // Then
        (result as EntityResult.Error).also {
            assertThat(it.error).isInstanceOf(ErrorEntity.NotFound::class.java)
        }
    }
}

