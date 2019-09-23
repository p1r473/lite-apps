#!/usr/bin/env bash
# Runs the Jekyll server for development.
./node_modules/.bin/babel --extensions .jsx --watch _site/library/ --out-dir library/ & # Run in the background.
bundle exec jekyll serve --watch --trace --incremental --config _config.yml,_config_dev.yml
