package de.cypdashuhn.mtgmanager.cards

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

object BaseCardTable : IntIdTable("BaseCard") {
    val objectType = reference("objectType", ObjectTypeTable)
    val oracleId = uuid("oracleId")
    val layout = reference("layout", LayoutTable)
    val manaCost = reference("manaCost", ManaCostTable)
    val colorId = integer("colorId").references(ColorTable.combinationId)
    val colorIdentityId = integer("colorIdentityId").references(ColorTable.combinationId)
    val producedManaColorsId = integer("producedManaColorsId").references(ColorTable.combinationId).nullable()
    val formatLegalities = integer("formatLegalityId").references(FormatLegalityTable.combinationId)
    val games = integer("gamesId").references(GameCombinationTable.combinationId)
    val oversized = bool("oversized")
    val power = float("power").nullable()
    val toughness = float("toughness").nullable()
    val loyalty = integer("loyalty").nullable()
    val defense = integer("defense").nullable()
    val edhRank = integer("edhRank")
    val pennyRank = integer("pennyRank")
    val name = varchar("name", 127)
    val oracleText = varchar("oracleText", 1000)
    val colorIndicatorId = integer("colorIndicatorId").references(ColorTable.combinationId).nullable()
}

object ObjectTypeTable : IntIdTable("ObjectType") {
    val objectType = varchar("objectType", 15)
}

object CardFacesTable : Table("CardFaces") {
    val card = reference("cardId", BaseCardTable)
    val face = reference("faceId", BaseCardTable)
}

object ManaCostTable : IntIdTable("ManaCost") {
    val manaCost = varchar("manaCost", 31)
    val cmc = float("cmc")
}

object CardEditionTable : IntIdTable("CardEdition") {
    val baseCard = reference("baseCardId", BaseCardTable) // Creates a foreign key to BaseCardTable
    val set = reference("setId", EditionsTable)
    val foilable = bool("foilable")
    val finishesId = integer("finishesId").references(FinishCombinations.combinationId)
    val promo = bool("promo")
    val reprint = bool("reprint")
    val variation = bool("variation")
    val collectorNumber = integer("collectorNumber")
    val digital = bool("digital")
    val rarity = reference("rarityId", RarityTable)
    val cardBackId = uuid("cardBackId") /* Find out what the heck this is */
    val illustrationId = uuid("illustrationId")
    val borderColor = reference("borderColorId", BorderColorTable)
    val frame = reference("frameId", FrameTable)
    val fullart = bool("fullart")
    val textless = bool("textless")
    val booster = bool("booster")
    val storySpotlight = bool("storySpotlight")
    val promoTypesId = integer("promoTypesId").references(PromoTypeCombinationTable.combinationId)
    val securityStamp = reference("securityStampId", SecurityStampTable)
    val watermarkId = reference("watermarkId", WatermarkTable).nullable()
    val frameEffectId = integer("frameEffectId").references(FrameEffectCombinationsTable.combinationId).nullable()

}

object FrameEffectCombinationsTable : Table("FrameEffectCombinations") {
    val combinationId = integer("combinationId")
    val frameEffect = reference("frameEffectId", FrameEffectsTable)
}

object FrameEffectsTable : IntIdTable("FrameEffects") {
    val frameEffect = varchar("frameEffect", 31)
}

object WatermarkTable : IntIdTable("Watermark") {
    val watermark = varchar("watermark", 31)
}

object CardPartsTable : Table("CardParts") {
    val card = reference("cardId", CardEditionTable)
    val part = reference("partId", CardEditionTable)
    val partType = varchar("partType", 31)
}

object SecurityStampTable : IntIdTable("SecurityStamp") {
    val securityStamp = varchar("securityStamp", 15)
}

object PromoTypeCombinationTable : Table("PromoTypeCombinations") {
    val combinationId = integer("combinationId")
    val promoType = reference("promoTypeId", PromoTypeTable)
}

object PromoTypeTable : IntIdTable("PromoType") {
    val type = varchar("type", 31)
}

object FrameTable : IntIdTable("Frame") {
    val frame = varchar("frame", 15)
}

object BorderColorTable : IntIdTable("BorderColor") {
    val border = varchar("border", 15)
}

object CardArtistsTable : Table("CardArtists") {
    val cardEdition = reference("cardEditionId", CardEditionTable) // Foreign key to CardEditionTable
    val artist = uuid("artistId").references(ArtistsTable.artistId)
}

object ArtistsTable : Table("Artists") {
    val artist = varchar("artist", 50)
    val artistId = uuid("artistId")
}

object RarityTable : IntIdTable("Rarity") {
    val rarity = varchar("rarity", 15)
}

