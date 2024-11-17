package de.cypdashuhn.mtgmanager.cards

import de.cypdashuhn.mtgmanager.Card
import de.cypdashuhn.mtgmanager.Legalities
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object CardImporter {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

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
        return transaction {
            column.table.slice(column.max()).selectAll().firstOrNull()?.get(column.max())?.plus(1) ?: 1
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

    fun getOrSetIndirectCombinationId(
        idColumn: Column<Int>,
        valueReferenceColumn: Column<EntityID<Int>>,
        valueColumn: Column<String>,
        valueList: List<String>
    ): Int {
        return transaction {
            val ids = valueList.map { value ->
                getOrSetEntityId(valueColumn, value)
            }

            return@transaction getOrSetCombinationId(idColumn, valueReferenceColumn, ids, false)
        }
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

            requireNotNull(card.keywords) { "Card has no keywords" }
            card.keywords!!.forEach { word ->
                CardKeywordsTable.insert {
                    it[cardId] = baseId
                    it[keyword] = getOrSetId(KeywordTable.keyword, word)
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

                requireNotNull(card.finishes) { "Card has no finishes" }
                it[finishesId] = getOrSetIndirectCombinationId(
                    FinishCombinations.combinationId,
                    FinishCombinations.finish,
                    Finishes.finish,
                    card.finishes!!
                )

                requireNotNull(card.promo) { "Card has no promo info" }
                it[isPromo] = card.promo!!

                requireNotNull(card.reprint) { "Card has no reprint info" }
                it[isReprint] = card.reprint!!

                requireNotNull(card.variation) { "Card has no variation info" }
                it[isVariation] = card.variation!!

                requireNotNull(card.collectorNumber) { "Card has no collector Number" }
                it[collectorNumber] = card.collectorNumber!!

                requireNotNull(card.digital) { "Card has no digital info" }
                it[isDigital] = card.digital!!

                requireNotNull(card.rarity) { "Card has no rarity" }
                it[rarity] = getOrSetId(RarityTable.rarity, card.rarity!!)

                requireNotNull(card.cardBackId) { "Card has no card back id" }
                it[cardBackId] = UUID.fromString(card.cardBackId!!)

                requireNotNull(card.illustrationId) { "Card has no illustration id" }
                it[illustrationId] = UUID.fromString(card.illustrationId!!)

                requireNotNull(card.borderColor) { "Card has no border color" }
                it[borderColor] = getOrSetId(BorderColorTable.border, card.borderColor!!)

                requireNotNull(card.frame) { "Card has no frame info" }
                it[frame] = getOrSetId(FrameTable.frame, card.frame!!)

                requireNotNull(card.fullArt) { "Card has no full art info" }
                it[isFullart] = card.fullArt!!

                requireNotNull(card.textless) { "Card has no textless info" }
                it[isTextless] = card.textless!!

                requireNotNull(card.booster) { "Card has no inBooster info" }
                it[inBooster] = card.booster!!

                requireNotNull(card.storySpotlight) { "Card has no story spotlight info" }
                it[isStorySpotlight] = card.storySpotlight!!

                requireNotNull(card.promoTypes) { "Card has no promo types" }
                it[promoTypesId] = getOrSetIndirectCombinationId(
                    PromoTypeCombinationTable.combinationId,
                    PromoTypeCombinationTable.promoType,
                    PromoTypeTable.type,
                    card.promoTypes!!
                )

                requireNotNull(card.securityStamp) { "Card has no security stamp" }
                it[securityStamp] = getOrSetId(SecurityStampTable.securityStamp, card.securityStamp!!)

                requireNotNull(card.watermark) { "Card has no watermark" }
                it[watermarkId] = getOrSetId(WatermarkTable.watermark, card.watermark!!)

                requireNotNull(card.frameEffects) { "Card has no frame effect" }
                it[frameEffectId] = getOrSetIndirectCombinationId(
                    FrameEffectCombinationsTable.combinationId,
                    FrameEffectCombinationsTable.frameEffect,
                    FrameEffectsTable.frameEffect,
                    card.frameEffects!!
                )
            }

            requireNotNull(card.artistIds) { "Card has no artist ids" }
            requireNotNull(card.artist) { "Card has no artist" }
            val artists = card.artist!!.split("&").map { it.trim() }
            card.artistIds!!.withIndex().forEach { (index, artistId) ->
                CardArtistsTable.insert {
                    it[cardEdition] = editionId
                    val artistUUID = UUID.fromString(artistId)
                    setArtist(artistUUID, artists[index])
                    it[artist] = artistUUID
                }
            }

            var langReference: EntityID<Int> = EntityID(0, CardLanguageTable)
            val languageId = LocalizedCardTable.insertAndGetId {
                it[baseCard] = baseId

                requireNotNull(card.lang) { "Card has no language" }
                langReference = getOrSetEntityId(CardLanguageTable.lang, card.lang!!)
                it[lang] = langReference

                requireNotNull(card.printedName) { "Card has no printed name" }
                it[printedName] = card.printedName!!

                requireNotNull(card.printedText) { "Card has no printed text" }
                it[printedText] = card.printedText!!
            }

            requireNotNull(card.printedTypeLine) { "Card has no printed type" }
            val (parents, children) = card.printedTypeLine
                ?.split("—", limit = 2)
                ?.map { it.trim() }
                ?.let {
                    val parents = it[0].split(" ").map(String::trim)
                    val children = it.getOrNull(1)?.split(" ")?.map(String::trim).orEmpty()
                    parents to children
                } ?: (emptyList<String>() to emptyList())

            parents.forEach { parent ->
                CardParentTypeTable.insert {
                    it[cardId] = languageId
                    it[parentType] = getOrSetType(ParentTypeTable.lang, ParentTypeTable.type, langReference, parent)
                }
            }

            children.forEach { child ->
                CardChildTypeTable.insert {
                    it[cardId] = languageId
                    it[childType] = getOrSetType(ChildTypeTable.lang, ChildTypeTable.type, langReference, child)
                }
            }

            val combinedCardId = CombinedCardTable.insertAndGetId {
                it[cardEdition] = editionId
                it[cardLanguage] = languageId

                requireNotNull(card.id) { "Card has no scryfall id" }
                it[scryfallId] = UUID.fromString(card.id!!)

                requireNotNull(card.arenaId) { "Card has no arena id" }
                it[arenaId] = card.arenaId!!

                requireNotNull(card.tcgplayerId) { "Card has no tcgplayer id" }
                it[tcgplayerId] = card.tcgplayerId!!

                requireNotNull(card.cardmarketId) { "Card has no cardmarket id" }
                it[cardmarketId] = card.cardmarketId!!

                requireNotNull(card.flavorText) { "Card has no flavor text" }
                it[flavorText] = card.flavorText!!

                if (card.preview != null) {
                    it[preview] = getOrSetPreview(card)
                }

                requireNotNull(card.imageStatus) { "Card has no image status" }
                it[imageStatus] = getOrSetId(ImageStatusTable.status, card.imageStatus!!)
            }

            requireNotNull(card.prices) { "Card has no prices" }
            CardPricesTable.insert {
                it[combinedCard] = combinedCardId

                fun getPrice(price: Int) = getOrSetId(PriceTable.price, price.toBigDecimal())
                if (card.prices!!.usd != null) it[usd] = getPrice(card.prices!!.usd!!)
                if (card.prices!!.usdFoil != null) it[usdFoil] = getPrice(card.prices!!.usdFoil!!)
                if (card.prices!!.usdEtched != null) it[usdEtched] = getPrice(card.prices!!.usdEtched!!)
                if (card.prices!!.eur != null) it[eur] = getPrice(card.prices!!.eur!!)
                if (card.prices!!.eurFoil != null) it[eurFoil] = getPrice(card.prices!!.eurFoil!!)
                if (card.prices!!.tix != null) it[tix] = getPrice(card.prices!!.tix!!)
            }

            requireNotNull(card.multiverseIds) { "Card has no multiverse ids" }
            card.multiverseIds!!.forEach { multiverseId ->
                MultiverseIdTable.insert {
                    it[combinedCard] = combinedCardId
                    it[MultiverseIdTable.multiverseId] = multiverseId
                }
            }
        }
    }

    fun setArtist(artistId: UUID, artist: String) {
        transaction {
            val entry = ArtistsTable.select { ArtistsTable.artist eq artist }.firstOrNull()
            if (entry == null) {
                ArtistsTable.insert {
                    it[ArtistsTable.artist] = artist
                    it[ArtistsTable.artistId] = artistId
                }
            }
        }
    }

    fun getOrSetPreview(card: Card): EntityID<Int>? {
        return transaction {
            if (card.preview == null) return@transaction null

            requireNotNull(card.preview!!.previewedAt) { "Card has no preview date" }
            val date = LocalDate.parse(card.preview!!.previewedAt, formatter)

            requireNotNull(card.preview!!.sourceUri) { "Card has no preview uri" }

            val entry =
                PreviewTable.select { PreviewTable.previewedAt eq date and (PreviewTable.sourceUri eq card.preview!!.sourceUri!!) }
                    .firstOrNull()
            if (entry != null) {
                return@transaction entry[PreviewTable.id]
            } else {
                val id = PreviewTable.insertAndGetId {
                    it[previewedAt] = date

                    it[sourceUri] = card.preview!!.sourceUri!!

                    requireNotNull(card.preview!!.source) { "Card has no preview name" }
                    it[previewSource] = card.preview!!.source!!
                }
                return@transaction id
            }
        }
    }

    fun getOrSetType(
        langColumn: Column<EntityID<Int>>,
        typeColumn: Column<String>,
        langId: EntityID<Int>,
        type: String
    ): EntityID<Int> {
        return transaction {
            val table = langColumn.table as IntIdTable
            val entry = table.select { langColumn eq langId and (typeColumn eq type) }.firstOrNull()
            if (entry != null) {
                return@transaction entry[table.id]
            } else {
                val id = table.insertAndGetId {
                    it[langColumn] = langId
                    it[typeColumn] = type
                }
                return@transaction id
            }
        }
    }

    fun getOrSetSet(card: Card): EntityID<Int> {
        return transaction {
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
                    it[releasedAt] = LocalDate.parse(card.releasedAt!!, formatter)

                    it[EditionsTable.setId] = setId

                    requireNotNull(card.setType) { "Card has no set type" }
                    it[setType] = card.setType!!
                }
                return@transaction id
            }
        }
    }
}