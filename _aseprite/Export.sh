#!/usr/bin/env bash

# Path to the Aseprite CLI (replace with your actual location)
ASEPRITE="'/Users/henrystratton/Library/Application Support/Steam/steamapps/common/Aseprite/Aseprite.app/Contents/MacOS/aseprite'"

# Input Aseprite file
INPUT="./Source.aseprite"

# Output directory for slice exports
OUTPUT_DIR="../src/main/resources/assets/paperpunchcards/textures"

# Create output dir if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Run Aseprite in batch mode, exporting each slice as its own PNG
"$ASEPRITE" -b "$INPUT" \
  --scale 1 \
  --save-as "$OUTPUT_DIR/{slice}.png"
