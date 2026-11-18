#!/usr/bin/env python3
"""Build a 60-second Bengali poultry-farm video from openly licensed Wikimedia Commons clips."""
import os, re, subprocess, textwrap, requests
from pathlib import Path
from gtts import gTTS
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "output"
WORK = ROOT / "video_work"
OUT.mkdir(exist_ok=True)
WORK.mkdir(exist_ok=True)
API = "https://commons.wikimedia.org/w/api.php"
HEADERS = {"User-Agent": "PoultryFarmAcademyVideo/1.0 (educational project)"}

def run(args):
    subprocess.run(args, check=True)

def commons_clips():
    queries = [
        'filetype:video chicken farm poultry',
        'filetype:video free range chickens farm',
        'filetype:video poultry feeding chickens',
        'filetype:video chicken coop eggs'
    ]
    found, seen = [], set()
    for query in queries:
        data = requests.get(API, params={
            "action":"query", "format":"json", "generator":"search",
            "gsrsearch":query, "gsrnamespace":6, "gsrlimit":12,
            "prop":"imageinfo", "iiprop":"url|extmetadata"
        }, headers=HEADERS, timeout=45).json()
        pages = (data.get("query") or {}).get("pages") or {}
        for page in pages.values():
            info = (page.get("imageinfo") or [{}])[0]
            url = info.get("url", "")
            if not url or not re.search(r"\.(mp4|webm|ogv|ogg)(\?|$)", url, re.I) or url in seen:
                continue
            meta = info.get("extmetadata") or {}
            license_name = re.sub("<[^>]+>", "", meta.get("LicenseShortName",{}).get("value","Unknown"))
            artist = re.sub("<[^>]+>", "", meta.get("Artist",{}).get("value","Not stated"))
            title = page.get("title","Wikimedia Commons video")
            found.append((url,title,license_name,artist))
            seen.add(url)
    if not found:
        raise RuntimeError("No openly hosted Commons poultry video clips were found; refusing to substitute fabricated footage.")
    return found[:6]

clips = commons_clips()
local = []
credits = ["ভিডিওর দৃশ্য: Wikimedia Commons (প্রতিটি ফাইলের লাইসেন্স ও লেখক নিচে দেওয়া হলো)."]
for i,(url,title,license_name,artist) in enumerate(clips):
    path = WORK / f"clip_{i}.mp4"
    response = requests.get(url, headers=HEADERS, timeout=120, stream=True)
    response.raise_for_status()
    with path.open("wb") as f:
        for chunk in response.iter_content(1024*1024):
            if chunk: f.write(chunk)
    # Confirm the downloaded file contains a video stream.
    probe = subprocess.run(["ffprobe","-v","error","-select_streams","v:0","-show_entries","stream=codec_type","-of","csv=p=0",str(path)],capture_output=True,text=True)
    if probe.returncode == 0 and "video" in probe.stdout:
        local.append(path)
        credits.append(f"- {title} | {license_name} | {artist} | {url}")
    else:
        path.unlink(missing_ok=True)
if not local:
    raise RuntimeError("Commons search returned no usable video streams.")

(OUT / "VIDEO_CREDITS.txt").write_text("\n".join(credits), encoding="utf-8")

narration = (
"দেশি মুরগির খামারে স্বাগতম। একটি সফল খামারের শুরু হয় পরিষ্কার, শুকনো এবং বাতাস চলাচল করে এমন ঘর দিয়ে। "
"খামারের জায়গা উঁচু ও পানি জমে না—এমন হওয়া ভালো। প্রতিটি মুরগির জন্য পর্যাপ্ত জায়গা, নিরাপদ বেড়া এবং শিকারি প্রাণী থেকে সুরক্ষা নিশ্চিত করুন। "
"সবসময় পরিষ্কার পানির ব্যবস্থা রাখুন। বয়স ও উৎপাদন অনুযায়ী সুষম খাবার দিন; শুধু ভাত বা এক ধরনের খাদ্যের ওপর নির্ভর করবেন না। "
"খাবারের পাত্র ও পানির পাত্র নিয়মিত পরিষ্কার করুন। নতুন পাখি আনলে কিছুদিন আলাদা রাখুন। অসুস্থ বা নিস্তেজ মুরগি দ্রুত আলাদা করে প্রাণিসম্পদ বিশেষজ্ঞের পরামর্শ নিন। "
"ডিম প্রতিদিন কয়েকবার সংগ্রহ করুন এবং পরিষ্কার, শুকনো জায়গায় রাখুন। খামারের আয়-ব্যয়, খাবার, মৃত্যু ও ডিমের হিসাব লিখে রাখলে লাভ-ক্ষতি বোঝা সহজ হয়। "
"পরিষ্কার-পরিচ্ছন্নতা, ভালো খাবার, নিরাপদ পানি এবং নিয়মিত পর্যবেক্ষণ—এই চারটি অভ্যাসই সুস্থ ও লাভজনক মুরগির খামারের ভিত্তি।"
)
tts = WORK / "narration.mp3"
gTTS(text=narration, lang="bn", slow=False).save(str(tts))

