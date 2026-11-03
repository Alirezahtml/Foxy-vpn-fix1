# Release signing

The delivered release APK is signed with the temporary local keystore delivered separately as `FoxyVPN-release-keystore.jks`. Keep that keystore and its password private if future updates must install over this APK. For a public production release, replace it with the owner's permanent signing key and set `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`, and `SIGNING_STORE_PASSWORD` before running `./gradlew :app:assembleRelease`.
