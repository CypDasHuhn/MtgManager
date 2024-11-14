package de.cypdashuhn.mtgmanager.cards

import de.cypdashuhn.mtgmanager.Card
import de.cypdashuhn.mtgmanager.Legalities
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object CardImporter {
    fun <T : Any> getOrSetEntityId(column: Column<T>, value: T, vararg extra: Pair<Column<Any>, Any>): EntityID<Int> {
        val table = column.table as IntIdTable
        return transaction {
            val existing = table.select { column eq value }.singleOrNull()

            return@transaction if (existing != null) {
                existing[table.id]
            } else {

                table
                    .insert {
                        it[column] = value
                        extra.forEach { (column, value) ->
                            it[column] = value
                        }
                    }[table.id]


            }
        }
    }

    fun <T : Any> getOrSetId(column: Column<T>, value: T, vararg extra: Pair<Column<Any>, Any>): Int {
        return getOrSetEntityId(column, value, *extra).value
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

    fun getOrSetIndirectCombinationId(): EntityID<Int> {

    }

    fun formatLegalityId(legalities: Legalities): Int {
        return transaction {
            val legalityIds = legalities::class.members.map { property ->
                val format = property.name
                val legalityStatus = property.call() as String?
                requireNotNull(legalityStatus) { "Format $format has no legality status" }

                getOrSetEntityId(FormatTable.format, format) to getOrSetEntityId(
                    LegalityStatusTable.legalityStatus,
                    legalityStatus
                )
            }

            val conditions = legalityIds.map { (formatId, legalityStatusId) ->
                (FormatLegalityTable.format eq formatId) and (FormatLegalityTable.legalityStatus eq legalityStatusId)
            }.reduce { acc, condition -> acc or condition }

            val entrie = FormatLegalityTable
                .slice(FormatLegalityTable.combinationId)
                .select { conditions }
                .groupBy(FormatLegalityTable.combinationId)
                .having {
                    FormatLegalityTable.combinationId.count() eq legalityIds.size.toLong()
                }
                .map { it[FormatLegalityTable.combinationId] }
                .singleOrNull()

            if (entrie == null) {
                val newId = newId(FormatLegalityTable.combinationId)

                legalityIds.forEach { (formatId, legalityStatusId) ->
                    FormatLegalityTable.insert {
                        it[combinationId] = newId
                        it[format] = formatId
                        it[legalityStatus] = legalityStatusId
                    }
                }

                newId
            } else {
                entrie
            }
        }
    }

    fun importCards(cards: List<Card>) {
        cards.forEach { card ->
            val baseId = BaseCardTable.insertAndGetId {
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

                requireNotNull(card.legalities) { "Card has no legalities" }
                it[formatLegalities] = formatLegalityId(card.legalities!!)

                requireNotNull(card.games) { "Card has no games" }
                it[games] = getOrSetCombinationId(
                    GameCombinationTable.combinationId,
                    GameCombinationTable.game,
                    card.games!!.map { getOrSetEntityId(GamesTable.game, it) },
                    false
                )

                requireNotNull(card.oversized)
                it[oversized] = card.oversized!!

                if (card.power?.toFloatOrNull() != null) {
                    it[power] = card.power!!.toFloat()
                }

                if (card.toughness?.toFloatOrNull() != null) {
                    it[toughness] = card.toughness!!.toFloat()
                }

                if (card.loyalty != null) {
                    it[loyalty] = card.loyalty!!
                }

                if (card.defense != null) {
                    it[defense] = card.defense!!
                }

                requireNotNull(card.edhrecRank) { "Card has no edhrank" }
                it[edhRank] = card.edhrecRank!!

                requireNotNull(card.pennyRank) { "Card has no penny rank" }
                it[pennyRank] = card.pennyRank!!

                requireNotNull(card.name) { "Card has no name" }
                it[name] = card.name!!

                requireNotNull(card.oracleText) { "Card has no oracle text" }
                it[oracleText] = card.oracleText!!

                if (card.colorIndicator != null) {
                    it[colorIndicatorId] =
                        getOrSetCombinationId(ColorTable.combinationId, ColorTable.color, card.colorIndicator!!, true)
                }
            }

            if (!card.cardFaces.isNullOrEmpty()) {
                /* TODO: Card Faces */
            }

            val editionId = CardEditionTable.insertAndGetId {
                it[baseCard] = baseId
                it[set] = getOrSetSet(card)

                requireNotNull(card.foil) { "Card has no foil info" }
                it[canFoil] = card.foil!!

                requireNotNull(card.nonfoil) { "Card has no non-foil info" }
                it[canNonFoil] = card.nonfoil!!

                getOrSetCombinationId(FinishCombinations.combinationId)
            }

        }
    }

    fun getOrSetSet(card: Card): EntityID<Int> {
        transaction {
            requireNotNull(card.setId) { "Card has no Set id" }
            val setId = UUID.fromString(card.setId!!)
            val entry = EditionsTable.select { EditionsTable.setId eq setId }.firstOrNull()
            if (entry != null) {
                return@transaction entry[EditionsTable.id]
            } else {
                val id = EditionsTable.insertAndGetId {
                    requireNotNull(card.set) { "Card has no set code" }
                    it[code] = card.set!!

                    requireNotNull(card.setName) { "Card has no Set name" }
                    it[name] = card.setName!!

                    requireNotNull(card.releasedAt) { "Card has no Release date" }
                    it[releasedAt] = card.releasedAt!! // TODO: Date parsing

                    it[EditionsTable.setId] = setId

                    requireNotNull(card.setType) { "Card has no set type" }
                    it[setType] = card.setType!!
                }
                return@transaction id
            }
        }
    }
}