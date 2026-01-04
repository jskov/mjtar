#!/bin/bash

# Creates test files for extraction
# For comparison, use 'tar xpf test-files.tar'

dir=$(mktemp -d)

echo "Create files in $dir"


cd $dir
mkdir root
cd root

mkdir -p level1/level2/level3

echo "level1" > level1/file.txt
echo "level2" > level1/level2/file.txt
echo "level3" > level1/level2/level3/file.txt

echo "root" > root-file.txt

ln -s level1/level2/level3          lvl3-softlink
ln -s level1/level2/level3/file.txt lvl3-file-softlink

ln -s /dev/null                     device-softlink
ln -s ..                            lvl0-softlink
ln -s /tmp                          out-of-tree-softlink

echo "outside" > /tmp/out-of-tree
ln level1/file.txt                  lvl1-file-hardlink
ln /tmp/out-of-tree                 out-of-tree-hardlink

# Note that 000/100/200 will not be included in the archive
mkdir mods
echo 1777 2777 4777 000 100 200 400 500 600 700 410 420 440 401 402 404 | tr ' ' '\n' | xargs -I {} sh -c "touch mods/f{} ; chmod {} mods/f{}"
echo 1777 2777 4777 000 100 200 400 500 600 700 410 420 440 401 402 404 | tr ' ' '\n' | xargs -I {} sh -c "mkdir mods/d{} ; chmod {} mods/d{}"

# No owner/group tests since these would require sudo for extraction
# Same for nodes of /dev

target=$(dirname "$0")/test-files.tar

cd $dir
rm -f $target
tar cf $target root  || true

