require 'json'
package = JSON.parse(File.read('../package.json'))

Pod::Spec.new do |s|
  s.name                = "FirebasePushNotifications"
  s.version             = package["version"]
  s.description         = package["description"]
  s.summary             = <<-DESC
                            Firebase push notications supporting iOS & Android.
                          DESC
  s.homepage            = "https://www.afrihost.com"
  s.license             = package['license']
  s.authors             = "N/A"
  s.source              = { :git => "https://github.com/afrihost/react-native-firebase-push-notifications.git", :tag => "v#{s.version}" }
  s.social_media_url    = 'http://twitter.com/afrihost'
  s.platform            = :ios, "9.0"
  s.source_files        = './**/*.{h,m}'
  s.dependency          'React'
  s.dependency          'Firebase/Core'
  s.subspec 'Crashlytics' do |cs|
    cs.dependency 'Fabric'
    cs.dependency 'Crashlytics'
  end
  # allow this package to be used with use_frameworks!
  s.static_framework = true
end
