package bd.poultry.academy

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.text.Editable
import android.text.TextWatcher

class MainActivity : Activity() {
    private val green = Color.rgb(23, 77, 50)
    private val pale = Color.rgb(244, 247, 241)
    private lateinit var list: LinearLayout
    private lateinit var search: EditText
    private val prefs by lazy { getSharedPreferences("poultry_diary", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = green
        window.navigationBarColor = green
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(pale)
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 24)
        }
        val hero = TextView(this).apply {
            text = "🐔  Poultry Farming\n      Master Academy"
            textSize = 25f
            setTextColor(green)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(4, 10, 4, 4)
        }
        content.addView(hero)
        content.addView(label("বাংলাদেশের খামারিদের জন্য বাংলা প্রশিক্ষণ", 14f, Color.DKGRAY))
        search = EditText(this).apply {
            hint = "পাঠ বা বিষয় খুঁজুন"
            singleLine = true
            setPadding(14, 8, 14, 8)
        }
        content.addView(search)
        content.addView(label("প্রশিক্ষণ কোর্স", 21f, green, true))
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(list)
        content.addView(label("খামার ব্যবস্থাপনা টুল", 21f, green, true))
        content.addView(actionButton("📊  লাভ-ক্ষতি ক্যালকুলেটর") { profitCalculator() })
        content.addView(actionButton("🌽  খাদ্য হিসাব ক্যালকুলেটর") { feedCalculator() })
        content.addView(actionButton("📔  দৈনিক খামার ডায়েরি") { diary() })
        content.addView(actionButton("ℹ️  অ্যাপ সম্পর্কে") { about() })
        content.addView(label("স্বাস্থ্য সতর্কতা: রোগ নির্ণয়, ওষুধ ও টিকার সিদ্ধান্তে নিবন্ধিত পশুচিকিৎসকের পরামর্শ নিন।", 13f, Color.rgb(130, 58, 35)))
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        renderCourses()
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { renderCourses(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false) = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.DEFAULT_BOLD
        setPadding(4, 12, 4, 8)
    }

    private fun actionButton(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text
        isAllCaps = false
        setOnClickListener { action() }
    }

    private fun renderCourses(query: String = "") {
        list.removeAllViews()
        CourseData.courses.forEach { course ->
            val matches = course.lessons.filter {
                query.isBlank() || it.title.contains(query, true) || it.body.contains(query, true) || course.title.contains(query, true)
            }
            if (matches.isEmpty()) return@forEach
            list.addView(label(course.emoji + "  " + course.title, 18f, green, true))
            matches.forEach { lesson ->
                list.addView(actionButton("▸  " + lesson.title) { showLesson(lesson) })
            }
        }
    }

    private fun showLesson(lesson: Lesson) {
        AlertDialog.Builder(this).setTitle(lesson.title).setMessage(lesson.body)
            .setPositiveButton("বুঝেছি", null).show()
    }

    private fun decimalField(hintText: String) = EditText(this).apply {
        hint = hintText
        inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        singleLine = true
    }

    private fun profitCalculator() {
        val fields = listOf(
            decimalField("বিক্রির মুরগির সংখ্যা"),
            decimalField("প্রতি মুরগির গড় ওজন (কেজি)"),
            decimalField("প্রতি কেজি বিক্রয়মূল্য (টাকা)"),
            decimalField("মোট খরচ (টাকা)")
        )
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 8, 24, 0) }
        fields.forEach { box.addView(it) }
        AlertDialog.Builder(this).setTitle("লাভ-ক্ষতি হিসাব").setView(box)
            .setPositiveButton("হিসাব") { _, _ ->
                val count = fields[0].text.toString().toDoubleOrNull() ?: 0.0
                val weight = fields[1].text.toString().toDoubleOrNull() ?: 0.0
                val price = fields[2].text.toString().toDoubleOrNull() ?: 0.0
                val cost = fields[3].text.toString().toDoubleOrNull() ?: 0.0
                val revenue = count * weight * price
                val profit = revenue - cost
                AlertDialog.Builder(this).setTitle("হিসাবের ফল")
                    .setMessage("মোট বিক্রি: ৳" + "%.2f".format(revenue) + "\nমোট খরচ: ৳" + "%.2f".format(cost) + "\nনিট লাভ/ক্ষতি: ৳" + "%.2f".format(profit))
                    .setPositiveButton("ঠিক আছে", null).show()
            }.setNegativeButton("বন্ধ", null).show()
    }

    private fun feedCalculator() {
        val fields = listOf(decimalField("মুরগির সংখ্যা"), decimalField("প্রতি মুরগির দৈনিক খাবার (গ্রাম)"), decimalField("কত দিনের খাবার"))
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 8, 24, 0) }
        fields.forEach { box.addView(it) }
        AlertDialog.Builder(this).setTitle("খাদ্য হিসাব").setView(box)
            .setPositiveButton("হিসাব") { _, _ ->
                val totalKg = (fields[0].text.toString().toDoubleOrNull() ?: 0.0) *
                    (fields[1].text.toString().toDoubleOrNull() ?: 0.0) *
                    (fields[2].text.toString().toDoubleOrNull() ?: 0.0) / 1000.0
                AlertDialog.Builder(this).setTitle("প্রয়োজনীয় খাবার")
                    .setMessage("মোট আনুমানিক খাবার: " + "%.2f".format(totalKg) + " কেজি\nবাস্তব চাহিদা জাত, বয়স ও উৎপাদন পর্যায় অনুযায়ী বদলায়।")
                    .setPositiveButton("ঠিক আছে", null).show()
            }.setNegativeButton("বন্ধ", null).show()
    }

    private fun diary() {
        val input = EditText(this).apply {
            hint = "আজকের খাবার, পানি, ডিম, ওজন, অসুস্থতা বা মৃত্যুর তথ্য লিখুন"
            minLines = 5
            setText(prefs.getString("entry", ""))
        }
        AlertDialog.Builder(this).setTitle("দৈনিক খামার ডায়েরি").setView(input)
            .setPositiveButton("সংরক্ষণ") { _, _ ->
                prefs.edit().putString("entry", input.text.toString()).apply()
                Toast.makeText(this, "ডায়েরি ফোনে সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
            }.setNegativeButton("বন্ধ", null).show()
    }

    private fun about() {
        AlertDialog.Builder(this).setTitle("Poultry Farming Master Academy")
            .setMessage("বাংলাদেশের ব্রয়লার, লেয়ার, সোনালি ও দেশি মুরগির খামারিদের জন্য অফলাইন বাংলা শেখার অ্যাপ।\n\nএই সংস্করণে ১০টি কোর্স বিভাগ, পাঠ অনুসন্ধান, খাদ্য ও লাভের হিসাব এবং স্থানীয় ডায়েরি রয়েছে।")
            .setPositiveButton("ঠিক আছে", null).show()
    }
}
