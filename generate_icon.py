"""
Run this script once to generate a minimal ic_launcher.png for the mipmap-hdpi folder.
Requires Pillow: pip install Pillow

Usage: python generate_icon.py
"""
try:
    from PIL import Image, ImageDraw, ImageFont
    img = Image.new("RGBA", (72, 72), (26, 35, 126, 255))  # #1A237E
    draw = ImageDraw.Draw(img)
    draw.text((8, 20), "DPC", fill=(255, 255, 255, 255))
    img.save("app/src/main/res/mipmap-hdpi/ic_launcher.png")
    print("Icon generated.")
except ImportError:
    print("Pillow not installed — use any 72x72 PNG named ic_launcher.png in mipmap-hdpi/")
