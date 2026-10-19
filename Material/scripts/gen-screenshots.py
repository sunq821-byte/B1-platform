"""Generate mock PNG/JPG screenshots for Material seed."""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent / "04-界面截图"

SHOTS = [
    ("book-mgr-ui.png", "png", "Book Manager", "Borrow list screenshot"),
    ("redis-cache-screenshot.png", "png", "Redis Benchmark", "QPS 4200 / hit 96%"),
    ("vue-component-preview.jpg", "jpg", "Vue3 Components", "BaseButton demo"),
    ("servlet-session-run.jpg", "jpg", "Servlet Session", "Login session demo"),
]


def render(path: Path, title: str, subtitle: str, fmt: str) -> None:
    w, h = 960, 540
    img = Image.new("RGB", (w, h), (245, 247, 250))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, w, 72), fill=(22, 119, 255))
    try:
        font_l = ImageFont.truetype("msyh.ttc", 22)
        font_s = ImageFont.truetype("msyh.ttc", 14)
    except OSError:
        font_l = ImageFont.load_default()
        font_s = ImageFont.load_default()
    draw.text((24, 20), title, fill=(255, 255, 255), font=font_l)
    draw.text((24, 120), subtitle, fill=(100, 116, 139), font=font_s)
    draw.rectangle((24, 180, w - 24, h - 60), outline=(203, 213, 225), width=2)
    draw.text((24, h - 40), "Hengji demo screenshot", fill=(148, 163, 184), font=font_s)
    if fmt == "png":
        img.save(path, format="PNG", optimize=True)
    else:
        img.save(path, format="JPEG", quality=90)


def main() -> None:
    ROOT.mkdir(parents=True, exist_ok=True)
    for name, fmt, title, subtitle in SHOTS:
        target = ROOT / name
        render(target, title, subtitle, fmt)
        print(f"{target.name}: {target.stat().st_size} bytes")


if __name__ == "__main__":
    main()
