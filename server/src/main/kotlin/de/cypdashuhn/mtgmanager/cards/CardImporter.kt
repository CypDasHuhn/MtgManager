package de.cypdashuhn.mtgmanager.cards

import de.cypdashuhn.mtgmanager.Card
import de.cypdashuhn.mtgmanager.Legalities
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object CardImporter {
    fun <T : Any> getOrSetId(column: Column<T>, value: T, vararg extra: Pair<Column<Any>, Any>): Int {
        val table = column.table as IntIdTable
        return transaction {
            val existing = table.select { column eq value }.singleOrNull()

            return@transaction if (existing != null) {
                existing[table.id].value
            } else {

                table
                    .insert {
                        it[column] = value
                        extra.forEach { (column, value) ->
                            it[column] = value
                        }
                    }[table.id].value


            }
        }
    }

    fun newId(column: Column<Int>): Int {
        val table = column.table
        return transaction {
            (table.selectAll().orderBy(column, SortOrder.DESC).firstOrNull()?.get(column) ?: 0) + 1
        }
    }

    fun <T : Any?> getOrSetCombinationId(
        idColumn: Column<Int>,
        valueColumn: Column<T>,
        valueList: List<T>,
        allowNull: Boolean
    ): Int {
        val table = idColumn.table
        return transaction {
            if (valueList.isEmpty() && allowNull) {
                val entry = ColorTable.select(valueColumn as Column<T?> eq null).firstOrNull()
                if (entry != null) return@transaction entry[idColumn]
                else {
                    val newId = newId(idColumn)

                    table.insert {
                        it[idColumn] = newId(idColumn)
                        it[valueColumn] = null
                    }

                    return@transaction newId
                }
            } else {
                val matchingEntries = table
                    .slice(idColumn, valueColumn)
                    .select { valueColumn inList valueList }
                    .groupBy(idColumn)
                    .having { valueColumn.count() eq valueList.size.toLong() }
                    .map { it[idColumn] }

                if (matchingEntries.isNotEmpty()) {
                    return@transaction matchingEntries.first()
                } else {
                    val newId = newId(idColumn)

                    valueList.forEach { value ->
                        table.insert {
                            it[idColumn] = newId
                            it[valueColumn] = value
                        }
                    }

                    return@transaction newId
                }
            }
        }
    }

    fun formatLegalityId(legalities: Legalities) {
        fun legalityStatusId(legalityStatus: String): Int {
            return transaction {
                val entry =
                    FormatLegalityTable.select(LegalityStatusTable.legalityStatus eq legalityStatus).firstOrNull()
                if (entry != null) entry[LegalityStatusTable.id].value
                else {
                    LegalityStatusTable.insert {
                        it[LegalityStatusTable.legalityStatus] = legalityStatus
                    }[LegalityStatusTable.id].value
                }
            }
        }

        val legalityIds = legalities::class.members.map { property ->
            val format = property.name
            val legalityStatus = property.call() as String?
            requireNotNull(legalityStatus) { "Format $format has no legality status" }

            format to getOrSetId(LegalityStatusTable.legalityStatus, legalityStatus)
        }
    }

    fun importCards(cards: List<Card>) {
        cards.forEach { card ->
            BaseCardTable.insert {
                println("Card: ${card.id}, name: ${card.name}")

                requireNotNull(card.objectName) { "Card has no object name" }
                it[objectType] = getOrSetId(ObjectTypeTable.objectType, card.objectName!!)

                requireNotNull(card.oracleId) { "Card has no oracle id" }
                it[oracleId] = UUID.fromString(card.oracleId)

                requireNotNull(card.layout) { "Card has no layout" }
                it[layout] = getOrSetId(LayoutTable.layout, card.layout!!)

                requireNotNull(card.manaCost) { "Card has no mana cost" }
                it[manaCost] = getOrSetId(
                    ManaCostTable.manaCost,
                    card.manaCost!!,
                    ManaCostTable.cmc as Column<Any> to card.cmc!! as Any
                )

                requireNotNull(card.colors) { "Card has no colors" }
                it[colorId] = getOrSetCombinationId(ColorTable.combinationId, ColorTable.color, card.colors!!, true)

                requireNotNull(card.colorIdentity) { "Card has no color identity" }
                it[colorIdentityId] =
                    getOrSetCombinationId(ColorTable.combinationId, ColorTable.color, card.colorIdentity!!, true)

                if (card.producedMana != null) {
                    it[producedManaColorsId] =
                        getOrSetCombinationId(ColorTable.combinationId, ColorTable.color, card.producedMana!!, true)
                }


            }
        }
    }
}