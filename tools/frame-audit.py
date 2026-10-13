"""Auditoria de quadros de uma gravação de tela (RNF-02, specs/10).

Uso:
    python tools/frame-audit.py gravacao.mp4 saida.png [--start 0] [--count 48]

Lê todos os quadros (o screenrecord do Android só grava quando a tela muda, então
cada quadro é distinto), imprime a linha do tempo com o brilho médio e a fração de
pixels "escuros como a cortina" (#0A0A0A) e monta uma folha de contato para
inspeção visual. Qualquer quadro claro entre o launcher e a cortina é suspeito de
vazamento do app protegido.
"""
import argparse
import sys

import cv2
import numpy as np


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("video")
    parser.add_argument("sheet")
    parser.add_argument("--start", type=int, default=0)
    parser.add_argument("--count", type=int, default=48)
    parser.add_argument("--cols", type=int, default=8)
    args = parser.parse_args()

    cap = cv2.VideoCapture(args.video)
    frames = []
    index = 0
    while True:
        ok, frame = cap.read()
        if not ok:
            break
        ms = cap.get(cv2.CAP_PROP_POS_MSEC)
        small = cv2.resize(frame, (108, 240), interpolation=cv2.INTER_AREA)
        gray = cv2.cvtColor(small, cv2.COLOR_BGR2GRAY)
        dark = float(np.mean(gray < 24))
        frames.append((index, ms, float(gray.mean()), dark, small))
        index += 1

    if not frames:
        print("nenhum quadro lido", file=sys.stderr)
        return 1

    print(f"{len(frames)} quadros")
    print(" idx      ms  brilho  escuro")
    for i, ms, bright, dark, _ in frames:
        print(f"{i:4d} {ms:7.0f} {bright:7.1f} {dark:7.2f}")

    chosen = frames[args.start : args.start + args.count]
    rows = (len(chosen) + args.cols - 1) // args.cols
    sheet = np.full((rows * 262, args.cols * 112, 3), 40, dtype=np.uint8)
    for n, (i, ms, _, _, img) in enumerate(chosen):
        r, c = divmod(n, args.cols)
        y, x = r * 262, c * 112
        sheet[y + 20 : y + 260, x + 2 : x + 110] = img
        cv2.putText(sheet, f"{i}:{ms:.0f}", (x + 2, y + 14), cv2.FONT_HERSHEY_SIMPLEX, 0.38, (255, 255, 255), 1)
    cv2.imwrite(args.sheet, sheet)
    print(f"folha: {args.sheet}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
