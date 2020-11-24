require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))
firebase_sdk_version = package['sdkVersions']['ios']['firebase'] || '~> 6.31.0'

Pod::Spec.new do |s|
  s.name         = "react-native-firebase-push-notifications"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  react-native-firebase-push-notifications
                   DESC
  s.homepage     = "https://github.com/afrihost/react-native-firebase-push-notifications"
  s.license      = "MIT"
  # s.license    = { :type => "MIT", :file => "FILE_LICENSE" }
  s.authors      = { "Your Name" => "open-source@afrihost.com" }
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/afrihost/react-native-firebase-push-notifications.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  # React Native dependencies
  s.dependency          'React-Core'

  if defined?($FirebaseSDKVersion)
    Pod::UI.puts "#{s.name}: Using user specified Firebase SDK version '#{$FirebaseSDKVersion}'"
    firebase_sdk_version = $FirebaseSDKVersion
  end

  # Firebase dependencies
  s.dependency          'Firebase/CoreOnly', firebase_sdk_version
  s.dependency          'Firebase/Messaging', firebase_sdk_version

  if defined?($RNFirebaseAsStaticFramework)
    Pod::UI.puts "#{s.name}: Using overridden static_framework value of '#{$RNFirebaseAsStaticFramework}'"
    s.static_framework = $RNFirebaseAsStaticFramework
  else
    s.static_framework = false
  end
end

