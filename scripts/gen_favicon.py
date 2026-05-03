"""Genera favicon multi-tamaño para D5 (color amber #f59e0b, letra '5')."""
from PIL import Image, ImageDraw, ImageFont
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "app" / "src" / "main" / "resources" / "static"
OUT.mkdir(parents=True, exist_ok=True)

BG = (245, 158, 11, 255)  # #f59e0b
FG = (255, 255, 255, 255)

def make_png(size: int) -> Image.Image:
    img = Image.new("RGBA", (size, size), BG)
    draw = ImageDraw.Draw(img)
    # circle border for nicer look at large sizes
    if size >= 64:
        draw.ellipse([(0, 0), (size - 1, size - 1)], fill=BG)
    text = "5"
    # try a bold font, fall back to default
    font = None
    for candidate in [
        "C:/Windows/Fonts/arialbd.ttf",
        "C:/Windows/Fonts/arial.ttf",
        "C:/Windows/Fonts/segoeuib.ttf",
    ]:
        try:
            font = ImageFont.truetype(candidate, int(size * 0.7))
            break
        except OSError:
            continue
    if font is None:
        font = ImageFont.load_default()
    bbox = draw.textbbox((0, 0), text, font=font)
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    x = (size - tw) // 2 - bbox[0]
    y = (size - th) // 2 - bbox[1]
    draw.text((x, y), text, fill=FG, font=font)
    return img


def main():
    sizes = [16, 32, 48, 64, 128, 256]
    pngs = [make_png(s) for s in sizes]
    # 256 png standalone
    pngs[-1].save(OUT / "favicon-256.png")
    # multi-size .ico (16/32/48/64)
    pngs[0].save(OUT / "favicon.ico", sizes=[(16, 16), (32, 32), (48, 48), (64, 64)])
    # svg (simple square + 5)
    svg = (
        '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64">'
        '<rect width="64" height="64" rx="10" fill="#f59e0b"/>'
        '<text x="32" y="46" font-family="Arial,Helvetica,sans-serif" font-weight="bold" '
        'font-size="44" fill="#ffffff" text-anchor="middle">5</text></svg>'
    )
    (OUT / "favicon.svg").write_text(svg, encoding="utf-8")
    print(f"favicon written to {OUT}")


if __name__ == "__main__":
    main()
