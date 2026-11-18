# Poultry Farming Master Academy

বাংলাদেশের খামারিদের জন্য বাংলা ভাষার অফলাইন পোল্ট্রি প্রশিক্ষণ ও ব্যবস্থাপনা Android অ্যাপ।

## বর্তমান সংস্করণ
প্রাথমিক Android MVP: ১০টি প্রশিক্ষণ বিভাগ, অফলাইন পাঠ, পাঠ অনুসন্ধান, ব্রয়লার/লেয়ার/সোনালি/দেশি খামার বিষয়ক গাইড, লাভ-ক্ষতি ও খাদ্য হিসাবের টুল, দৈনিক খামার নোট এবং স্থানীয়ভাবে সংরক্ষিত ডেটা।

## Android অ্যাপ তৈরি
প্রয়োজন: JDK 17 এবং Android SDK (API 35)।
```bash
gradle assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions-এ push হলেই Android debug APK build হবে। Actions-এর artifact থেকে APK ডাউনলোড করা যাবে।

## বিষয়বস্তু ও নিরাপত্তা
- প্রশিক্ষণ তথ্য শিক্ষামূলক; রোগ নির্ণয়, ওষুধ বা টিকার বিকল্প নয়।
- রোগের লক্ষণ, টিকা ও চিকিৎসার ক্ষেত্রে বাংলাদেশ প্রাণিসম্পদ অধিদপ্তর/উপজেলা প্রাণিসম্পদ কর্মকর্তা এবং নিবন্ধিত ভেটেরিনারিয়ানের সর্বশেষ নির্দেশনা অনুসরণ করুন।
- খাদ্য, ওষুধ, বাচ্চা ও বিক্রয়মূল্য স্থান, সময় ও সরবরাহকারীভেদে বদলায়। ক্যালকুলেটরে নিজের প্রকৃত দাম লিখুন।
- এই repository-তে কেবল লাইসেন্স-অনুমোদিত ছবি যোগ করুন। ছবি লাইব্রেরির উৎস ও লাইসেন্স প্রতিটি ছবির সঙ্গে নথিভুক্ত করুন।

## Roadmap
1. MVP Android app and offline lesson library.
2. Expand reviewed Bangla lessons and Bangladesh-specific references.
3. Add licensed, captioned farm photographs and original diagrams.
4. Add reminders, export/backup, quizzes and richer farm records.

## License
Source code: MIT. Third-party learning content and media retain their own licenses; see `THIRD_PARTY_ASSETS.md`.
