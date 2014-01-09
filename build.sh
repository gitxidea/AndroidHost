F=${PWD}
mkdir $F/assets/
P=/Users/wy/Documents/workspace/A
cd ${P}
sh build.sh
cp ./bin/A.apk $F/assets/

cd ../B/
sh build.sh
cp ./bin/B.apk $F/assets/

cd ../C/
sh build.sh
cp ./bin/C.apk $F/assets/

