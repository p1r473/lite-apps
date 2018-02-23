#!/bin/bash
FONTS_DIR=basic-fonts
# Remove existing downloaded fonts (both zip and directory)
rm -Rf "${FONTS_DIR}.zip" "${FONTS_DIR}"
mkdir -p "${FONTS_DIR}"
# Fetch all the font TTFs again, because fonts are constantly being updated by their authors.
# This is the canonical list of fonts; there is no other list. This is intentional to keep things simple and consistent.
wget --directory-prefix=${FONTS_DIR} \
    https://github.com/google/fonts/raw/master/apache/opensans/OpenSans-Regular.ttf  \
    https://github.com/google/fonts/raw/master/apache/roboto/Roboto-Regular.ttf \
    https://github.com/google/fonts/raw/master/apache/robotomono/RobotoMono-Medium.ttf \
    https://github.com/google/fonts/raw/master/ofl/alegreya/Alegreya-Regular.ttf \
    https://github.com/google/fonts/raw/master/ofl/cabin/Cabin-Regular.ttf \
    https://github.com/google/fonts/raw/master/ofl/firasanscondensed/FiraSansCondensed-Regular.ttf \
    https://github.com/google/fonts/raw/master/ofl/hammersmithone/HammersmithOne-Regular.ttf \
    https://github.com/google/fonts/raw/master/ofl/lora/Lora-Regular.ttf \
    https://github.com/google/fonts/raw/master/ofl/montserrat/Montserrat-Regular.ttf \
    https://github.com/google/fonts/raw/master/ofl/muli/Muli-Regular.ttf \
    https://github.com/google/fonts/raw/master/ofl/overpass/Overpass-Regular.ttf \
    https://github.com/google/fonts/raw/master/ofl/quattrocentosans/QuattrocentoSans-Italic.ttf \
    https://github.com/google/fonts/raw/master/ofl/raleway/Raleway-Regular.ttf \
    https://github.com/google/fonts/raw/master/ofl/sanchez/Sanchez-Regular.ttf
# Change to the subdirectory before zipping, so that the zip directory entry does not include any subdirectory names.
cd "${FONTS_DIR}"
# Use the best compression (option "-9")
zip -9 "../${FONTS_DIR}.zip" *
# Remove temporary files, and only keep the zip file.
cd ..
rm -Rf "${FONTS_DIR}"