object EditionsTable : IntIdTable("Editions") {
    val code = varchar("code", 7)
    val name = varchar("name", 50)
    val releasedAt = date("released_at")
    val setId = uuid("set_id")
    val setType = varchar("set_type", 15)
}

object FinishCombinations : Table("FinishCombinations") {
    val combinationId = integer("combinationId")
    val finish = reference("finishId", Finishes)
}

object Finishes : IntIdTable("Finishes") {
    val finish = varchar("finish", 31)
}

object LayoutTable : IntIdTable("Layout") {
    val layout = varchar("layout", 20)
}

object LocalizedCardTable : IntIdTable("LocalizedCard") {
    val baseCard = reference("baseCardId", BaseCardTable) // Creates a foreign key to BaseCardTable
    val lang = reference("cardLanguage", CardLanguageTable)
    val printedName = varchar("name", 150)
}

object CardParentTypeTable : IntIdTable("CardType") {
    val cardId = reference("cardId", LocalizedCardTable)
    val parentType = reference("parentType", ParentTypeTable)
}

object CardSubTypeTable : IntIdTable("CardSubType") {
    val cardId = reference("cardId", LocalizedCardTable)
    val subType = reference("subType", SubTypeTable)
}

object ParentTypeTable : IntIdTable("ParentType") {
    val lang = reference("lang", CardLanguageTable)
    val type = varchar("type", 15)
}

object SubTypeTable : IntIdTable("SubType") {
    val lang = reference("lang", CardLanguageTable)
    val type = varchar("type", 31)
}

object CardLanguageTable : IntIdTable("CardLanguage") {
    val lang = varchar("lang", 2)
}

object CombinedCardTable : IntIdTable("CombinedCard") {
    val cardEdition = reference("cardEditionId", CardEditionTable) // Foreign key to CardEditionTable
    val cardLanguage = reference("cardLanguageId", LocalizedCardTable) // Foreign key to CardLanguageTable
    val scryfallId = uuid("scryfallId")
    val arenaId = integer("arenaId")
    val tcgplayerId = integer("tcgplayerId")
    val cardmarketId = integer("cardmarketId")
    val flavorText = varchar("flavorText", 1000).nullable()
    val preview = reference("previewId", PreviewTable).nullable()
}

object PreviewTable : IntIdTable("Preview") {
    val previewedAt = date("previewedAt")
    val previewSource = varchar("source", 63)
    val sourceUri = varchar("sourceUri", 255)
}

object CardPricesTable : Table("CardPrices") {
    val combinedCard = reference("combinedCardId", CombinedCardTable).nullable()
    val usd = reference("usdId", PriceTable).nullable()
    val usdFoil = reference("usdFoilId", PriceTable).nullable()
    val usdEtched = reference("usdEtchedId", PriceTable).nullable()
    val eur = reference("eurId", PriceTable).nullable()
    val eurFoil = reference("eurFoilId", PriceTable).nullable()
    val tix = reference("tixId", PriceTable).nullable()
}

object PriceTable : IntIdTable("Price") {
    val price = decimal("price", 10, 2)
}

object ImageStatusTable : IntIdTable("ImageStatus") {
    val status = varchar("status", 15)
}

object MultiverseIdTable : Table("MultiverseId") {
    val combinedCard = reference("combinedCardId", CombinedCardTable)
    val multiverseId = integer("multiverseId")
}

object ColorTable : Table("Color") {
    val combinationId = integer("combinationId")
    val color = char("color").nullable()

    override val primaryKey = PrimaryKey(combinationId)
}

object CardKeywordsTable : Table("CardKeywords") {
    val localizedCardId = reference("localizedCardId", LocalizedCardTable)
    val keywordId = reference("keywordId", KeywordTable)
}

object KeywordTable : IntIdTable("Keyword") {
    val keyword = varchar("keyword", 31)
}

object FormatLegalityTable : Table("FormatLegality") {
    val combinationId = integer("combinationId")
    val format = reference("formatId", FormatTable)
    val legalityStatus = reference("legalityStatusId", LegalityStatusTable)
}

object FormatTable : IntIdTable("Formats") {
    val format = varchar("format", 31)
}

object LegalityStatusTable : IntIdTable("LegalityStatusTable") {
    val legalityStatus = varchar("legalityStatus", 15)
}

object GameCombinationTable : Table("GameCombinations") {
    val combinationId = integer("combinationId")
    val game = reference("gameId", GamesTable)

    override val primaryKey = PrimaryKey(combinationId)
}

object GamesTable : IntIdTable("Games") {
    val game = varchar("game", 15)
}