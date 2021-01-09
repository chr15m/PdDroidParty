OUT=../droidparty.net/builds/`pwd | xargs basename`-debug-`git log | grep -c commit`.apk

build/outputs/apk/debug/PdDroidParty-debug.apk: src/**/**
	./gradlew build

debug-build: build/outputs/apk/debug/PdDroidParty-debug.apk
	cp $< $(OUT)
	@echo "Wrote to:" $(OUT)

install:
	./gradlew installDebug

release:
	./gradlew build
	cp ./build/outputs/apk/release/`pwd | xargs basename`-release.apk .