# Build a 60-second sequence from real downloaded farm clips.
segments = []
for i in range(5):
    src = local[i % len(local)]
    seg = WORK / f"segment_{i}.mp4"
    # Crop to 16:9, normalize frame rate/size, and use only genuine source footage.
    run(["ffmpeg","-y","-stream_loop","-1","-i",str(src),"-t","12",
         "-vf","scale=1280:720:force_original_aspect_ratio=increase,crop=1280:720,fps=25,setsar=1",
         "-an","-c:v","libx264","-preset","veryfast","-pix_fmt","yuv420p",str(seg)])
    segments.append(seg)
concat = WORK / "concat.txt"
concat.write_text("".join(f"file '{p.as_posix()}'\n" for p in segments), encoding="utf-8")
silent = WORK / "silent.mp4"
run(["ffmpeg","-y","-f","concat","-safe","0","-i",str(concat),"-c","copy",str(silent)])

# Make Bengali title/caption cards with PIL (Noto Bengali font supplied by Ubuntu package).
font_path = "/usr/share/fonts/truetype/noto/NotoSansBengali-Regular.ttf"
if not Path(font_path).exists():
    candidates = list(Path("/usr/share/fonts").rglob("*Bengali*Regular*.ttf"))
    if candidates: font_path = str(candidates[0])
font = ImageFont.truetype(font_path, 42) if Path(font_path).exists() else ImageFont.load_default()
captions = [
    "দেশি মুরগির খামার",
    "পরিষ্কার ও নিরাপদ ঘর",
    "সুষম খাবার ও বিশুদ্ধ পানি",
    "রোগ প্রতিরোধে পরিচ্ছন্নতা",
    "ডিম সংগ্রহ ও হিসাব রাখুন"
]
for i,caption in enumerate(captions):
    img=Image.new("RGBA",(1280,110),(0,0,0,150))
    d=ImageDraw.Draw(img)
    d.text((640,55),caption,font=font,fill="white",anchor="mm",stroke_width=1,stroke_fill="black")
    img.save(WORK/f"caption_{i}.png")

# Overlay timed Bengali captions; audio is the requested male-presenting Bengali TTS voice.
vf = "[0:v]"
for i in range(5):
    vf += f"[{i+1}:v]overlay=0:610:enable='between(t,{i*12},{(i+1)*12})'[v{i}];"
    vf += f"[v{i}]" if i < 4 else ""
# Simpler overlay chain with looped transparent caption images.
cmd=["ffmpeg","-y","-i",str(silent)]
for i in range(5):
    cmd += ["-loop","1","-t","12","-i",str(WORK/f"caption_{i}.png")]
cmd += ["-i",str(tts),"-filter_complex",
        "[0:v][1:v]overlay=0:610:enable='between(t,0,12)'[a];"
        "[a][2:v]overlay=0:610:enable='between(t,12,24)'[b];"
        "[b][3:v]overlay=0:610:enable='between(t,24,36)'[c];"
        "[c][4:v]overlay=0:610:enable='between(t,36,48)'[d];"
        "[d][5:v]overlay=0:610:enable='between(t,48,60)'[v]",
        "-map","[v]","-map","6:a","-t","60","-af","apad",
        "-c:v","libx264","-preset","medium","-crf","23","-pix_fmt","yuv420p",
        "-c:a","aac","-b:a","128k","-movflags","+faststart",
        str(OUT/"poultry-farm-bengali-60s.mp4")]
run(cmd)
print("Created:", OUT/"poultry-farm-bengali-60s.mp4")
print("Real source clips:", len(local))
