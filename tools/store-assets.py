"""Gera o material gráfico da ficha da Play Store (specs/08 e specs/09).

Uso:
    python tools/store-assets.py <pasta-das-capturas> <pasta-de-saida>

A pasta de capturas precisa ter play_home.png, play_apps.png, play_settings.png e
play_lock.png (1080x2400, tiradas do build de depuração em modo demonstração).
Gera:
    icone-512.png               ícone da loja (512x512, sem cantos: o Google arredonda)
    recurso-grafico-1024x500.png
    captura-1..4.png            capturas de 1080x1920 com legenda
"""
import os
import sys

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FONT_DIR = os.path.join(ROOT, "app", "src", "main", "res", "font")
BG = (10, 10, 10)
GRAD = [(0.0, (0xA1, 0x3B, 0xFF)), (0.55, (0x6A, 0x3D, 0xF0)), (1.0, (0x00, 0xC8, 0xE0))]


def font(weight: str, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(os.path.join(FONT_DIR, f"montserrat_{weight}.ttf"), size)


def gradient(w: int, h: int) -> Image.Image:
    """Gradiente linear da marca no eixo (8,6) -> (88,90) do viewBox 96."""
    img = Image.new("RGB", (w, h))
    px = img.load()
    ax, ay, bx, by = 8 / 96 * w, 6 / 96 * h, 88 / 96 * w, 90 / 96 * h
    dx, dy = bx - ax, by - ay
    norm = dx * dx + dy * dy
    for y in range(h):
        for x in range(w):
            t = max(0.0, min(1.0, ((x - ax) * dx + (y - ay) * dy) / norm))
            for (t0, c0), (t1, c1) in zip(GRAD, GRAD[1:]):
                if t <= t1:
                    k = (t - t0) / (t1 - t0)
                    px[x, y] = tuple(int(c0[i] + (c1[i] - c0[i]) * k) for i in range(3))
                    break
    return img


def glyph_mask(size: int, scale: float = 1.0, offset=(0.0, 0.0)) -> Image.Image:
    """Símbolo V + fechadura (brand/vedra-glyph-mono.svg) como máscara."""
    ss = 4
    s = size * ss / 96 * scale
    ox, oy = offset[0] * size * ss, offset[1] * size * ss
    m = Image.new("L", (size * ss, size * ss), 0)
    d = ImageDraw.Draw(m)

    def p(x, y):
        return (ox + x * s, oy + y * s)

    d.polygon([p(20, 24), p(33.5, 24), p(48, 56.5), p(62.5, 24), p(76, 24), p(55, 74), p(41, 74)], fill=255)
    cx, cy = p(48, 33)
    r = 6.5 * s
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=255)
    d.polygon([p(45.6, 36), p(50.4, 36), p(51.8, 47), p(44.2, 47)], fill=255)
    return m.resize((size, size), Image.LANCZOS)


def icon(size: int, rounded: bool) -> Image.Image:
    base = gradient(size, size).convert("RGBA")
    white = Image.new("RGBA", (size, size), (255, 255, 255, 255))
    base.paste(white, (0, 0), glyph_mask(size))
    if rounded:
        mask = Image.new("L", (size * 4, size * 4), 0)
        ImageDraw.Draw(mask).rounded_rectangle([0, 0, size * 4 - 1, size * 4 - 1], radius=size, fill=255)
        base.putalpha(mask.resize((size, size), Image.LANCZOS))
    return base


def aura(w: int, h: int, cx: float, cy: float, r: int) -> Image.Image:
    layer = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(0x8A, 0x2C, 0xE2, 90))
    d.ellipse([cx - r * 0.2, cy - r * 0.6, cx + r * 1.1, cy + r * 0.7], fill=(0x00, 0xE5, 0xFF, 50))
    return layer.filter(ImageFilter.GaussianBlur(r * 0.45))


def feature_graphic(path: str) -> None:
    w, h = 1024, 500
    img = Image.new("RGBA", (w, h), BG + (255,))
    img.alpha_composite(aura(w, h, 250, 250, 200))
    ic = icon(220, rounded=True)
    img.alpha_composite(ic, (140, 140))
    d = ImageDraw.Draw(img)
    d.text((420, 150), "Vedra", font=font("extrabold", 112), fill=(242, 242, 242))
    d.text((424, 290), "Seus apps, só para você.", font=font("semibold", 38), fill=(176, 124, 255))
    d.text((424, 348), "PIN ou digital · sem internet · sem anúncios", font=font("semibold", 24), fill=(154, 154, 154))
    img.convert("RGB").save(path)


def framed(shot: str, caption: str, path: str) -> None:
    w, h = 1080, 1920
    img = Image.new("RGBA", (w, h), BG + (255,))
    img.alpha_composite(aura(w, h, w / 2, 520, 420))
    d = ImageDraw.Draw(img)
    f = font("bold", 64)
    lines = caption.split("\n")
    y = 90
    for line in lines:
        tw = d.textlength(line, font=f)
        d.text(((w - tw) / 2, y), line, font=f, fill=(242, 242, 242))
        y += 82
    src = Image.open(shot).convert("RGB")
    sh = h - y - 90
    sw = int(src.width * sh / src.height)
    src = src.resize((sw, sh), Image.LANCZOS)
    mask = Image.new("L", (sw, sh), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, sw - 1, sh - 1], radius=48, fill=255)
    x = (w - sw) // 2
    d.rounded_rectangle([x - 6, y + 34, x + sw + 5, y + 40 + sh + 5], radius=54, fill=(42, 42, 42))
    img.paste(src, (x, y + 40), mask)
    img.convert("RGB").save(path)


def main() -> int:
    shots, out = sys.argv[1], sys.argv[2]
    os.makedirs(out, exist_ok=True)
    icon(512, rounded=False).convert("RGB").save(os.path.join(out, "icone-512.png"))
    feature_graphic(os.path.join(out, "recurso-grafico-1024x500.png"))
    captions = [
        ("play_home.png", "Veja na hora se seus\napps estão protegidos"),
        ("play_lock.png", "PIN ou digital antes\nde qualquer conteúdo"),
        ("play_apps.png", "Escolha quais apps\nsó você pode abrir"),
        ("play_settings.png", "Rebloqueio do seu jeito"),
    ]
    for i, (name, caption) in enumerate(captions, 1):
        framed(os.path.join(shots, name), caption, os.path.join(out, f"captura-{i}.png"))
    print("ok:", sorted(os.listdir(out)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
