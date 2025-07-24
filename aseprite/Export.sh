#!/usr/bin/env bash

#paths are relative to this script, so make sure to cd to the scripts directory.
cd `dirname $0`

ASEPRITE="/Users/henrystratton/Library/Application Support/Steam/steamapps/common/Aseprite/Aseprite.app/Contents/MacOS/aseprite"
INPUT="./Source.aseprite"
OUTPUT_DIR="../src/main/resources/assets/paperpunchcards/textures"

mkdir -p "$OUTPUT_DIR"

"$ASEPRITE" -b "$INPUT" \
  --scale 1 \
  --save-as "$OUTPUT_DIR/{slice}.png"