package de.cypdashuhn.mtgmanager

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Card(
    @SerialName("object") val objectName: String? = null,
    val id: String? = null,
    @SerialName("oracle_id") val oracleId: String? = null,
    @SerialName("multiverse_ids") val multiverseIds: List<Int>? = null,
    @SerialName("mtgo_id") val mtgoId: Int? = null,
    @SerialName("arena_id") val arenaId: Int? = null,
    @SerialName("tcgplayer_id") val tcgplayerId: Int? = null,
    val name: String? = null,
    val lang: String? = null,
    @SerialName("released_at") val releasedAt: String? = null,
    val uri: String? = null,
    @SerialName("scryfall_uri") val scryfallUri: String? = null,
    val layout: String? = null,
    @SerialName("highres_image") val highresImage: Boolean? = null,
    @SerialName("image_status") val imageStatus: String? = null,
    @SerialName("image_uris") val imageUris: ImageUris? = null,
    @SerialName("mana_cost") val manaCost: String? = null,
    val cmc: Float? = null,
    @SerialName("type_line") val typeLine: String? = null,
    @SerialName("oracle_text") val oracleText: String? = null,
    val colors: List<Char>? = null,
    @SerialName("color_identity") val colorIdentity: List<Char>? = null,
    val keywords: List<String>? = null,
    @SerialName("produced_mana") val producedMana: List<Char>? = null,
    val legalities: Legalities? = null,
    val games: List<String>? = null,
    val reserved: Boolean? = null,
    val foil: Boolean? = null,
    val nonfoil: Boolean? = null,
    val finishes: List<String>? = null,
    val oversized: Boolean? = null,
    val promo: Boolean? = null,
    val reprint: Boolean? = null,
    val variation: Boolean? = null,
    @SerialName("set_id") val setId: String? = null,
    val set: String? = null,
    @SerialName("set_name") val setName: String? = null,
    @SerialName("set_type") val setType: String? = null,
    @SerialName("set_uri") val setUri: String? = null,
    @SerialName("set_search_uri") val setSearchUri: String? = null,
    @SerialName("scryfall_set_uri") val scryfallSetUri: String? = null,
    @SerialName("rulings_uri") val rulingsUri: String? = null,
    @SerialName("prints_search_uri") val printsSearchUri: String? = null,
    @SerialName("collector_number") val collectorNumber: String? = null,
    val digital: Boolean? = null,
    val rarity: String? = null,
    @SerialName("card_back_id") val cardBackId: String? = null,
    val artist: String? = null,
    @SerialName("artist_ids") val artistIds: List<String>? = null,
    @SerialName("illustration_id") val illustrationId: String? = null,
    @SerialName("border_color") val borderColor: String? = null,
    val frame: String? = null,
    @SerialName("full_art") val fullArt: Boolean? = null,
    val textless: Boolean? = null,
    val booster: Boolean? = null,
    @SerialName("story_spotlight") val storySpotlight: Boolean? = null,
    val prices: Prices? = null,
    @SerialName("related_uris") val relatedUris: RelatedUris? = null,
    @SerialName("purchase_uris") val purchaseUris: PurchaseUris? = null,
    @SerialName("mtgo_foil_id") val mtgoFoilId: Int? = null,
    @SerialName("cardmarket_id") val cardmarketId: Int? = null,
    val power: String? = null,
    val toughness: String? = null,
    @SerialName("flavor_text") val flavorText: String? = null,
    @SerialName("edhrec_rank") val edhrecRank: Int? = null,
    @SerialName("penny_rank") val pennyRank: Int? = null,
    @SerialName("all_parts") val allParts: List<Card>? = null,
    val component: String? = null,
    @SerialName("promo_types") val promoTypes: List<String>? = null,
    @SerialName("security_stamp") val securityStamp: String? = null,
    @SerialName("card_faces") val cardFaces: List<Card>? = null,
    @SerialName("artist_id") val artistId: String? = null,
    val preview: Preview? = null,
    val watermark: String? = null,
    @SerialName("frame_effects") val frameEffects: List<String>? = null,
    val loyalty: Int? = null,
    @SerialName("printed_name") val printedName: String? = null,
    val defense: Int? = null,
    @SerialName("color_indicator") val colorIndicator: List<Char>? = null,
    @SerialName("flavor_name") val flavorName: String? = null,
    @SerialName("tcgplayer_etched_id") val tcgplayerEtchedId: String? = null,
)

@Serializable
data class Preview(
    val source: String? = null,
    @SerialName("source_uri") val sourceUri: String? = null,
    @SerialName("previewed_at") val previewedAt: String? = null
)

@Serializable
data class ImageUris(
    val small: String? = null,
    val normal: String? = null,
    val large: String? = null,
    val png: String? = null,
    @SerialName("art_crop") val artCrop: String? = null,
    @SerialName("border_crop") val borderCrop: String? = null
)

@Serializable
data class Legalities(
    val standard: String? = null,
    val future: String? = null,
    val historic: String? = null,
    val timeless: String? = null,
    val gladiator: String? = null,
    val pioneer: String? = null,
    val explorer: String? = null,
    val modern: String? = null,
    val legacy: String? = null,
    val pauper: String? = null,
    val vintage: String? = null,
    val penny: String? = null,
    val commander: String? = null,
    val oathbreaker: String? = null,
    val standardbrawl: String? = null,
    val brawl: String? = null,
    val alchemy: String? = null,
    val paupercommander: String? = null,
    val duel: String? = null,
    val oldschool: String? = null,
    val premodern: String? = null,
    val predh: String? = null
)

@Serializable
data class Prices(
    val usd: String? = null,
    @SerialName("usd_foil") val usdFoil: String? = null,
    @SerialName("usd_etched") val usdEtched: String? = null,
    val eur: String? = null,
    @SerialName("eur_foil") val eurFoil: String? = null,
    val tix: String? = null
)

@Serializable
data class RelatedUris(
    val gatherer: String? = null,
    @SerialName("tcgplayer_infinite_articles") val tcgplayerInfiniteArticles: String? = null,
    @SerialName("tcgplayer_infinite_decks") val tcgplayerInfiniteDecks: String? = null,
    val edhrec: String? = null
)

@Serializable
data class PurchaseUris(
    val tcgplayer: String? = null,
    val cardmarket: String? = null,
    val cardhoarder: String? = null
)

expect class CardReader {
    companion object {
        fun getCards(): List<Card>
    }
}

