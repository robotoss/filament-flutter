#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint filament_flutter_ios.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'filament_flutter_ios'
  s.version          = '0.0.1'
  s.summary          = 'Flutter widgets for Filament, at iOS.'
  s.description      = <<-DESC
Flutter widgets for Filament, at iOS.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Konstantin' => 'zoxo@outlook.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '12.0'
  s.dependency 'Filament', '>= 1.51.1', '< 1.51.10'
  s.static_framework = true
  s.user_target_xcconfig = { 
    'DEFINES_MODULE' => 'YES', 
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386', 
    "CLANG_CXX_LANGUAGE_STANDARD" => "c++17",
    'OTHER_CFLAGS' => '"-fvisibility=default" "$(inherited)"',
  }

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 
    'DEFINES_MODULE' => 'YES', 
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386',
    "CLANG_CXX_LANGUAGE_STANDARD" => "c++17",
    'OTHER_CXXFLAGS' => '"--std=c++17" "-fmodules" "-fcxx-modules" "-fvisibility=default" "$(inherited)"',
    'OTHER_CFLAGS' => '"-fvisibility=default" "$(inherited)"',
  }
  s.swift_version = '5.0'
end
