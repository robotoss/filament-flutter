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
  s.source_files     = 'Classes/**/*', 'Materials/*'
  s.dependency       'Flutter'
  s.platform         = :ios, '12.0'
  s.dependency       'Filament', '>= 1.51.1', '< 1.51.10'
  s.static_framework = true

  s.user_target_xcconfig = { 
    'DEFINES_MODULE' => 'YES', 
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386', 
    'CLANG_CXX_LANGUAGE_STANDARD' => 'c++17',
    'OTHER_CFLAGS' => '"-fvisibility=default" "$(inherited)"',
    'USER_HEADER_SEARCH_PATHS' => '"${PODS_ROOT}/../.symlinks/plugins/filament_flutter_ios/ios/include" "${PODS_ROOT}/../.symlinks/plugins/filament_flutter_ios/ios/Materials" "$(inherited)"',
    'ALWAYS_SEARCH_USER_PATHS' => 'YES'
  }

  s.pod_target_xcconfig = { 
    'DEFINES_MODULE' => 'YES', 
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386',
    'CLANG_CXX_LANGUAGE_STANDARD' => 'c++17',
    'OTHER_CXXFLAGS' => '"--std=c++17" "-fmodules" "-fcxx-modules" "-fvisibility=default" "$(inherited)"',
    'OTHER_CFLAGS' => '"-fvisibility=default" "$(inherited)"',
    'USER_HEADER_SEARCH_PATHS' => '"${PODS_ROOT}/../.symlinks/plugins/filament_flutter_ios/ios/include" "${PODS_ROOT}/../.symlinks/plugins/filament_flutter_ios/ios/Materials" "$(inherited)"',
    'ALWAYS_SEARCH_USER_PATHS' => 'YES'
  }

  s.swift_version = '5.0'
end
