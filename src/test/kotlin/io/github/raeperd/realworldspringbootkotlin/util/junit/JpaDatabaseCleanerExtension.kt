package io.github.raeperd.realworldspringbootkotlin.util.junit

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.persistence.EntityManager
import javax.persistence.Table
import javax.sql.DataSource
import kotlin.reflect.full.findAnnotation

class JpaDatabaseCleanerExtension : AfterEachCallback {

    private lateinit var tableNames: List<String>

    override fun afterEach(context: ExtensionContext?) {
        if (context == null) {
            throw IllegalStateException("No extension context found")
        }
        if (!this::tableNames.isInitialized) {
            SpringExtension.getApplicationContext(context).getBean(EntityManager::class.java)
                .also { entityManager -> entityManager.initTableNames() }
        }
        SpringExtension.getApplicationContext(context).getBean(DataSource::class.java)
            .also { dataSource ->
                dataSource.connection.use { connection ->
                    connection.prepareStatement("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate()
                    tableNames.forEach { name -> connection.prepareStatement("TRUNCATE TABLE $name").executeUpdate() }
                    connection.prepareStatement("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate()
                }
            }
    }

    private fun EntityManager.initTableNames() {
        tableNames = metamodel.managedTypes
            .mapNotNull { it.javaType.kotlin.findAnnotation<Table>() }
            .map { table -> table.name }
    }
}