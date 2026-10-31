package ir.fastlove.app.core.data

import ir.fastlove.app.core.model.FastLoveData
import ir.fastlove.app.core.model.Gender
import ir.fastlove.app.core.model.Orientation
import ir.fastlove.app.core.model.Person
import ir.fastlove.app.core.model.RelationGoal
import kotlin.random.Random

/**
 * Deterministic demo data. A fixed seed means tests and the UI always get the same people,
 * so interactions (likes, matches, dates) are reproducible.
 */
object Seed {

    private const val SEED = 20261030L

    private val femaleNames = listOf(
        "رها", "سارا", "نیلوفر", "مهسا", "آتنا", "پریا", "لیلا", "نازنین", "هستی", "یاسمن",
        "مهدیس", "گلناز", "شیما", "فرناز", "الهام", "مینا", "سپیده", "زهرا", "فاطمه", "مریم",
        "نگار", "آیدا", "سحر", "پگاه", "دنیا", "کیمیا", "رویا", "بهار", "شیدا", "نرگس"
    )
    private val maleNames = listOf(
        "آرمین", "کاوه", "نوید", "امیر", "رضا", "حسین", "علی", "محمد", "سینا", "پارسا",
        "آرش", "کیان", "مانی", "دانیال", "نیما", "بهراد", "سپهر", "پویا", "سامان", "فرهاد"
    )
    private val emojisF = listOf("🌷", "🌿", "🌼", "🪻", "✨", "🌙", "🔥", "🌸", "🌺", "🌹", "💖", "🦋", "🌻", "🍀", "🎀")
    private val emojisM = listOf("🏔️", "📷", "🩺", "🎸", "⚽", "🎯", "🚀", "💻", "📚", "🎬", "🏋️", "🎮", "☕", "🌊", "🦊")
    private val tintsF = listOf(0xFFFFE0E6, 0xFFE0F2E9, 0xFFFFF0D6, 0xFFEDE0FF, 0xFFFFE8D6, 0xFFE0E8FF, 0xFFFFE0CC, 0xFFFFE4EC, 0xFFE8F5E9, 0xFFF3E5F5)
    private val tintsM = listOf(0xFFD6EAF8, 0xFFF5D6E0, 0xFFD5F5E3, 0xFFE8DAEF, 0xFFD6EAF8, 0xFFFADBD8, 0xFFD5F5E3, 0xFFD4E6F1, 0xFFFCF3CF, 0xFFE8F8F5)
    private val abouts = listOf(
        "اهل کافه‌گردی و کتاب. دنبال آشنایی واقعی.",
        "طبیعت‌گردی و موسیقی حال من را خوب می‌کند.",
        "کنجکاوم دنیا را بیشتر بشناسم.",
        "رابطه‌ای می‌خواهم که در آن هر دو رشد کنیم.",
        "عاشق ساخت ویدیو و داستان‌گویی.",
        "گوش دادن خوب بلدم و عاشق گفت‌وگوهای عمیقم.",
        "عاشق کوهنوردی و قهوه.",
        "زندگی را از پشت لنز می‌بینم.",
        "پر از انرژی و عاشق ماجراجویی.",
        "به دنبال آرامش و ارتباط عمیق."
    )
    private val idealDates = listOf(
        "یک کافه دنج و گفت‌وگوی طولانی.",
        "پیاده‌روی در پارک و بعد یک بستنی.",
        "گالری نقاشی و بعد شام سبک.",
        "کوه صبح زود و صبحانه با منظره.",
        "سینمای هنری و تحلیل فیلم بعدش."
    )
    private val smiles = listOf(
        "موسیقی خوب وسط کار.",
        "پیام‌های بی‌دلیل مهربانانه.",
        "بوی قهوه‌ی صبح.",
        "خنده‌ی از ته دل دوستام.",
        "باران پشت پنجره."
    )
    private val values = listOf(
        "صداقت و گفت‌وگوی باز.",
        "احترام به فضای شخصی.",
        "حس شوخ‌طبعی مشترک.",
        "هم‌مسیر بودن در رشد.",
        "اعتماد بدون قضاوت."
    )
    private val goals = RelationGoal.entries.toList()

    private val tehranLatRange = 35.565..35.830
    private val tehranLonRange = 51.185..51.630

    val people: List<Person> by lazy { generate() }

