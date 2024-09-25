#!/bin/bash

command=$1
case "$command" in
    test)
        echo "test"
        ;;
    build_ndk)
        cd app/src/main/jni
        ndk-build
        if [ $? -eq 0 ]; then
            echo "$command run success"
            cp -r ../libs/* ../jniLibs/
            if [ $? -eq 0 ]; then
                echo "copy libs success"
            else
                echo "copy libs failed"
            fi
        else
            echo "$command run failed"
        fi
        cd ../../../../
        ;;
    *)
        echo ": $command"
        exit 1
        ;;
esac