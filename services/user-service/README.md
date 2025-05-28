# Digital-Bank: user-service
# build service: Go to service directory
./gradlew clean build 
# run service:
./gradlew :user-service:bootRun 
# Service image build
gradle :user-service:bootBuildImage   