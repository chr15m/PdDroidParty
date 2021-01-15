VERSION=$(shell git log | grep -c commit)
OUT=../droidparty.net/builds/`pwd | xargs basename`-debug-$(VERSION).apk
NAME=`pwd | xargs basename`
REVNO=`git rev-parse HEAD | cut -b-8`
RELEASE=$(NAME)-$(VERSION)-$(REVNO)-release.apk

build/outputs/apk/debug/PdDroidParty-debug.apk: src/**/**
	./gradlew build

debug-build: build/outputs/apk/debug/PdDroidParty-debug.apk
	cp $< $(OUT)
	@echo "Wrote to:" $(OUT)

install:
	./gradlew installDebug

release:
	./gradlew -PversionCode=$(VERSION) -PversionName=$(REVNO) build
	cp ./build/outputs/apk/release/$(NAME)-release.apk $(RELEASE)
	@echo $(RELEASE)
