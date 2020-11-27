install:
	./gradlew installDebug

release:
	./gradlew build
	cp ./build/outputs/apk/release/`pwd | xargs basename`-release.apk .
