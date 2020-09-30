require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

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

  s.dependency "React"
  s.dependency "Firebase/Core", '~> 6.30.0'
  s.dependency "Firebase/Messaging", '~> 6.30.0'
  # ...
  # s.dependency "..."
end

