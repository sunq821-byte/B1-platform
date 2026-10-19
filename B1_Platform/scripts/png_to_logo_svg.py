"""Convert logo.png to SVG with transparent background."""

from __future__ import annotations

import sys
from pathlib import Path

import cv2
import numpy as np
from PIL import Image


def simplify_contour(contour: np.ndarray, epsilon_ratio: float) -> np.ndarray:
    peri = cv2.arcLength(contour, True)
    return cv2.approxPolyDP(contour, epsilon_ratio * peri, True)


def contour_to_path(contour: np.ndarray) -> str:
    points = contour.reshape(-1, 2)
    if len(points) < 2:
        return ""
    x0, y0 = points[0]
    parts = [f"M {x0:.1f} {y0:.1f}"]
    for x, y in points[1:]:
        parts.append(f"L {x:.1f} {y:.1f}")
    parts.append("Z")
    return " ".join(parts)


def build_mask(arr: np.ndarray, channel: str) -> np.ndarray:
    r = arr[:, :, 0]
    g = arr[:, :, 1]
    b = arr[:, :, 2]
    a = arr[:, :, 3]
    if channel == "blue":
        mask = (b > 200) & (r < 40) & (g > 70) & (g < 130) & (a > 128)
    elif channel == "red":
        mask = (r > 200) & (g < 100) & (b < 100) & (a > 128)
    else:
        raise ValueError(channel)
    return (mask.astype(np.uint8) * 255)


def paths_from_mask(
    mask: np.ndarray,
    fill: str,
    epsilon_ratio: float,
    min_area: int,
) -> list[str]:
    contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)
    paths: list[str] = []
    for contour in sorted(contours, key=cv2.contourArea, reverse=True):
        area = cv2.contourArea(contour)
        if area < min_area:
            continue
        simplified = simplify_contour(contour, epsilon_ratio)
        path_d = contour_to_path(simplified)
        if path_d:
            paths.append(f'  <path fill="{fill}" d="{path_d}" />')
    return paths


def crop_viewbox(mask: np.ndarray, padding: int = 8) -> tuple[int, int, int, int]:
    ys, xs = np.where(mask > 0)
    x0 = max(int(xs.min()) - padding, 0)
    y0 = max(int(ys.min()) - padding, 0)
    x1 = min(int(xs.max()) + padding + 1, mask.shape[1])
    y1 = min(int(ys.max()) + padding + 1, mask.shape[0])
    return x0, y0, x1 - x0, y1 - y0


def main() -> int:
    src = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[2] / "images" / "logo.png"
    dst = Path(sys.argv[2]) if len(sys.argv) > 2 else Path(__file__).resolve().parents[1] / "public" / "logo.svg"

    arr = np.array(Image.open(src).convert("RGBA"))
    blue_mask = build_mask(arr, "blue")
    red_mask = build_mask(arr, "red")

    kernel = np.ones((3, 3), np.uint8)
    blue_mask = cv2.morphologyEx(blue_mask, cv2.MORPH_CLOSE, kernel, iterations=2)
    red_mask = cv2.morphologyEx(red_mask, cv2.MORPH_CLOSE, kernel, iterations=1)

    combined = cv2.bitwise_or(blue_mask, red_mask)
    x0, y0, vw, vh = crop_viewbox(combined, padding=12)

    blue_paths = paths_from_mask(blue_mask, "#1677FF", 0.0025, min_area=500)
    red_paths = paths_from_mask(red_mask, "#E03C31", 0.002, min_area=80)

    svg_lines = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="{x0} {y0} {vw} {vh}" fill="none">',
        '  <title>衡迹</title>',
        *blue_paths,
        *red_paths,
        "</svg>",
    ]
    dst.write_text("\n".join(svg_lines) + "\n", encoding="utf-8")
    print(f"Wrote {dst} viewBox=({x0},{y0},{vw},{vh}) blue={len(blue_paths)} red={len(red_paths)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
