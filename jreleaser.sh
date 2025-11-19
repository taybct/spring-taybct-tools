#1) Verify release & deploy configuration
./gradlew jreleaserConfig
#2) Ensure a clean deployment
./gradlew clean
#3) Stage all artifacts to a local directory
./gradlew publish
#4) Deploy and release
./gradlew jreleaserDeploy