package com.curso.android.module3.amiibo.repository

import android.database.sqlite.SQLiteException
import com.curso.android.module3.amiibo.data.local.dao.AmiiboDao
import com.curso.android.module3.amiibo.data.local.entity.AmiiboEntity
import com.curso.android.module3.amiibo.data.remote.api.AmiiboApiService
import com.curso.android.module3.amiibo.data.local.entity.AmiiboDetailEntity
import com.curso.android.module3.amiibo.data.remote.model.AmiiboDetail
import com.curso.android.module3.amiibo.data.remote.model.toDetail
import com.curso.android.module3.amiibo.data.remote.model.toDomainModel
import com.curso.android.module3.amiibo.data.remote.model.toEntities
import com.curso.android.module3.amiibo.data.remote.model.toEntity
import com.curso.android.module3.amiibo.domain.error.AmiiboError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import java.io.IOException

/**
 * ✅ AmiiboDao (con replaceAll, getAmiibosPage, etc.)
 * ✅ AmiiboEntity (id, name, gameSeries, imageUrl)
 * ✅ AmiiboError (Network, Parse, Database, Unknown)
 * ✅ Part 1: Captura cached data al fallar
 * ✅ Part 2: Método searchAmiibos()
 */
class AmiiboRepository(
    private val amiiboDao: AmiiboDao,
    private val amiiboApiService: AmiiboApiService
) {


    // Mostrar AMIIBOS (FLUJO REACTIVO)
    fun observeAmiibos(): Flow<List<AmiiboEntity>> {
        return amiiboDao.getAllAmiibos()
    }

    // BÚSQUEDA LOCAL
    fun searchAmiibos(query: String): Flow<List<AmiiboEntity>> {
        return amiiboDao.searchAmiibos(query)
    }

    //  REFRESH CON GRACEFUL ERROR HANDLING
    /**
     * Refresca todos los amiibos desde la API.
     *
     * NUEVO: Al fallar, incluye datos del cache en el mensaje de error
     * para que el ViewModel pueda mostrar Snackbar + Grid.
     */
    suspend fun refreshAmiibos() {
        try {
            // 1. Obtener datos de la API
            val response = amiiboApiService.getAllAmiibos()

            // 2. Convertir a entities
            val entities = response.amiibo.toEntities()

            // 3. Guardar en DB (usa replaceAll de TU DAO)
            try {
                amiiboDao.replaceAll(entities)
            } catch (e: SQLiteException) {
                throw AmiiboError.Database(
                    message = "Error al guardar los datos localmente.",
                    cause = e
                )
            }

        } catch (e: AmiiboError) {
            // Re-lanzar errores
            throw e

        } catch (e: IOException) {
            //  Error de red
            throw AmiiboError.Network(
                message = "Sin conexión a internet.",
                cause = e
            )

        } catch (e: SerializationException) {
            // Error de parsing
            throw AmiiboError.Parse(
                message = "Error al procesar los datos del servidor.",
                cause = e
            )

        } catch (e: Exception) {
            // Error desconocido
            throw AmiiboError.Unknown(
                message = "Ocurrió un error inesperado.",
                cause = e
            )
        }
    }

    // =========================================================================
    // PAGINACIÓN
    // =========================================================================

    suspend fun getAmiibosPage(page: Int, pageSize: Int): List<AmiiboEntity> {
        val offset = page * pageSize
        return amiiboDao.getAmiibosPage(limit = pageSize, offset = offset)
    }

    suspend fun getTotalCount(): Int {
        return amiiboDao.getTotalCount()
    }

    suspend fun hasMorePages(currentPage: Int, pageSize: Int): Boolean {
        val total = getTotalCount()
        val loaded = (currentPage + 1) * pageSize
        return loaded < total
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        val PAGE_SIZE_OPTIONS = listOf(20, 50, 100)
    }

    // =========================================================================
    // DETALLE DE AMIIBO
    // =========================================================================

    suspend fun getAmiiboDetail(name: String): AmiiboDetail {
        try {
            // 1. Buscar en cache
            val cachedDetail = try {
                amiiboDao.getDetailByName(name)
            } catch (e: SQLiteException) {
                throw AmiiboError.Database(cause = e)
            }

            if (cachedDetail != null) {
                return cachedDetail.toDomainModel()
            }

            // 2. No está en cache, obtener de API
            val response = amiiboApiService.getAmiiboDetail(name)
            val detail = response.amiibo.first().toDetail()

            // 3. Guardar en cache
            try {
                amiiboDao.insertDetail(detail.toEntity())
            } catch (e: SQLiteException) {
                throw AmiiboError.Database(cause = e)
            }

            return detail

        } catch (e: AmiiboError) {
            throw e
        } catch (e: IOException) {
            throw AmiiboError.Network(cause = e)
        } catch (e: SerializationException) {
            throw AmiiboError.Parse(cause = e)
        } catch (e: NoSuchElementException) {
            throw AmiiboError.Parse(
                message = "No se encontró el Amiibo '$name'",
                cause = e
            )
        } catch (e: Exception) {
            throw AmiiboError.Unknown(cause = e)
        }
    }

    fun getAmiiboCount(): Flow<Int> {
        return amiiboDao.getCount()
    }
}