    private fun generate(): List<Person> {
        val rnd = Random(SEED)
        val list = mutableListOf<Person>()
        var id = 1

        fun coords(assignTehran: Boolean): Pair<Double, Double> =
            if (assignTehran) {
                val lat = tehranLatRange.start + rnd.nextDouble() * (tehranLatRange.endInclusive - tehranLatRange.start)
                val lon = tehranLonRange.start + rnd.nextDouble() * (tehranLonRange.endInclusive - tehranLonRange.start)
                lat to lon
            } else 0.0 to 0.0

        repeat(20) { i ->
            val tehran = i < 12
            val (lat, lon) = coords(tehran)
            val interests = FastLoveData.availableInterests.shuffled(rnd).take(3 + i % 3)
            list += Person(
                id = id++,
                name = femaleNames[i % femaleNames.size],
                age = 20 + (i % 18),
                gender = Gender.FEMALE,
                orientation = when (i % 5) { 0, 1, 2 -> Orientation.STRAIGHT; 3 -> Orientation.BISEXUAL; else -> Orientation.LESBIAN },
                goal = goals[i % goals.size],
                job = FastLoveData.availableJobs[i % FastLoveData.availableJobs.size],
                city = if (tehran) "تهران" else FastLoveData.iranianCities[i % FastLoveData.iranianCities.size],
                about = abouts[i % abouts.size],
                interests = interests,
                emoji = emojisF[i % emojisF.size],
                tintArgb = tintsF[i % tintsF.size],
                distanceKm = 1 + (i * 3) % 45,
                likedYou = i % 3 == 0,
                instagram = if (i % 3 != 0) "fastlove_${id - 1}" else "",
                spotify = if (i % 4 == 0) "fastlove_user${id - 1}" else "",
                idealFirstDate = idealDates[i % idealDates.size],
                makesMeSmile = smiles[i % smiles.size],
                relationshipValue = values[i % values.size],
                latitude = lat,
                longitude = lon
            )
        }
        repeat(16) { i ->
            val tehran = i < 12
            val (lat, lon) = coords(tehran)
            val interests = FastLoveData.availableInterests.shuffled(rnd).take(3 + i % 4)
            list += Person(
                id = id++,
                name = maleNames[i % maleNames.size],
                age = 21 + (i % 20),
                gender = Gender.MALE,
                orientation = when (i % 5) { 0, 1, 2 -> Orientation.STRAIGHT; 3 -> Orientation.GAY; else -> Orientation.BISEXUAL },
                goal = goals[(i + 1) % goals.size],
                job = FastLoveData.availableJobs[(i + 3) % FastLoveData.availableJobs.size],
                city = if (tehran) "تهران" else FastLoveData.iranianCities[(i + 5) % FastLoveData.iranianCities.size],
                about = abouts[(i + 2) % abouts.size],
                interests = interests,
                emoji = emojisM[i % emojisM.size],
                tintArgb = tintsM[i % tintsM.size],
                distanceKm = 2 + (i * 5) % 48,
                likedYou = i % 4 == 0,
                instagram = if (i % 4 != 1) "fastlove_${id - 1}" else "",
                spotify = if (i % 3 == 0) "fastlove_user${id - 1}" else "",
                idealFirstDate = idealDates[(i + 1) % idealDates.size],
                makesMeSmile = smiles[(i + 2) % smiles.size],
                relationshipValue = values[(i + 3) % values.size],
                latitude = lat,
                longitude = lon
            )
        }
        repeat(4) { i ->
            val (lat, lon) = coords(true)
            list += Person(
                id = id++,
                name = listOf("آریا", "سام", "رها", "کیان")[i],
                age = 24 + i,
                gender = Gender.OTHER,
                orientation = Orientation.OTHER,
                goal = goals[i % goals.size],
                job = FastLoveData.availableJobs[i],
                city = "تهران",
                about = abouts[i],
                interests = FastLoveData.availableInterests.shuffled(rnd).take(4),
                emoji = "⭐",
                tintArgb = 0xFFE0E0E0,
                distanceKm = 5 + i * 4,
                likedYou = i % 2 == 0,
                idealFirstDate = idealDates[i],
                makesMeSmile = smiles[i],
                relationshipValue = values[i],
                latitude = lat,
                longitude = lon
            )
        }
        return list
    }
}
