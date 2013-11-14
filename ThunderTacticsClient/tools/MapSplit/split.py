#!/usr/bin/env python2

import os, shutil, Image

mapPoints = 1024
mapTexture = 8192
split = 16
heightDir = "../../models/map"
textureDir = "../../textures/map"

def recreate(d):
    try:
        shutil.rmtree(d)
    except:
        pass
    os.mkdir(d)

def splitHeight():
    data = open("height.raw").read()
    data += "\0\0" # Adding two for comfort.

    tileSize = mapPoints / split
    tileSeg = tileSize + 1
    for y in xrange(split):
        for x in xrange(split):
            f = open(heightDir + "/tile%d_%d.raw" % (x, y), "w")
            for k in xrange(tileSeg):

                # Repeat the last line.
                if y == split-1 and k == tileSeg-1:
                    k2 = k - 1
                else:
                    k2 = k

                offset = ((y*tileSize+k2)*mapPoints*2 + x*tileSize*2)
                writeData = data[ offset : offset+tileSeg*2 ]

                # Repeat the last column.
                if x == split-1:
                    writeData = writeData[:-2]
                    writeData += writeData[-2:]

                f.write(writeData)
            f.close()

def splitImage(image, type_, mapName, ext):
    big = Image.open(image)
    tileSize = mapTexture / split

    tile = Image.new("RGB", (tileSize, tileSize))
    
    for y in xrange(split):
        for x in xrange(split):
            tile.paste(big, (-x*tileSize, -y*tileSize))
            name = textureDir + "/%s_%d_%d.%s" % (mapName, x, y, ext)
            if type_ == "JPEG":
                tile.save(name, type_, quality=95)
            else:
                tile.save(name, type_)

recreate(heightDir)
splitHeight()

recreate(textureDir)
splitImage("texture.jpg", "JPEG", "diffuse", "jpg")
splitImage("specular.png", "PNG", "specular", "png")
splitImage("normal.png", "PNG", "normal", "png")
