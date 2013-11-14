#!/usr/bin/env python2

import subprocess, os

def add(p):
    ret = "l%f,%f" % (p[0], p[1])
    ret += "q%f,%f %f,%f" % (p[2], p[3], p[4], p[5])
    ret += "q%f,%f %f,%f" % (p[6], p[7], p[8], p[9])
    ret += "l%f,%f" % (p[10], p[11])
    return ret

def drawPath(padding, fill, curveLength, curveHeight, size, direction):
    svg = "<path d='"

    cornerPos = padding
    straight = size/2.0 - cornerPos - curveLength
    
    svg += "M%f,%f" % (cornerPos, cornerPos)

    p = [
        straight, 0,
        curveLength, 0, curveLength, -curveHeight,
        0, curveHeight, curveLength, curveHeight,
        straight, 0
    ]
    p2, p3, p4 = [], [], []

    for i in xrange(0, len(p), 2):
        p[i+1] *= direction

        p2.append(-p[i+1])
        p2.append(p[i])

        p3.append(-p[i])
        p3.append(-p[i+1])

        p4.append(p[i+1])
        p4.append(-p[i])

    svg += add(p)
    svg += add(p2)
    svg += add(p3)
    svg += add(p4)

    svg += "z' style='fill:%s'/>" % fill
    return svg

def genSvg(fileName, stroke, fill, curveLength=8, curveHeight=4, size=64,\
        padding=4, strokeWidth=2, background="#000", direction=1, drawX=False,
        xPadding=14, xWidth=12, prefix=""):
    svg = "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 %d %d'>" % (size, size)
    svg += "<rect x='0' y='0' height='%d' width='%d' style='fill:%s'/>" % (size, size, background)

    svg += prefix

    if drawX:
        a, b = xPadding, size - xPadding
        svg += ("<path d='M%f,%f L%f,%f M%f,%f L%f,%f' style='stroke:%s; " + \
                "stroke-width:%fpx; stroke-linecap:round'/>") % (a, a, b, b, b, a, a, b, stroke, xWidth)
    else:
        svg += drawPath(padding, stroke, curveLength, curveHeight, size, direction)
        svg += drawPath(padding + strokeWidth, fill, curveLength, curveHeight, size, direction)

    svg += "</svg>"

    out = open(fileName, "w")
    out.write(svg)
    out.close()

def rgbToHex(rgb):
    return "#%02x%02x%02x" % rgb 

def multRgb(rgb, mult):
    return (rgb[0] * mult, rgb[1] * mult, rgb[2] * mult)

def create():
    outDir = "../../textures"
    globalMult = 0.7

    players = [
        ["neutral", (150, 150, 150)],
        ["player1", (255,   0,   0)],
        ["player2", (  0,   0, 255)],
        ["player3", (255, 255,   0)],
        ["player4", (255,   0, 255)]
    ]

    types = [
        ["normal",   8, 4,  2,  1, 0.4, False,  4],
        ["movable",  8, 16, 2,  1, 0.4, False, 16],
        ["selected", 8, 8,  3, -1, 1.0, False,  4],
        ["unmoved",  8, 4,  6,  1, 0.2, False,  4],
        ["attacked", 8, 4,  2,  1, 0.4,  True,  4]
    ]

    pneutral = players[0]
    tnormal = types[0]
    pstroke = rgbToHex(multRgb(pneutral[1], globalMult))
    pfill = rgbToHex(multRgb(pneutral[1], globalMult * tnormal[5]))
    prefix = drawPath(tnormal[7], pstroke, tnormal[1], tnormal[2], 64, 1) \
            + drawPath(tnormal[7] + tnormal[3], pfill, tnormal[1], tnormal[2], 64, 1)

    for p in players:
        for t in types:
            stroke = rgbToHex(multRgb(p[1], globalMult))
            fill = rgbToHex(multRgb(p[1], globalMult * t[5]))
            fileName = os.path.join(outDir, "cell-" + p[0] + "-" + t[0] + ".png")
            pre = prefix if t[0] in ("movable", "attacked") else "" 
            svg = genSvg("out.svg", stroke, fill, curveLength=t[1], \
                    curveHeight=t[2], strokeWidth=t[3], direction=t[4], \
                    drawX=t[6], padding=t[7], prefix=pre)
            subprocess.call(["rsvg-convert", "out.svg", "-o", fileName])
    os.remove("out.svg")
create()
