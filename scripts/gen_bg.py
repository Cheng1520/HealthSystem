# -*- coding: utf-8 -*-
"""生成医疗风格背景图（渐变 + 心电波形 + 装饰圆点）"""
from PIL import Image, ImageDraw, ImageFilter
import math, os, random

OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "images")
os.makedirs(OUT, exist_ok=True)

# 配色（浅色柔和，配合 FlatLaf Light）
PALETTES = {
    "login":  ((0xF0, 0xF6, 0xFF), (0xDF, 0xEE, 0xFB), (0x2F, 0x80, 0xED)),
    "main":   ((0xF2, 0xF7, 0xFF), (0xE2, 0xF2, 0xF6), (0x0F, 0xB5, 0x9A)),
    "panel":  ((0xF5, 0xF9, 0xFF), (0xEA, 0xF4, 0xF8), (0x8B, 0x5C, 0xF6)),
}


def make_bg(name, w, h, c1, c2, accent):
    img = Image.new("RGB", (w, h), c1)
    d = ImageDraw.Draw(img)

    # 垂直渐变
    for y in range(h):
        t = y / h
        r = int(c1[0] + (c2[0] - c1[0]) * t)
        g = int(c1[1] + (c2[1] - c1[1]) * t)
        b = int(c1[2] + (c2[2] - c1[2]) * t)
        d.line([(0, y), (w, y)], fill=(r, g, b))

    random.seed(42)
    # 半透明装饰圆点
    overlay = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    od = ImageDraw.Draw(overlay)
    for _ in range(26):
        x = random.randint(0, w)
        y = random.randint(0, h)
        r = random.randint(30, 160)
        alpha = random.randint(10, 34)
        color = accent + (alpha,)
        od.ellipse([x - r, y - r, x + r, y + r], fill=color)

    # 心电波形（右下角一条折线）
    wave_y = int(h * 0.78)
    x = 0
    wave_color = accent + (70,)
    points = []
    while x < w:
        points.append((x, wave_y))
        x += random.randint(60, 120)
        points.append((x, wave_y - random.randint(20, 70)))
        x += random.randint(30, 50)
        points.append((x, wave_y))
    if len(points) >= 2:
        od.line(points, fill=wave_color, width=4, joint="curve")

    # 大十字（医疗符号）左上角
    cx, cy = int(w * 0.12), int(h * 0.16)
    s = 60
    od.rounded_rectangle([cx - s, cy - s // 3, cx + s, cy + s // 3], radius=12, fill=accent + (26,))
    od.rounded_rectangle([cx - s // 3, cy - s, cx + s // 3, cy + s], radius=12, fill=accent + (26,))

    img = Image.alpha_composite(img.convert("RGBA"), overlay).convert("RGB")
    img = img.filter(ImageFilter.GaussianBlur(1.2))
    path = os.path.join(OUT, f"{name}.png")
    img.save(path, "PNG")
    print("saved", path, img.size)


make_bg("bg_login", 900, 700, *PALETTES["login"])
make_bg("bg_main", 1600, 1000, *PALETTES["main"])
make_bg("bg_panel", 1600, 1000, *PALETTES["panel"])
print("done")
