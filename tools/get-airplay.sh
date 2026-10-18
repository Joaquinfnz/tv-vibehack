#!/usr/bin/env bash
# Download the AirPlay receiver (jqssun/android-airplay-server, GPL-3.0) from its official release and
# verify it before you sideload it. The APK is not stored in this repo.
# Usage: tools/get-airplay.sh   ->  airplay-server.apk in the current directory
set -euo pipefail
VERSION="v0.0.33"
SHA256="2203dcc76ad884265e6e01c2147c917f100e93f37b91bf728bb3eed466396b32"
URL="https://github.com/jqssun/android-airplay-server/releases/download/$VERSION/app-release.apk"
curl -fL "$URL" -o airplay-server.apk
echo "$SHA256  airplay-server.apk" | shasum -a 256 -c -
echo "OK. Source: https://github.com/jqssun/android-airplay-server (GPL-3.0)"
