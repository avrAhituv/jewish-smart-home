package com.jewishhome.app.data.repository

import com.jewishhome.app.data.local.preferences.UserPreferences
import com.jewishhome.app.domain.model.*
import com.jewishhome.app.domain.repository.TextsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextsRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences
) : TextsRepository {

    private val allTexts = listOf(
        // ברכת המזון
        BrachaText(
            id = "birkat_hamazon",
            name = "ברכת המזון",
            category = BrachaCategory.BIRKAT_HAMAZON,
            textAshkenaz = """
בָּרוּךְ אַתָּה יְיָ אֱלֹהֵֽינוּ מֶֽלֶךְ הָעוֹלָם, הַזָּן אֶת הָעוֹלָם כֻּלּוֹ בְּטוּבוֹ בְּחֵן בְּחֶֽסֶד וּבְרַחֲמִים, הוּא נוֹתֵן לֶֽחֶם לְכָל בָּשָׂר כִּי לְעוֹלָם חַסְדּוֹ. וּבְטוּבוֹ הַגָּדוֹל תָּמִיד לֹא חָסַֽר לָֽנוּ, וְאַל יֶחְסַר לָֽנוּ מָזוֹן לְעוֹלָם וָעֶד. בַּעֲבוּר שְׁמוֹ הַגָּדוֹל, כִּי הוּא אֵל זָן וּמְפַרְנֵס לַכֹּל וּמֵטִיב לַכֹּל, וּמֵכִין מָזוֹן לְכָל בְּרִיּוֹתָיו אֲשֶׁר בָּרָא. בָּרוּךְ אַתָּה יְיָ, הַזָּן אֶת הַכֹּל.

נוֹדֶה לְּךָ יְיָ אֱלֹהֵֽינוּ עַל שֶׁהִנְחַֽלְתָּ לַאֲבוֹתֵֽינוּ אֶֽרֶץ חֶמְדָּה טוֹבָה וּרְחָבָה, וְעַל שֶׁהוֹצֵאתָֽנוּ יְיָ אֱלֹהֵֽינוּ מֵאֶֽרֶץ מִצְרַֽיִם, וּפְדִיתָֽנוּ מִבֵּית עֲבָדִים, וְעַל בְּרִיתְךָ שֶׁחָתַֽמְתָּ בִּבְשָׂרֵֽנוּ, וְעַל תּוֹרָתְךָ שֶׁלִּמַּדְתָּֽנוּ, וְעַל חֻקֶּֽיךָ שֶׁהוֹדַעְתָּֽנוּ, וְעַל חַיִּים חֵן וָחֶֽסֶד שֶׁחוֹנַנְתָּֽנוּ, וְעַל אֲכִילַת מָזוֹן שָׁאַתָּה זָן וּמְפַרְנֵס אוֹתָֽנוּ תָּמִיד, בְּכָל יוֹם וּבְכָל עֵת וּבְכָל שָׁעָה.

וְעַל הַכֹּל יְיָ אֱלֹהֵֽינוּ אֲנַֽחְנוּ מוֹדִים לָךְ וּמְבָרְכִים אוֹתָךְ, יִתְבָּרַךְ שִׁמְךָ בְּפִי כָל חַי תָּמִיד לְעוֹלָם וָעֶד. כַּכָּתוּב: וְאָכַלְתָּ וְשָׂבָֽעְתָּ, וּבֵרַכְתָּ אֶת יְיָ אֱלֹהֶֽיךָ עַל הָאָֽרֶץ הַטֹּבָה אֲשֶׁר נָֽתַן לָךְ. בָּרוּךְ אַתָּה יְיָ, עַל הָאָֽרֶץ וְעַל הַמָּזוֹן.

רַחֵם נָא יְיָ אֱלֹהֵֽינוּ עַל יִשְׂרָאֵל עַמֶּֽךָ וְעַל יְרוּשָׁלַֽיִם עִירֶֽךָ וְעַל צִיּוֹן מִשְׁכַּן כְּבוֹדֶֽךָ וְעַל מַלְכוּת בֵּית דָּוִד מְשִׁיחֶֽךָ וְעַל הַבַּֽיִת הַגָּדוֹל וְהַקָּדוֹשׁ שֶׁנִּקְרָא שִׁמְךָ עָלָיו. אֱלֹהֵֽינוּ אָבִֽינוּ, רְעֵֽנוּ זוּנֵֽנוּ פַּרְנְסֵֽנוּ וְכַלְכְּלֵֽנוּ וְהַרְוִיחֵֽנוּ, וְהַרְוַח לָֽנוּ יְיָ אֱלֹהֵֽינוּ מְהֵרָה מִכָּל צָרוֹתֵֽינוּ.

וְנָא אַל תַּצְרִיכֵֽנוּ יְיָ אֱלֹהֵֽינוּ, לֹא לִידֵי מַתְּנַת בָּשָׂר וָדָם וְלֹא לִידֵי הַלְוָאָתָם, כִּי אִם לְיָדְךָ הַמְּלֵאָה הַפְּתוּחָה הַקְּדוֹשָׁה וְהָרְחָבָה, שֶׁלֹּא נֵבוֹשׁ וְלֹא נִכָּלֵם לְעוֹלָם וָעֶד.

וּבְנֵה יְרוּשָׁלַֽיִם עִיר הַקֹּֽדֶשׁ בִּמְהֵרָה בְיָמֵֽינוּ. בָּרוּךְ אַתָּה יְיָ, בּוֹנֵה בְרַחֲמָיו יְרוּשָׁלָֽיִם. אָמֵן.

בָּרוּךְ אַתָּה יְיָ אֱלֹהֵֽינוּ מֶֽלֶךְ הָעוֹלָם, הָאֵל אָבִֽינוּ מַלְכֵּֽנוּ אַדִּירֵֽנוּ בּוֹרְאֵֽנוּ גֹּאֲלֵֽנוּ יוֹצְרֵֽנוּ קְדוֹשֵֽׁנוּ קְדוֹשׁ יַעֲקֹב, רוֹעֵֽנוּ רוֹעֵה יִשְׂרָאֵל, הַמֶּֽלֶךְ הַטּוֹב וְהַמֵּטִיב לַכֹּל, שֶׁבְּכָל יוֹם וָיוֹם הוּא הֵטִיב, הוּא מֵטִיב, הוּא יֵיטִיב לָֽנוּ. הוּא גְמָלָֽנוּ, הוּא גוֹמְלֵֽנוּ, הוּא יִגְמְלֵֽנוּ לָעַד, לְחֵן וּלְחֶֽסֶד וּלְרַחֲמִים וּלְרֶֽוַח הַצָּלָה וְהַצְלָחָה, בְּרָכָה וִישׁוּעָה, נֶחָמָה, פַּרְנָסָה וְכַלְכָּלָה, וְרַחֲמִים וְחַיִּים וְשָׁלוֹם וְכָל טוֹב, וּמִכָּל טוּב לְעוֹלָם אַל יְחַסְּרֵֽנוּ.
            """.trimIndent(),
            instructions = "מברכים אחרי סעודה עם לחם"
        ),

        // אשר יצר
        BrachaText(
            id = "asher_yatzar",
            name = "אשר יצר",
            category = BrachaCategory.ASHER_YATZAR,
            textAshkenaz = """
בָּרוּךְ אַתָּה יְיָ אֱלֹהֵֽינוּ מֶֽלֶךְ הָעוֹלָם, אֲשֶׁר יָצַר אֶת הָאָדָם בְּחָכְמָה, וּבָרָא בוֹ נְקָבִים נְקָבִים, חֲלוּלִים חֲלוּלִים. גָּלוּי וְיָדֽוּעַ לִפְנֵי כִסֵּא כְבוֹדֶֽךָ, שֶׁאִם יִפָּתֵֽחַ אֶחָד מֵהֶם, אוֹ יִסָּתֵם אֶחָד מֵהֶם, אִי אֶפְשַׁר לְהִתְקַיֵּם וְלַעֲמֹד לְפָנֶֽיךָ אֲפִילוּ שָׁעָה אֶחָת. בָּרוּךְ אַתָּה יְיָ, רוֹפֵא כָל בָּשָׂר וּמַפְלִיא לַעֲשׂוֹת.
            """.trimIndent(),
            instructions = "מברכים אחרי יציאה משירותים"
        ),

        // ברכות הנהנין - המוציא
        BrachaText(
            id = "hamotzi",
            name = "המוציא",
            category = BrachaCategory.MEZONOT,
            textAshkenaz = "בָּרוּךְ אַתָּה יְיָ אֱלֹהֵֽינוּ מֶֽלֶךְ הָעוֹלָם, הַמּוֹצִיא לֶֽחֶם מִן הָאָֽרֶץ.",
            instructions = "מברכים על לחם"
        ),

        // מזונות
        BrachaText(
            id = "mezonot",
            name = "בורא מיני מזונות",
            category = BrachaCategory.MEZONOT,
            textAshkenaz = "בָּרוּךְ אַתָּה יְיָ אֱלֹהֵֽינוּ מֶֽלֶךְ הָעוֹלָם, בּוֹרֵא מִינֵי מְזוֹנוֹת.",
            instructions = "מברכים על עוגות, עוגיות ומאפים"
        ),

        // הגפן
        BrachaText(
            id = "hagafen",
            name = "בורא פרי הגפן",
            category = BrachaCategory.MEZONOT,
            textAshkenaz = "בָּרוּךְ אַתָּה יְיָ אֱלֹהֵֽינוּ מֶֽלֶךְ הָעוֹלָם, בּוֹרֵא פְּרִי הַגָּֽפֶן.",
            instructions = "מברכים על יין ומיץ ענבים"
        ),

        // העץ
        BrachaText(
            id = "haetz",
            name = "בורא פרי העץ",
            category = BrachaCategory.MEZONOT,
            textAshkenaz = "בָּרוּךְ אַתָּה יְיָ אֱלֹהֵֽינוּ מֶֽלֶךְ הָעוֹלָם, בּוֹרֵא פְּרִי הָעֵץ.",
            instructions = "מברכים על פירות העץ"
        ),

        // האדמה
        BrachaText(
            id = "haadama",
            name = "בורא פרי האדמה",
            category = BrachaCategory.MEZONOT,
            textAshkenaz = "בָּרוּךְ אַתָּה יְיָ אֱלֹהֵֽינוּ מֶֽלֶךְ הָעוֹלָם, בּוֹרֵא פְּרִי הָאֲדָמָה.",
            instructions = "מברכים על ירקות ופירות האדמה"
        ),

        // שהכל
        BrachaText(
            id = "shehakol",
            name = "שהכל נהיה בדברו",
            category = BrachaCategory.MEZONOT,
            textAshkenaz = "בָּרוּךְ אַתָּה יְיָ אֱלֹהֵֽינוּ מֶֽלֶךְ הָעוֹלָם, שֶׁהַכֹּל נִהְיָה בִּדְבָרוֹ.",
            instructions = "מברכים על מים, בשר, דגים, ביצים ומאכלים אחרים"
        ),

        // תפילת הדרך
        BrachaText(
            id = "tefilat_haderech",
            name = "תפילת הדרך",
            category = BrachaCategory.TEFILAT_HADERECH,
            textAshkenaz = """
יְהִי רָצוֹן מִלְּפָנֶֽיךָ יְיָ אֱלֹהֵֽינוּ וֵאלֹהֵי אֲבוֹתֵֽינוּ, שֶׁתּוֹלִיכֵֽנוּ לְשָׁלוֹם וְתַצְעִידֵֽנוּ לְשָׁלוֹם, וְתִסְמְכֵֽנוּ לְשָׁלוֹם, וְתַדְרִיכֵֽנוּ לְשָׁלוֹם, וְתַגִּיעֵֽנוּ לִמְחוֹז חֶפְצֵֽנוּ לְחַיִּים וּלְשִׂמְחָה וּלְשָׁלוֹם. וְתַצִּילֵֽנוּ מִכַּף כָּל אוֹיֵב וְאוֹרֵב וְלִסְטִים וְחַיּוֹת רָעוֹת בַּדֶּֽרֶךְ, וּמִכָּל מִינֵי פֻּרְעָנֻיּוֹת הַמִּתְרַגְּשׁוֹת לָבֹא לָעוֹלָם. וְתִשְׁלַח בְּרָכָה בְּכָל מַעֲשֵׂה יָדֵֽינוּ, וְתִתְּנֵֽנוּ לְחֵן וּלְחֶֽסֶד וּלְרַחֲמִים בְּעֵינֶֽיךָ וּבְעֵינֵי כָל רֹאֵֽינוּ, וְתִשְׁמַע קוֹל תַּחֲנוּנֵֽינוּ, כִּי אֵל שׁוֹמֵֽעַ תְּפִלָּה וְתַחֲנוּן אָֽתָּה. בָּרוּךְ אַתָּה יְיָ, שׁוֹמֵֽעַ תְּפִלָּה.
            """.trimIndent(),
            instructions = "מברכים בתחילת נסיעה של יותר מ-72 דקות"
        ),

        // קריאת שמע על המיטה
        BrachaText(
            id = "shema_al_hamita",
            name = "קריאת שמע על המיטה",
            category = BrachaCategory.SHEMA,
            textAshkenaz = """
הֲרֵינִי מוֹחֵל לְכָל מִי שֶׁהִכְעִיס וְהִקְנִיט אוֹתִי, אוֹ שֶׁחָטָא כְּנֶגְדִּי, בֵּין בְּגוּפִי בֵּין בְּמָמוֹנִי בֵּין בִּכְבוֹדִי בֵּין בְּכָל אֲשֶׁר לִי, בֵּין בְּאֹֽנֶס בֵּין בְּרָצוֹן, בֵּין בְּשׁוֹגֵג בֵּין בְּמֵזִיד, בֵּין בְּדִבּוּר בֵּין בְּמַעֲשֶׂה, וְלֹא יֵעָנֵשׁ שׁוּם אָדָם בְּסִבָּתִי.

שְׁמַע יִשְׂרָאֵל יְיָ אֱלֹהֵֽינוּ יְיָ אֶחָד.

בָּרוּךְ שֵׁם כְּבוֹד מַלְכוּתוֹ לְעוֹלָם וָעֶד.

וְאָהַבְתָּ אֵת יְיָ אֱלֹהֶֽיךָ בְּכָל לְבָבְךָ וּבְכָל נַפְשְׁךָ וּבְכָל מְאֹדֶֽךָ. וְהָיוּ הַדְּבָרִים הָאֵֽלֶּה אֲשֶׁר אָנֹכִי מְצַוְּךָ הַיּוֹם עַל לְבָבֶֽךָ. וְשִׁנַּנְתָּם לְבָנֶֽיךָ וְדִבַּרְתָּ בָּם, בְּשִׁבְתְּךָ בְּבֵיתֶֽךָ וּבְלֶכְתְּךָ בַדֶּֽרֶךְ וּבְשָׁכְבְּךָ וּבְקוּמֶֽךָ. וּקְשַׁרְתָּם לְאוֹת עַל יָדֶֽךָ וְהָיוּ לְטֹטָפֹת בֵּין עֵינֶֽיךָ. וּכְתַבְתָּם עַל מְזֻזוֹת בֵּיתֶֽךָ וּבִשְׁעָרֶֽיךָ.

הַשְׁכִּיבֵֽנוּ יְיָ אֱלֹהֵֽינוּ לְשָׁלוֹם, וְהַעֲמִידֵֽנוּ מַלְכֵּֽנוּ לְחַיִּים, וּפְרֹשׂ עָלֵֽינוּ סֻכַּת שְׁלוֹמֶֽךָ, וְתַקְּנֵֽנוּ בְּעֵצָה טוֹבָה מִלְּפָנֶֽיךָ, וְהוֹשִׁיעֵֽנוּ לְמַֽעַן שְׁמֶֽךָ. וְהָגֵן בַּעֲדֵֽנוּ, וְהָסֵר מֵעָלֵֽינוּ אוֹיֵב, דֶּֽבֶר וְחֶֽרֶב וְרָעָב וְיָגוֹן, וְהָסֵר שָׂטָן מִלְּפָנֵֽינוּ וּמֵאַחֲרֵֽינוּ, וּבְצֵל כְּנָפֶֽיךָ תַּסְתִּירֵֽנוּ, כִּי אֵל שׁוֹמְרֵֽנוּ וּמַצִּילֵֽנוּ אָֽתָּה, כִּי אֵל מֶֽלֶךְ חַנּוּן וְרַחוּם אָֽתָּה. וּשְׁמֹר צֵאתֵֽנוּ וּבוֹאֵֽנוּ לְחַיִּים וּלְשָׁלוֹם מֵעַתָּה וְעַד עוֹלָם.

בְּיָדְךָ אַפְקִיד רוּחִי, פָּדִֽיתָ אוֹתִי יְיָ אֵל אֱמֶת.
            """.trimIndent(),
            instructions = "קוראים לפני השינה"
        ),

        // ברכות השחר
        BrachaText(
            id = "modeh_ani",
            name = "מודה אני",
            category = BrachaCategory.MORNING_BRACHOT,
            textAshkenaz = "מוֹדֶה אֲנִי לְפָנֶֽיךָ מֶֽלֶךְ חַי וְקַיָּם, שֶׁהֶחֱזַֽרְתָּ בִּי נִשְׁמָתִי בְּחֶמְלָה, רַבָּה אֱמוּנָתֶֽךָ.",
            instructions = "אומרים מיד עם הקימה בבוקר"
        ),

        BrachaText(
            id = "netilat_yadayim",
            name = "נטילת ידים",
            category = BrachaCategory.MORNING_BRACHOT,
            textAshkenaz = "בָּרוּךְ אַתָּה יְיָ אֱלֹהֵֽינוּ מֶֽלֶךְ הָעוֹלָם, אֲשֶׁר קִדְּשָֽׁנוּ בְּמִצְוֹתָיו וְצִוָּֽנוּ עַל נְטִילַת יָדָֽיִם.",
            instructions = "מברכים אחרי נטילת ידים"
        ),

        BrachaText(
            id = "elohai_neshama",
            name = "אלהי נשמה",
            category = BrachaCategory.MORNING_BRACHOT,
            textAshkenaz = """
אֱלֹהַי, נְשָׁמָה שֶׁנָּתַֽתָּ בִּי טְהוֹרָה הִיא. אַתָּה בְרָאתָהּ, אַתָּה יְצַרְתָּהּ, אַתָּה נְפַחְתָּהּ בִּי, וְאַתָּה מְשַׁמְּרָהּ בְּקִרְבִּי, וְאַתָּה עָתִיד לִטְּלָהּ מִמֶּֽנִּי וּלְהַחֲזִירָהּ בִּי לֶעָתִיד לָבֹא. כָּל זְמַן שֶׁהַנְּשָׁמָה בְקִרְבִּי מוֹדֶה אֲנִי לְפָנֶֽיךָ, יְיָ אֱלֹהַי וֵאלֹהֵי אֲבוֹתַי, רִבּוֹן כָּל הַמַּעֲשִׂים, אֲדוֹן כָּל הַנְּשָׁמוֹת. בָּרוּךְ אַתָּה יְיָ, הַמַּחֲזִיר נְשָׁמוֹת לִפְגָרִים מֵתִים.
            """.trimIndent(),
            instructions = "מברכים בבוקר"
        ),

        // ברכת הגומל
        BrachaText(
            id = "hagomel",
            name = "ברכת הגומל",
            category = BrachaCategory.SPECIAL,
            textAshkenaz = "בָּרוּךְ אַתָּה יְיָ אֱלֹהֵֽינוּ מֶֽלֶךְ הָעוֹלָם, הַגּוֹמֵל לְחַיָּבִים טוֹבוֹת, שֶׁגְּמָלַֽנִי כָּל טוֹב.",
            instructions = "מברכים אחרי הצלה מסכנה, נסיעה ארוכה, או יציאה מבית חולים"
        ),

        // שהחיינו
        BrachaText(
            id = "shehecheyanu",
            name = "שהחיינו",
            category = BrachaCategory.SPECIAL,
            textAshkenaz = "בָּרוּךְ אַתָּה יְיָ אֱלֹהֵֽינוּ מֶֽלֶךְ הָעוֹלָם, שֶׁהֶחֱיָֽנוּ וְקִיְּמָֽנוּ וְהִגִּיעָֽנוּ לַזְּמַן הַזֶּה.",
            instructions = "מברכים על פרי חדש, בגד חדש, או אירוע מיוחד"
        )
    )

    override fun getCategories(): List<TextCategory> {
        return listOf(
            TextCategory(
                id = "birkat_hamazon",
                name = "ברכת המזון",
                icon = "restaurant",
                texts = getTextsByCategory(BrachaCategory.BIRKAT_HAMAZON)
            ),
            TextCategory(
                id = "brachot_hanehenin",
                name = "ברכות הנהנין",
                icon = "food",
                texts = getTextsByCategory(BrachaCategory.MEZONOT)
            ),
            TextCategory(
                id = "asher_yatzar",
                name = "אשר יצר",
                icon = "healing",
                texts = getTextsByCategory(BrachaCategory.ASHER_YATZAR)
            ),
            TextCategory(
                id = "tefilat_haderech",
                name = "תפילת הדרך",
                icon = "directions_car",
                texts = getTextsByCategory(BrachaCategory.TEFILAT_HADERECH)
            ),
            TextCategory(
                id = "shema",
                name = "קריאת שמע על המיטה",
                icon = "bedtime",
                texts = getTextsByCategory(BrachaCategory.SHEMA)
            ),
            TextCategory(
                id = "morning",
                name = "ברכות השחר",
                icon = "wb_sunny",
                texts = getTextsByCategory(BrachaCategory.MORNING_BRACHOT)
            ),
            TextCategory(
                id = "special",
                name = "ברכות מיוחדות",
                icon = "star",
                texts = getTextsByCategory(BrachaCategory.SPECIAL)
            )
        )
    }

    override fun getTextsByCategory(category: BrachaCategory): List<BrachaText> {
        return allTexts.filter { it.category == category }
    }

    override fun getTextById(id: String): BrachaText? {
        return allTexts.find { it.id == id }
    }

    override fun searchTexts(query: String): List<BrachaText> {
        return allTexts.filter { text ->
            text.name.contains(query, ignoreCase = true) ||
            text.textAshkenaz.contains(query, ignoreCase = true) ||
            text.instructions?.contains(query, ignoreCase = true) == true
        }
    }

    override fun getCurrentNusach(): Flow<Nusach> {
        return userPreferences.nusach.map { name ->
            try {
                Nusach.valueOf(name)
            } catch (e: Exception) {
                Nusach.ASHKENAZ
            }
        }
    }

    override suspend fun setNusach(nusach: Nusach) {
        userPreferences.setNusach(nusach.name)
    }

    override fun getFontSize(): Flow<Float> {
        return userPreferences.fontSize
    }

    override suspend fun setFontSize(size: Float) {
        userPreferences.setFontSize(size)
    }
}
