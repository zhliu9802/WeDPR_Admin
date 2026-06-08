#!/bin/bash
SHELL_FOLDER=$(cd $(dirname $0);pwd)
cd ${SHELL_FOLDER}

SOURCE_DIR="./"
FILES="cert.cnf cert_script.sh readme.txt"

TARGET_DIR="./cert_tool"
ZIP_FILE="cert_tool.zip"

if [ ! -f "$ZIP_FILE" ]; then
    mkdir -p "$TARGET_DIR"
    for FILE in $FILES; do
        if [ -f "$SOURCE_DIR/$FILE" ]; then
            cp "$SOURCE_DIR/$FILE" "$TARGET_DIR/"
        else
            echo "Warning: File $SOURCE_DIR/$FILE does not exist."
        fi
    done
    zip -qr "$ZIP_FILE" "$TARGET_DIR"
fi
