package com.manojka

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

// --- Table definition (like models.py in FastAPI) ---
object Notes : Table("notes") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val content = text("content").nullable()
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

// --- Data classes (like schemas.py in FastAPI) ---
@Serializable
data class Note(
    val id: Int = 0,
    val title: String,
    val content: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class NoteCreate(
    val title: String,
    val content: String? = null
)

@Serializable
data class BulkDeleteRequest(
    val ids: List<Int>
)

@Serializable
data class BulkDeleteResponse(
    val deleted: Int,
    val ids: List<Int>
)

// --- Service (handles all DB operations) ---
class NoteService(private val database: Database) {

    init {
        transaction(database) {
            SchemaUtils.create(Notes)
            // Seed data if empty
            if (Notes.selectAll().count() == 0L) {
                Notes.insert {
                    it[title] = "Welcome"
                    it[content] = "This is my personal notebook. I will write my thoughts and stories here."
                    it[createdAt] = System.currentTimeMillis()
                }
                Notes.insert {
                    it[title] = "First Story"
                    it[content] = "Today was a great day. I built and deployed my first Ktor backend!"
                    it[createdAt] = System.currentTimeMillis()
                }
            }
        }
    }

    fun getAll(): List<Note> = transaction(database) {
        Notes.selectAll()
            .orderBy(Notes.createdAt, SortOrder.DESC)
            .map { it.toNote() }
    }

    fun getById(id: Int): Note? = transaction(database) {
        Notes.selectAll()
            .where { Notes.id eq id }
            .map { it.toNote() }
            .singleOrNull()
    }

    fun create(note: NoteCreate): Note = transaction(database) {
        val insertedId = Notes.insert {
            it[title] = note.title
            it[content] = note.content
            it[createdAt] = System.currentTimeMillis()
        }[Notes.id]
        getById(insertedId)!!
    }

    fun update(id: Int, note: NoteCreate): Note? = transaction(database) {
        Notes.update({ Notes.id eq id }) {
            it[title] = note.title
            it[content] = note.content
        }
        getById(id)
    }

    fun delete(id: Int): Boolean = transaction(database) {
        Notes.deleteWhere { Notes.id eq id } > 0
    }

    fun deleteBulk(ids: List<Int>): Int = transaction(database) {
        Notes.deleteWhere { Notes.id inList ids }
    }

    private fun ResultRow.toNote() = Note(
        id = this[Notes.id],
        title = this[Notes.title],
        content = this[Notes.content],
        createdAt = this[Notes.createdAt]
    )
}
